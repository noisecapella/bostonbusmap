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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import java.util.List;

import boston.Bus.Map.R;

import com.commonsware.android.mapsv2.popups.AbstractMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;

import com.schneeloch.bostonbusmap_library.data.IntersectionLocation;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.provider.DatabaseAgent;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import boston.Bus.Map.provider.TransitContentProvider;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import boston.Bus.Map.tutorials.IntroTutorial;
import boston.Bus.Map.tutorials.Tutorial;

import boston.Bus.Map.ui.MapManager;
import boston.Bus.Map.ui.ModeAdapter;
import boston.Bus.Map.util.SearchHelper;

import com.schneeloch.bostonbusmap_library.util.Constants;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.os.PersistableBundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

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
	
	private static final String introScreenKey = "introScreen";
	
	public static final String tutorialStepKey = "tutorialStep";
	
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

    private ListView drawerList;
    private DrawerLayout drawerLayout;

    private UpdateArguments arguments;


    public static final int UPDATE_INTERVAL_INVALID = 9999;
	public static final int UPDATE_INTERVAL_SHORT = 15;
	public static final int UPDATE_INTERVAL_MEDIUM = 50;
	public static final int UPDATE_INTERVAL_LONG = 100;
	public static final int UPDATE_INTERVAL_NONE = 0;
	
	public static final String ROUTE_KEY = "route";
	public static final String STOP_KEY = "stop";
	public static final String MODE_KEY = "mode";

    protected int firstRunSelectedId = MapManager.NOT_SELECTED;

    private final static int DRAWER_INTERSECTIONS_MENU_ITEM_POS = 0;
    private final static int DRAWER_CHOOSE_STOP_POS = 1;
    private final static int DRAWER_CENTER_ON_CITY_POS = 2;
    private final static int DRAWER_ROUTES_POS = 3;
    private final static int DRAWER_SETTINGS_POS = 4;
    private static final String[] drawerOptions = new String[5];
    static {
        drawerOptions[DRAWER_INTERSECTIONS_MENU_ITEM_POS] = "Places";
        drawerOptions[DRAWER_CHOOSE_STOP_POS] = "Favorite Stops";
        drawerOptions[DRAWER_CENTER_ON_CITY_POS] = "Center on Boston";
        drawerOptions[DRAWER_ROUTES_POS] = "Routes";
        drawerOptions[DRAWER_SETTINGS_POS] = "Settings";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        firstRunMode = true;
        
        TransitSystem.setDefaultTimeFormat(this);
        
        //get widgets
        SupportMapFragment fragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.setRetainInstance(true);
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {

                toggleButton = (Spinner) findViewById(R.id.predictionsOrLocations);
                chooseAPlaceButton = (Button) findViewById(R.id.chooseAPlaceButton);
                chooseAFavoriteButton = (Button) findViewById(R.id.chooseFavoriteButton);
                searchView = (EditText) findViewById(R.id.searchTextView);
                final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);

                ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
                Button skipTutorialButton = (Button) findViewById(R.id.mapViewTutorialSkipButton);

                Button routesButton = (Button) findViewById(R.id.routes_button);
                routesButton.setVisibility(View.GONE);

                // TODO: find a better place for this
                drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerList = (ListView) findViewById(R.id.left_drawer);
                drawerList.setAdapter(new ArrayAdapter<String>(Main.this,
                        R.layout.drawer_list_item,
                        drawerOptions));
                drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectMenuItem(position);
                        drawerLayout.closeDrawer(drawerList);
                    }
                });

                ImageButton drawerButton = (ImageButton) findViewById(R.id.drawerButton);
                drawerButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                            drawerLayout.closeDrawer(GravityCompat.END);
                        } else {
                            drawerLayout.openDrawer(GravityCompat.END);
                        }
                    }
                });


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);

                final IDatabaseAgent databaseAgent = new DatabaseAgent(getContentResolver());

                final ProgressDialog progressDialog = new ProgressDialog(Main.this);
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

                refreshButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean b = handler.instantRefresh();
                        if (b == false) {
                            Toast.makeText(Main.this, "Please wait 10 seconds before clicking Refresh again", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                final ITransitSystem transitSystem = new TransitSystem();
                {
                    ITransitDrawables busDrawables = new TransitDrawables(
                            R.drawable.busstop_intersect, R.drawable.busstop_intersect_selected,
                            R.drawable.busstop, R.drawable.busstop_selected,
                            R.drawable.busstop_updated, R.drawable.busstop_selected
                    );
                    ITransitDrawables subwayDrawables = new TransitDrawables(
                            R.drawable.busstop_intersect, R.drawable.busstop_intersect_selected,
                            R.drawable.busstop, R.drawable.busstop_selected,
                            R.drawable.busstop_updated, R.drawable.busstop_selected
                    );
                    ITransitDrawables commuterRailDrawables = new TransitDrawables(
                            R.drawable.busstop_intersect, R.drawable.busstop_intersect_selected,
                            R.drawable.busstop, R.drawable.busstop_selected,
                            R.drawable.busstop_updated, R.drawable.busstop_selected
                    );
                    ITransitDrawables hubwayDrawables = new TransitDrawables(
                            R.drawable.busstop_intersect, R.drawable.busstop_intersect_selected,
                            R.drawable.busstop_bike, R.drawable.busstop_bike_selected,
                            R.drawable.busstop_bike_updated, R.drawable.busstop_bike_selected
                    );


                    transitSystem.setDefaultTransitSource(busDrawables, subwayDrawables, commuterRailDrawables, hubwayDrawables,
                            databaseAgent);
                }
                SpinnerAdapter modeSpinnerAdapter = new ModeAdapter(Main.this, Arrays.asList(Selection.modesSupported));

                toggleButton.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        if (firstRunMode) {
                            firstRunMode = false;
                        } else if (arguments != null && handler != null) {
                            if (position >= 0 && position < Selection.modesSupported.length) {
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    builder.setTitle(getString(R.string.chooseRouteInBuilder));
                    builder.setItems(routeTitles, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            setNewRoute(item, true, true);
                        }
                    });
                    routeChooserDialog = builder.create();
                }

                //get the busLocations variable if it already exists. We need to do that step here since handler
                long lastUpdateTime = 0;
                int previousUpdateConstantlyInterval = UPDATE_INTERVAL_NONE;

                RefreshAsyncTask majorHandler = null;


                Selection selection;
                Locations busLocations = null;
                {
                    locationEnabled = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);
                    int selectedRouteIndex = prefs.getInt(selectedRouteIndexKey, 0);
                    int modeInt = prefs.getInt(selectedBusPredictionsKey, Selection.Mode.BUS_PREDICTIONS_ALL.modeInt);
                    selection = new Selection(Selection.Mode.VEHICLE_LOCATIONS_ALL, null);
                    for (Selection.Mode mode : Selection.Mode.values()) {
                        if (mode.modeInt == modeInt) {
                            String route = dropdownRouteKeysToTitles.getTagUsingIndex(selectedRouteIndex);
                            selection = new Selection(mode, route);
                            break;
                        }
                    }
                }

                if (locationEnabled) {
                    map.setMyLocationEnabled(true);
                }

                if (busLocations == null) {
                    busLocations = new Locations(databaseAgent, transitSystem, selection);
                }

                Button reportButton = (Button) findViewById(R.id.report_problem_button);
                reportButton.setVisibility(View.GONE);
                Button moreInfoButton = (Button) findViewById(R.id.moreinfo_button);
                moreInfoButton.setVisibility(View.GONE);
                Button alertsButton = (Button) findViewById(R.id.alerts_button);
                alertsButton.setVisibility(View.GONE);
                Button editButton = (Button)findViewById(R.id.edit_button);
                editButton.setVisibility(View.GONE);
                Button deleteButton = (Button)findViewById(R.id.delete_button);
                deleteButton.setVisibility(View.GONE);

                MapManager manager = new MapManager(Main.this, map, transitSystem,
                        busLocations, reportButton, moreInfoButton, alertsButton, routesButton,
                        editButton, deleteButton);

                arguments = new UpdateArguments(progress, progressDialog,
                        map, databaseAgent, manager,
                        majorHandler, busLocations, transitSystem, Main.this);
                handler = new UpdateHandler(arguments);
                manager.setHandler(handler);

                PopupAdapter popupAdapter = new PopupAdapter(Main.this, manager, dropdownRouteKeysToTitles);
                map.setInfoWindowAdapter(popupAdapter);

                populateHandlerSettings();

                {
                    int centerLat = prefs.getInt(centerLatKey, Integer.MAX_VALUE);
                    int centerLon = prefs.getInt(centerLonKey, Integer.MAX_VALUE);
                    int zoomLevel = prefs.getInt(zoomLevelKey, Integer.MAX_VALUE);
                    setMode(selection.getMode(), true, false);

                    updateSearchText(selection);

                    if (centerLat != Integer.MAX_VALUE && centerLon != Integer.MAX_VALUE && zoomLevel != Integer.MAX_VALUE) {
                        LatLng latLng = new LatLng(centerLat * Constants.InvE6, centerLon * Constants.InvE6);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                    } else {
                        LatLng latLng = new LatLng(TransitSystem.getCenterLat(), TransitSystem.getCenterLon());
                        //move maps widget to center of transit network
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                    }
                    //make the textView blank
                }

                handler.setLastUpdateTime(lastUpdateTime);

                //show all icons if there are any
                handler.triggerUpdate();
                if (handler.getUpdateConstantlyInterval() != UPDATE_INTERVAL_NONE &&
                        previousUpdateConstantlyInterval == UPDATE_INTERVAL_NONE) {
                    handler.instantRefresh();
                }


                //enable plus/minus zoom buttons in map
                map.getUiSettings().setZoomControlsEnabled(true);

                // if app is started with selection information, use it
                Intent intent = getIntent();
                if (intent != null) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        String route = bundle.getString(ROUTE_KEY);
                        String stop = bundle.getString(STOP_KEY);
                        String modeString = bundle.getString(MODE_KEY);
                        Selection.Mode modeInt = Selection.Mode.BUS_PREDICTIONS_ALL;
                        if (modeString != null) {
                            for (Selection.Mode mode : Selection.Mode.values()) {
                                if (modeString.equals(mode.modeString)) {
                                    modeInt = mode;
                                    break;
                                }
                            }
                        }

                        if (route != null && stop != null) {
                            setNewStop(route, stop);
                            setMode(modeInt, true, true);
                        } else if (route != null) {
                            int routePosition = dropdownRouteKeysToTitles.getIndexForTag(route);
                            setNewRoute(routePosition, false, false);
                            setMode(modeInt, true, true);
                        }
                    }

                    // from http://stackoverflow.com/questions/13372326/how-to-get-getintent-to-return-null-after-activity-called-with-an-intent-set
                    intent.setData(null);
                }

                manager.setFirstRunSelectionId(firstRunSelectedId);
            }
        });
	}
		
	/**
	 * Updates search text depending on current mode
	 */
	private void updateSearchText(Selection selection) {
		if (searchView != null)
		{
            if (arguments != null && arguments.getOverlayGroup().isAlwaysFocusRoute()) {
                String route = selection.getRoute();
                String routeTitle = dropdownRouteKeysToTitles.getTitle(route);
                searchView.setText("Route " + routeTitle);
                searchView.setHint("Search routes");
            }
            else if (selection.getMode() == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
                    selection.getMode() == Selection.Mode.BUS_PREDICTIONS_ALL ||
                    selection.getMode() == Selection.Mode.BUS_PREDICTIONS_STAR) {
                searchView.setText("");
                searchView.setHint("Search routes");
            }
            else {
                String route = selection.getRoute();
                String routeTitle = dropdownRouteKeysToTitles.getTitle(route);
                searchView.setText("Route " + routeTitle);
                searchView.setHint("Search routes");
            }
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
	public void setNewRoute(int position, boolean saveNewQuery, boolean updateMode)
    {
		if (arguments != null && handler != null)
		{
			String route = dropdownRouteKeysToTitles.getTagUsingIndex(position);
			Locations locations = arguments.getBusLocations();
			Selection selection = locations.getSelection();
			locations.setSelection(selection.withDifferentRoute(route));

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

            if (updateMode) {
                if (selection.getMode() == Selection.Mode.BUS_PREDICTIONS_STAR ||
                        selection.getMode() == Selection.Mode.BUS_PREDICTIONS_ALL) {
                    setMode(Selection.Mode.BUS_PREDICTIONS_ONE, true, false);
                }
                else if (selection.getMode() == Selection.Mode.VEHICLE_LOCATIONS_ALL) {
                    setMode(Selection.Mode.VEHICLE_LOCATIONS_ONE, true, false);
                }
            }

            handler.immediateRefresh();
            handler.triggerUpdate();
        }
    }


	@Override
    protected void onPause() {
    	if (arguments != null)
    	{
    		final GoogleMap mapView = arguments.getMapView();
    		LatLng point = mapView.getCameraPosition().target;
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = prefs.edit();

    		Selection selection = arguments.getBusLocations().getSelection();
    		editor.putInt(selectedBusPredictionsKey, selection.getMode().modeInt);
    		editor.putInt(selectedRouteIndexKey, arguments.getBusLocations().getRouteAsIndex(selection.getRoute()));
    		editor.putInt(centerLatKey, (int)(point.latitude * Constants.E6));
    		editor.putInt(centerLonKey, (int)(point.longitude * Constants.E6));
    		editor.putInt(zoomLevelKey, (int)mapView.getCameraPosition().zoom);
    		editor.apply();
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

    private void selectMenuItem(int selection) {
        //when the menu button is clicked, a menu comes up
        switch (selection)
        {
            case DRAWER_SETTINGS_POS:
                startActivity(new Intent(this, Preferences.class));
                break;
            case DRAWER_CENTER_ON_CITY_POS:

                if (arguments != null)
                {
                    LatLng point = new LatLng(TransitSystem.getCenterLat(), TransitSystem.getCenterLon());
                    arguments.getMapView().moveCamera(CameraUpdateFactory.newLatLng(point));
                    handler.triggerUpdate(1500);
                }
                break;
            case DRAWER_ROUTES_POS:
                routeChooserDialog.show();

                break;

            case DRAWER_INTERSECTIONS_MENU_ITEM_POS:
                showIntersectionsDialog();
                break;


            case DRAWER_CHOOSE_STOP_POS:
                showChooseStopDialog();
                break;
            default:
                throw new RuntimeException("Unable to find selection " + selection);
        }
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
				String routeTag = stop.getFirstRoute();
				String routeTitle = arguments.getBusLocations().getRouteTitle(routeTag);
				String title = stop.getTitle() + " (route " + routeTitle + ")";
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
						setMode(Selection.Mode.BUS_PREDICTIONS_STAR, true, true);
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
                        arguments.getOverlayGroup().setNextClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(final LatLng latLng) {
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
                                        } else {
                                            float latitudeAsDegrees = (float) latLng.latitude;
                                            float longitudeAsDegrees = (float) latLng.longitude;
                                            IntersectionLocation.Builder builder = new IntersectionLocation.Builder(newName, latitudeAsDegrees, longitudeAsDegrees);
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
                    } else if (which >= 1 && which < titlesArray.length) {
                        setNewIntersection(titlesArray[which]);
                    }
                }
            });
			AlertDialog stopChooserDialog = builder.create();
			stopChooserDialog.show();

		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (arguments != null) {
            outState.putInt("selectedId", arguments.getOverlayGroup().getSelectedBusId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        firstRunSelectedId = savedInstanceState.getInt("selectedId");
    }

    @Override
	protected void onResume() {
		super.onResume();

        if (arguments != null && handler != null) {
            //check the result
            populateHandlerSettings();
            updateSearchText(arguments.getBusLocations().getSelection());
            handler.resume();

            // workaround for bad design decisions
            if (arguments.getProgressDialog() == null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);

                arguments.setProgressDialog(progressDialog);
            }

            if (arguments.getProgress() == null) {
                final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);

                arguments.setProgress(progress);
            }

        }
		
    	Tutorial tutorial = new Tutorial(IntroTutorial.populate());
    	tutorial.start(this);
	}

    private void populateHandlerSettings() {
        if (handler == null || arguments == null) {
            return;
        }
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	int updateInterval = getUpdateInterval(prefs);
    	handler.setUpdateConstantlyInterval(updateInterval);
        boolean showTraffic = prefs.getBoolean("showTraffic", false);
        MapManager manager = arguments.getOverlayGroup();
        manager.setShowTraffic(showTraffic);
    	boolean allRoutesBlue = prefs.getBoolean(getString(R.string.allRoutesBlue), TransitSystem.isDefaultAllRoutesBlue());
    	manager.setAllRoutesBlue(allRoutesBlue);

        boolean changeRouteIfSelected = prefs.getBoolean("showLinesOnSelected", true);
        manager.setChangeRouteIfSelected(changeRouteIfSelected);

        boolean alwaysFocusRoute = prefs.getBoolean("alwaysFocusRoute", false);
        manager.setAlwaysFocusRoute(alwaysFocusRoute);

        manager.setAllRoutesBlue(allRoutesBlue);

        manager.setDrawLine(prefs.getBoolean("showRouteLineCheckbox2", true));

    	locationEnabled = prefs.getBoolean(getString(R.string.alwaysShowLocationCheckbox), true);

    	String intervalString = Integer.valueOf(updateInterval).toString();
    	//since the default value for this flag is true, make sure we let the preferences know of this
    	prefs.edit().
    		putBoolean(getString(R.string.alwaysShowLocationCheckbox), locationEnabled).
    		putString(getString(R.string.updateContinuouslyInterval), intervalString).
            putBoolean("showRouteLineCheckbox2", arguments.getOverlayGroup().isShowLine()).
    		putBoolean(getString(R.string.allRoutesBlue), allRoutesBlue).
            putBoolean("showTraffic", showTraffic).
            putBoolean("showLinesOnSelected", changeRouteIfSelected).
    		apply();
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

			
			final SearchHelper helper = new SearchHelper(this, dropdownRouteKeysToTitles, arguments, query, new DatabaseAgent(getContentResolver()));
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

	public void setMode(Selection.Mode mode, boolean updateIcon, boolean triggerRefresh)
	{
		Selection.Mode setTo = Selection.Mode.VEHICLE_LOCATIONS_ALL;
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
		Selection.Mode mode = selection.getMode();
		if (mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
			chooseAFavoriteButton.setVisibility(View.VISIBLE);
			chooseAPlaceButton.setVisibility(View.GONE);
		}
		else
		{
			chooseAFavoriteButton.setVisibility(View.GONE);
			chooseAPlaceButton.setVisibility(View.GONE);
		}
	}

	public void setNewIntersection(String name) {
		if (arguments != null) {
			Locations locations = arguments.getBusLocations();
			
			setMode(Selection.Mode.BUS_PREDICTIONS_ALL, true, false);
			
			IntersectionLocation newLocation = locations.getIntersection(name);
			if (newLocation != null) {

                LatLng latlng = new LatLng(newLocation.getLatitudeAsDegrees(), newLocation.getLongitudeAsDegrees());

                arguments.getMapView().moveCamera(CameraUpdateFactory.newLatLng(latlng));
                arguments.getMapView().moveCamera(CameraUpdateFactory.scrollBy(0, -100));

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
				setNewRoute(routePosition, false, false);
			}
		}
		
		setMode(Selection.Mode.BUS_PREDICTIONS_ONE, true, true);

        LatLng latlng = new LatLng(stopLocation.getLatitudeAsDegrees(), stopLocation.getLongitudeAsDegrees());
        arguments.getMapView().moveCamera(CameraUpdateFactory.newLatLng(latlng));
        arguments.getMapView().moveCamera(CameraUpdateFactory.scrollBy(0, -100));
	}
}