package boston.Bus.Map.transit;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.R.string;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.StopLocationGroup;
import boston.Bus.Map.data.VehicleLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
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
	
	//these four variables cover a very wide area just in case
	public static final double lowerLeftLat = 41.582579601430346;
	public static final double lowerLeftLon = -72.0428466796875;
	public static final double upperRightLat = 42.74701217318067;
	public static final double upperRightLon = -69.774169921875;

	public static final String[] emails = new String[]{"bostonbusmap@gmail.com", "t-trackertrial@mbta.com"};
	public static final String emailSubject = "BostonBusMap error report";

	private static final AlertsMapping alertsMapping = new AlertsMapping();
	
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
	

	
	private final MyHashMap<String, TransitSource> transitSourceMap = new MyHashMap<String, TransitSource>();
	private final ArrayList<TransitSource> transitSources = new ArrayList<TransitSource>();
	
	/**
	 * Be careful with this; this stays around forever since it's static
	 */
	private TransitSource defaultTransitSource;
	
	public void setDefaultTransitSource(TransitDrawables busDrawables, TransitDrawables subwayDrawables, TransitDrawables commuterRailDrawables)
	{
		if (defaultTransitSource == null)
		{
			defaultTransitSource = new BusTransitSource(this, busDrawables, alertsMapping);
			SubwayTransitSource subwayTransitSource = new SubwayTransitSource(subwayDrawables, alertsMapping);
			transitSourceMap.put(SubwayTransitSource.RedLine, subwayTransitSource);
			transitSourceMap.put(SubwayTransitSource.OrangeLine, subwayTransitSource);
			transitSourceMap.put(SubwayTransitSource.BlueLine, subwayTransitSource);
			
			CommuterRailTransitSource commuterRailTransitSource = new CommuterRailTransitSource(commuterRailDrawables, alertsMapping);
			for (String route : commuterRailTransitSource.getRoutes())
			{
				transitSourceMap.put(route, commuterRailTransitSource);
			}
			
			transitSources.add(commuterRailTransitSource);
			transitSources.add(subwayTransitSource);
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

	public MyHashMap<String, String> getRouteKeysToTitles() {
		if (transitSources.size() <= 1)
		{
			return defaultTransitSource.getRouteKeysToTitles();
		}
		else
		{
			MyHashMap<String, String> ret = new MyHashMap<String, String>();
			
			for (TransitSource source : transitSources)
			{
				MyHashMap<String, String> sourceRouteKeyMap = source.getRouteKeysToTitles();
				if (sourceRouteKeyMap != null)
				{
					ret.putAll(sourceRouteKeyMap);
				}
			}
			
			return ret;
		}
	}

	public void refreshData(int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude, ConcurrentHashMap<String, VehicleLocation> busMapping,
			String routeToUpdate, RoutePool routePool,
			Locations locations) throws IOException, ParserConfigurationException, SAXException {
		for (TransitSource source : transitSources)
		{
			source.refreshData(selectedBusPredictions, maxStops, centerLatitude,
					centerLongitude, busMapping, routeToUpdate, routePool, locations);
		}
	}


	private static final TimeZone bostonTimeZone = TimeZone.getTimeZone("America/New_York");
	private static DateFormat defaultTimeFormat;
	private static DateFormat defaultDateFormat;
		
	public static TimeZone getTimeZone()
	{
		return bostonTimeZone;
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

	public ArrayList<TransitSource> getTransitSources() {
		return transitSources;
	}

}
