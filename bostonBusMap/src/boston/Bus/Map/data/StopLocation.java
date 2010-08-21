package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
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
	private final double latitude;
	private final double longitude;
	private final double latitudeAsDegrees;
	private final double longitudeAsDegrees;
	private final Drawable busStop;
	
	private final String tag;
	
	private final String title;
	
	private final SortedSet<Prediction> predictions = new TreeSet<Prediction>();
	
	private final TreeSet<RouteConfig> routes = new TreeSet<RouteConfig>(new RouteComparator());
	
	private boolean isFavorite;
	
	private static final int LOCATIONTYPE = 3; 
	
	public StopLocation(double latitudeAsDegrees, double longitudeAsDegrees,
			Drawable busStop, String tag, String title)
	{
		this.latitude = latitudeAsDegrees * Constants.degreesToRadians;
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitude = longitudeAsDegrees * Constants.degreesToRadians;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.busStop = busStop;
		this.tag = tag;
		this.title = title;
	}
	
	public void addRoute(RouteConfig route)
	{
		synchronized (routes)
		{
			routes.add(route);
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getId() {
		return (tag.hashCode() & 0xffffff) | LOCATIONTYPE << 24;
	}

	@Override
	public double getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public double getLongitudeAsDegrees() {
		return longitudeAsDegrees;
	}

	@Override
	public boolean hasHeading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String makeTitle() {
		String ret = "Stop: " + tag + ", Routes: ";

		//java doesn't have a join function? seriously?
		int index = 0;
		synchronized (routes)
		{
			for (RouteConfig route : routes)
			{
				ret += route.getRouteName();

				if (index != routes.size() - 1)
				{
					ret += ", ";
				}

				index++;
			}
		}
		
		ret += "\n" + "Title: " + title;
		
		return ret;
	}

	@Override
	public String makeSnippet(RouteConfig routeConfig) {
		String ret = "";

		synchronized (predictions)
		{
			if (predictions.size() == 0)
			{
				return null;
			}


			final int max = 3;
			int count = 0;
			for (Prediction prediction : predictions)
			{
				if (routeConfig != null && routeConfig != prediction.getRoute())
				{
					continue;
				}

				ret += "\n" + prediction.toString();

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
		ArrayList<Prediction> newPredictions = new ArrayList<Prediction>();
		synchronized (predictions)
		{
			for (Prediction prediction : predictions)
			{
				if (prediction.getRoute() != routeConfig)
				{
					newPredictions.add(prediction);
				}
			}
			predictions.clear();
			predictions.addAll(newPredictions);
		}
	}
	
	public void addPrediction(int minutes, long epochTime, int vehicleId,
			String direction, RouteConfig route) {
		String directionToShow = route.getDirectionTitle(direction);
		
		synchronized (predictions)
		{
			predictions.add(new Prediction(minutes, epochTime, vehicleId, directionToShow, route));
		}
		
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void serialize(Box dest) throws IOException {
		dest.writeDouble(latitudeAsDegrees);
		dest.writeDouble(longitudeAsDegrees);
		dest.writeString(tag);

		dest.writeString(title);
	}

	
	public StopLocation(Box source, Drawable busStop) throws IOException {


		latitudeAsDegrees = source.readDouble();
		longitudeAsDegrees = source.readDouble();
		this.latitude = latitudeAsDegrees * Constants.degreesToRadians;
		this.longitude = longitudeAsDegrees * Constants.degreesToRadians;
		
		

		tag = source.readString();

		title = source.readString();
		this.busStop = busStop;
	}

	/**
	 * This should be in Locations instead but I need to synchronize routes
	 * @param urlString
	 */
	public void createPredictionsUrl(StringBuilder urlString, RouteConfig routeConfig) {
		if (routeConfig != null)
		{
			//only do it for the given route
			TransitSystem.bindPredictionElementsForUrl(urlString, routeConfig, tag);
		}
		else
		{
			//do it for all routes we know about
			synchronized (routes)
			{
				for (RouteConfig route : routes)
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

}
