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
package boston.Bus.Map;

import com.google.android.maps.MapView;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This is not currently used, but it would get the current location from the GPS device on the phone
 * Remember that you need to declare this in the permissions part of AndroidManifest.xml
 *
 */
public class MyLocationListener implements android.location.LocationListener {

	private MapView mapView;
	
	private double latitude;
	private double longitude;
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}
	
	public MyLocationListener(MapView mapView)
	{
		this.mapView = mapView;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double milliseconds = System.currentTimeMillis();
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		String str = latitude + ", " + longitude + " at " + milliseconds;
		//textView.setText(str);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
