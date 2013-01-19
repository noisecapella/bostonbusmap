package boston.Bus.Map.ui;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MapManager implements OnMapClickListener, OnMarkerClickListener {
	private final GoogleMap map;
	public static final int NOT_SELECTED = -1;

	private final Map<String, Marker> markers = Maps.newHashMap();
	private final List<Polyline> polylines = Lists.newArrayList();
	
	private final Map<String, Location> markerIdToLocation = Maps.newHashMap();
	private String selectedMarkerId;

	private Locations locations;
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
	
	public void clearMarkers() {
		for (Marker marker : markers.values()) {
			marker.remove();
		}
		
		markers.clear();
		markerIdToLocation.clear();
	}
	
	public void setLocations(Locations locations) {
		this.locations = locations;
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
			PolylineOptions options = new PolylineOptions();
			
			int color = allRoutesBlue ? Color.BLUE : path.getColor();
			options.color(color);
			for (int i = 0; i < path.getPointsSize(); i++) {
				double lat = path.getPointLat(i);
				double lon = path.getPointLon(i);
				
				options.add(new LatLng(lat, lon));
			}
			
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
			Location location = markerIdToLocation.get(selectedMarkerId);
			if (location == null) {
				return NOT_SELECTED;
			}
			else
			{
				return location.getId();
			}
		}
	}
	
	public void addAllLocations(List<Location> locations) {
		for (Location location : locations) {
			int id = location.getDrawable(context, false, false);
			LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());
			MarkerOptions options = new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromResource(id))
			.position(latlng)
			.title("TITLE")
			.snippet("SNIPPET");
			
			Marker marker = map.addMarker(options);
			markers.put(marker.getId(), marker);
			markerIdToLocation.put(marker.getId(), location);
		}
	}

	public Location getLocationFromMarkerId(String id) {
		return markerIdToLocation.get(id);
	}

	public void setSelectedBusId(int newSelectedBusId) {
		for (Marker marker : markers.values()) {
			Location location = markerIdToLocation.get(marker.getId());
			if (location != null && location.getId() == newSelectedBusId) {
				marker.showInfoWindow();
				return;
			}
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
}
