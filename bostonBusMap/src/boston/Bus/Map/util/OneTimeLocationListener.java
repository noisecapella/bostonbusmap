/*
    BostonBusMap
 
    Copyright (C) 2009  George Schneeloch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package boston.Bus.Map.util;

import boston.Bus.Map.main.UpdateHandler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This gets the location and sets the mapView to it, then unregisters itself so that this only gets executed once
 * Remember that you need to declare this in the permissions part of AndroidManifest.xml
 *
 */
public class OneTimeLocationListener implements LocationListener {

	private final MapView mapView;
	
	private double latitude;
	private double longitude;

	private final LocationManager locationManager;
	
	private final Context context;
	private final UpdateHandler updateable;
	/**
	 * Max time to wait before cancelling locate, in millis
	 */
	private final int terminateAfter = 40000;
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}
	
	public OneTimeLocationListener(MapView mapView, LocationManager locationManager, Context context, UpdateHandler updateable)
	{
		this.mapView = mapView;
		this.locationManager = locationManager;
		this.context = context;
		this.updateable = updateable;
		updateable.setLocationListener(this);
	}

	public void start()
	{
		updateable.removeMessages(UpdateHandler.LOCATION_NOT_FOUND);
		updateable.removeMessages(UpdateHandler.LOCATION_FOUND);
		
		updateable.sendEmptyMessageDelayed(UpdateHandler.LOCATION_NOT_FOUND, terminateAfter);
		Toast.makeText(context, "Finding current location...", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * If the activity is pausing, silently remove any callbacks  
	 */
	public void release()
	{		
		updateable.removeMessages(UpdateHandler.LOCATION_NOT_FOUND);
		updateable.removeMessages(UpdateHandler.LOCATION_FOUND);
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		final int e6 = 1000000;
		
		int latAsInt = (int)(latitude * e6);
		int lonAsInt = (int)(longitude * e6);
		
		//TODO: is there some way we can communicate a radius too?
		
		//TODO: this happens in this method, but other stuff happens in UpdateHandler... make this more intuitive
		mapView.getController().animateTo(new GeoPoint(latAsInt, lonAsInt));
		
		
		//we only update once, so remove it now
		release();
		
		Message msg = new Message();
		msg.what = UpdateHandler.LOCATION_FOUND;
		msg.arg1 = latAsInt;
		msg.arg2 = lonAsInt;
		updateable.sendMessage(msg);
		
		
		updateable.triggerUpdate(1500);
	}

	private final String locationUnavailable = "Current location is unavailable";
	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(mapView.getContext(), locationUnavailable, Toast.LENGTH_LONG).show();
		release();
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		switch (arg1)
		{
		case LocationProvider.AVAILABLE:
			break;
		case LocationProvider.OUT_OF_SERVICE:
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Toast.makeText(mapView.getContext(), locationUnavailable, Toast.LENGTH_LONG).show();
			release();
			break;
		}
	}

}
