package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import boston.Bus.Map.main.Main;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface LocationGroup {

	float getLatitudeAsDegrees();

	float getLongitudeAsDegrees();
	
	int getLatAsInt();
	int getLonAsInt();

	float distanceFrom(double centerLatitude,
			double centerLongitude);

	Drawable getDrawable(Context context, boolean shadow, boolean isSelected);


	/**
	 * Prepare the textbox text and store it in the class
	 * @param selectedRoute show only this route, if not null
	 * @param context used for formatting the time
	 */
	void makeSnippetAndTitle(RouteConfig selectedRoute, MyHashMap<String, String> routeKeysToTitles, Context context);
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
	ArrayList<Alert> getSnippetAlerts();

	List<String> getAllRoutes();
	
	boolean isVehicle();
	
	boolean isBeta();

	String getFirstRoute();
}
