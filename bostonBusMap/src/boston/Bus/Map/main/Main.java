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
package boston.Bus.Map.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import umich.Bus.Map.R;


import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.CurrentState;
import boston.Bus.Map.main.Preferences;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.main.UpdateHandler;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.BusOverlay;

import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.ModeAdapter;
import boston.Bus.Map.ui.RouteOverlay;
import boston.Bus.Map.ui.ViewingMode;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
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
import android.os.Message;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * The main activity
 *
 */
public class Main extends MapActivity
{
	private static final String selectedRouteIndexKey = "selectedRouteIndex";
	private static final String selectedBusPredictionsKey = "selectedBusPredictions";
	private static final String centerLatKey = "centerLat";
	private static final String centerLonKey = "centerLon";
	private static final String zoomLevelKey = "zoomLevel";
	private MapView mapView;
	private TextView textView;
	
	
	
	/**
	 * Used to make updateBuses run every 10 seconds or so
	 */
	private UpdateHandler handler;
	
	private Locations busLocations;

	/**
	 * Five minutes in milliseconds
	 */
	private final double timeoutInMillis = 10 * 60 * 1000; //10 minutes
	
	private int selectedRouteIndex;
	
	/**
	 * This is used to indicate to the mode spinner to ignore the first time we set it, so we don't update every time the screen changes
	 */
	private boolean firstRunMode;
	
	/**
	 * This is used to indicate to the mode spinner to ignore the first time we set it, so we don't update every time the screen changes
	 */
	private boolean firstRunRoute;

	
	private BusOverlay busOverlay;
	private RouteOverlay routeOverlay;
	private LocationOverlay myLocationOverlay;
	
	private Spinner toggleButton;
	private Drawable busStop;
	
	private MenuItem favoriteMenuItem;
	private int currentFavoriteStatus;

	public static final int VEHICLE_LOCATIONS_ALL = 1;
	public static final int BUS_PREDICTIONS_ONE = 2;
	public static final int VEHICLE_LOCATIONS_ONE = 3;
	public static final int BUS_PREDICTIONS_ALL = 4;
	public static final int BUS_PREDICTIONS_STAR = 5;
	
	public static final int[] modesSupported = new int[]{
		BUS_PREDICTIONS_ONE, BUS_PREDICTIONS_STAR
	};
	
	public static final int[] modeIconsSupported = new int[]{
		R.drawable.busstop_one, R.drawable.busstop_star
	};
	
	public static final String[] modeTextSupported = new String[]{
		"Stops and predictions on one route", "Favorite stops"
	};
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        
        firstRunMode = true;
        firstRunRoute = true;
        
        //get widgets
        mapView = (MapView)findViewById(R.id.mapview);
        textView = (TextView)findViewById(R.id.statusView);
        toggleButton = (Spinner)findViewById(R.id.predictionsOrLocations);
        
        Resources resources = getResources();

        Drawable locationDrawable = resources.getDrawable(R.drawable.ic_maps_indicator_current_position);

        Drawable busPicture = resources.getDrawable(R.drawable.bus_statelist);
        
        Drawable arrow = resources.getDrawable(R.drawable.arrow);
        Drawable tooltip = resources.getDrawable(R.drawable.tooltip);
        
        busStop = resources.getDrawable(R.drawable.busstop_statelist);
        
        TransitSystem.setDefaultTransitSource(busStop, busPicture, arrow);
        SpinnerAdapter modeSpinnerAdapter = makeModeSpinner(); 

