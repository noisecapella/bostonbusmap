package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.RouteComparator;

import boston.Bus.Map.util.CanBeSerialized;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopLocation implements Location, CanBeSerialized
{
	private final float latitude;
	private final float longitude;
	private final Drawable busStop;
	
	private final String tag;
	
	private final String title;
	
	private ArrayList<Prediction> predictions = null;
	
	private final ArrayList<String> routes;
	
	private boolean isFavorite;
	private final HashMap<String, String> routeKeysToTitles;
	
	private static final int LOCATIONTYPE = 3; 
	
	public StopLocation(float latitudeAsDegrees, float longitudeAsDegrees,
			Drawable busStop, String tag, String title, HashMap<String, String> routeKeysToTitles)
	{
		this.latitude = latitudeAsDegrees * Constants.degreesToRadians;
		this.longitude = longitudeAsDegrees * Constants.degreesToRadians;
		this.busStop = busStop;
		this.tag = tag;
		this.title = title;
		this.routes = new ArrayList<String>(1);
		this.routeKeysToTitles = routeKeysToTitles;
	}
	
	public void addRoute(RouteConfig route)
	{
		synchronized (routes)
		{
			String routeName = route.getRouteName();
			if (routes.contains(routeName) == false)
			{
				routes.add(routeName);
			}
		}
	}
	
	@Override
	public double distanceFrom(double centerLatitude, double centerLongitude) {
		return LocationConstants.computeCompareDistance(latitude, longitude, centerLatitude, centerLongitude);
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
	public double getLatitudeAsDegrees() {
		return latitude * Constants.radiansToDegrees;
	}

	@Override
	public double getLongitudeAsDegrees() {
		return longitude * Constants.radiansToDegrees;
	}

	@Override
	public boolean hasHeading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String makeTitle() {
		String ret = title;

		ret += "\nStop: " + tag + ", Routes: ";

		//java doesn't have a join function? seriously?
		int index = 0;
		synchronized (routes)
		{
			Collections.sort(routes);
			for (String route : routes)
			{
				ret += routeKeysToTitles.get(route);

				if (index != routes.size() - 1)
				{
					ret += ", ";
				}

				index++;
			}
		}
		
		
		return ret;
	}

	@Override
	public String makeSnippet(RouteConfig routeConfig) {
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

			Collections.sort(predictions);

			final int max = 3;
			int count = 0;
			for (Prediction prediction : predictions)
			{
				if (routeConfig != null && routeConfig.getRouteName().equals(prediction.getRouteName()) == false)
				{
					continue;
				}

				if (count != 0)
				{
					ret += "\n";
				}
				
				ret += "\n" + prediction.makeSnippet();

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
	
	public void addPrediction(int minutes, long epochTime, int vehicleId,
			String direction, RouteConfig route, Directions directions) {
		if (predictions == null)
		{
			predictions = new ArrayList<Prediction>();
		}
		
		synchronized (predictions)
		{
			predictions.add(new Prediction(minutes, epochTime, vehicleId, 
					directions.getTitleAndName(direction), route.getRouteName()));
		}
		
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void serialize(Box dest) throws IOException {
		dest.writeFloat(latitude);
		dest.writeFloat(longitude);
		dest.writeString(tag);

		dest.writeString(title);
		
		dest.writeStrings(routes);
	}

	
	public StopLocation(Box source, Drawable busStop, HashMap<String, String> routeKeysToTitles) throws IOException {


		this.latitude = source.readFloat();
		this.longitude = source.readFloat();
		
		

		tag = source.readString();

		title = source.readString();
		routes = source.readStrings();
		this.routeKeysToTitles = routeKeysToTitles;
		this.busStop = busStop;
	}

	/**
	 * This should be in Locations instead but I need to synchronize routes
	 * @param urlString
	 */
	public void createPredictionsUrl(StringBuilder urlString, String routeName) {
		if (routeName != null)
		{
			//only do it for the given route
			TransitSystem.bindPredictionElementsForUrl(urlString, routeName, tag);
		}
		else
		{
			//do it for all routes we know about
			synchronized (routes)
			{
				for (String route : routes)
				{
					TransitSystem.bindPredictionElementsForUrl(urlString, route, tag);
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

	public Collection<String> getRoutes() {
		return routes;
	}

	public String getFirstRoute() {
		return routes.get(0);
	}

}
