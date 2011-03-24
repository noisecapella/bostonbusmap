package boston.Bus.Map.data;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;

public interface Location {
	/**
	 * Some unique value for the location
	 * @return
	 */
	int getId();

	boolean hasHeading();

	int getHeading();

	Drawable getDrawable(Context context, boolean shadow, boolean isSelected);

	float getLatitudeAsDegrees();
	
	float getLongitudeAsDegrees();

	/**
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	double distanceFrom(double centerLatitude, double centerLongitude);

	boolean isFavorite();

	/**
	 * Prepare the textbox text and store it in the class
	 * @param selectedRoute show only this route, if not null
	 * @param context used for formatting the time
	 */
	void makeSnippetAndTitle(RouteConfig selectedRoute, HashMap<String, String> routeKeysToTitles, Context context);

	/**
	 * In case two locations share the same space, combine the textbox text in a nice way
	 * @param routeConfig show only this route, if not null
	 * @param location whose textbox info you're adding to this class
	 * @param context used for formatting the time
	 */
	void addToSnippetAndTitle(RouteConfig routeConfig, Location location, HashMap<String, String> routeKeysToTitles, Context context);

	/**
	 * Get the title you previously created in makeSnippetAndTitle. This is HTML, so make sure newlines are <br />
	 * @return
	 */
	String getSnippetTitle();
	
	/**
	 * Get the title you previously created in makeSnippetAndTitle. This is HTML, so make sure newlines are <br />
	 * @return
	 */
	String getSnippet();

	/**
	 * Does this location match the given id?
	 * @param selectedBusId
	 * @return
	 */
	boolean containsId(int selectedBusId);
}
