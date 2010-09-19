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
	private static final String mbtaLocationsDataUrlOneRoute = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaLocationsDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta";
	
	private static final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";
	private static final String subwayPredictionsDataUrl = "http://developer.mbta.com/Data/";

	
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
	
	public static String getRouteConfigUrl()
	{
		return getRouteConfigUrl(null);
	}
	
	public static String getPredictionsUrl(List<Location> locations, int maxStops, String route)
	{
		if (isSubway(route) && route != null)
		{
			return subwayPredictionsDataUrl + route + ".json";
		}
		else
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
	}
	
	

	public static String getSizeOfRouteConfigUrl() {
		return "7.5MB";
	}

	public static void bindPredictionElementsForUrl(StringBuilder urlString,
			String routeName, String stopId) {
		urlString.append("&stops=").append(routeName).append("%7C%7C").append(stopId);
		
	}

	public static String getSubwayRouteConfigUrl() {
		return "http://developer.mbta.com/RT_Archive/RealTimeHeavyRailKeys.csv";
	}

	public static boolean isSubway(String route) {
		for (String subwayRoute : subwayRoutes)
		{
			if (subwayRoute.equals(route))
			{
				return true;
			}
		}
		return false;
	}

	private static final String[] subwayRoutes = new String[] {"Red", "Orange", "Blue"};
	
	public static String[] getAllSubwayRoutes() {
		return subwayRoutes;
	}
}
