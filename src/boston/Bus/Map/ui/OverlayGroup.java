package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class OverlayGroup {
	private LocationOverlay locationOverlay;
	private final MyHashMap<String, RouteOverlay> routeOverlays = new MyHashMap<String, RouteOverlay>();
	private BusOverlay busOverlay;
	
	public static final String ROUTE_OVERLAY_KEY = "ROUTE";
	public static final String GET_DIRECTIONS_OVERLAY_KEY = "GET_DIRECTIONS";
	
	public OverlayGroup(BusOverlay busOverlay, LocationOverlay locationOverlay,
			MyHashMap<String, RouteOverlay> routeOverlays) {
		this.busOverlay = busOverlay;
		this.locationOverlay = locationOverlay;
		for (String key : routeOverlays.keySet()) {
			RouteOverlay value = routeOverlays.get(key);
			this.routeOverlays.put(key, value);
		}
	}
	
	public OverlayGroup(Main main, Drawable busPicture, MapView mapView,
			MyHashMap<String, String> dropdownRouteKeysToTitles,
			UpdateHandler handler) {
    	busOverlay = new BusOverlay(busPicture, main, mapView, dropdownRouteKeysToTitles);
    	locationOverlay = new LocationOverlay(main, mapView, handler);
    	
    	RouteOverlay routeOverlay = new RouteOverlay(mapView.getProjection());
    	RouteOverlay getDirectionsOverlay = new RouteOverlay(mapView.getProjection());
    	this.routeOverlays.put(ROUTE_OVERLAY_KEY, routeOverlay);
    	this.routeOverlays.put(GET_DIRECTIONS_OVERLAY_KEY, getDirectionsOverlay);
	}

	public BusOverlay getBusOverlay() {
		return busOverlay;
	}
	
	public LocationOverlay getMyLocationOverlay() {
		return locationOverlay;
	}
	
	public void nullify() {
		routeOverlays.clear();
		busOverlay = null;
		locationOverlay = null;
	}

	public OverlayGroup cloneOverlays(Main context, MapView mapView, 
			MyHashMap<String, String> dropDownRouteKeysToTitles, UpdateHandler handler) {
		
    	final BusOverlay newBusOverlay = new BusOverlay(busOverlay, context, mapView, dropDownRouteKeysToTitles);

    	MyHashMap<String, RouteOverlay> newRouteOverlays = new MyHashMap<String, RouteOverlay>();
    	for (String key : routeOverlays.keySet()) {
    		RouteOverlay oldRouteOverlay = routeOverlays.get(key);
    		RouteOverlay newRouteOverlay = new RouteOverlay(oldRouteOverlay, mapView.getProjection());
    		newRouteOverlays.put(key, newRouteOverlay);
    	}
    	LocationOverlay newLocationOverlay = new LocationOverlay(context, mapView, handler);
    	
    	return new OverlayGroup(newBusOverlay, newLocationOverlay, newRouteOverlays);
	}

	public void refreshMapView(MapView mapView) {
    	mapView.getOverlays().clear();
    	for (RouteOverlay routeOverlay : routeOverlays.values()) {
    		mapView.getOverlays().add(routeOverlay);
    	}
    	mapView.getOverlays().add(locationOverlay);
    	mapView.getOverlays().add(busOverlay);
		
	}

	public RouteOverlay getRouteOverlay() {
		return routeOverlays.get(ROUTE_OVERLAY_KEY);
	}
	
	public RouteOverlay getDirectionsOverlay() {
		return routeOverlays.get(GET_DIRECTIONS_OVERLAY_KEY);
	}
}
