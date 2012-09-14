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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;
import org.xml.sax.SAXException;

import boston.Bus.Map.R;
import boston.Bus.Map.algorithms.GetDirections;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.provider.TransitContentProvider;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.BusOverlay;

import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.ModeAdapter;
import boston.Bus.Map.ui.OverlayGroup;
import boston.Bus.Map.ui.RouteOverlay;
import boston.Bus.Map.ui.ViewingMode;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.SearchHelper;
import boston.Bus.Map.util.StringUtil;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import android.provider.SearchRecentSuggestions;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
	//private static final String gpsAlwaysOn = "gpsAlwaysOn";
	private static final String markUpdatedStops = "markUpdatedStops";
	
	private static final String introScreenKey = "introScreen";
	private EditText searchView;

	/**
	 * This only exists so TransitContentProvider
	 */
	public static int suggestionsMode;
	
	/**
	 * Used to make updateBuses run every 10 seconds or so
	 */
	private UpdateHandler handler;
	
	private int selectedRouteIndex;
	
	/**
	 * This is used to indicate to the mode spinner to ignore the first time we set it, so we don't update every time the screen changes
	 */
	private boolean firstRunMode;
	
	/**
	 * Is location overlay supposed to be enabled? Used mostly for onResume()
	 */
	private boolean locationEnabled; 
	
	private Spinner toggleButton;
	/**
	 * The list of routes that's selectable in the routes dropdown list
	 */
	private String[] dropdownRoutes;
	private MyHashMap<String, String> dropdownRouteKeysToTitles;
	private AlertDialog routeChooserDialog;

	private ImageButton searchButton;

	private UpdateArguments arguments;
	
	public static final int VEHICLE_LOCATIONS_ALL = 1;
	public static final int BUS_PREDICTIONS_ONE = 2;
	public static final int VEHICLE_LOCATIONS_ONE = 3;
	public static final int BUS_PREDICTIONS_ALL = 4;
	public static final int BUS_PREDICTIONS_STAR = 5;
	
	public static final int UPDATE_INTERVAL_INVALID = 9999;
	public static final int UPDATE_INTERVAL_SHORT = 15;
	public static final int UPDATE_INTERVAL_MEDIUM = 50;
	public static final int UPDATE_INTERVAL_LONG = 100;
	public static final int UPDATE_INTERVAL_NONE = 0;
	
	public static final int[] modesSupported = new int[]{
		VEHICLE_LOCATIONS_ALL, VEHICLE_LOCATIONS_ONE, BUS_PREDICTIONS_ALL, 
		BUS_PREDICTIONS_ONE, BUS_PREDICTIONS_STAR
	};
	
	public static final int[] modeIconsSupported = new int[]{
		R.drawable.bus_all, R.drawable.bus_one, R.drawable.busstop_all, R.drawable.busstop_one, R.drawable.busstop_star,
		
	};
	
	public static final int[] modeTextSupported = new int[]{
		R.string.all_buses, R.string.vehicles_on_one_route, R.string.stops_and_predictions_on_all_routes, R.string.stops_and_predictions_on_one_route, R.string.favorite_stops,
		
	};
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        firstRunMode = true;
        
        TransitSystem.setDefaultTimeFormat(this);
        
        //get widgets
        final MapView mapView = (MapView)findViewById(R.id.mapview);
        toggleButton = (Spinner)findViewById(R.id.predictionsOrLocations);
        searchView = (EditText)findViewById(R.id.searchTextView);
        final ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
        searchButton = (ImageButton)findViewById(R.id.searchButton);
        
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);

        
        progress.setVisibility(View.INVISIBLE);
        
        searchView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSearchRequested();
			}
		});
        
        searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSearchRequested();
			}
		});
        
        Resources resources = getResources();

        // busPicture is used to initialize busOverlay, otherwise it would
        // joint the rest of the drawables in the brackets 
    	Drawable busPicture = resources.getDrawable(R.drawable.bus_statelist);
    	final TransitSystem transitSystem = new TransitSystem();
        {
        	Drawable busStopUpdated = resources.getDrawable(R.drawable.busstop_statelist_updated);
        	Drawable arrow = resources.getDrawable(R.drawable.arrow);
        	Drawable tooltip = resources.getDrawable(R.drawable.tooltip);
        	Drawable rail = resources.getDrawable(R.drawable.rail_statelist);
        
        	Drawable busStop = resources.getDrawable(R.drawable.busstop_statelist);
        
        	TransitDrawables busDrawables = new TransitDrawables(busStop, busStopUpdated, busPicture, arrow);
        	TransitDrawables subwayDrawables = new TransitDrawables(busStop, busStopUpdated, rail, arrow);
        	TransitDrawables commuterRailDrawables = new TransitDrawables(busStop, busStopUpdated, rail, arrow);
        	transitSystem.setDefaultTransitSource(busDrawables, subwayDrawables, commuterRailDrawables);
        }
        SpinnerAdapter modeSpinnerAdapter = makeModeSpinner(); 

        toggleButton.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (firstRunMode)
				{
					firstRunMode = false;
				}
				else if (arguments != null && handler != null)
				{
					if (position >= 0 && position < modesSupported.length)
					{
						setMode(modesSupported[position], false);
					}
				}				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//do nothing
			}
			

		});

        toggleButton.setAdapter(modeSpinnerAdapter);

        
        dropdownRoutes = transitSystem.getRoutes();
        dropdownRouteKeysToTitles = transitSystem.getRouteKeysToTitles();
        
        {
            String[] routeTitles = getRouteTitles(dropdownRoutes, dropdownRouteKeysToTitles);
            
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(getString(R.string.chooseRouteInBuilder));
        	builder.setItems(routeTitles, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int item) {
        			setNewRoute(item, true);
        		}
        	});
        	routeChooserDialog = builder.create();
        }
		
        //get the busLocations variable if it already exists. We need to do that step here since handler
        long lastUpdateTime = 0;
        int previousUpdateConstantlyInterval = UPDATE_INTERVAL_NONE;

        UpdateAsyncTask majorHandler = null;
        
        Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
        final OverlayGroup overlayGroup;
        Locations busLocations = null;
        if (lastNonConfigurationInstance != null)
        {
        	CurrentState currentState = (CurrentState)lastNonConfigurationInstance;
        	currentState.restoreWidgets();
        	
        	overlayGroup = currentState.cloneOverlays(this, mapView, dropdownRouteKeysToTitles, handler);
        	overlayGroup.refreshMapView(mapView);
        	
        	if (currentState.getLocationEnabled())
        	{
        		locationEnabled = true;
        		overlayGroup.getMyLocationOverlay().enableMyLocation();
        	}
        	
        	overlayGroup.getBusOverlay().refreshBalloons();
        	
        	final UpdateArguments otherArguments = currentState.getUpdateArguments();
        	
        	if (otherArguments != null) {
        		busLocations = otherArguments.getBusLocations();
        	}

        	lastUpdateTime = currentState.getLastUpdateTime();
        	previousUpdateConstantlyInterval = currentState.getUpdateConstantlyInterval();
        	selectedRouteIndex = currentState.getSelectedRouteIndex();
        	setMode(currentState.getSelectedBusPredictions());
        	progress.setVisibility(currentState.getProgressState() ? View.VISIBLE : View.INVISIBLE);
        	
        	
        	if (otherArguments != null) {
            	majorHandler = otherArguments.getMajorHandler();
        	}
        	//continue posting status updates on new textView
        	if (majorHandler != null)
        	{
        		majorHandler.setProgress(progress, progressDialog);
        	}
        }
        else
        {
        	overlayGroup = new OverlayGroup(this, busPicture, mapView, dropdownRouteKeysToTitles, handler);
        	
        	locationEnabled = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);
        }

        //final boolean showIntroScreen = prefs.getBoolean(introScreenKey, true);
    	//only show this screen once
    	//prefs.edit().putBoolean(introScreenKey, false).commit();

        if (busLocations == null)
        {
        	busLocations = new Locations(this, transitSystem);
        }

        arguments = new UpdateArguments(progress, progressDialog,
        		mapView, this, overlayGroup,
        		majorHandler, busLocations, transitSystem);
        handler = new UpdateHandler(arguments);
        overlayGroup.getBusOverlay().setUpdateable(handler);
        
        populateHandlerSettings();
        
        if (lastNonConfigurationInstance != null)
        {
			String route = dropdownRoutes[selectedRouteIndex];
			String routeTitle = dropdownRouteKeysToTitles.get(route);
        	updateSearchText();
        	handler.setSelectedBusPredictions(getMode());
        	handler.setRouteToUpdate(route);
        }
        else
        {
            int centerLat = prefs.getInt(centerLatKey, Integer.MAX_VALUE);
            int centerLon = prefs.getInt(centerLonKey, Integer.MAX_VALUE);
            int zoomLevel = prefs.getInt(zoomLevelKey, Integer.MAX_VALUE);
            selectedRouteIndex = prefs.getInt(selectedRouteIndexKey, 0);
            setMode(prefs.getInt(selectedBusPredictionsKey, VEHICLE_LOCATIONS_ONE));
            
            if (selectedRouteIndex < 0 || selectedRouteIndex >= dropdownRoutes.length)
            {
            	selectedRouteIndex = dropdownRoutes.length - 1;
            }
        	String route = dropdownRoutes[selectedRouteIndex];
        	String routeTitle = dropdownRouteKeysToTitles.get(route);
        	updateSearchText();
            handler.setSelectedBusPredictions(getMode());
            handler.setRouteToUpdate(route);

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
        }
        
        handler.setLastUpdateTime(lastUpdateTime);

        //show all icons if there are any
    	handler.triggerUpdate();
        if (handler.getUpdateConstantlyInterval() != UPDATE_INTERVAL_NONE &&
        		previousUpdateConstantlyInterval == UPDATE_INTERVAL_NONE)
        {
        	handler.instantRefresh();
        }
        
    	//enable plus/minus zoom buttons in map
        mapView.setBuiltInZoomControls(true);
        
        /*handler.post(new Runnable() {
        	public void run() {
                if (showIntroScreen || true)
                {
                	displayInstructions(Main.this);
                }
        		
        	}
        });*/
    }
		
	private static String[] getRouteTitles(String[] dropdownRoutes,
			MyHashMap<String, String> dropdownRouteKeysToTitles) {
    	String[] ret = new String[dropdownRoutes.length];
    	for (int i = 0; i < dropdownRoutes.length; i++)
    	{
    		String route = dropdownRoutes[i];
    		ret[i] = dropdownRouteKeysToTitles.get(route);
    		
    		if (ret[i] == null)
    		{
    			ret[i] = route;
    		}
    	}
    	
    	return ret;
	}

	/**
	 * Updates search text depending on current mode
	 */
	private void updateSearchText() {
		if (searchView != null)
		{
			String route = dropdownRoutes[selectedRouteIndex];
			String routeTitle = dropdownRouteKeysToTitles.get(route);
			searchView.setText("Route " + routeTitle);
		}
		else
		{
			Log.i("BostonBusMap", "Warning: search view is null");
		}
	}
	
	/**
	 * This should be called only by SearchHelper 
	 * 
	 * @param position
	 * @param saveNewQuery save a search term in the search history as if user typed it in
	 */
	public void setNewRoute(int position, boolean saveNewQuery)
    {
		if (arguments != null && handler != null)
		{
			selectedRouteIndex = position;
			String route = dropdownRoutes[position];
			handler.setRouteToUpdate(route);

			handler.immediateRefresh();
			handler.triggerUpdate();

			String routeTitle = dropdownRouteKeysToTitles.get(route);
			if (routeTitle == null)
			{
				routeTitle = route;
			}

			updateSearchText();

			if (saveNewQuery)
			{
				final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(Main.this, TransitContentProvider.AUTHORITY,
						TransitContentProvider.MODE);
				suggestions.saveRecentQuery("route " + routeTitle, null);
			}
		}
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


	private int getMode()
    {
    	int pos = toggleButton.getSelectedItemPosition();
    	if (pos < 0 || pos >= modesSupported.length)
    	{
    		return VEHICLE_LOCATIONS_ONE;
    	}
    	else
    	{
    		return modesSupported[pos];
    	}
    	
    }

    private void setMode(int selection)
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

	@Override
    protected void onPause() {
    	if (arguments != null)
    	{
    		final MapView mapView = arguments.getMapView();
    		GeoPoint point = mapView.getMapCenter();
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();

    		editor.putInt(selectedBusPredictionsKey, getMode());
    		editor.putInt(selectedRouteIndexKey, selectedRouteIndex);
    		editor.putInt(centerLatKey, point.getLatitudeE6());
    		editor.putInt(centerLonKey, point.getLongitudeE6());
    		editor.putInt(zoomLevelKey, mapView.getZoomLevel());
    		editor.commit();
    	}
    	
		
		if (handler != null)
		{
			handler.removeAllMessages();
			handler.nullifyProgress();
		}
		
		if (arguments != null) {
			arguments.getOverlayGroup().getMyLocationOverlay().disableMyLocation();
			if (arguments.getProgressDialog() != null) {
				arguments.getProgressDialog().dismiss();
			}
		}
		
		super.onPause();
    }

	
	@Override
	protected void onDestroy() {
		handler = null;
		if (arguments != null) {
			arguments.getOverlayGroup().getBusOverlay().setUpdateable(null);
			arguments.getOverlayGroup().getBusOverlay().clear();
			arguments.getMapView().getOverlays().clear();
			
			arguments.nullify();
		}
		arguments = null;
		
		
		searchView = null;
		
		toggleButton = null;
		
		
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
    			Toast.makeText(this, "Please wait 10 seconds before clicking Refresh again", Toast.LENGTH_LONG).show();
    		}
    		break;
    	case R.id.settingsMenuItem:
    		startActivity(new Intent(this, Preferences.class));
    		break;
    	case R.id.centerOnBostonMenuItem:
    	
    		if (arguments != null)
    		{
    			GeoPoint point = new GeoPoint(TransitSystem.getCenterLatAsInt(), TransitSystem.getCenterLonAsInt());
    			arguments.getMapView().getController().animateTo(point);
    			handler.triggerUpdate(1500);
    		}
    		break;
    	
    	case R.id.centerOnLocationMenuItem:
    		
    		if (arguments != null)
    		{
    			final LocationOverlay myLocationOverlay = arguments.getOverlayGroup().getMyLocationOverlay();
    			if (myLocationOverlay.isMyLocationEnabled() == false)
    			{
    				myLocationOverlay.enableMyLocation();
    				
    				locationEnabled = true;
    				
    				Toast.makeText(this, getString(R.string.findingCurrentLocation), Toast.LENGTH_SHORT).show();
    			}
   				myLocationOverlay.updateMapViewPosition();
    		}
    		
    		break;
 
    	
    	
    	case R.id.chooseRoute:
    		routeChooserDialog.show();
    		
    		break;
    		
    	case R.id.getDirectionsMenuItem:
    		{
    			// this activity starts with an Intent with an empty Bundle, which indicates
    			// all fields are blank
    			startActivityForResult(new Intent(this, GetDirectionsDialog.class), GetDirectionsDialog.GETDIRECTIONS_REQUEST_CODE);
    		}
    		
    		break;
    		
    	case R.id.chooseStop:
    		if (arguments != null)
    		{
    			StopLocation[] favoriteStops = arguments.getBusLocations().getCurrentFavorites();
    			
    			final StopLocation[] stops = StopLocation.consolidateStops(favoriteStops);

    			String[] titles = new String[stops.length];
    			for (int i = 0; i < stops.length; i++)
    			{
    				StopLocation stop = stops[i];
    				String routes;
    				if (stop.getCombinedRoutes() != null)
    				{
    					String[] array = stop.getCombinedRoutes();
    					routes = StringUtil.join(array, ", ");
    				}
    				else
    				{
    					routes = stop.getFirstRoute();
    				}
    				String title = stop.getTitle() + " (route " + routes + ")";
    				titles[i] = title;
    			}
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setTitle(getString(R.string.chooseStopInBuilder));
    			builder.setItems(titles, new DialogInterface.OnClickListener() {

    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if (which >= 0 && which < stops.length)
    					{
    						StopLocation stop = stops[which];
    						
    						String route = stop.getFirstRoute();
    						setNewStop(route, stop.getStopTag());
    						setMode(BUS_PREDICTIONS_STAR, true);
    					}
    				}
    			});
    			AlertDialog stopChooserDialog = builder.create();
    			stopChooserDialog.show();
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

        return true;
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		//TODO: what exactly should we return here? 
		if (arguments != null && arguments.getMapView().getOverlays().size() != 0)
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

		if (locationEnabled && arguments != null)
		{
			arguments.getOverlayGroup().getMyLocationOverlay().enableMyLocation();
		}
		
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
		else if (arguments != null)
		{
			final MapView mapView = arguments.getMapView();
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
    	
    	int updateInterval = getUpdateInterval(prefs);
    	handler.setUpdateConstantlyInterval(updateInterval);
    	handler.setShowUnpredictable(prefs.getBoolean(getString(R.string.showUnpredictableBusesCheckbox), false));
    	handler.setHideHighlightCircle(prefs.getBoolean(getString(R.string.hideCircleCheckbox), false));
    	handler.setInferBusRoutes(false);
    	arguments.getOverlayGroup().getRouteOverlay().setDrawLine(prefs.getBoolean(getString(R.string.showRouteLineCheckbox), false));
    	boolean showCoarseRouteLineCheckboxValue = prefs.getBoolean(getString(R.string.showCoarseRouteLineCheckbox), true); 
    	//handler.setInitAllRouteInfo(prefs.getBoolean(getString(R.string.initAllRouteInfoCheckbox2), true));
    	handler.setInitAllRouteInfo(true);
    	
    	boolean alwaysUpdateLocationValue = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);
    	
    	String intervalString = Integer.valueOf(updateInterval).toString();
    	//since the default value for this flag is true, make sure we let the preferences know of this
    	prefs.edit().
    		putBoolean(getString(R.string.alwaysShowLocationCheckbox), alwaysUpdateLocationValue).
    		putString(getString(R.string.updateContinuouslyInterval), intervalString).
    		putBoolean(getString(R.string.showCoarseRouteLineCheckbox), showCoarseRouteLineCheckboxValue)
    		.commit();
    }

	@Override
	public Object onRetainNonConfigurationInstance() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int updateConstantlyInterval = getUpdateInterval(prefs);
		
		boolean progressVisibility = false;
		if (arguments != null && arguments.getProgress() != null) {
			progressVisibility = arguments.getProgress().getVisibility() == View.VISIBLE;
		}
		return new CurrentState(arguments, handler.getLastUpdateTime(), updateConstantlyInterval,
				selectedRouteIndex, getMode(),
				progressVisibility, locationEnabled);
	}

	
	private int getUpdateInterval(SharedPreferences prefs) {
		String intervalString = prefs.getString(getString(R.string.updateContinuouslyInterval), "");
		int interval;
		if (intervalString.length() == 0) {
			interval = prefs.getBoolean(getString(R.string.runInBackgroundCheckbox), true) ? UPDATE_INTERVAL_SHORT : UPDATE_INTERVAL_NONE;
		}
		else
		{
			interval = Integer.parseInt(intervalString);
		}
		return interval;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return super.onKeyUp(keyCode, event);
		}
		else if (arguments != null)
		{
			final MapView mapView = arguments.getMapView();
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
		if (arguments != null)
		{
			handler.triggerUpdate(250);
			return arguments.getMapView().onTrackballEvent(event);
		}
		else
		{
			return false;
		}
	}

	@Override
	public void onNewIntent(Intent newIntent) {
		//since Main is marked singletop, it only uses one activity and onCreate won't get called. Use this to handle search requests 
		if (Intent.ACTION_SEARCH.equals(newIntent.getAction()))
		{
			String query = newIntent.getStringExtra(SearchManager.QUERY);

			if (query == null)
			{
				return;
			}

			
			final SearchHelper helper = new SearchHelper(this, dropdownRoutes, dropdownRouteKeysToTitles, arguments, query);
			helper.runSearch(new Runnable()
			{
				@Override
				public void run() {
					//search is finished
					
					String suggestionsQuery = helper.getSuggestionsQuery();
					if (suggestionsQuery != null)
					{
						SearchRecentSuggestions suggestions = new SearchRecentSuggestions(Main.this, TransitContentProvider.AUTHORITY,
								TransitContentProvider.MODE);
						suggestions.saveRecentQuery(suggestionsQuery, null);
						
						if (handler != null)
						{
							handler.triggerUpdate();
						}
					}
				}
			});

			
		}
	}

	public void setMode(int mode, boolean updateIcon)
	{
		int setTo = VEHICLE_LOCATIONS_ALL; 
		for (int i = 0; i < modesSupported.length; i++)
		{
			if (modesSupported[i] == mode)
			{
				setTo = mode;
				break;
			}
		}
		
		if (updateIcon)
		{
			setMode(mode);
		}
		
		suggestionsMode = setTo;
		handler.setSelectedBusPredictions(setTo);

		handler.triggerUpdate();
		handler.immediateRefresh();
		
		updateSearchText();
	}
	
	/**
	 * Sets the current selected stop to stopTag, moves map over it, sets route to route, sets mode to stops for one route
	 * @param route
	 * @param stopTag
	 */
	public void setNewStop(String route, String stopTag)
	{
		StopLocation stopLocation = arguments.getBusLocations().setSelectedStop(route, stopTag);

		if (stopLocation == null)
		{
			Log.e("BostonBusMap", "Error: stopLocation was null");
			return;
		}
		
		int routePosition = -1;
		for (int position = 0; position < dropdownRoutes.length; position++)
		{
			if (route.equals(dropdownRoutes[position]))
			{
				routePosition = position;
				break;
			}
		}
		
		
		final int id = stopLocation.getId();
		handler.triggerUpdateThenSelect(id);

		
		
		if (routePosition != -1)
		{
			//should always happen, but we just ignore this if something went wrong
			String currentRoute = getRoute();
			if (stopLocation.getRoutes().contains(currentRoute) == false)
			{
				//only set it if some route which contains this stop isn't already set
				setNewRoute(routePosition, false);
			}
		}
		
		setMode(BUS_PREDICTIONS_ONE, true);
		
		MapController controller = arguments.getMapView().getController();
		
		int latE6 = (int)(stopLocation.getLatitudeAsDegrees() * Constants.E6);
		int lonE6 = (int)(stopLocation.getLongitudeAsDegrees() * Constants.E6);
		
		GeoPoint geoPoint = new GeoPoint(latE6, lonE6);
		controller.setCenter(geoPoint);
		controller.scrollBy(0, -100);
	}

	private String getRoute() {
		return dropdownRoutes[selectedRouteIndex];
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, Intent data) {
		if (requestCode == GetDirectionsDialog.GETDIRECTIONS_REQUEST_CODE) {
			
			final String startTag = data != null ? data.getStringExtra(GetDirectionsDialog.START_TAG_KEY) : null;
			final String stopTag = data != null ? data.getStringExtra(GetDirectionsDialog.STOP_TAG_KEY) : null;
			final String startDisplay = data != null ? data.getStringExtra(GetDirectionsDialog.START_DISPLAY_KEY) : null;
			final String stopDisplay = data != null ? data.getStringExtra(GetDirectionsDialog.STOP_DISPLAY_KEY) : null;
			
			switch (resultCode) {
			case GetDirectionsDialog.EVERYTHING_OK:
			{
				if (startTag == null) {
					Toast.makeText(this, "Starting location is not set", Toast.LENGTH_LONG).show();
					break;
				}
				if (stopTag == null) {
					Toast.makeText(this, "Ending location is not set", Toast.LENGTH_LONG).show();
					break;
				}
				
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
				arguments.getBusLocations().startGetDirectionsTask(arguments, startTag, stopTag,
						location != null ? location.getLatitude() : 0,
								location != null ? location.getLongitude() : 0);
				break;
			}
			case GetDirectionsDialog.NEEDS_INPUT_FROM:
			case GetDirectionsDialog.NEEDS_INPUT_TO:
				setMode(Main.BUS_PREDICTIONS_ALL, true);
				arguments.getOverlayGroup().getBusOverlay().captureNextTap(new BusOverlay.OnClickListener() {
					
					@Override
					public void onClick(boston.Bus.Map.data.Location location) {
						if (location instanceof StopLocation) {
							StopLocation stopLocation = (StopLocation)location;
							if (resultCode == GetDirectionsDialog.NEEDS_INPUT_FROM) {
								String newStartTag = stopLocation.getStopTag();
								Intent intent = new Intent(Main.this, GetDirectionsDialog.class);
								intent.putExtra(GetDirectionsDialog.START_TAG_KEY, newStartTag);
								intent.putExtra(GetDirectionsDialog.STOP_TAG_KEY, stopTag);
								intent.putExtra(GetDirectionsDialog.START_DISPLAY_KEY, stopLocation.getTitle());
								intent.putExtra(GetDirectionsDialog.STOP_DISPLAY_KEY, stopDisplay);
								startActivityForResult(intent, GetDirectionsDialog.GETDIRECTIONS_REQUEST_CODE);
							}
							else
							{
								String newStopTag = stopLocation.getStopTag();
								Intent intent = new Intent(Main.this, GetDirectionsDialog.class);
								intent.putExtra(GetDirectionsDialog.START_TAG_KEY, startTag);
								intent.putExtra(GetDirectionsDialog.STOP_TAG_KEY, newStopTag);
								intent.putExtra(GetDirectionsDialog.START_DISPLAY_KEY, startDisplay);
								intent.putExtra(GetDirectionsDialog.STOP_DISPLAY_KEY, stopLocation.getTitle());
								startActivityForResult(intent, GetDirectionsDialog.GETDIRECTIONS_REQUEST_CODE);
							}
						}
						else
						{
							Log.e("BostonBusMap", "weird... that should have selected a stop, not a vehicle");
						}
						
					}
				});
				Toast.makeText(this, "Click on the stop you wish to select", Toast.LENGTH_LONG).show();
				break;
			}
			
		}
	}
}