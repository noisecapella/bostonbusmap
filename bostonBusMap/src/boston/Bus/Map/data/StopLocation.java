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
import boston.Bus.Map.transit.MBTABusTransitSource;
import boston.Bus.Map.transit.SubwayTransitSource;
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
	
	private ArrayList<Prediction> predictions = null;
	
	private boolean isFavorite;
	
	/**
	 * A mapping of routes to dirTags
	 */
	private final HashMap<String, String> dirTags;
	
	private String snippetTitle;
	private String snippetStop;
	private String snippetRoutes;
	private String snippetPredictions;
	/**
	 * Other stops which are temporarily using the same overlay
	 */
	private ArrayList<StopLocation> sharedSnippetStops;
	
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig routeConfig, HashMap<String, String> routeKeysToTitles, Context context) {
		TreeSet<String> routes = new TreeSet<String>();
		routes.addAll(dirTags.keySet());
		snippetRoutes = makeSnippetRoutes(routes, routeKeysToTitles);
		snippetTitle = title;
		snippetStop = tag;
		
		snippetPredictions = makeSnippet(routeConfig, predictions, routeKeysToTitles, context);
		sharedSnippetStops = null;
	}
	
	private String makeSnippetRoutes(Collection<String> routes, HashMap<String, String> routeKeysToTitles) {
		String ret = "";
		
		//java doesn't have a join function? seriously?
		synchronized (routes)
		{
			ret = StringUtil.join(routes, ", ");
		}
		
		return ret;
	}

	private ArrayList<Prediction> combinedPredictions;
	private TreeSet<String> combinedRoutes;
	private TreeSet<String> combinedTitles;
	
	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig, Location location, HashMap<String, String> routeKeysToTitles, Context context) {
		StopLocation stopLocation = (StopLocation)location;
		
		if (sharedSnippetStops == null)
		{
			sharedSnippetStops = new ArrayList<StopLocation>();
		}
		
		
		
		sharedSnippetStops.add(stopLocation);
		
		combinedTitles = new TreeSet<String>();
		combinedTitles.add(title);
		
		for (StopLocation s : sharedSnippetStops)
		{
			combinedTitles.add(s.getTitle());
		}
		
		if (combinedTitles.size() > 1)
		{
			snippetTitle = title + ", ...";
		}
		else
		{
			snippetTitle = title;
		}
		
		/*
		 * uncomment to show all titles on front page
		snippetTitle = makeSnippetTitle(combinedTitles);
		*/
		
		snippetStop += ", " + stopLocation.tag;
		
		combinedRoutes = new TreeSet<String>();
		combinedRoutes.addAll(dirTags.keySet());
		for (StopLocation s : sharedSnippetStops)
		{
			combinedRoutes.addAll(s.getRoutes());
		}
		snippetRoutes = makeSnippetRoutes(combinedRoutes, routeKeysToTitles);
		
		combinedPredictions = new ArrayList<Prediction>();
		if (predictions != null)
		{
			combinedPredictions.addAll(predictions);
		}
		for (StopLocation s : sharedSnippetStops)
		{
			if (s.predictions != null)
			{
				ArrayList<Prediction> predictions = s.predictions;
				combinedPredictions.addAll(predictions);
			}
		}
		
		snippetPredictions = makeSnippet(routeConfig, combinedPredictions, routeKeysToTitles, context);
	}
	
	@Override
	public String getSnippet() {
		return snippetPredictions;
	}
	
	@Override
	public String getSnippetTitle() {
		String ret = snippetTitle;
		return ret;
	}
	
	private String makeSnippet(RouteConfig routeConfig, ArrayList<Prediction> predictions, HashMap<String, String> routeKeysToTitles,
			Context context) {
		String ret = "";
		
		if (predictions == null)
		{
			return ret;
		}
		
		synchronized (predictions)
		{
			if (predictions.size() == 0)
			{
				return null;
			}

			boolean anyNulls = false;
			for (Prediction prediction : predictions)
			{
				if (prediction == null)
				{
					anyNulls = true;
					break;
				}
			}
			
			if (anyNulls)
			{
				//argh, this shouldn't happen but one person reported a null ref with a prediction in predictions,
				//so I should handle it. This isn't a bottleneck so it shouldn't matter that I'm cloning the list
				
				ArrayList<Prediction> newPredictions = new ArrayList<Prediction>(predictions.size());
				for (Prediction prediction : predictions)
				{
					if (prediction != null)
					{
						newPredictions.add(prediction);
					}
				}
				predictions = newPredictions;
			}
			
			Collections.sort(predictions);

			final int max = 3;
			int count = 0;
			
			
			
			for (Prediction prediction : predictions)
			{
				if (routeConfig != null && routeConfig.getRouteName().equals(prediction.getRouteName()) == false)
				{
					continue;
				}
				
				if (prediction.getMinutes() < 0)
				{
					continue;
				}

				
				
				if (count != 0)
				{
					ret += "<br />";
				}
				
				ret += "<br />" + prediction.makeSnippet(routeKeysToTitles, context);

				count++;
				if (count >= max)
				{
					break;
				}
			}
		}
		return ret;
	}
	
	public String getStopTag() {
		return tag;
	}

	public void clearPredictions(RouteConfig routeConfig)
	{
		if (predictions == null)
		{
			return;
		}
		
		ArrayList<Prediction> newPredictions = new ArrayList<Prediction>();
		synchronized (predictions)
		{
			for (Prediction prediction : predictions)
			{
				if (prediction.getRouteName().equals(routeConfig.getRouteName()) == false)
				{
					newPredictions.add(prediction);
				}
			}
			predictions.clear();
			predictions.addAll(newPredictions);
		}
	}
	
	public void addPrediction(Prediction prediction)
	{
		if (predictions == null)
		{
			predictions = new ArrayList<Prediction>();
		}
		
		synchronized (predictions) {
			if (predictions.contains(prediction) == false)
			{
				predictions.add(prediction);
			}
		}
	}
	
	public void addPrediction(int minutes, long epochTime, int vehicleId,
			String direction, RouteConfig route, Directions directions, boolean affectedByLayover, boolean isDelayed) {
		if (predictions == null)
		{
			predictions = new ArrayList<Prediction>();
		}
		
		synchronized (predictions)
		{
			Prediction prediction = new Prediction(minutes, vehicleId, 
					directions.getTitleAndName(direction), route.getRouteName(), affectedByLayover, isDelayed);
			if (predictions.contains(prediction) == false)
			{
				predictions.add(prediction);
			}
		}
		
	}

	public String getTitle() {
		return title;
	}

	/**
	 * This should be in Locations instead but I need to synchronize routes
	 * 
	 * NOTE: this is only for bus routes
	 * 
	 * @param urlString
	 */
	public void createBusPredictionsUrl(StringBuilder urlString, String routeName) {
		if (routeName != null)
		{
			//only do it for the given route
			MBTABusTransitSource.bindPredictionElementsForUrl(urlString, routeName, tag, dirTags.get(routeName));
		}
		else
		{
			//do it for all routes we know about
			synchronized (dirTags)
			{
				for (String route : dirTags.keySet())
				{
					if (SubwayTransitSource.isSubway(route) == false)
					{
						MBTABusTransitSource.bindPredictionElementsForUrl(urlString, route, tag, dirTags.get(route));
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

	public ArrayList<Prediction> getPredictions() {
		return predictions;
	}
	
	public ArrayList<Prediction> getCombinedPredictions() {
		if (combinedPredictions == null)
		{
			return predictions;
		}
		else
		{
			return combinedPredictions;
		}
	}
	
	public String getCombinedRoutes() {
		return snippetRoutes;
	}
	
	public String[] getCombinedTitles() {
		if (combinedTitles != null)
		{
			return combinedTitles.toArray(new String[0]);
		}
		else
		{
			return new String[]{title};
		}
	}

	public String getDirTagForRoute(String route) {
		return dirTags.get(route);
	}

	public String getCombinedStops() {
		if (snippetStop != null)
		{
			return snippetStop;
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
		else if (sharedSnippetStops != null)
		{
			for (StopLocation stop : sharedSnippetStops)
			{
				if (stop.getId() == selectedBusId)
				{
					return true;
				}
			}
		}
		return false;
	}
}
