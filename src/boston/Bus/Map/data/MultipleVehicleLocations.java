package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.List;

import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.util.Constants;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class MultipleVehicleLocations implements VehicleLocationGroup {
	private String snippet;
	private String snippetTitle;
	private ArrayList<Alert> snippetAlerts;
	private final Directions directions;

	private ArrayList<VehicleLocation> locations = new ArrayList<VehicleLocation>(2);
	
	public MultipleVehicleLocations(VehicleLocation vehicle1, VehicleLocation vehicle2) {
		locations.add(vehicle1);
		locations.add(vehicle2);
		directions = vehicle1.getDirections();
	}
	
	@Override
	public float getLatitudeAsDegrees() {
		return locations.get(0).getLatitudeAsDegrees();
	}

	@Override
	public float getLongitudeAsDegrees() {
		return locations.get(0).getLongitudeAsDegrees();
	}

	@Override
	public int hashCode() {
		return getLatAsInt() ^ getLonAsInt();
	}
	
	public int getLatAsInt() {
		return locations.get(0).getLatAsInt();
	}
	
	@Override
	public int getLonAsInt() {
		return locations.get(0).getLonAsInt();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof LocationGroup) {
			return ((LocationGroup) o).getLatitudeAsDegrees() == getLatitudeAsDegrees() &&
					((LocationGroup)o).getLongitudeAsDegrees() == getLongitudeAsDegrees();
		}
		else
		{
			return false;
		}
	}

	@Override
	public float distanceFrom(double centerLatitude, double centerLongitude)
	{
		return Geometry.computeCompareDistance(getLatitudeAsDegrees() * Geometry.degreesToRadians,
				getLongitudeAsDegrees() * Geometry.degreesToRadians, centerLatitude, centerLongitude);
	}

	@Override
	public Drawable getDrawable(Context context, boolean shadow,
			boolean isSelected) {
		return locations.get(0).getDrawable(context, shadow, isSelected);
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig selectedRoute,
			MyHashMap<String, String> routeKeysToTitles, Context context) {
		boolean first = true;
		
		for (VehicleLocation location : locations) {
			if (first) {
				snippet = location.makeSnippet(selectedRoute);
				snippetTitle = location.makeTitle();
				if (selectedRoute.getRouteName().equals(location.getRouteId()))
				{
					snippetAlerts = selectedRoute.getAlerts();
				}
			}
			else
			{
				snippet += "<br />" + location.makeSnippet(selectedRoute);

				if (location.predictable) {
					snippetTitle += VehicleLocation.makeDirection(location.getDirTag(), directions);
				}
			}
			first = false;
		}
		
	}

	@Override
	public String getSnippetTitle() {
		return snippetTitle;
	}

	@Override
	public String getSnippet() {
		return snippet;
	}

	@Override
	public ArrayList<Alert> getSnippetAlerts() {
		return snippetAlerts;
	}

	@Override
	public boolean isVehicle() {
		return true;
	}

	@Override
	public boolean isBeta() {
		for (VehicleLocation location : locations) {
			if (location.isBeta()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<VehicleLocation> getVehicles() {
		return locations;
	}

	@Override
	public List<String> getAllRoutes() {
		ArrayList<String> routes = new ArrayList<String>(locations.size());
		for (VehicleLocation location : locations) {
			String route = location.getFirstRoute();
			if (routes.contains(route) == false) {
				routes.add(route);
			}
		}
		return routes;
	}

	@Override
	public String getFirstRoute() {
		return locations.get(0).getFirstRoute();
	}

	@Override
	public String getFirstVehicleNumber() {
		return locations.get(0).getFirstVehicleNumber();
	}

	@Override
	public List<String> getAllVehicleNumbers() {
		List<String> ret = new ArrayList<String>();
		for (VehicleLocation vehicle : locations) {
			ret.add(vehicle.getFirstVehicleNumber());
		}
		return ret;
	}
}
