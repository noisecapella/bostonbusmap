package boston.Bus.Map.ui;

import android.content.Context;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.schneeloch.latransit.R;

import boston.Bus.Map.main.UpdateHandler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class LocationOverlay extends MyLocationOverlay {
	private final Context context; 
	private UpdateHandler handler;
	private final MapView mapView;
	
	public LocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
		
		this.context = context;
		this.mapView = mapView;
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
			Toast.makeText(context, context.getString(R.string.locationUnavailable), Toast.LENGTH_LONG).show();
			break;
		}
	}

	public void setUpdateable(UpdateHandler handler) {
		this.handler = handler;
	}

	public void updateMapViewPosition() {
		Log.v("BostonBusMap", "updateMapViewPosition");
		Log.v("BostonBusMap", "inside handler in updateMapViewPosition");
		runOnFirstFix(new Runnable() {

			@Override
			public void run() {
				Log.v("BostonBusMap", "inside updateMapViewPosition.run");

				mapView.getController().animateTo(getMyLocation());
				UpdateHandler updateHandler = LocationOverlay.this.handler;
				if (updateHandler != null)
				{
					updateHandler.triggerUpdate(1500);
				}
			}
		});
	}


	
}
