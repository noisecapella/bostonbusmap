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

import boston.Bus.Map.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
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
	private Button button;
	
	
	private final double bostonLatitude = 42.3583333;
	private final double bostonLongitude = -71.0602778;
	private final int bostonLatitudeAsInt = (int)(bostonLatitude * 1000000);
	private final int bostonLongitudeAsInt = (int)(bostonLongitude * 1000000);
	
	//watertown is slightly north and west of boston
	private final double watertownLatitude = 42.37;
	private final double watertownLongitude = -71.183;
	private final int watertownLatitudeAsInt = (int)(watertownLatitude * 1000000);
	private final int watertownLongitudeAsInt = (int)(watertownLongitude * 1000000);
	
	
	private Drawable busPicture;
	private Drawable arrow;
	
	private final int maxOverlays = 20;
	
	/**
	 * The last time we updated, in milliseconds. Used to make sure we don't update more frequently than
	 * every 10 seconds, to avoid unnecessary strain on their server
	 */
	private double lastUpdateTime;
	
	/**
	 * This contains the code that updates the bus locations
	 */
	private Runnable updateBuses;
	/**
	 * Used to make updateBuses run every 10 seconds or so
	 */
	private Handler handler = new Handler();
	
	/**
	 * The minimum time in milliseconds between updates. The XML feed requires a minimum of 10 seconds,
	 * I'm doing 13 just in case
	 */
	private final int fetchDelay = 13000;

	private BusLocations busLocations = new BusLocations();

	/**
	 * Time when onCreate was last called (in millis)
	 */
	private double onCreateTime;
	
	/**
	 * Five minutes in milliseconds
	 */
	private final double timeoutInMillis = 10 * 60 * 1000; //10 minutes
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        onCreateTime = System.currentTimeMillis();

        //get widgets
        mapView = (MapView)findViewById(R.id.mapview);
        textView = (TextView)findViewById(R.id.TextView01);
        button = (Button)findViewById(R.id.Button01);
        
        
        
        
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
        
    	//enable plus/minus zoom buttons in map
        mapView.setBuiltInZoomControls(true);

    	//store picture of bus
        busPicture = getResources().getDrawable(R.drawable.bus_statelist);
        
        arrow = getResources().getDrawable(R.drawable.arrow);
        
        
        
        //this is the refresh button
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if (System.currentTimeMillis() - lastUpdateTime < fetchDelay)
				{
					textView.setText("Please wait 10 seconds before clicking Refresh again");
					return;
				}

				onCreateTime = System.currentTimeMillis();
				handler.removeCallbacks(updateBuses);
				if(doUpdateConstantly())
				{
					//if the runInBackground checkbox is clicked, start the handler updating
				    handler.postAtTime(updateBuses, (long)(fetchDelay * 1.5));
				}
				
				
				runUpdateTask("Finished update!");
			}
		});
        
        
        updateBuses = new Runnable() {
		
			@Override
			public void run() {
				double currentTime = System.currentTimeMillis();
		
				boolean doTimeout = doTimeout();
				if ((currentTime - onCreateTime > timeoutInMillis) && doTimeout)
				{
					//timeout
					//this is done to help prevent the phone from wasting battery
					//power if the user leaves the app running on their phone
					
					if (updateAsyncTask != null && updateAsyncTask.getStatus().equals(UpdateAsyncTask.Status.FINISHED))
					{
						runUpdateTask("Finished update! 10 minutes reached; to update further click Refresh");
						return;
					}
					
				}
				
				if (currentTime - lastUpdateTime > fetchDelay)
				{
					//if not too soon, do the update
					runUpdateTask("Finished update!");
				}

				//make updateBuses execute every 10 seconds (or whatever fetchDelay is)
				//to disable this, the user should go into the settings and uncheck 'Run in background'
				handler.postDelayed(updateBuses, fetchDelay);

			}
		};
    }
		

    @Override
    protected void onDestroy() {
    	if (mapView != null)
    	{

    		GeoPoint point = mapView.getMapCenter();
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();


    		editor.putInt("centerLat", point.getLatitudeE6());
    		editor.putInt("centerLon", point.getLongitudeE6());
    		editor.putInt("zoomLevel", mapView.getZoomLevel());
    		editor.commit();
    	}
    	
    	super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	//when the menu button is clicked, a menu comes up
    	//currently, the only item is 'Settings'
    	switch (item.getItemId())
    	{
    	case R.id.settingsMenuItem:
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.centerOnBostonMenuItem:
    	
    		if (mapView != null)
    		{
    			GeoPoint point = new GeoPoint(bostonLatitudeAsInt, bostonLongitudeAsInt);
    			mapView.getController().animateTo(point);
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
			OneTimeLocationListener listener = new OneTimeLocationListener(mapView, locationManager);

			String provider = locationManager.getBestProvider(criteria, true);
			if (provider == null)
			{
				Toast.makeText(this, noProviders, Toast.LENGTH_LONG).show();
			}
			else
			{
				locationManager.requestLocationUpdates(provider, 0, 0, listener, getMainLooper());

				Toast.makeText(this, "Finding current location...", Toast.LENGTH_SHORT).show();
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

	private UpdateAsyncTask updateAsyncTask;
	
	/**
	 * executes the update
	 */
	private void runUpdateTask(String finalMessage) {
		//update around the current center of the mapView
		GeoPoint center = mapView.getMapCenter();
		
		//make sure we don't update too often
		lastUpdateTime = System.currentTimeMillis();

		//don't do two updates at once
		if (updateAsyncTask != null)
		{
			if (updateAsyncTask.getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
			{
				//task is not finished yet
				return;
			}
			
		}
		
		
		updateAsyncTask = new UpdateAsyncTask(textView, busPicture, mapView, finalMessage, arrow);
		updateAsyncTask.execute(new Double(center.getLatitudeE6() / 1000000.0),
				new Double(center.getLongitudeE6() / 1000000.0), new Integer(maxOverlays), busLocations, doShowUnpredictable());
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		//check the result
		handler.removeCallbacks(updateBuses);
		if(doUpdateConstantly())
		{
			//if the runInBackground checkbox is clicked, start the handler updating
		    handler.postAtTime(updateBuses, fetchDelay);
		}
		
	}





	private boolean doUpdateConstantly()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		return prefs.getBoolean(getString(R.string.runInBackgroundCheckbox), false);
	}
	
	private boolean doShowUnpredictable()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		return prefs.getBoolean(getString(R.string.showUnpredictableBusesCheckbox), false);
	}

	private boolean doTimeout()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		return prefs.getBoolean(getString(R.string.timeoutPreferenceCheckbox), false);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		
		return new CurrentState(textView, mapView, busLocations, lastUpdateTime);
	}
}