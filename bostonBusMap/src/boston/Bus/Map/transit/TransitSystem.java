package boston.Bus.Map.transit;

import java.util.ArrayList;
import java.util.List;

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
	private static final String mbtaLocationsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta";
	
	private static final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";

	
	public static String getVehicleLocationsUrl(long time)
	{
		return mbtaLocationsDataUrl + time;
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
	
	public static String getRouteConfigUrl()
	{
		return getRouteConfigUrl(null);
	}
	
	public static String getPredictionsUrl(List<Location> locations, int maxStops, RouteConfig route)
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
		
		Log.v("BostonBusMap", "urlString for bus predictions, all: " + urlString);
		
		return urlString.toString();
	}
	
	

	public static String getSizeOfRouteConfigUrl() {
		return "7.5MB";
	}

	public static void bindPredictionElementsForUrl(StringBuilder urlString,
			RouteConfig routeConfig, String stopId) {
		urlString.append("&stops=").append(routeConfig.getRouteName()).append("%7C%7C").append(stopId);
		
	}
}
