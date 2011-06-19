package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.RouteComparator;
import boston.Bus.Map.util.StringUtil;

import boston.Bus.Map.util.CanBeSerialized;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopLocation implements Location
{
	private final float latitude;
	private final float longitude;
	private final float latitudeAsDegrees;
	private final float longitudeAsDegrees;
	private final Drawable busStop;
	
	private final String tag;
	
	private final String title;
	
	private Predictions predictions;
	
	private boolean isFavorite;
	
	/**
	 * A mapping of routes to dirTags
	 */
	private final HashMap<String, String> dirTags;

	private static final int LOCATIONTYPE = 3;
	
	public StopLocation(float latitudeAsDegrees, float longitudeAsDegrees,
			Drawable busStop, String tag, String title)
	{
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
		this.longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		this.busStop = busStop;
		this.tag = tag;
		this.title = title;
		this.dirTags = new HashMap<String, String>();
	}

	/**
	 * Add a route and the dirTag for that stop which is mentioned in the routeConfig xml
	 * @param route
	 * @param dirTag
	 */
	public void addRouteAndDirTag(String route, String dirTag)
	{
		synchronized (dirTags)
		{
			dirTags.put(route, dirTag);
		}
	}
	
	@Override
	public float distanceFrom(double centerLatitude, double centerLongitude)
	{
		return Geometry.computeCompareDistance(latitude, longitude, centerLatitude, centerLongitude);
	}

	@Override
	public Drawable getDrawable(Context context, boolean shadow,
			boolean isSelected) {
		return busStop;
	}

	@Override
	public int getHeading() {
		return 0;
	}

	@Override
	public int getId() {
		return (tag.hashCode() & 0xffffff) | LOCATIONTYPE << 24;
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
	public void makeSnippetAndTitle(RouteConfig routeConfig, HashMap<String, String> routeKeysToTitles, Context context)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		predictions.makeSnippetAndTitle(routeConfig, routeKeysToTitles, context, dirTags, title, tag);
	}
	
	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig, Location location, HashMap<String, String> routeKeysToTitles,
			Context context)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		StopLocation stopLocation = (StopLocation)location;
		
		predictions.addToSnippetAndTitle(routeConfig, stopLocation, routeKeysToTitles, context, title, dirTags);
	}
	
	@Override
	public String getSnippet()
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
	}
	
	public void addPrediction(Prediction prediction)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		predictions.addPredictionIfNotExists(prediction);
	}
	
	public void addPrediction(int minutes, long epochTime, int vehicleId,
			String direction, RouteConfig route, Directions directions, boolean affectedByLayover, boolean isDelayed)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		Prediction prediction = new Prediction(minutes, vehicleId, 
				directions.getTitleAndName(direction), route.getRouteName(), affectedByLayover, isDelayed);
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
	public void createBusPredictionsUrl(TransitSystem system, StringBuilder urlString, String routeName) {
		if (routeName != null)
		{
			//only do it for the given route
			TransitSource source = system.getTransitSource(routeName);
			if (source != null)
			{
				source.bindPredictionElementsForUrl(urlString, routeName, tag, dirTags.get(routeName));
			}
		}
		else
		{
			//do it for all routes we know about
			synchronized (dirTags)
			{
				for (String route : dirTags.keySet())
				{
					TransitSource source = system.getTransitSource(route);
					if (source != null)
					{
						source.bindPredictionElementsForUrl(urlString, route, tag, dirTags.get(route));
					}
				}
			}
		}
	}

	public void setFavorite(boolean b)
	{
		isFavorite = b;
	}
	
	@Override
	public boolean isFavorite() {
		return isFavorite;
	}
	/**
	 * The list of routes that owns the StopLocation. NOTE: this is not in any particular order
	 * @return
	 */
	public Collection<String> getRoutes() {
		return dirTags.keySet();
	}

	public String getFirstRoute() {
		TreeSet<String> ret = new TreeSet<String>();
		ret.addAll(dirTags.keySet());
		return ret.first();
	}

	public Prediction[] getCombinedPredictions()
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
	
	public String[] getCombinedRoutes()
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
	
	public String[] getCombinedTitles()
	{
		if (predictions != null)
		{
			String[] combinedTitles = predictions.getCombinedTitles();
			if (combinedTitles != null)
			{
				return combinedTitles;
			}
		}
		
		return new String[]{title};
	}

	public String getDirTagForRoute(String route) {
		return dirTags.get(route);
	}

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
	public boolean containsId(int selectedBusId) {
		if (getId() == selectedBusId)
		{
			return true;
		}
		else if (predictions != null)
		{
			return predictions.containsId(selectedBusId);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Only list stops once if they share the same location
	 * @param stops
	 * @return
	 */
	public static StopLocation[] consolidateStops(StopLocation[] stops) {
		if (stops.length < 2)
		{
			return stops;
		}
		
		ArrayList<StopLocation> ret = new ArrayList<StopLocation>();
		for (int i = 0; i < stops.length; i++)
		{
			ret.add(stops[i]);
		}
		
		//make sure stops sharing a location are touching each other
		final StopLocation firstStop = stops[0];
		Collections.sort(ret, new LocationComparator(firstStop.getLatitudeAsDegrees(), firstStop.getLongitudeAsDegrees()));
		
		ArrayList<StopLocation> ret2 = new ArrayList<StopLocation>(stops.length);
		StopLocation prev = null;
		for (StopLocation stop : ret)
		{
			if (prev != null && prev.getLatitudeAsDegrees() == stop.getLatitudeAsDegrees() &&
					prev.getLongitudeAsDegrees() == stop.getLongitudeAsDegrees())
			{
				//skip
			}
			else
			{
				ret2.add(stop);
			}
			
			prev = stop;
		}
		
		return ret2.toArray(new StopLocation[0]);
	}
	
	@Override
	public String toString() {
		return "Stop@" + getStopTag();
	}
}
