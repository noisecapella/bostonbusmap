package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.util.Box;

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
	
	private final int id;
	
	private final String title;
	private final String inBound;
	
	private final RouteConfig route;
	
	private final SortedSet<Prediction> predictions = new TreeSet<Prediction>();
	
	private boolean isFavorite;
	
	private static final int LOCATIONTYPE = 3; 
	
	public StopLocation(double latitudeAsDegrees, double longitudeAsDegrees,
			Drawable busStop, int id, String title, String inBound, RouteConfig route)
	{
		this.latitude = latitudeAsDegrees * LocationComparator.degreesToRadians;
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitude = longitudeAsDegrees * LocationComparator.degreesToRadians;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.busStop = busStop;
		this.id = id;
		this.title = title;
		this.inBound = inBound;
		this.route = route;
	}
	
	
	@Override
	public double distanceFrom(double centerLatitude, double centerLongitude) {
		return LocationComparator.computeDistance(latitude, longitude, centerLatitude, centerLongitude);
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
		return id | LOCATIONTYPE << 16;
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
		String directionToShow = route.getDirectionTitle(inBound);
		
		String ret = "Route: " + route.getRouteName() + ", stop: " + id + "\n" + directionToShow + "\nTitle: " + title;
		
		return ret;
	}

	@Override
	public String makeSnippet() {
		if (predictions.size() == 0)
		{
			return null;
		}
		
		
		String ret = "";
		final int max = 3;
		int count = 0;
		for (Prediction prediction : predictions)
		{
			ret += "\n" + prediction.toString();
			
			count++;
			if (count >= max)
			{
				break;
			}
		}
		
		return ret;
	}
	
	public int getStopNumber() {
		return id;
	}

	public void clearPredictions()
	{
		predictions.clear();
	}
	
	public void addPrediction(int minutes, long epochTime, int vehicleId,
			String direction) {
		String directionToShow = route.getDirectionTitle(direction);
		predictions.add(new Prediction(minutes, epochTime, vehicleId, directionToShow));
		
	}

	public String getTitle() {
		return title;
	}

	public String getDirtag() {
		return inBound;
	}
	
	public void toggleFavorite()
	{
		this.isFavorite = !isFavorite;
	}
	
	@Override
	public int getIsFavorite() {
		return isFavorite ? IS_FAVORITE : NOT_FAVORITE;
	}

	@Override
	public void serialize(Box dest) throws IOException {
		dest.writeDouble(latitudeAsDegrees);
		dest.writeDouble(longitudeAsDegrees);
		dest.writeInt(id);
		if (dest.getVersionNumber() >= DatabaseHelper.ADDED_FAVORITES_DB_VERSION)
		{
			dest.writeBoolean(isFavorite);
		}
		dest.writeString(title);
		dest.writeString(inBound);
		
		
		dest.writePredictions(predictions);
	}

	
	public StopLocation(Box source, RouteConfig routeConfig, Drawable busStop) throws IOException {


		latitudeAsDegrees = source.readDouble();
		longitudeAsDegrees = source.readDouble();
		this.latitude = latitudeAsDegrees * LocationComparator.degreesToRadians;
		this.longitude = longitudeAsDegrees * LocationComparator.degreesToRadians;
		
		

		id = source.readInt();
		if (source.getVersionNumber() >= DatabaseHelper.ADDED_FAVORITES_DB_VERSION)
		{
			isFavorite = source.readBoolean();
		}

		title = source.readString();
		inBound = source.readString();

		route = routeConfig;
		
		this.busStop = busStop;

		source.readPredictions(predictions);
	}


	public void becomeWeak() {
		predictions.clear();
	}
}
