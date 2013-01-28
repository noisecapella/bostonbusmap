package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import boston.Bus.Map.commands.Command;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;
import boston.Bus.Map.util.LogUtil;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

public class MapManager implements OnMapClickListener, OnMarkerClickListener, OnInfoWindowClickListener,
		OnCameraChangeListener {
	private final GoogleMap map;
	public static final int NOT_SELECTED = -1;

	private final List<Polyline> polylines = Lists.newArrayList();
	
	private final Map<String, Marker> markers = Maps.newHashMap();
	private final BiMap<String, Integer> markerIdToLocationId = HashBiMap.create();
	private final Map<Integer, Location> locationIdToLocation = Maps.newHashMap();
	private int selectedLocationId = NOT_SELECTED;

	private OnMapClickListener nextTapListener;
	private boolean allRoutesBlue;
	
	private final Set<String> routes = Sets.newHashSet();
	private final Main main;
	
	/**
	 * This should be set in constructor but we need to instantiate this object
	 * before it exists, so you should use setHandler once you have access
	 * to an UpdateHandler instead
	 */
	private UpdateHandler handler;
	private final Locations locations;
	private final RouteTitles routeTitles;
	private boolean drawLine;
	
	private boolean firstRun = true;
	
	public MapManager(Main main, GoogleMap map,
			Locations locations, RouteTitles routeTitles) {
		this.main = main;
		this.map = map;
		this.locations = locations;
		this.routeTitles = routeTitles;
		
		map.setOnMapClickListener(this);
		map.setOnMarkerClickListener(this);
		map.setOnInfoWindowClickListener(this);
		map.setOnCameraChangeListener(this);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		String markerId = marker.getId();
		Integer id = markerIdToLocationId.get(markerId);
		if (id == null) {
			selectedLocationId = NOT_SELECTED;
		}
		else
		{
			selectedLocationId = id;
		}
		
		return false;
	}

	@Override
	public void onMapClick(LatLng latlng) {
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
			PolylineOptions options = new PolylineOptions()
				.width(3f)
				.color(color)
				.visible(drawLine)
				.add(latlngs);
			
			Polyline polyline = map.addPolyline(options);
			polylines.add(polyline);
		}
		routes.add(route);
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
			for (Polyline polyline : polylines) {
				polyline.setVisible(drawLine);
			}
		}
		this.drawLine = drawLine;
	}

	public void updateNewLocations(List<Location> locations, int newSelection) {
		if (firstRun) {
			// map may contain old markers and route lines if it was retained
			map.clear();
			firstRun = false;
		}
		
		boolean selectionMade = false;
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
				int id = location.getDrawable(main, false, false);
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
		if (handler == null) {
			// handler not yet set, wait until we call setHandler
			return;
		}
		
		String markerId = marker.getId();
		Integer locationId = markerIdToLocationId.get(markerId);
		if (locationId != null) {
			Location location = locationIdToLocation.get(locationId);
			if (location != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(main);
				
				final List<Command> commands = location.getCommands();
				String[] array = new String[commands.size()];
				for (int i = 0; i < commands.size(); i++) {
					array[i] = commands.get(i).getDescription();
				}
				builder.setItems(array, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							commands.get(which).execute(main, handler,
									locations, routeTitles, marker);
						} catch (Exception e) {
							Toast.makeText(main, "Unknown error occurred", Toast.LENGTH_LONG).show();
							LogUtil.e(e);
						}
						// TODO: confirm that info window view is updated here
					}
				});
				builder.show();
			}
		}
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (handler != null) {
			handler.triggerUpdate();
		}
	}
}
