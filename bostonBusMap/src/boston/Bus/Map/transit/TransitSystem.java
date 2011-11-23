package boston.Bus.Map.transit;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.schneeloch.sftransit.main.Main;

import android.R.string;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.Constants;
/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
public class TransitSystem {
	private static final double sfLatitude = 37.766667;
	private static final double sfLongitude = -122.433333;
	
	private static final String website = "http://www.terribleinformation.org/george/bostonbusmap";
	
	//these four variables cover a very wide area just in case
	public static final double lowerLeftLat = 41.582579601430346;
	public static final double lowerLeftLon = -72.0428466796875;
	public static final double upperRightLat = 42.74701217318067;
	public static final double upperRightLon = -69.774169921875;
	
	public static final String[] emails = new String[]{"bostonbusmap@gmail.com"};
	public static final String emailSubject = "SF BusMap error report";

	private static final AlertsMapping alertsMapping = new AlertsMapping();
	
	public static double getCenterLat() {
		return sfLatitude;
	}

	public static double getCenterLon() {
		return sfLongitude;
	}

	public static int getCenterLatAsInt()
	{
		return (int)(sfLatitude * Constants.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(sfLongitude * Constants.E6);
	}

	public static String getWebSite() {
		return website;
	}
	

	
	private final HashMap<String, TransitSource> transitSourceMap = new HashMap<String, TransitSource>();
	private final ArrayList<TransitSource> transitSources = new ArrayList<TransitSource>();
	
	/**
	 * Be careful with this; this stays around forever since it's static
	 */
	private TransitSource defaultTransitSource;
	
	public void setDefaultTransitSource(Drawable busStop, Drawable busStopUpdated, Drawable bus, Drawable arrow, Drawable rail, Drawable railArrow)
	{
		if (defaultTransitSource == null)
		{
			defaultTransitSource = new SFBusTransitSource(this, busStop, busStopUpdated, bus, arrow);
			transitSources.add(defaultTransitSource);
		}
	}
	
	public TransitSource getTransitSource(String routeToUpdate) {
		if (null == routeToUpdate)
		{
			return defaultTransitSource;
		}
		else
		{
			
			TransitSource transitSource = transitSourceMap.get(routeToUpdate);
			if (transitSource == null)
			{
				return defaultTransitSource;
			}
			else
			{
				return transitSource;
				
			}
		}
	}

	public String[] getRoutes() {
		if (transitSources.size() > 1)
		{
			ArrayList<String> ret = new ArrayList<String>();

			for (TransitSource source : transitSources)
			{
				for (String route : source.getRoutes())
				{
					ret.add(route);
				}
			}
			
			return ret.toArray(new String[0]);
		}
		else
		{
			String[] routes = defaultTransitSource.getRoutes();
			return routes;
		}
	}

	public HashMap<String, String> getRouteKeysToTitles() {
		if (transitSources.size() <= 1)
		{
			return defaultTransitSource.getRouteKeysToTitles();
		}
		else
		{
			HashMap<String, String> ret = new HashMap<String, String>();
			
			for (TransitSource source : transitSources)
			{
				HashMap<String, String> sourceRouteKeyMap = source.getRouteKeysToTitles();
				if (sourceRouteKeyMap != null)
				{
					ret.putAll(sourceRouteKeyMap);
				}
			}
			
			return ret;
		}
	}

	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude, ConcurrentHashMap<String, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool,
			Directions directions, Locations locations) throws IOException, ParserConfigurationException, SAXException {
		for (TransitSource source : transitSources)
		{
			source.refreshData(routeConfig, selectedBusPredictions, maxStops, centerLatitude,
					centerLongitude, busMapping, selectedRoute, routePool, directions, locations);
		}
	}

	/**
	 * Create a StopLocation from the parameters. 
	 * This will use the route parameter to pick a TransitSource which does the instantiating 
	 * 
	 * @param lat
	 * @param lon
	 * @param stopTag
	 * @param title
	 * @param platformOrder
	 * @param branch
	 * @param route
	 * @param dirTag
	 * @return
	 */
	public StopLocation createStop(float lat, float lon, String stopTag, String title, int platformOrder, 
			String branch, String route, String dirTag)
	{
		TransitSource source = getTransitSource(route);
		
		return source.createStop(lat, lon, stopTag, title, platformOrder, branch, route, dirTag);
	}

	private static final TimeZone sfTimeZone = TimeZone.getTimeZone("America/San_Francisco");
	private static DateFormat defaultTimeFormat;
	private static DateFormat defaultDateFormat;
		
	public static TimeZone getTimeZone()
	{
		return sfTimeZone;
	}

	/**
	 * Return current time in GMT
	 * @return
	 */
	public static long currentTimeMillis()
	{
		long now = System.currentTimeMillis();
		return now + getTimeZone().getOffset(now);
	}

	public static void setDefaultTimeFormat(Context context)
	{
		defaultTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		defaultDateFormat = android.text.format.DateFormat.getDateFormat(context);
	}
	
	public static DateFormat getDefaultTimeFormat() {
		return defaultTimeFormat;
	}
	
	public static DateFormat getDefaultDateFormat()
	{
		return defaultDateFormat;
	}

	/**
	 * Looks for a route that's similar to the search term
	 * @param indexingQuery
	 * @param lowercaseQuery
	 * @return null if nothing found, otherwise the route key 
	 */
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		for (TransitSource source : transitSources)
		{
			String potentialRoute = source.searchForRoute(indexingQuery, lowercaseQuery);
			if (potentialRoute != null)
			{
				return potentialRoute;
			}
		}
		return null;
	}

}
