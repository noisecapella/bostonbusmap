package boston.Bus.Map.ui;

import android.app.AlertDialog;
import android.text.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schneeloch.bostonbusmap_library.data.Alert;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Favorite;
import com.schneeloch.bostonbusmap_library.data.IPrediction;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.IntersectionLocation;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.Path;
import com.schneeloch.bostonbusmap_library.data.PredictionView;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.StopPredictionView;
import com.schneeloch.bostonbusmap_library.data.TimeBounds;
import com.schneeloch.bostonbusmap_library.math.Geometry;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import org.nayuki.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import boston.Bus.Map.R;
import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.main.UpdateHandler;

public class MapManager implements OnMapClickListener, OnMarkerClickListener,
        OnCameraChangeListener, OnInfoWindowClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private final GoogleMap map;
    public static final int NOT_SELECTED = -1;

    private final Map<String, Marker> markers = Maps.newHashMap();
    private final BiMap<String, Integer> markerIdToLocationId = HashBiMap.create();
    private final Map<Integer, Location> locationIdToLocation = Maps.newHashMap();
    private final Map<String, Integer> markerIdToResourceId = Maps.newHashMap();
    private final Map<String, PredictionView> markerIdToPredictionView = Maps.newHashMap();
    private final Map<String, Favorite> markerIdToFavorite = Maps.newHashMap();
    private final Button reportButton;
    private final Button alertsButton;
    private final Button moreInfoButton;
    private final Button routesButton;
    private final Button editButton;
    private final Button deleteButton;
    private int selectedLocationId = NOT_SELECTED;

    private OnMapClickListener nextTapListener;
    private boolean allRoutesBlue;

    private final Map<String, PolylineGroup> polylines = Maps.newHashMap();
    private final ITransitSystem transitSystem;
    private final Locations locations;

    /**
     * This should be set in constructor but we need to instantiate this object
     * before it exists, so you should use setHandler once you have access
     * to an UpdateHandler instead
     */
    private UpdateHandler handler;
    private boolean drawLine;

    private boolean firstRun = true;
    private int firstRunSelectionId = NOT_SELECTED;
    private final Main context;
    private boolean changeRouteIfSelected;
    private boolean alwaysFocusRoute;

    public MapManager(Main context, GoogleMap map,
                      ITransitSystem transitSystem, Locations locations,
                      Button reportButton, Button moreInfoButton, Button alertsButton, Button routesButton,
                      Button editButton, Button deleteButton) {
        this.context = context;
        this.map = map;
        this.transitSystem = transitSystem;
        this.moreInfoButton = moreInfoButton;
        this.reportButton = reportButton;
        this.alertsButton = alertsButton;
        this.routesButton = routesButton;
        this.editButton = editButton;
        this.deleteButton = deleteButton;
        this.locations = locations;

        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMyLocationButtonClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String markerId = marker.getId();
        Integer id = markerIdToLocationId.get(markerId);
        int newLocationid;
        if (id == null) {
            newLocationid = NOT_SELECTED;
        }
        else
        {
            newLocationid = id;
        }

        setSelectedBusId(newLocationid);

        return true;
    }

    private void setMarker(String markerId, int icon) {
        Integer previousIcon = markerIdToResourceId.get(markerId);
        if (previousIcon == null || previousIcon != icon) {
            Marker marker = markers.get(markerId);
            if (marker == null) {
                return;
            }
            marker.setIcon(BitmapDescriptorFactory.fromResource(icon));
            markerIdToResourceId.put(markerId, icon);
        }
    }

    private void updateInfo(String markerId) {
        Integer locationId = markerIdToLocationId.get(markerId);
        if (locationId == null) {
            return;
        }
        Location location = locationIdToLocation.get(locationId);
        if (location == null) {
            return;
        }

        try {
            Selection.Mode mode = locations.getSelection().getMode();
            RouteConfig selectedRouteConfig;
            if (mode == Selection.Mode.BUS_PREDICTIONS_STAR ||
                    mode == Selection.Mode.BUS_PREDICTIONS_ALL) {
                //we want this to be null. Else, the snippet drawing code would only show data for a particular route
                selectedRouteConfig = null;
            }
            else {
                selectedRouteConfig = locations.getRoute(locations.getSelection().getRoute());
            }

            location.makeSnippetAndTitle(selectedRouteConfig, locations.getRouteTitles(), locations);
            // getLocations only used here to find all locations at exact same place
            for (Location locationNearMe : locations.getLocations(15, location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees(), false, locations.getSelection())) {
                if (locationNearMe.getId() != location.getId() &&
                        locationNearMe.getLatitudeAsDegrees() == location.getLatitudeAsDegrees() &&
                        locationNearMe.getLongitudeAsDegrees() == location.getLongitudeAsDegrees()) {
                    location.addToSnippetAndTitle(selectedRouteConfig, locationNearMe, locations.getRouteTitles(), locations);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        PredictionView predictionView = location.getPredictionView();
        PredictionView previousPredictionView = markerIdToPredictionView.get(markerId);
        Favorite favorite = markerIdToFavorite.get(markerId);
        if (previousPredictionView == null || previousPredictionView != predictionView ||
                favorite == null || favorite != location.isFavorite()) {
            Marker marker = markers.get(markerId);
            if (marker == null) {
                return;
            }

            marker.showInfoWindow();
            markerIdToFavorite.put(markerId, favorite);
            markerIdToPredictionView.put(markerId, predictionView);
        }

    }

    private void updateMarkerAndButtons(int oldSelectedId, int newSelectedId) {
        // hide old marker if applicable
        String oldMarkerId = markerIdToLocationId.inverse().get(oldSelectedId);
        Marker oldMarker = markers.get(oldMarkerId);

        String newMarkerId = markerIdToLocationId.inverse().get(newSelectedId);
        Marker newMarker = markers.get(newMarkerId);

        if (oldMarker != null) {
            // handle old marker
            if (newMarker == null || oldMarker != newMarker) {
                // hide old marker
                Location oldLocation = locationIdToLocation.get(oldSelectedId);

                // since they are different markers hide the old one
                oldMarker.hideInfoWindow();
                markerIdToPredictionView.remove(oldMarkerId);
                ITransitDrawables transitDrawables = transitSystem.getTransitSourceByRouteType(
                        oldLocation.getTransitSourceType()).getDrawables();

                int icon = transitDrawables.getBitmapDescriptor(oldLocation, false);
                setMarker(oldMarkerId, icon);
            }
            // else, select the same stop
            // this is probably the typical case since it happens every time a refresh happens
        }

        if (newMarker == null) {
            moreInfoButton.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
            alertsButton.setVisibility(View.GONE);
            routesButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
        else {
            final Location newLocation = locationIdToLocation.get(newSelectedId);
            if (newLocation == null) {
                moreInfoButton.setVisibility(View.GONE);
                reportButton.setVisibility(View.GONE);
                alertsButton.setVisibility(View.GONE);
                routesButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);

            } else {
                // we are selecting a new marker
                final ITransitDrawables transitDrawables = transitSystem.getTransitSourceByRouteType(
                        newLocation.getTransitSourceType()).getDrawables();

                int icon = transitDrawables.getBitmapDescriptor(newLocation, true);
                setMarker(newMarkerId, icon);
                updateInfo(newMarkerId);


                if (newLocation instanceof StopLocation) {
                    moreInfoButton.setVisibility(View.VISIBLE);
                } else {
                    moreInfoButton.setVisibility(View.GONE);
                }
                moreInfoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //user shouldn't be able to click this if this is a BusLocation, but just in case...
                        if (newLocation instanceof StopLocation) {
                            RouteTitles routeKeysToTitles = transitSystem.getRouteKeysToTitles();

                            StopLocation stopLocation = (StopLocation) newLocation;
                            Intent intent = new Intent(context, MoreInfo.class);

                            intent.putExtra(AlertInfo.snippetTitleKey, newLocation.getPredictionView().getSnippetTitle());


                            StopPredictionView predictionView = (StopPredictionView) stopLocation.getPredictionView();
                            IPrediction[] predictionArray = predictionView.getPredictions();
                            if (predictionArray != null) {
                                intent.putExtra(MoreInfo.predictionsKey, predictionArray);
                            }

                            try {
                                TimeBounds[] bounds = new TimeBounds[predictionView.getRouteTitles().length];
                                int i = 0;
                                for (String routeTitle : predictionView.getRouteTitles()) {
                                    String routeKey = routeKeysToTitles.getKey(routeTitle);
                                    bounds[i] = locations.getRoute(routeKey).getTimeBounds();
                                    i++;
                                }
                                intent.putExtra(MoreInfo.boundKey, bounds);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            String[] combinedTitles = predictionView.getTitles();
                            intent.putExtra(MoreInfo.titleKey, combinedTitles);

                            String[] combinedRoutes = predictionView.getRouteTitles();
                            intent.putExtra(MoreInfo.routeTitlesKey, combinedRoutes);

                            String combinedStops = predictionView.getStops();
                            intent.putExtra(MoreInfo.stopsKey, combinedStops);

                            intent.putExtra(MoreInfo.stopIsBetaKey, stopLocation.isBeta());

                            context.startActivity(intent);
                        }
                    }
                });

                if (newLocation.hasReportProblem()) {
                    reportButton.setVisibility(View.VISIBLE);
                    reportButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Who should get the error report?");
                            builder.setItems(new String[]{
                                    TransitSystem.getAgencyName(),
                                    "App Developer"
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        AlertDialog.Builder transitBuilder = new AlertDialog.Builder(context);
                                        transitBuilder.setTitle("");
                                        transitBuilder.setMessage("The report message has been copied to your clipboard." +
                                                " This message contains specifics about the stop, route or vehicle" +
                                                " that was selected when you clicked 'Report Problem'." +
                                                " Click 'Ok' to visit their customer comment form, then you may paste " +
                                                " this report into their textbox.");
                                        transitBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        transitBuilder.setPositiveButton("Visit their website", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                manager.setText(createEmailBody(newLocation));

                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(TransitSystem.getFeedbackUrl()));
                                                context.startActivity(intent);
                                            }
                                        });
                                        transitBuilder.create().show();
                                    } else if (which == 1) {
                                        //Intent intent = new Intent(context, ReportProblem.class);
                                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                                        intent.setType("plain/text");

                                        intent.putExtra(android.content.Intent.EXTRA_EMAIL, TransitSystem.getEmails());
                                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, TransitSystem.getEmailSubject());


                                        String otherText = createEmailBody(newLocation);

                                        intent.putExtra(android.content.Intent.EXTRA_TEXT, otherText);
                                        context.startActivity(Intent.createChooser(intent, "Send email..."));
                                    }
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
                } else {
                    reportButton.setVisibility(View.GONE);
                }

                final ImmutableCollection<Alert> alertsList = newLocation.getPredictionView().getAlerts();
                int numAlerts = alertsList.size();
                if (numAlerts == 0) {
                    alertsButton.setVisibility(View.GONE);
                } else {
                    alertsButton.setVisibility(View.VISIBLE);
                    if (numAlerts == 1) {
                        alertsButton.setText("\u26a0 " + numAlerts + " Alert");
                    } else {
                        alertsButton.setText("\u26a0 " + numAlerts + " Alerts");
                    }
                }
                alertsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Alert[] alerts = alertsList.toArray(new Alert[0]);

                        Intent intent = new Intent(context, AlertInfo.class);
                        intent.putExtra(AlertInfo.alertsKey, alerts);

                        intent.putExtra(AlertInfo.snippetTitleKey, newLocation.getPredictionView().getSnippetTitle());

                        context.startActivity(intent);
                    }
                });

                if (newLocation instanceof StopLocation || newLocation instanceof IntersectionLocation) {
                    final List<String> routeTitlesForStop = Lists.newArrayList();
                    for (String route : newLocation.getRoutes()) {
                        routeTitlesForStop.add(locations.getTransitSystem().getRouteKeysToTitles().getTitle(route));
                    }
                    Collections.sort(routeTitlesForStop);
                    final String[] routeTitlesArray = routeTitlesForStop.toArray(new String[0]);
                    final List<String> allRouteTitles = Arrays.asList(locations.getTransitSystem().getRouteKeysToTitles().titleArray());

                    routesButton.setVisibility(View.VISIBLE);
                    if (newLocation instanceof StopLocation) {
                        routesButton.setText("Routes for stop");
                    }
                    else {
                        routesButton.setText("Routes nearby");
                    }
                    routesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(context.getString(R.string.chooseRouteInBuilder));
                            builder.setItems(routeTitlesArray, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    String title = routeTitlesArray[item];
                                    int newPosition = allRouteTitles.indexOf(title);
                                    context.setNewRoute(newPosition, true, true);
                                }
                            });
                            builder.create().show();
                        }
                    });

                } else {
                    routesButton.setVisibility(View.GONE);
                }

                if (newLocation instanceof IntersectionLocation) {
                    editButton.setVisibility(View.VISIBLE);
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IntersectionLocation intersectionLocation = (IntersectionLocation) newLocation;

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Edit place name");

                            final EditText textView = new EditText(context);
                            textView.setHint("Place name (ie, Home)");
                            final String oldName = intersectionLocation.getName();
                            textView.setText(oldName);
                            builder.setView(textView);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newName = textView.getText().toString();
                                    if (newName.length() == 0) {
                                        Toast.makeText(context, "Place name cannot be empty", Toast.LENGTH_LONG).show();
                                    } else {
                                        locations.editIntersection(oldName, newName);
                                        handler.triggerUpdate();
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
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Delete Place");
                            builder.setMessage("Are you sure?");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IntersectionLocation intersection = (IntersectionLocation)newLocation;
                                    locations.removeIntersection(intersection.getName());
                                    handler.triggerUpdate();
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
                else {
                    editButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }
            }
        }
    }

    protected String createEmailBody(Location location)
    {
        Selection selection = locations.getSelection();
        if (selection == null) {
            selection = new Selection(null, null);
        }

        String routeTitle = selection.getRoute();
        if (routeTitle != null) {
            routeTitle = locations.getRouteTitle(routeTitle);
        }
        else
        {
            routeTitle = "";
        }

        StringBuilder otherText = new StringBuilder();
        otherText.append("(This is an automatically generated error report by BostonBusMap which includes information about the state of the app when the error report was created. Feel free to include any extra information before this message.)\n\n");
        otherText.append("\n\n");
        createInfoForAgency(location, otherText, selection.getMode(), routeTitle);
        otherText.append("\n\n");
        createInfoForDeveloper(otherText, selection.getMode(), routeTitle);

        return otherText.toString();
    }
    protected void createInfoForDeveloper(StringBuilder otherText, Selection.Mode mode, String routeTitle)
    {
        otherText.append("There was a problem with ");
        if (mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
            otherText.append("bus predictions on one route. ");
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
            otherText.append("bus predictions for favorited routes. ");
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_ALL) {
            otherText.append("bus predictions for all routes. ");
        }
        else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL) {
            otherText.append("vehicle locations on all routes. ");
        }
        else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ONE) {
            otherText.append("vehicle locations for one route. ");
        } else {
            otherText.append("something that I can't figure out. ");
        }

        try
        {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String versionText = packageInfo.versionName;
            otherText.append("App version: ").append(versionText).append(". ");
        }
        catch (PackageManager.NameNotFoundException e)
        {
            //don't worry about it
        }
        otherText.append("OS: ").append(android.os.Build.MODEL).append(". ");

        otherText.append("Currently selected route is '").append(routeTitle).append("'. ");
    }

    protected void createInfoForAgency(Location location, StringBuilder ret, Selection.Mode mode, String routeTitle)
    {
        if (location instanceof StopLocation)
        {
            StopLocation stopLocation = (StopLocation)location;
            String stopTag = stopLocation.getStopTag();
            ConcurrentMap<String, StopLocation> stopTags = locations.getAllStopsAtStop(stopTag);

            if (mode == Selection.Mode.BUS_PREDICTIONS_ONE)
            {
                if (stopTags.size() <= 1)
                {
                    ret.append("The stop id is ").append(stopTag).append(" (").append(stopLocation.getTitle()).append(")");
                    ret.append(" on route ").append(routeTitle).append(". ");
                }
                else
                {
                    List<String> stopTagStrings = Lists.newArrayList();
                    for (StopLocation stop : stopTags.values())
                    {
                        String text = stop.getStopTag() + " (" + stop.getTitle() + ")";
                        stopTagStrings.add(text);
                    }
                    String stopTagsList = Joiner.on(",\n").join(stopTagStrings);

                    ret.append("The stop ids are: ").append(stopTagsList).append(" on route ").append(routeTitle).append(". ");
                }
            }
            else
            {
                ArrayList<String> pairs = Lists.newArrayList();
                for (StopLocation stop : stopTags.values())
                {
                    String routesJoin = Joiner.on(", ").join(stop.getRoutes());
                    pairs.add(stop.getStopTag() + "(" + stop.getTitle() + ") on routes " + routesJoin);
                }

                //String list = Joiner.on(",\n").join(pairs);
                ret.append("The stop ids are: ");
                ret.append(Joiner.on(", ").join(pairs));
                ret.append(". ");
            }
        }
        else if (location instanceof BusLocation)
        {
            BusLocation busLocation = (BusLocation)location;
            String busRouteId = busLocation.getRouteId();
            ret.append("The bus number is ").append(busLocation.getBusNumber());
            ret.append(" on route ").append(locations.getRouteTitle(busRouteId)).append(". ");
        }

    }

    @Override
    public void onMapClick(LatLng latlng) {
        setSelectedBusId(NOT_SELECTED);

        if (nextTapListener != null) {
            nextTapListener.onMapClick(latlng);
            nextTapListener = null;
        }

    }

    public void setNextClickListener(OnMapClickListener onMapClickListener) {
        nextTapListener = onMapClickListener;
    }

    public void setAllRoutesBlue(boolean allRoutesBlue) {
        if (allRoutesBlue != this.allRoutesBlue) {
            for (PolylineGroup polylineGroup : polylines.values()) {
                for (int i = 0; i < polylineGroup.size(); i++) {
                    Polyline polyline = polylineGroup.getPolyline(i);
                    Path path = polylineGroup.getPath(i);
                    polyline.setColor(getColor(path));
                }
            }
        }
        this.allRoutesBlue = allRoutesBlue;
    }

    public void setMultiplePathsAndColors(Map<String, Path[]> paths) {
        for (String otherRoute : polylines.keySet()) {
            if (!paths.containsKey(otherRoute)) {
                PolylineGroup polylineGroup = polylines.get(otherRoute);
                for (int i = 0; i < polylineGroup.size(); i++) {
                    polylineGroup.getPolyline(i).remove();
                }
            }
        }

        Map<String, PolylineGroup> tempPolylines = Maps.newHashMap();
        for (String route : paths.keySet()) {
            if (polylines.containsKey(route)) {
                tempPolylines.put(route, polylines.get(route));
            }
        }
        polylines.clear();
        polylines.putAll(tempPolylines);

        for (Map.Entry<String, Path[]> entry : paths.entrySet()) {
            String route = entry.getKey();
            if (!polylines.containsKey(route)) {
                addPathsAndColor(entry.getValue(), route);
            }
        }
    }

    private int getColor(Path path) {
        if (allRoutesBlue) {
            return 0x66000099;
        }
        else
        {
            int pathColor = path.getColor();
            pathColor &= 0xffffff; //remove alpha component
            pathColor |= 0x66000000; //add alpha component
            return pathColor;
        }
    }

    public void addPathsAndColor(Path[] paths, String route) {
        if (polylines.get(route) != null) {
            return;
        }

        Polyline[] polylineArray = new Polyline[paths.length];
        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            int color = getColor(path);
            LatLng[] latlngs = new LatLng[path.getPointsSize()];
            for (int j = 0; j < path.getPointsSize(); j++) {
                double lat = path.getPointLat(j);
                double lon = path.getPointLon(j);

                latlngs[j] = new LatLng(lat, lon);
            }
            PolylineOptions options = new PolylineOptions()
                    .width(12f)
                    .color(color)
                    .visible(drawLine)
                    .add(latlngs);

            Polyline polyline = map.addPolyline(options);
            polylineArray[i] = polyline;
        }
        PolylineGroup polylineGroup = new PolylineGroup(polylineArray, paths);
        polylines.put(route, polylineGroup);
    }

    public int getSelectedBusId() {
        return selectedLocationId;
    }

    public boolean setSelectedBusId(int newSelectedBusId) {
        int oldSelectedId = selectedLocationId;

        boolean success = false;
        String markerId = markerIdToLocationId.inverse().get(newSelectedBusId);
        if (markerId != null) {
            Marker marker = markers.get(markerId);
            if (marker != null) {
                success = true;
            }
        }

        if (!success) {
            newSelectedBusId = NOT_SELECTED;
        }
        selectedLocationId = newSelectedBusId;
        updateMarkerAndButtons(oldSelectedId, newSelectedBusId);
        updateRouteLine(newSelectedBusId);

        return success;
    }

    private void updateRouteLine(int newSelectedBusId) {
        Location selectedLocation = locationIdToLocation.get(newSelectedBusId);

        Selection.Mode mode = locations.getSelection().getMode();

        Map<String, Path[]> pathMap = Maps.newHashMap();
        if (alwaysFocusRoute) {
            String route = locations.getSelection().getRoute();
            pathMap.put(route, locations.getPaths(route));
        }
        if (changeRouteIfSelected && selectedLocation != null) {
            for (String route : selectedLocation.getRoutes()) {
                pathMap.put(route, locations.getPaths(route));
            }
        }
        else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ONE || mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
            String route = locations.getSelection().getRoute();
            pathMap.put(route, locations.getPaths(route));
        }

        setMultiplePathsAndColors(pathMap);
    }

    public GoogleMap getMap() {
        return map;
    }

    public boolean isShowLine() {
        return drawLine;
    }

    public void setDrawLine(boolean drawLine) {
        if (drawLine != this.drawLine) {
            for (PolylineGroup polylineGroup : polylines.values()) {
                for (int i = 0; i < polylineGroup.size(); i++) {
                    polylineGroup.getPolyline(i).setVisible(drawLine);
                }
            }
        }
        this.drawLine = drawLine;
    }

    /**
     * NOTE: must run in UI thread
     * @param locations
     * @param newSelection
     * @param newCenter
     */
    public void updateNewLocations(List<Location> locations, int newSelection, LatLng newCenter, boolean forceNewMarker) {
        if (firstRun) {
            // map may contain old markers and route lines if it was retained
            map.clear();
            polylines.clear();

            if (firstRunSelectionId != NOT_SELECTED) {
                newSelection = firstRunSelectionId;
            }

            firstRun = false;
        }

        boolean selectionMade = false;
        Set<Integer> locationIdsToRemove = Sets.newHashSet();
        Set<Integer> locationIdsForNewMarkers = Sets.newHashSet();
        locationIdsToRemove.addAll(locationIdToLocation.keySet());
        for (Location location : locations) {
            locationIdsToRemove.remove(location.getId());

            // this all assumes that the only thing that changes a marker is its heading
            // for vehicles (which implies stops and places don't ever change markers)
            Location oldLocation = locationIdToLocation.get(location.getId());
            boolean reuseMarker = false;
            if (oldLocation != null) {
                if (!oldLocation.hasHeading()) {
                    reuseMarker = true;
                }
                else if (oldLocation.getHeading() == location.getHeading() &&
                        oldLocation.getLatitudeAsDegrees() == location.getLatitudeAsDegrees() &&
                        oldLocation.getLongitudeAsDegrees() == location.getLongitudeAsDegrees()) {
                    reuseMarker = true;
                }
            }

            if (reuseMarker && !forceNewMarker) {
                // replace with new location, leave marker
                locationIdToLocation.put(location.getId(), location);
            }
            else
            {
                locationIdsForNewMarkers.add(location.getId());
                ITransitDrawables transitDrawables = transitSystem.getTransitSourceByRouteType(location.getTransitSourceType()).getDrawables();
                int icon = transitDrawables.getBitmapDescriptor(location, false);
                LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());
                MarkerOptions options = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(icon))
                        .position(latlng);

                String oldMarkerId = markerIdToLocationId.inverse().get(location.getId());
                if (oldMarkerId != null) {
                    markerIdToLocationId.remove(oldMarkerId);
                    Marker oldMarker = markers.get(oldMarkerId);
                    markers.remove(oldMarkerId);
                    oldMarker.remove();

                }

                Marker marker = map.addMarker(options);
                markers.put(marker.getId(), marker);
                markerIdToLocationId.put(marker.getId(), location.getId());
                locationIdToLocation.put(location.getId(), location);
            }
        }

        for (Integer removeId : locationIdsToRemove) {
            String markerId = markerIdToLocationId.inverse().get(removeId);
            locationIdToLocation.remove(removeId);
            markerIdToLocationId.remove(markerId);
            Marker marker = markers.get(markerId);
            markers.remove(markerId);
            marker.remove();
        }

        if (selectionMade == false) {
            boolean success = setSelectedBusId(newSelection);
            if (!success) {
                // set to NOT_SELECTED to avoid confusion
                setSelectedBusId(NOT_SELECTED);
            }
        }

        List<Point> points = Lists.newArrayListWithCapacity(locations.size());
        double lonFactor = 0;
        if (locations.size() > 0) {
            Location firstLocation = locations.get(0);
            lonFactor = Math.cos(firstLocation.getLatitudeAsDegrees() * Geometry.degreesToRadians);
        }
        for (Location location : locations) {
            points.add(new Point(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees() * lonFactor));
        }
    }

    public Location getLocationFromMarkerId(String id) {
        Integer locationId = markerIdToLocationId.get(id);
        if (locationId == null) {
            return null;
        }
        else
        {
            return locationIdToLocation.get(locationId);
        }
    }

    public Location getSelectedLocation() {
        return locationIdToLocation.get(selectedLocationId);
    }

    public void setHandler(UpdateHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if (handler != null) {
            handler.triggerUpdate();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String markerId = marker.getId();
        Integer id = markerIdToLocationId.get(markerId);
        int newLocationid;
        if (id == null) {
            return;
        }
        else
        {
            newLocationid = id;
        }

        Location location = locationIdToLocation.get(newLocationid);
        if (location == null || !(location instanceof StopLocation)) {
            return;
        }
        StopLocation stopLocation = (StopLocation)location;

        try {
            locations.toggleFavorite(stopLocation);
            updateInfo(markerId);
        }
        catch (RemoteException e) {
            LogUtil.e(e);
        }
    }

    public void setFirstRunSelectionId(int firstRunSelectionId) {
        this.firstRunSelectionId = firstRunSelectionId;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        handler.triggerRefresh(1500);
        return false;
    }

    public void setChangeRouteIfSelected(boolean changeRouteIfSelected) {
        this.changeRouteIfSelected = changeRouteIfSelected;
    }

    public void setShowTraffic(boolean showTraffic) {
        map.setTrafficEnabled(showTraffic);
    }

    public void setAlwaysFocusRoute(boolean alwaysFocusRoute) {
        this.alwaysFocusRoute = alwaysFocusRoute;
    }

    public boolean isAlwaysFocusRoute() {
        return alwaysFocusRoute;
    }
}
