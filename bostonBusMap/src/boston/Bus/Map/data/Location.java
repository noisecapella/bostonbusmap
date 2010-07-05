package boston.Bus.Map.data;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface Location {

	/**
	 * Some unique value for the location
	 * @return
	 */
	int getId();

	boolean hasHeading();

	int getHeading();

	Drawable getDrawable(Context context, boolean shadow, boolean isSelected);

	String makeTitle();

	double getLatitudeAsDegrees();
	
	double getLongitudeAsDegrees();

	/**
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	double distanceFrom(double centerLatitude, double centerLongitude);

	String makeSnippet();

	int getIsFavorite();
}
