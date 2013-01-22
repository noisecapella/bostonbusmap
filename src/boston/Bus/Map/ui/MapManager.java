package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Path;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MapManager implements OnMapClickListener, OnMarkerClickListener {
	private final GoogleMap map;
	public static final int NOT_SELECTED = -1;

	private final List<Polyline> polylines = Lists.newArrayList();
	
	private final Map<String, Marker> markers = Maps.newHashMap();
	private final BiMap<String, Integer> markerIdToLocationId = HashBiMap.create();
	private final Map<Integer, Location> locationIdToLocation = Maps.newHashMap();
	private String selectedMarkerId;

	private OnMapClickListener nextTapListener;
	private boolean allRoutesBlue;
	
	private final Set<String> routes = Sets.newHashSet();
	private final Context context;
	private boolean drawLine;
	
	public MapManager(Context context, GoogleMap map) {
		this.context = context;
		this.map = map;
		
		map.setOnMapClickListener(this);
		map.setOnMarkerClickListener(this);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		selectedMarkerId = marker.getId();
		return false;
	}

	@Override
	public void onMapClick(LatLng latlng) {
		selectedMarkerId = null;

		if (nextTapListener != null) {
    		nextTapListener.onMapClick(latlng);
    		nextTapListener = null;
    	}
	}

	public void setNextClickListener(OnMapClickListener onMapClickListener) {
		nextTapListener = onMapClickListener;
	}

	public void setDrawHighlightCircle(boolean drawCircle) {
		// TODO
		
	}

	public void setAllRoutesBlue(boolean allRoutesBlue) {
		if (allRoutesBlue != this.allRoutesBlue) {
			for (Polyline polyline : polylines) {
				polyline.setColor(Color.BLUE);
			}
		}
		this.allRoutesBlue = allRoutesBlue;
	}

	public void setPathsAndColor(Path[] paths, String route) {
		if (routes.size() == 1 && routes.contains(route)) {
			return;
		}
		clearPaths();
		addPathsAndColor(paths, route);
	}

	public void clearPaths() {
		for (Polyline polyline : polylines) {
			polyline.remove();
		}
		polylines.clear();
		routes.clear();
	}

	public void addPathsAndColor(Path[] paths, String route) {
		for (Path path : paths) {
			int color = allRoutesBlue ? Color.BLUE : path.getColor();
			LatLng[] latlngs = new LatLng[path.getPointsSize()];
			for (int i = 0; i < path.getPointsSize(); i++) {
				double lat = path.getPointLat(i);
				double lon = path.getPointLon(i);
				
				latlngs[i] = new LatLng(lat, lon);
			}
			PolylineOptions options = new PolylineOptions().width(3f).color(color).add(latlngs);
			
			Polyline polyline = map.addPolyline(options);
			polylines.add(polyline);
		}
		routes.add(route);
	}

	public int getSelectedBusId() {
		if (selectedMarkerId == null) {
			return NOT_SELECTED;
		}
		else
		{
			Integer locationId = markerIdToLocationId.get(selectedMarkerId);
			if (locationId == null) {
				return NOT_SELECTED;
			}
			else
			{
				return locationId;
			}
		}
	}
	
	public void setSelectedBusId(int newSelectedBusId) {
		String markerId = markerIdToLocationId.inverse().get(newSelectedBusId);
		if (markerId != null) {
			selectedMarkerId = markerId;
			Marker marker = markers.get(markerId);
			if (marker != null) {
				marker.showInfoWindow();
			}
		}
		else
		{
			selectedMarkerId = null;
		}
	}

	public GoogleMap getMap() {
		return map;
	}

	public boolean isShowLine() {
		return drawLine;
	}

	public void setDrawLine(boolean drawLine) {
		if (drawLine != this.drawLine) {
			for (Polyline polyline : polylines) {
				polyline.setVisible(drawLine);
			}
		}
		this.drawLine = drawLine;
	}

	public void updateNewLocations(List<Location> locations) {
		Set<Integer> toRemove = Sets.newHashSet();
		toRemove.addAll(locationIdToLocation.keySet());
		for (Location location : locations) {
			toRemove.remove(location.getId());
			Location oldLocation = locationIdToLocation.get(location.getId());
			if (oldLocation != null && oldLocation.needsUpdating() == false) {
				// replace with new location, leave marker
				locationIdToLocation.put(location.getId(), location);
			}
			else
			{
				int id = location.getDrawable(context, false, false);
				LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());
				MarkerOptions options = new MarkerOptions()
				.icon(BitmapDescriptorFactory.fromResource(id))
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
		
		for (Integer removeId : toRemove) {
			String markerId = markerIdToLocationId.inverse().get(removeId);
			locationIdToLocation.remove(removeId);
			markerIdToLocationId.remove(markerId);
			Marker marker = markers.get(markerId);
			markers.remove(markerId);
			marker.remove();
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
}
