package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.util.Constants;

public class MultipleStopLocations implements StopLocationGroup {
	private final ArrayList<StopLocation> stops = new ArrayList<StopLocation>(2);
	private Predictions predictions;
	public MultipleStopLocations(StopLocation stop1, StopLocation stop2) {
		stops.add(stop1);
		stops.add(stop2);
	}

	public void addStop(StopLocation stop) {
		stops.add(stop);
	}
	
	@Override
	public List<StopLocation> getStops() {
		return stops;
	}

	@Override
	public String getFirstStopTag() {
		return stops.get(0).getStopTag();
	}
	
	@Override
	public float getLatitudeAsDegrees() {
		return stops.get(0).getLatitudeAsDegrees();
	}

	@Override
	public float getLongitudeAsDegrees() {
		return stops.get(0).getLongitudeAsDegrees();
	}
	
	@Override
	public int hashCode() {
		return getLatAsInt() ^ getLonAsInt();
	}
	
	public int getLatAsInt() {
		return stops.get(0).getLatAsInt();
	}
	
	@Override
	public int getLonAsInt() {
		return stops.get(0).getLonAsInt();
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
		return stops.get(0).getDrawable(context, shadow, isSelected);
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig selectedRoute,
			MyHashMap<String, String> routeKeysToTitles, Context context) {
		if (predictions == null)
		{
			predictions = new Predictions();
		}

		boolean first = true;
		for (StopLocation stop : stops) {
			String tag = stop.getStopTag();
			String title = stop.getTitle();
			String stopRoute = stop.getFirstRoute();
			if (first) {
				predictions.makeSnippetAndTitle(selectedRoute, routeKeysToTitles, context, stopRoute, title, tag);
			}
			else
			{
				predictions.addToSnippetAndTitle(selectedRoute, stop, routeKeysToTitles, context, title, stopRoute);
			}
			
			first = false;
		}
	}

	@Override
	public String getSnippetTitle()
	{
		if (predictions != null)
		{
			return predictions.getSnippetTitle();
		}
		else
		{
			return null;
		}
	}

	@Override
	public String getSnippet()
	{
		if (isBeta() == false)
		{
			if (predictions != null)
			{
				return predictions.getSnippetPredictions();
			}
			else
			{
				return null;
			}
		}
		else
		{
			StringBuilder ret = new StringBuilder();
			ret.append("<font color='red' size='1'>Commuter rail predictions are experimental</font>");
			if (predictions != null)
			{
				 String predictionsString = predictions.getSnippetPredictions();
				 if (predictionsString != null)
				 {
					 ret.append("<br />").append(predictionsString);
				 }
			}
			return ret.toString();

		}
	}

	@Override
	public ArrayList<Alert> getSnippetAlerts() {
		if (predictions != null)
		{
			return predictions.getSnippetAlerts();
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isVehicle() {
		return false;
	}
	
	public boolean isBeta() {
		for (StopLocation stop : stops) {
			if (stop.isBeta()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getAllRoutes() {
		ArrayList<String> routes = new ArrayList<String>(stops.size());
		for (StopLocation stop : stops) {
			String route = stop.getFirstRoute();
			if (routes.contains(route) == false) {
				routes.add(route);
			}
		}
		return routes;
	}

	@Override
	public String getFirstRoute() {
		return stops.get(0).getFirstRoute();
	}

	@Override
	public List<String> getAllTitles() {
		ArrayList<String> titles = new ArrayList<String>(stops.size());
		for (StopLocation stop : stops) {
			String title = stop.getFirstTitle();
			if (titles.contains(title) == false) {
				titles.add(title);
			}
		}
		return titles;
	}

	@Override
	public String getFirstTitle() {
		return stops.get(0).getFirstTitle();
	}

	@Override
	public List<Prediction> getCombinedPredictions()
	{
		if (predictions != null)
		{
			return predictions.getCombinedPredictions();
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public List<String> getCombinedRoutes()
	{
		if (predictions != null)
		{
			return predictions.getSnippetRoutes();
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public List<String> getCombinedTitles()
	{
		if (predictions != null)
		{
			List<String> combinedTitles = predictions.getCombinedTitles();
			if (combinedTitles != null)
			{
				return combinedTitles;
			}
		}
		
		return Collections.singletonList(getFirstTitle());
	}

	@Override
	public String getCombinedStops() {
		if (predictions != null)
		{
			return predictions.getCombinedStops();
		}
		else
		{
			return "";
		}
	}

	@Override
	public TransitSource getTransitSource() {
		TransitSource ret = null;
		boolean first = true;
		for (StopLocation stop : stops) {
			if (first) {
				ret = stop.getTransitSource();
			}
			else
			{
				if (ret != stop.getTransitSource()) {
					//confused; return null
					return null;
				}
			}
			first = false;
		}
		return ret;
	}

	@Override
	public void clearRecentlyUpdated() {
		for (StopLocation stop : stops) {
			stop.clearRecentlyUpdated();
		}
	}

	@Override
	public String getString() {
		return stops.get(0).getString();
	}
	
}
