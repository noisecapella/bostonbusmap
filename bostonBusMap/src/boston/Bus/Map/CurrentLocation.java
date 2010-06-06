package boston.Bus.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class CurrentLocation implements Location
{
	private final Drawable drawable;
	private final double latitude;
	private final double longitude;
	private final double latitudeAsDegrees;
	private final double longitudeAsDegrees;
	
	public static final int LOCATIONTYPE = 2;
	
	public static final int ID = 0 | LOCATIONTYPE << 16;
	
	public CurrentLocation(Drawable drawable, int latitudeAsDegreesE6, int longitudeAsDegreesE6)
	{
		this.drawable = drawable;
		final double e6 = 1000000.0;
		this.latitudeAsDegrees = latitudeAsDegreesE6 / e6; 
		this.longitudeAsDegrees = longitudeAsDegreesE6 / e6;
		this.latitude = latitudeAsDegrees * LocationComparator.degreesToRadians;
		this.longitude = longitudeAsDegrees * LocationComparator.degreesToRadians;
		
	}

	@Override
	public Drawable getDrawable(Context context, boolean shadow,
			boolean isSelected) {
		return drawable;
	}

	@Override
	public int getHeading() {
		return 0;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public double getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public double getLongitudeAsDegrees() {
		// TODO Auto-generated method stub
		return longitudeAsDegrees;
	}

	@Override
	public boolean hasHeading() {
		return false;
	}

	@Override
	public String makeTitle() {
		return "Current location";
	}
	
	@Override
	public String makeSnippet() {
		return "";
	}

	@Override
	public double distanceFrom(double centerLatitude, double centerLongitude) {
		// TODO Auto-generated method stub
		return LocationComparator.computeDistance(latitude, longitude, centerLatitude, centerLongitude);
	}

}
