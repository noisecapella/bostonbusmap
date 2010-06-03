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

import java.util.List;

import boston.Bus.Map.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.SystemClock;
import android.os.Handler.Callback;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

/**
 * The main activity
 *
 */
public class Main extends MapActivity
{
	private MapView mapView;
	private TextView textView;
	
	
	private final double bostonLatitude = 42.3583333;
	private final double bostonLongitude = -71.0602778;
	private final int bostonLatitudeAsInt = (int)(bostonLatitude * 1000000);
	private final int bostonLongitudeAsInt = (int)(bostonLongitude * 1000000);
	
	//watertown is slightly north and west of boston
	private final double watertownLatitude = 42.37;
	private final double watertownLongitude = -71.183;
	private final int watertownLatitudeAsInt = (int)(watertownLatitude * 1000000);
	private final int watertownLongitudeAsInt = (int)(watertownLongitude * 1000000);
	
	
	
	/**
	 * Used to make updateBuses run every 10 seconds or so
	 */
	private UpdateHandler handler;
	
	private Locations busLocations;

	/**
	 * Five minutes in milliseconds
	 */
	private final double timeoutInMillis = 10 * 60 * 1000; //10 minutes
	
	/**
	 * What is used to figure out the current location
	 */
	private OneTimeLocationListener locationListener;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //get widgets
        mapView = (MapView)findViewById(R.id.mapview);
        textView = (TextView)findViewById(R.id.statusView);
        
