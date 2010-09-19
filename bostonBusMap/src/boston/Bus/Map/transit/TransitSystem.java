package boston.Bus.Map.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.util.Constants;
/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
public class TransitSystem {
	private static final double bostonLatitude = 42.3583333;
	private static final double bostonLongitude = -71.0602778;
	
	private static final String website = "http://www.terribleinformation.org/george/bostonbusmap";
	
	
	public static double getCenterLat() {
		return bostonLatitude;
	}

	public static double getCenterLon() {
		return bostonLongitude;
	}

	public static int getCenterLatAsInt()
	{
		return (int)(bostonLatitude * Constants.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(bostonLongitude * Constants.E6);
	}

	public static String getWebSite() {
		return website;
	}
	
	/**
	 * The XML feed URL
	 */
	private static final String mbtaLocationsDataUrlOneRoute = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaLocationsDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta";
	
	private static final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";

	
	public static String getVehicleLocationsUrl(long time, String route)
	{
		if (route != null)
		{
			return mbtaLocationsDataUrlOneRoute + time + "&r=" + route;
		}
		else
		{
			return mbtaLocationsDataUrlAllRoutes + time;
		}
	}
	
	public static String getRouteConfigUrl(String route)
	{
		if (route == null)
		{
			return mbtaRouteConfigDataUrlAllRoutes;
		}
		else
		{
			return mbtaRouteConfigDataUrl + route;
		}
	}
	
	public static String getPredictionsUrl(List<Location> locations, int maxStops, String route)
	{
		StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);
		
		for (Location location : locations)
		{
			if (location instanceof StopLocation)
			{
				StopLocation stopLocation = (StopLocation)location;
				stopLocation.createPredictionsUrl(urlString, route);
			}
		}
		
		//TODO: hard limit this to 150 requests
		
		Log.v("BostonBusMap", "urlString for bus predictions, all: " + urlString);
		
		return urlString.toString();
	}
	
	

	public static String getSizeOfRouteConfigUrl() {
		return "7.5MB";
	}

	public static void bindPredictionElementsForUrl(StringBuilder urlString,
			String routeName, String stopId) {
		urlString.append("&stops=").append(routeName).append("%7C%7C").append(stopId);
		
	}

	private final static HashMap<String, TransitSource> transitSources = new HashMap<String, TransitSource>();  
	private static TransitSource defaultTransitSource;
	
	public static void addTransitSource(String route, TransitSource source)
	{
		transitSources.put(route, source);
	}
	
	public static void setDefaultTransitSource(Drawable busStop)
	{
		if (defaultTransitSource == null)
		{
			defaultTransitSource = new MBTABusTransitSource(busStop);
		}
	}
	
	public static TransitSource getTransitSource(String routeToUpdate) {
		return transitSources.get(routeToUpdate);
	}
}
