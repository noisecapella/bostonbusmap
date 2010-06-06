package boston.Bus.Map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopLocation implements Location
{
	private final double latitude;
	private final double longitude;
	private final double latitudeAsDegrees;
	private final double longitudeAsDegrees;
	private final Drawable busStop;
	private final Drawable tooltip;
	
	private final int id;
	
	private final String title;
	private final String inBound;
	
	private final RouteConfig route;
	
	private final ArrayList<Prediction> predictions = new ArrayList<Prediction>();
	
	private static final int LOCATIONTYPE = 3; 
	
	public StopLocation(double latitudeAsDegrees, double longitudeAsDegrees, Drawable busStop, Drawable tooltip, int id, String title, String inBound, RouteConfig route)
	{
		this.latitude = latitudeAsDegrees * LocationComparator.degreesToRadians;
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitude = longitudeAsDegrees * LocationComparator.degreesToRadians;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.busStop = busStop;
		this.tooltip = tooltip;
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
		String directionToShow = route.getDirection(inBound);
		
		String ret = "Stop: " + id + /*"\n" + directionToShow +*/ "\nTitle: " + title;
		
		return ret;
	}

	@Override
	public String makeSnippet() {
		String ret = "";
		for (Prediction prediction : predictions)
		{
			ret += "\n" + prediction.toString();
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
	
	public void addPrediction(int seconds, long epochTime, int vehicleId,
			String direction) {
		String directionToShow = route.getDirection(direction);
		predictions.add(new Prediction(seconds, epochTime, vehicleId, directionToShow));
		
	}

}
