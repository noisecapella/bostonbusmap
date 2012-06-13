package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.schneeloch.suffixarray.ObjectWithString;

import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Constants;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class StopLocation implements Location, StopLocationGroup
{
	private final float latitudeAsDegrees;
	private final float longitudeAsDegrees;
	private final TransitSource transitSource;
	
	private final String tag;
	
	private final String title;
	
	private Predictions predictions;
	
	private boolean recentlyUpdated;
	
	private final String route;
	
	private static final int LOCATIONTYPE = 3;
	
	public StopLocation(float latitudeAsDegrees, float longitudeAsDegrees,
			TransitSource transitSource, String tag, String title, String route)
	{
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.transitSource = transitSource;
		this.tag = tag;
		this.title = title;
		this.route = route;
	}

	@Override
	public float distanceFrom(double centerLatitude, double centerLongitude)
	{
		return Geometry.computeCompareDistance(latitudeAsDegrees * Geometry.degreesToRadians,
				longitudeAsDegrees * Geometry.degreesToRadians, centerLatitude, centerLongitude);
	}

	@Override
	public Drawable getDrawable(Context context, boolean shadow,
			boolean isSelected) {
		TransitDrawables drawables = transitSource.getDrawables();
		return recentlyUpdated ? drawables.getStopUpdated() : drawables.getStop();
	}

	@Override
	public void clearRecentlyUpdated()
	{
		recentlyUpdated = false;
	}
	
	@Override
	public int getHeading() {
		return 0;
	}

	@Override
	public float getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public float getLongitudeAsDegrees() {
		return longitudeAsDegrees;
	}

	@Override
	public boolean hasHeading() {
		return false;
	}

	public Predictions getPredictions()
	{
		return predictions;
	}
	
	@Override
	public void makeSnippetAndTitle(RouteConfig routeConfig, MyHashMap<String, String> routeKeysToTitles, Context context)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		predictions.makeSnippetAndTitle(routeConfig, routeKeysToTitles, context, route, title, tag);
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
	
	public String getStopTag()
	{
		return tag;
	}

	public void clearPredictions(RouteConfig routeConfig)
	{
		if (predictions != null)
		{
			predictions.clearPredictions(routeConfig.getRouteName());
		}
		
		recentlyUpdated = true;
	}
	
	public void addPrediction(Prediction prediction)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		predictions.addPredictionIfNotExists(prediction);
	}
	
	public void addPrediction(int minutes, long epochTime, String vehicleId,
			String direction, RouteConfig route, Directions directions, boolean affectedByLayover, boolean isDelayed, int lateness)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		Prediction prediction = new Prediction(minutes, vehicleId, 
				directions.getTitleAndName(direction), route.getRouteName(), affectedByLayover, isDelayed, lateness);
		predictions.addPredictionIfNotExists(prediction);
	}

	public String getTitle()
	{
		return title;
	}

	/**
	 * This should be in Locations instead but I need to synchronize routes
	 * 
	 * NOTE: this is only for bus routes
	 * 
	 * @param urlString
	 */
	public void createBusPredictionsUrl(TransitSystem system, StringBuilder urlString, String routeName, Directions directions) {
		if (routeName != null)
		{
			//only do it for the given route
			TransitSource source = system.getTransitSource(routeName);
			if (source != null)
			{
				source.bindPredictionElementsForUrl(urlString, routeName, tag);
			}
		}
		else
		{
			//do it for all routes we know about
			transitSource.bindPredictionElementsForUrl(urlString, routeName, tag);
		}
	}

	public String getFirstRoute() {
		return route;
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
		
		return Collections.singletonList(title);
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
	public String toString() {
		return "Stop@" + getStopTag();
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
	
	/**
	 * Are these predictions experimental?
	 * @return
	 */
	public boolean isBeta()
	{
		return false;
	}

	@Override
	public String getString() {
		return title;
	}

	
	// these two are used in LocationGroup, so they only compare by location
	@Override
	public int hashCode() {
		return getLatAsInt() ^ getLonAsInt();
	}
	
	public int getLatAsInt() {
		return (int)(latitudeAsDegrees * Constants.E6);
	}
	
	@Override
	public int getLonAsInt() {
		return (int)(longitudeAsDegrees * Constants.E6);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof LocationGroup) {
			return ((LocationGroup) o).getLatitudeAsDegrees() == latitudeAsDegrees &&
					((LocationGroup)o).getLongitudeAsDegrees() == longitudeAsDegrees;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isVehicle() {
		return false;
	}

	@Override
	public TransitSource getTransitSource() {
		return transitSource;
	}

	@Override
	public List<String> getAllRoutes() {
		return Collections.singletonList(route);
	}

	@Override
	public List<String> getAllTitles() {
		return Collections.singletonList(title);
	}

	@Override
	public String getFirstTitle() {
		return title;
	}
	
	@Override
	public List<StopLocation> getStops() {
		return Collections.singletonList(this);
	}

	@Override
	public String getFirstStopTag() {
		return tag;
	}
}