    	//store picture of bus
        Drawable locationDrawable = getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);

        Drawable busPicture = getResources().getDrawable(R.drawable.bus_statelist);
        
        Drawable arrow = getResources().getDrawable(R.drawable.arrow);
        Drawable tooltip = getResources().getDrawable(R.drawable.tooltip);
        
        Drawable busStop = getResources().getDrawable(R.drawable.busstop);
        
        if (busLocations == null)
        {
        	busLocations = new Locations(busPicture, arrow, tooltip, locationDrawable, busStop);
        }
        
        double lastUpdateTime = 0;
        
        Object obj = getLastNonConfigurationInstance();
        if (obj != null)
        {
        	CurrentState currentState = (CurrentState)obj;
        	currentState.restoreWidgets(textView, mapView);
        	lastUpdateTime = currentState.getLastUpdateTime();
        	busLocations = currentState.getBusLocations();
        	
        }
        else
        {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int centerLat = prefs.getInt("centerLat", Integer.MAX_VALUE);
            int centerLon = prefs.getInt("centerLon", Integer.MAX_VALUE);
            int zoomLevel = prefs.getInt("zoomLevel", Integer.MAX_VALUE);

            if (centerLat != Integer.MAX_VALUE && centerLon != Integer.MAX_VALUE && zoomLevel != Integer.MAX_VALUE)
            {

            	GeoPoint point = new GeoPoint(centerLat, centerLon);
            	MapController controller = mapView.getController();
            	controller.setCenter(point);
            	controller.setZoom(zoomLevel);
            }
            else
            {
            	//move maps widget to point to boston or watertown
            	MapController controller = mapView.getController();
            	GeoPoint bostonLocation = new GeoPoint(bostonLatitudeAsInt, bostonLongitudeAsInt);
            	controller.setCenter(bostonLocation);

            	//set zoom depth
            	controller.setZoom(14);
            }
        	//make the textView blank
        	textView.setText("");
        }
        
        handler = new UpdateHandler(textView, busPicture, mapView, arrow, tooltip, busLocations, this);
        handler.setLastUpdateTime(lastUpdateTime);
        populateHandlerSettings();
        
        if (handler.getUpdateConstantly())
        {
        	handler.instantRefresh();
        }
        
    	//enable plus/minus zoom buttons in map
        mapView.setBuiltInZoomControls(true);

    }
		


	@Override
    protected void onPause() {
    	if (mapView != null)
    	{

    		GeoPoint point = mapView.getMapCenter();
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();

    		//TODO: these strings should be stored as const strings somewhere, to avoid typos and for easy lookup
    		editor.putInt("centerLat", point.getLatitudeE6());
    		editor.putInt("centerLon", point.getLongitudeE6());
    		editor.putInt("zoomLevel", mapView.getZoomLevel());
    		editor.commit();
    	}
    	
		if (locationListener != null)
		{
			locationListener.release();
		}
    	
		
		if (handler != null)
		{
			handler.removeAllMessages();
		}
		super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	//when the menu button is clicked, a menu comes up
    	switch (item.getItemId())
    	{
    	case R.id.refreshItem:
    		boolean b = handler.instantRefresh();
    		if (b == false)
    		{
    			textView.setText("Please wait 10 seconds before clicking Refresh again");
    		}
    		break;
    	case R.id.settingsMenuItem:
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.centerOnBostonMenuItem:
    	
    		if (mapView != null)
    		{
    			GeoPoint point = new GeoPoint(bostonLatitudeAsInt, bostonLongitudeAsInt);
    			mapView.getController().animateTo(point);
    			handler.triggerUpdate(1500);
    		}
    		break;
    	
    	case R.id.centerOnLocationMenuItem:
    		if (mapView != null)
    		{
    			centerOnCurrentLocation();
    		}
    		
    		break;
 
    	}
    	return true;
    }

    /**
     * Figure out the current location of the phone, and move the map to it
     */
	private void centerOnCurrentLocation() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		
		String noProviders = "Cannot use any location service. \nAre any enabled (like GPS) in your system settings?";
		
		LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		if (locationManager != null)
		{
			String provider = locationManager.getBestProvider(criteria, true);
			if (provider == null)
			{
				Toast.makeText(this, noProviders, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (locationListener != null)
				{
					locationManager.removeUpdates(locationListener);
				}
				else
				{
					locationListener = new OneTimeLocationListener(mapView, locationManager, this, handler);
				}
				
				locationListener.start();
				
				locationManager.requestLocationUpdates(provider, 0, 0, locationListener, getMainLooper());

				
				//... it might take a few seconds. 
				//TODO: make sure that it eventually shows the error message if location is never found
			}
		}
		else
		{
			//i don't think this will happen, but just in case
			Toast.makeText(this, noProviders, Toast.LENGTH_LONG).show();
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		//check the result
		populateHandlerSettings();
		handler.resume();
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return super.onKeyDown(keyCode, event);
		}
		else if (mapView != null)
		{
			return mapView.onKeyDown(keyCode, event);
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	
    private void populateHandlerSettings() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	handler.setUpdateConstantly(prefs.getBoolean(getString(R.string.runInBackgroundCheckbox), false));
    	handler.setShowUnpredictable(prefs.getBoolean(getString(R.string.showUnpredictableBusesCheckbox), false));
    	handler.setHideHighlightCircle(prefs.getBoolean(getString(R.string.hideCircleCheckbox), false));
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		
		return new CurrentState(textView, mapView, busLocations, handler.getLastUpdateTime());
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return super.onKeyUp(keyCode, event);
		}
		else if (mapView != null)
		{
			handler.triggerUpdate(250);
			
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
			{
				float centerX = mapView.getWidth() / 2;
				float centerY = mapView.getHeight() / 2;
				
				//make it a tap to the center of the screen
					
				MotionEvent downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, centerX, centerY, 0);
				MotionEvent upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP, centerX, centerY, 0);
				
				
				mapView.onTouchEvent(downEvent);
				return mapView.onTouchEvent(upEvent);
			}
			else
			{
			
				return mapView.onKeyUp(keyCode, event);
			}
		}
		else
		{
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (mapView != null)
		{
			handler.triggerUpdate(250);
			return mapView.onTrackballEvent(event);
		}
		else
		{
			return false;
		}
	}
}