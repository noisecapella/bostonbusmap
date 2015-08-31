package boston.Bus.Map.ui;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Path;
import com.schneeloch.bostonbusmap_library.math.Geometry;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import boston.Bus.Map.main.UpdateHandler;

public class MapManager implements OnMapClickListener, OnMarkerClickListener, OnInfoWindowClickListener,
        OnCameraChangeListener {
    private final GoogleMap map;
    public static final int NOT_SELECTED = -1;

    private final Map<String, Marker> markers = Maps.newHashMap();
    private final BiMap<String, Integer> markerIdToLocationId = HashBiMap.create();
    private final Map<Integer, Location> locationIdToLocation = Maps.newHashMap();
    private int selectedLocationId = NOT_SELECTED;

    private OnMapClickListener nextTapListener;
    private boolean allRoutesBlue;

    private final Map<String, PolylineGroup> polylines = Maps.newHashMap();
    private final ITransitSystem transitSystem;

    /**
     * This should be set in constructor but we need to instantiate this object
     * before it exists, so you should use setHandler once you have access
     * to an UpdateHandler instead
     */
    private UpdateHandler handler;
    private boolean drawLine;

    private boolean firstRun = true;
    private Circle circle;

    public MapManager(GoogleMap map,
                      ITransitSystem transitSystem) {
        this.map = map;
        this.transitSystem = transitSystem;

        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnCameraChangeListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showSelectedMarker(false);

        String markerId = marker.getId();
        Integer id = markerIdToLocationId.get(markerId);
        if (id == null) {
            selectedLocationId = NOT_SELECTED;
        }
        else
        {
            selectedLocationId = id;
        }

        marker.showInfoWindow();

        showSelectedMarker(true);

        return true;
    }

    private void showSelectedMarker(boolean selected) {
        Location location = locationIdToLocation.get(selectedLocationId);
        if (location == null) {
            return;
        }
        ITransitDrawables transitDrawables = transitSystem.getTransitSourceByRouteType(location.getTransitSourceType()).getDrawables();

        String markerId = markerIdToLocationId.inverse().get(selectedLocationId);
        if (markerId == null) {
            return;
        }
        Marker marker = markers.get(markerId);
        if (marker == null) {
            return;
        }

        BitmapDescriptor icon = transitDrawables.getBitmapDescriptor(location, selected);
        marker.setIcon(icon);
    }

    @Override
    public void onMapClick(LatLng latlng) {
        showSelectedMarker(false);

        selectedLocationId = NOT_SELECTED;

        if (nextTapListener != null) {
            nextTapListener.onMapClick(latlng);
            nextTapListener = null;
        }
    }

    public void setNextClickListener(OnMapClickListener onMapClickListener) {
        nextTapListener = onMapClickListener;
    }

    public void setDrawHighlightCircle(boolean drawCircle) {
        if (circle != null) {
            if (drawCircle) {
                circle.setVisible(true);
            }
            else {
                circle.setVisible(false);
            }
        }
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

    public void setPathsAndColor(Path[] paths, String route) {
        for (String otherRoute : polylines.keySet()) {
            if (!otherRoute.equals(route)) {
                PolylineGroup polylineGroup = polylines.get(otherRoute);
                for (int i = 0; i < polylineGroup.size(); i++) {
                    polylineGroup.getPolyline(i).remove();
                }
            }
        }

        PolylineGroup thisPolylineGroup = polylines.get(route);
        polylines.clear();
        if (thisPolylineGroup != null) {
            polylines.put(route, thisPolylineGroup);
        }
        else
        {
            addPathsAndColor(paths, route);
        }
    }

    private int getColor(Path path) {
        if (allRoutesBlue) {
            return 0x99000099;
        }
        else
        {
            int pathColor = path.getColor();
            pathColor &= 0xffffff; //remove alpha component
            pathColor |= 0x99000000; //add alpha component
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
                    .width(3f)
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
        selectedLocationId = newSelectedBusId;

        boolean success = false;
        String markerId = markerIdToLocationId.inverse().get(newSelectedBusId);
        if (markerId != null) {
            Marker marker = markers.get(markerId);
            if (marker != null) {
                marker.showInfoWindow();
                success = true;
            }
        }

        return success;
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

    public void updateNewLocations(List<Location> locations, int newSelection, LatLng newCenter) {
        if (firstRun) {
            // map may contain old markers and route lines if it was retained
            map.clear();
            polylines.clear();

            firstRun = false;
        }

        if (circle != null) {
            circle.remove();
        }
        boolean selectionMade = false;
        Set<Integer> toRemove = Sets.newHashSet();
        toRemove.addAll(locationIdToLocation.keySet());
        for (Location location : locations) {
            toRemove.remove(location.getId());

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

            if (reuseMarker) {
                // replace with new location, leave marker
                locationIdToLocation.put(location.getId(), location);
            }
            else
            {
                ITransitDrawables transitDrawables = transitSystem.getTransitSourceByRouteType(location.getTransitSourceType()).getDrawables();
                BitmapDescriptor icon = transitDrawables.getBitmapDescriptor(location, false);
                LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());
                MarkerOptions options = new MarkerOptions()
                        .icon(icon)
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

                if (selectedLocationId == location.getId() && selectedLocationId == newSelection) {
                    // no need to call setSelectedBusId
                    marker.showInfoWindow();
                    selectionMade = true;
                }
            }
        }

        for (Integer removeId : toRemove) {
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

        double radius = 0;
        Location lastLocation = null;
        if (locations.size() > 0) {
            lastLocation = locations.get(locations.size() - 1);
        }
        LatLng firstLocation = null;
        if (locations.size() > 0) {
            Location firstLocationObject = locations.get(0);
            firstLocation = new LatLng(
                    firstLocationObject.getLatitudeAsDegrees(),
                    firstLocationObject.getLongitudeAsDegrees()
            );
        }
        if (lastLocation != null) {
            radius = Geometry.computeDistanceInMiles(
                    firstLocation.latitude * Geometry.degreesToRadians,
                    firstLocation.longitude * Geometry.degreesToRadians,
                    lastLocation.getLatitudeAsDegrees() * Geometry.degreesToRadians,
                    lastLocation.getLongitudeAsDegrees() * Geometry.degreesToRadians
            )
            * Geometry.milesToMeters;
        }

        circle = map.addCircle(new CircleOptions()
                        .strokeColor(0x99000099)
                        .center(firstLocation)
                        .radius(radius)
                        .visible(true)
        );
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

    public void setHandler(UpdateHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        LogUtil.i("onInfoWindowClick");
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if (handler != null) {
            handler.triggerUpdate();
        }
    }
}