        toggleButton.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (firstRunMode)
				{
					firstRunMode = false;
				}
				else if (busLocations != null && handler != null)
				{
					if (position < 0 || position >= modesSupported.length)
					{
						handler.setSelectedBusPredictions(VEHICLE_LOCATIONS_ALL);
					}
					else
					{
						handler.setSelectedBusPredictions(modesSupported[position]);
					}

					handler.triggerUpdate();
					handler.immediateRefresh();
				}				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//do nothing
			}
			

		});

        toggleButton.setAdapter(modeSpinnerAdapter);

        
        DatabaseHelper helper = new DatabaseHelper(this, busStop);
        
        
        String[][] routeKeyMap = getOrObtainRoutes(helper);
        HashMap<String, String> routeKeysToTitles = new HashMap<String, String>();
        for (int i = 0; i < routeKeyMap[0].length; i++)
        {
        	routeKeysToTitles.put(routeKeyMap[0][i], routeKeyMap[1][i]);
        }
        String[] routesSupported = routeKeyMap[0];
        
        //get the busLocations variable if it already exists. We need to do that step here since handler
        double lastUpdateTime = 0;
        boolean previousUpdateConstantly = false;

        UpdateAsyncTask majorHandler = null;
        
        Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
        if (lastNonConfigurationInstance != null)
        {
        	CurrentState currentState = (CurrentState)lastNonConfigurationInstance;
        	currentState.restoreWidgets(textView);
        	
        	busOverlay = currentState.cloneBusOverlay(this, mapView);
        	routeOverlay = currentState.cloneRouteOverlay(mapView.getProjection());
        	myLocationOverlay = new LocationOverlay(this, mapView);
        	
        	mapView.getOverlays().clear();
        	mapView.getOverlays().add(routeOverlay);
        	mapView.getOverlays().add(myLocationOverlay);
        	mapView.getOverlays().add(busOverlay);
        	
        	busOverlay.refreshBalloons();
        	
        	busLocations = currentState.getBusLocations();

        	lastUpdateTime = currentState.getLastUpdateTime();
        	previousUpdateConstantly = currentState.getUpdateConstantly();
        	selectedRouteIndex = currentState.getSelectedRouteIndex();
        	setSelectedBusPredictions(currentState.getSelectedBusPredictions());
        	
        	
        	
        	majorHandler = currentState.getMajorHandler();
        	//continue posting status updates on new textView
        	if (majorHandler != null)
        	{
        		majorHandler.setTextView(textView);
        	}
        }
        else
        {
        	busOverlay = new BusOverlay(busPicture, this, mapView);
        	routeOverlay = new RouteOverlay(mapView.getProjection());
        	myLocationOverlay = new LocationOverlay(this, mapView);
        }
        
        if (busLocations == null)
        {
        	busLocations = new Locations(busPicture, arrow, locationDrawable, busStop,
        			routesSupported, helper, routeKeysToTitles);
        }

        handler = new UpdateHandler(textView, mapView, arrow, tooltip, busLocations, 
        		this, helper, busOverlay, routeOverlay, myLocationOverlay, majorHandler);
        busOverlay.setUpdateable(handler);
        myLocationOverlay.setUpdateable(handler);
        
        populateHandlerSettings();

        
        if (lastNonConfigurationInstance != null)
        {
        	handler.setSelectedBusPredictions(getSelectedBusPredictions());
        	handler.setRouteIndex(selectedRouteIndex);
        }
        else
        {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int centerLat = prefs.getInt(centerLatKey, Integer.MAX_VALUE);
            int centerLon = prefs.getInt(centerLonKey, Integer.MAX_VALUE);
            int zoomLevel = prefs.getInt(zoomLevelKey, Integer.MAX_VALUE);
            selectedRouteIndex = prefs.getInt(selectedRouteIndexKey, 0);
            setSelectedBusPredictions(prefs.getInt(selectedBusPredictionsKey, VEHICLE_LOCATIONS_ALL));
            
            handler.setRouteIndex(selectedRouteIndex);
            handler.setSelectedBusPredictions(getSelectedBusPredictions());

            if (centerLat != Integer.MAX_VALUE && centerLon != Integer.MAX_VALUE && zoomLevel != Integer.MAX_VALUE)
            {

            	GeoPoint point = new GeoPoint(centerLat, centerLon);
            	MapController controller = mapView.getController();
            	controller.setCenter(point);
            	controller.setZoom(zoomLevel);
            }
            else
            {
            	//move maps widget to center of transit network
            	MapController controller = mapView.getController();
            	GeoPoint location = new GeoPoint(TransitSystem.getCenterLatAsInt(), TransitSystem.getCenterLonAsInt());
            	controller.setCenter(location);

            	//set zoom depth
            	controller.setZoom(14);
            }
        	//make the textView blank
        	textView.setText("");
        }
        
        handler.setLastUpdateTime(lastUpdateTime);

        if (handler.getUpdateConstantly() && previousUpdateConstantly == false)
        {
        	handler.instantRefresh();
        }
        
    	//enable plus/minus zoom buttons in map
        mapView.setBuiltInZoomControls(true);
        
        
    }
		

    private SpinnerAdapter makeRouteSpinnerAdapter(String[][] routeKeyMap) {
    	final ArrayList<HashMap<String, String>> routeList = new ArrayList<HashMap<String, String>>();
        
    	String[] keys = routeKeyMap[0];
    	String[] titles = routeKeyMap[1];
    	
        for (int i = 0; i < keys.length; i++)
        {
        	HashMap<String, String> map = new HashMap<String, String>();
       		map.put("key", keys[i]);
        	map.put("name", titles[i]);
        	routeList.add(map);
        }
        
        
        SimpleAdapter adapter = new SimpleAdapter(this, routeList, android.R.layout.simple_spinner_item, new String[]{"name"}, 
        		new int[]{android.R.id.text1});

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        return adapter;
	}


	private SpinnerAdapter makeModeSpinner() {
    	final ArrayList<ViewingMode> modes = new ArrayList<ViewingMode>();
        
        for (int i = 0; i < modesSupported.length; i++)
        {
        	ViewingMode mode = new ViewingMode(modeIconsSupported[i], modeTextSupported[i]);
        	modes.add(mode);
        }
        
        ModeAdapter adapter = new ModeAdapter(this, modes);
        
        return adapter;
	}


	private int getSelectedBusPredictions()
    {
    	int pos = toggleButton.getSelectedItemPosition();
    	if (pos < 0 || pos >= modesSupported.length)
    	{
    		return VEHICLE_LOCATIONS_ALL;
    	}
    	else
    	{
    		return modesSupported[pos];
    	}
    	
    }

    private void setSelectedBusPredictions(int selection)
    {
    	for (int i = 0; i < modesSupported.length; i++)
    	{
    		if (modesSupported[i] == selection)
    		{
    			toggleButton.setSelection(i);
    			return;
    		}
    	}
    }

	private String[][] getOrObtainRoutes(DatabaseHelper helper) {
		//TODO: download it from the xml feed and store it in the database. For now it's just a resource
		Resources res = getResources();
		
		return new String[][]{res.getStringArray(R.array.routeKeys), res.getStringArray(R.array.routeTitles)};
	}




	@Override
    protected void onPause() {
    	if (mapView != null)
    	{

    		GeoPoint point = mapView.getMapCenter();
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();

    		editor.putInt(selectedBusPredictionsKey, getSelectedBusPredictions());
    		editor.putInt(selectedRouteIndexKey, selectedRouteIndex);
    		editor.putInt(centerLatKey, point.getLatitudeE6());
    		editor.putInt(centerLonKey, point.getLongitudeE6());
    		editor.putInt(zoomLevelKey, mapView.getZoomLevel());
    		editor.commit();
    	}
    	
		
		if (handler != null)
		{
			handler.removeAllMessages();
		}
		
		if (myLocationOverlay != null)
		{
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.setUpdateable(null);
		}
		

		
		super.onPause();
    }

	
	@Override
	protected void onDestroy() {
		handler = null;
		busLocations = null;
		if (busOverlay != null)
		{
			busOverlay.setUpdateable(null);
			busOverlay = null;
		}
		
		routeOverlay = null;
		if (mapView != null)
		{
			mapView.getOverlays().clear();
			mapView = null;
		}
		
		textView = null;
		
		busStop = null;
		toggleButton = null;
		favoriteMenuItem = null;
		
		
		super.onDestroy();
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
    	case R.id.favoriteItem:
    		if (busLocations != null)
    		{
    			int id = busOverlay.toggleFavorite(busLocations);
    			item.setIcon(id);
    			Log.v("BostonBusMap", "setting favorite icon to " + (id == R.drawable.full_star ? "full star" : "empty star"));
    		}
    		break;
    	case R.id.centerOnBostonMenuItem:
    	
    		if (mapView != null)
    		{
    			GeoPoint point = new GeoPoint(TransitSystem.getCenterLatAsInt(), TransitSystem.getCenterLonAsInt());
    			mapView.getController().animateTo(point);
    			handler.triggerUpdate(1500);
    		}
    		break;
    	
    	case R.id.centerOnLocationMenuItem:
    		Log.v("BostonBusMap", "clicked My Location, which is " + (myLocationOverlay == null ? "null" : "not null"));
    		if (myLocationOverlay != null)
    		{
    			if (myLocationOverlay.isMyLocationEnabled() == false)
    			{
    				myLocationOverlay.enableMyLocation();
    				Toast.makeText(this, getString(R.string.findingCurrentLocation), Toast.LENGTH_SHORT).show();
    			}
   				myLocationOverlay.updateMapViewPosition();
    		}
    		
    		break;
 
    	}
    	return true;
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        for (int i = 0; i < menu.size(); i++)
        {
        	MenuItem item = menu.getItem(i);
        	if (item.getItemId() == R.id.favoriteItem)
        	{
        		if (currentFavoriteStatus != 0)
        		{
        			item.setIcon(currentFavoriteStatus);
        		}
    			favoriteMenuItem = item;
        		break;
        	}
        }
        
        return true;
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		//TODO: what exactly should we return here? 
		if (mapView != null && mapView.getOverlays().size() != 0)
		{
			return true;
		}
		else
		{
			return false;
		}
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
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
			{
				float centerX = mapView.getWidth() / 2;
				float centerY = mapView.getHeight() / 2;
				
				//make it a tap to the center of the screen
					
				MotionEvent downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, centerX, centerY, 0);
				
				
				return mapView.onTouchEvent(downEvent);
				
				
			}
			else
			{
				return mapView.onKeyDown(keyCode, event);
			}
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	
    private void populateHandlerSettings() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	boolean runInBackgroundCheckboxValue = prefs.getBoolean(getString(R.string.runInBackgroundCheckbox), true);
    	handler.setUpdateConstantly(runInBackgroundCheckboxValue);
    	handler.setShowUnpredictable(true);
    	handler.setHideHighlightCircle(true);
    	handler.setInferBusRoutes(false);
    	handler.setShowRouteLine(prefs.getBoolean(getString(R.string.showRouteLineCheckbox), false));
    	boolean showCoarseRouteLineCheckboxValue = prefs.getBoolean(getString(R.string.showCoarseRouteLineCheckbox), true); 
    	handler.setShowCoarseRouteLine(showCoarseRouteLineCheckboxValue);
    	//handler.setInitAllRouteInfo(prefs.getBoolean(getString(R.string.initAllRouteInfoCheckbox2), true));
    	handler.setInitAllRouteInfo(true);
    	
    	//since the default value for this flag is true, make sure we let the preferences know of this
    	prefs.edit().putBoolean(getString(R.string.runInBackgroundCheckbox), runInBackgroundCheckboxValue).
    		putBoolean(getString(R.string.showCoarseRouteLineCheckbox), showCoarseRouteLineCheckboxValue).commit();
    }

	@Override
	public Object onRetainNonConfigurationInstance() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean updateConstantly = prefs.getBoolean(getString(R.string.runInBackgroundCheckbox), true);
		
		return new CurrentState(textView, busLocations, handler.getLastUpdateTime(), updateConstantly,
				selectedRouteIndex, getSelectedBusPredictions(), busOverlay, routeOverlay,
				myLocationOverlay, handler.getMajorHandler());
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return super.onKeyUp(keyCode, event);
		}
		else if (mapView != null)
		{
			if (keyCode == KeyEvent.KEYCODE_MENU)
			{
				return super.onKeyUp(keyCode, event);
			}
			handler.triggerUpdate(250);
			
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
			{
				float centerX = mapView.getWidth() / 2;
				float centerY = mapView.getHeight() / 2;
				
				//make it a tap to the center of the screen
					
				MotionEvent upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP, centerX, centerY, 0);
				
				
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


	public void setFavoriteStatus(int drawable) {
		if (favoriteMenuItem != null)
		{
			Log.v("BostonBusMap", "setting favorite icon now!");
			favoriteMenuItem.setIcon(drawable);
		}
		
		currentFavoriteStatus = drawable;
	}
}