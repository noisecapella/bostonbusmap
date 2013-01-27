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
import java.util.Collection;
import java.util.Collections;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;
import org.xml.sax.SAXException;

import boston.Bus.Map.R;
import boston.Bus.Map.algorithms.GetDirections;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Locations;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.provider.TransitContentProvider;
import boston.Bus.Map.transit.TransitSystem;

import boston.Bus.Map.ui.MapManager;
import boston.Bus.Map.ui.ModeAdapter;
import boston.Bus.Map.ui.ViewingMode;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.SearchHelper;
import boston.Bus.Map.util.StringUtil;


import com.commonsware.android.AbstractMapActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;

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
public class Main extends AbstractMapActivity
{
	private static final String selectedRouteIndexKey = "selectedRouteIndex";
	private static final String selectedBusPredictionsKey = "selectedBusPredictions";
	private static final String centerLatKey = "centerLat";
	private static final String centerLonKey = "centerLon";
	private static final String zoomLevelKey = "zoomLevel";
	private static final String selectedIntersectionKey = "selectedIntersection";
	//private static final String gpsAlwaysOn = "gpsAlwaysOn";
	private static final String markUpdatedStops = "markUpdatedStops";
	private static final String selectedKey = "selected";
	
	private static final String introScreenKey = "introScreen";
	private EditText searchView;
	
	/**
	 * Used to make updateBuses run every 10 seconds or so
	 */
	private UpdateHandler handler;
	
	/**
	 * This is used to indicate to the mode spinner to ignore the first time we set it, so we don't update every time the screen changes
	 */
	private boolean firstRunMode;
	
	/**
	 * Is location overlay supposed to be enabled? Used mostly for onResume()
	 */
	private boolean locationEnabled; 
	
	private Spinner toggleButton;
	
	private Button chooseAPlaceButton;
	private Button chooseAFavoriteButton;
	
	/**
	 * The list of routes that's selectable in the routes dropdown list
	 */
	private RouteTitles dropdownRouteKeysToTitles;
	private AlertDialog routeChooserDialog;

	private ImageButton searchButton;

	private UpdateArguments arguments;
	
	
	public static final int UPDATE_INTERVAL_INVALID = 9999;
	public static final int UPDATE_INTERVAL_SHORT = 15;
	public static final int UPDATE_INTERVAL_MEDIUM = 50;
	public static final int UPDATE_INTERVAL_LONG = 100;
	public static final int UPDATE_INTERVAL_NONE = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        firstRunMode = true;
        
        TransitSystem.setDefaultTimeFormat(this);
        
        //get widgets
        
        SupportMapFragment fragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.setRetainInstance(true);
        final GoogleMap map = fragment.getMap();
        
        toggleButton = (Spinner)findViewById(R.id.predictionsOrLocations);
        chooseAPlaceButton = (Button)findViewById(R.id.chooseAPlaceButton);
        chooseAFavoriteButton = (Button)findViewById(R.id.chooseFavoriteButton);
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
        
        chooseAPlaceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showIntersectionsDialog();
			}
		});
        
        chooseAFavoriteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showChooseStopDialog();
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
        	
        	transitSystem.setDefaultTransitSource(busDrawables, subwayDrawables, commuterRailDrawables, this);
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
					if (position >= 0 && position < Selection.modesSupported.length)
					{
						setMode(Selection.modesSupported[position], false, true);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//do nothing
			}
			

		});

        toggleButton.setAdapter(modeSpinnerAdapter);

        
        dropdownRouteKeysToTitles = transitSystem.getRouteKeysToTitles();
        
        {
            String[] routeTitles = dropdownRouteKeysToTitles.titleArray();
            
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

        RefreshAsyncTask majorHandler = null;
        
        Selection selection;
        locationEnabled = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);
        int selectedRouteIndex = prefs.getInt(selectedRouteIndexKey, 0);
        int mode = prefs.getInt(selectedBusPredictionsKey, Selection.BUS_PREDICTIONS_ONE);
        String route = dropdownRouteKeysToTitles.getTagUsingIndex(selectedRouteIndex);
        String intersection = prefs.getString(selectedIntersectionKey, null);
        selection = new Selection(mode, route, intersection);

        //final boolean showIntroScreen = prefs.getBoolean(introScreenKey, true);
    	//only show this screen once
    	//prefs.edit().putBoolean(introScreenKey, false).commit();

       	Drawable intersectionDrawable = getResources().getDrawable(R.drawable.busstop_intersect_statelist);
       	Locations busLocations = new Locations(this, transitSystem, selection, intersectionDrawable);

        MapManager manager = new MapManager(this, map, 
        		busLocations, dropdownRouteKeysToTitles);
        
        
        arguments = new UpdateArguments(progress, progressDialog,
        		map, this,
        		majorHandler, busLocations, manager, transitSystem);
        handler = new UpdateHandler(arguments);
        manager.setHandler(handler);
        
        populateHandlerSettings();
        
        PopupAdapter popupAdapter = new PopupAdapter(this,
        		handler, busLocations, dropdownRouteKeysToTitles, manager);
        map.setInfoWindowAdapter(popupAdapter);

        int centerLat = prefs.getInt(centerLatKey, Integer.MAX_VALUE);
        int centerLon = prefs.getInt(centerLonKey, Integer.MAX_VALUE);
        int zoomLevel = prefs.getInt(zoomLevelKey, Integer.MAX_VALUE);
        int selected = prefs.getInt(selectedKey, MapManager.NOT_SELECTED);
        manager.setSelectedBusId(selected);
        setMode(selection.getMode(), true, false);

        updateSearchText(selection);

        if (centerLat != Integer.MAX_VALUE && centerLon != Integer.MAX_VALUE && zoomLevel != Integer.MAX_VALUE)
        {
        	LatLng latLng = new LatLng(centerLat * Constants.InvE6, centerLon * Constants.InvE6);
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        }
        else
        {
        	//move maps widget to center of transit network
        	LatLng latLng = TransitSystem.getCenter();
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
        //make the textView blank
        
        handler.setLastUpdateTime(lastUpdateTime);

        //show all icons if there are any
    	handler.triggerUpdate();
        if (handler.getUpdateConstantlyInterval() != UPDATE_INTERVAL_NONE &&
        		previousUpdateConstantlyInterval == UPDATE_INTERVAL_NONE)
        {
        	handler.instantRefresh();
        }
        
    	//enable plus/minus zoom buttons in map
        map.getUiSettings().setZoomControlsEnabled(true);
    }
		
	/**
	 * Updates search text depending on current mode
	 */
	private void updateSearchText(Selection selection) {
		if (searchView != null)
		{
			if (selection.getMode() == Selection.BUS_PREDICTIONS_INTERSECT) {
				String intersection = selection.getIntersection();
				if (arguments.getBusLocations().containsIntersection(intersection)) {
					if (intersection.toLowerCase().startsWith("place ")) {
						searchView.setText(intersection);
					}
					else
					{
						searchView.setText("Place " + intersection);
					}
				}
				else
				{
					searchView.setText("No place selected, click '...'");
				}
			}
			String route = selection.getRoute();
			String routeTitle = dropdownRouteKeysToTitles.getTitle(route);
			searchView.setText("Route " + routeTitle);
		}
		else
		{
			Log.i("BostonBusMap", "ERROR: search view is null");
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
			String route = dropdownRouteKeysToTitles.getTagUsingIndex(position);
			Locations locations = arguments.getBusLocations();
			Selection selection = locations.getSelection();
			locations.setSelection(selection.withDifferentRoute(route));

			handler.immediateRefresh();
			handler.triggerUpdate();

			String routeTitle = dropdownRouteKeysToTitles.getTitle(route);
			if (routeTitle == null)
			{
				routeTitle = route;
			}

			updateSearchText(locations.getSelection());

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
        
        for (int i = 0; i < Selection.modesSupported.length; i++)
        {
        	ViewingMode mode = new ViewingMode(Selection.modeIconsSupported[i], Selection.modeTextSupported[i]);
        	modes.add(mode);
        }
        
        ModeAdapter adapter = new ModeAdapter(this, modes);
        
        return adapter;
	}

	@Override
    protected void onPause() {
    	if (arguments != null)
    	{
    		LatLng point = arguments.getMapView().getCameraPosition().target;
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();

    		Selection selection = arguments.getBusLocations().getSelection();
    		editor.putInt(selectedBusPredictionsKey, selection.getMode());
    		editor.putString(selectedIntersectionKey, selection.getIntersection());
    		editor.putInt(selectedRouteIndexKey, arguments.getBusLocations().getRouteAsIndex(selection.getRoute()));
    		editor.putInt(centerLatKey, (int)(point.latitude * Constants.E6));
    		editor.putInt(centerLonKey, (int)(point.longitude * Constants.E6));
    		editor.putInt(zoomLevelKey, (int)arguments.getMapView().getCameraPosition().zoom);
    		editor.putInt(selectedKey, (int)arguments.getOverlayGroup().getSelectedBusId());
    		editor.commit();
    	}
    	
		
		if (handler != null)
		{
			handler.removeAllMessages();
			handler.nullifyProgress();
		}
		
		if (arguments != null) {
			if (arguments.getProgressDialog() != null) {
				arguments.getProgressDialog().dismiss();
			}
		}
		
		super.onPause();
    }

	
	@Override
	protected void onDestroy() {
		handler = null;
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
    			arguments.getMapView().animateCamera(CameraUpdateFactory.newLatLng(TransitSystem.getCenter()));
    			handler.triggerUpdate(1500);
    		}
    		break;
    	
    	case R.id.chooseRoute:
    		routeChooserDialog.show();
    		
    		break;
    		
    	case R.id.intersectionsMenuItem:
    		showIntersectionsDialog();
    		break;
    	
    		
    	/*case R.id.getDirectionsMenuItem:
    		{
    			// this activity starts with an Intent with an empty Bundle, which indicates
    			// all fields are blank
    			startActivityForResult(new Intent(this, GetDirectionsDialog.class), GetDirectionsDialog.GETDIRECTIONS_REQUEST_CODE);
    		}
    		
    		break;
    		*/
    	case R.id.chooseStop:
    		showChooseStopDialog();
    		break;
    	}
    	return true;
    }

    
    private void showChooseStopDialog() {
		if (arguments != null)
		{
			StopLocation[] favoriteStops = arguments.getBusLocations().getCurrentFavorites();
			
			final StopLocation[] stops = StopLocation.consolidateStops(favoriteStops);

			String[] titles = new String[stops.length];
			for (int i = 0; i < stops.length; i++)
			{
				StopLocation stop = stops[i];
				String routes = stop.getFirstRoute();
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
						setMode(Selection.BUS_PREDICTIONS_STAR, true, true);
					}
				}
			});
			AlertDialog stopChooserDialog = builder.create();
			stopChooserDialog.show();
		}
	}

	private void showIntersectionsDialog() {
    	
		if (arguments != null) {
			Collection<String> unsortedTitles = arguments.getBusLocations().getIntersectionNames();
			
			List<String> titles = Lists.newArrayList(unsortedTitles);
			Collections.sort(titles);
			final String[] titlesArray = new String[titles.size() + 1];
			titlesArray[0] = "Add new place...";
			for (int i = 0; i < titles.size(); i++) {
				titlesArray[i+1] = titles.get(i);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.places));
			builder.setItems(titlesArray, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						Toast.makeText(Main.this, "Tap a spot on the map to create a place", Toast.LENGTH_LONG).show();
						arguments.getOverlayGroup().setNextClickListener(new OnMapClickListener() {
							
							@Override
							public void onMapClick(final LatLng point) {
								AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
								builder.setTitle("New place name");

								final EditText textView = new EditText(Main.this);
								textView.setHint("Place name (ie, Home or Work)");
								builder.setView(textView);
								builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String newName = textView.getText().toString();
										if (newName.length() == 0) {
											Toast.makeText(Main.this, "Place name cannot be empty", Toast.LENGTH_LONG).show();
										}
										else
										{
											float latitudeAsDegrees = (float)point.latitude; 
											float longitudeAsDegrees = (float)point.longitude; 
											Drawable drawable = getResources().getDrawable(R.drawable.busstop_intersect);
											IntersectionLocation.Builder builder = new IntersectionLocation.Builder(newName, latitudeAsDegrees, longitudeAsDegrees, drawable);
											Locations locations = arguments.getBusLocations();
											
											locations.addIntersection(builder);
											Toast.makeText(Main.this, "New place created!", Toast.LENGTH_LONG).show();
											setNewIntersection(newName);
										}
										dialog.dismiss();
									}
								});
								
								builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								
								builder.create().show();
							}
							
						});
					}
					else if (which >= 1 && which < titlesArray.length) {
						setNewIntersection(titlesArray[which]);
					}
				}
			});
			AlertDialog stopChooserDialog = builder.create();
			stopChooserDialog.show();

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
	protected void onResume() {
		super.onResume();

		if (locationEnabled && arguments != null)
		{
			arguments.getMapView().setMyLocationEnabled(true);
		}
		
		//check the result
		populateHandlerSettings();
		handler.resume();
		
	}


	
    private void populateHandlerSettings() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	int updateInterval = getUpdateInterval(prefs);
    	handler.setUpdateConstantlyInterval(updateInterval);
    	handler.setShowUnpredictable(prefs.getBoolean(getString(R.string.showUnpredictableBusesCheckbox), false));
    	handler.setHideHighlightCircle(prefs.getBoolean(getString(R.string.hideCircleCheckbox), false));
    	boolean allRoutesBlue = prefs.getBoolean(getString(R.string.allRoutesBlue), TransitSystem.defaultAllRoutesBlue);
    	handler.setAllRoutesBlue(allRoutesBlue);
        arguments.getOverlayGroup().setDrawLine(prefs.getBoolean(getString(R.string.showRouteLineCheckbox), false));
    	boolean showCoarseRouteLineCheckboxValue = prefs.getBoolean(getString(R.string.showCoarseRouteLineCheckbox), true); 
    	//handler.setInitAllRouteInfo(prefs.getBoolean(getString(R.string.initAllRouteInfoCheckbox2), true));
    	handler.setInitAllRouteInfo(true);
    	
    	boolean alwaysUpdateLocationValue = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);
    	
    	String intervalString = Integer.valueOf(updateInterval).toString();
    	//since the default value for this flag is true, make sure we let the preferences know of this
    	prefs.edit().
    		putBoolean(getString(R.string.alwaysShowLocationCheckbox), alwaysUpdateLocationValue).
    		putString(getString(R.string.updateContinuouslyInterval), intervalString).
    		putBoolean(getString(R.string.showCoarseRouteLineCheckbox), showCoarseRouteLineCheckboxValue).
    		putBoolean(getString(R.string.allRoutesBlue), allRoutesBlue)
    		.commit();
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
	public void onNewIntent(Intent newIntent) {
		//since Main is marked singletop, it only uses one activity and onCreate won't get called. Use this to handle search requests 
		if (Intent.ACTION_SEARCH.equals(newIntent.getAction()))
		{
			String query = newIntent.getStringExtra(SearchManager.QUERY);

			if (query == null)
			{
				return;
			}

			
			final SearchHelper helper = new SearchHelper(this, dropdownRouteKeysToTitles, arguments, query);
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

	public void setMode(int mode, boolean updateIcon, boolean triggerRefresh)
	{
		int setTo = Selection.VEHICLE_LOCATIONS_ALL; 
		for (int i = 0; i < Selection.modesSupported.length; i++)
		{
			if (Selection.modesSupported[i] == mode)
			{
				setTo = mode;
				break;
			}
		}
		
		if (updateIcon)
		{
	    	for (int i = 0; i < Selection.modesSupported.length; i++)
	    	{
	    		if (Selection.modesSupported[i] == mode)
	    		{
	    			toggleButton.setSelection(i);
	    			break;
	    		}
	    	}
		}
		
		Locations locations = arguments.getBusLocations();
		Selection oldSelection = locations.getSelection();
		Selection newSelection = oldSelection.withDifferentMode(setTo);
		locations.setSelection(newSelection);

		if (triggerRefresh) {
			handler.triggerUpdate();
			handler.immediateRefresh();
		}
		
		updateSearchText(newSelection);
		updateButtonVisibility(newSelection);
	}
	
	private void updateButtonVisibility(Selection selection) {
		int mode = selection.getMode();
		if (mode == Selection.BUS_PREDICTIONS_STAR) {
			chooseAFavoriteButton.setVisibility(View.VISIBLE);
			chooseAPlaceButton.setVisibility(View.GONE);
		}
		else if (mode == Selection.BUS_PREDICTIONS_INTERSECT) {
			chooseAFavoriteButton.setVisibility(View.GONE);
			chooseAPlaceButton.setVisibility(View.VISIBLE);
		}
		else
		{
			chooseAFavoriteButton.setVisibility(View.GONE);
			chooseAPlaceButton.setVisibility(View.GONE);
		}
	}

	private void animateCamera(GoogleMap map, boston.Bus.Map.data.Location location) {
		LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());

		map.animateCamera(CameraUpdateFactory.newLatLng(latlng));
		map.animateCamera(CameraUpdateFactory.scrollBy(0, -100));

	}
	
	public void setNewIntersection(String name) {
		if (arguments != null) {
			Locations locations = arguments.getBusLocations();
			Selection oldSelection = locations.getSelection();
			Selection newSelection = oldSelection.withDifferentIntersection(name);
			locations.setSelection(newSelection);
			
			setMode(Selection.BUS_PREDICTIONS_INTERSECT, true, false);
			
			IntersectionLocation newLocation = locations.getIntersection(name);
			if (newLocation != null) {
				GoogleMap map = arguments.getMapView();
				animateCamera(map, newLocation);
				handler.triggerUpdateThenSelect(newLocation.getId());
				
			}
			
		}
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
		
		int routePosition = dropdownRouteKeysToTitles.getIndexForTag(route);
		
		
		final int id = stopLocation.getId();
		handler.triggerUpdateThenSelect(id);

		
		
		if (routePosition != -1)
		{
			//should always happen, but we just ignore this if something went wrong
			String currentRoute = dropdownRouteKeysToTitles.getTagUsingIndex(routePosition);
			if (stopLocation.getRoutes().contains(currentRoute) == false)
			{
				//only set it if some route which contains this stop isn't already set
				setNewRoute(routePosition, false);
			}
		}
		
		setMode(Selection.BUS_PREDICTIONS_ONE, true, true);
		
		animateCamera(arguments.getMapView(), stopLocation);
	}

}