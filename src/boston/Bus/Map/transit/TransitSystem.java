package boston.Bus.Map.transit;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import android.R.string;
import android.content.ContentResolver;
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
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;
import boston.Bus.Map.util.Constants;
/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
public class TransitSystem implements ITransitSystem {
	private static final double bostonLatitude = 42.3583333;
	private static final double bostonLongitude = -71.0602778;
	
	private static final String website = "http://www.georgeschneeloch.com/bostonbusmap";

	private static final String[] emails = new String[]{"bostonbusmap@gmail.com", "t-trackertrial@mbta.com"};
	private static final String emailSubject = "BostonBusMap error report";

	private RouteTitles routeTitles;
	
	private AlertsMapping alertsMapping;
	
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
	 * Mapping of route name to its transit source
	 */
	private ImmutableMap<String, TransitSource> transitSourceMap;
	private ImmutableList<TransitSource> transitSources;
	
	/**
	 * Be careful with this; this stays around forever since it's static
	 */
	private TransitSource defaultTransitSource;
	
	/**
	 * Only call this on the UI thread!
	 * @param busDrawables
	 * @param subwayDrawables
	 * @param commuterRailDrawables
	 * @param alertsData
	 */
	@Override
	public void setDefaultTransitSource(TransitDrawables busDrawables, TransitDrawables subwayDrawables, 
			TransitDrawables commuterRailDrawables, Context context)
	{
		if (defaultTransitSource == null)
		{
			ContentResolver resolver = context.getContentResolver();
			routeTitles = DatabaseAgent.getRouteTitles(resolver);
			alertsMapping = DatabaseAgent.getAlerts(resolver);

			TransitSourceTitles busTransitRoutes = routeTitles.getMappingForSource(Schema.Routes.enumagencyidBus);
			TransitSourceTitles subwayTransitRoutes = routeTitles.getMappingForSource(Schema.Routes.enumagencyidSubway);
			TransitSourceTitles commuterRailTransitRoutes = routeTitles.getMappingForSource(Schema.Routes.enumagencyidCommuterRail);
			
			defaultTransitSource = new BusTransitSource(this, busDrawables, busTransitRoutes, routeTitles);
			
			ImmutableMap.Builder<String, TransitSource> mapBuilder = ImmutableMap.builder();
			HeavyRailTransitSource subwayTransitSource = new HeavyRailTransitSource(subwayDrawables, subwayTransitRoutes);
			for (String route : subwayTransitSource.getRouteTitles().routeTags()) {
				mapBuilder.put(route, subwayTransitSource);
			}
			
			CommuterRailTransitSource commuterRailTransitSource = new CommuterRailTransitSource(commuterRailDrawables, commuterRailTransitRoutes);
			for (String route : commuterRailTransitSource.getRouteTitles().routeTags())
			{
				mapBuilder.put(route, commuterRailTransitSource);
			}
			transitSourceMap = mapBuilder.build();

			transitSources = ImmutableList.of(commuterRailTransitSource, subwayTransitSource, defaultTransitSource);
		
		}
		else
		{
			Log.e("BostonBusMap", "ERROR: called setDefaultTransitSource twice");
		}
	}
	
	@Override
	public TransitSource getDefaultTransitSource() {
		return defaultTransitSource;
	}
	
	@Override
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

	@Override
	public RouteTitles getRouteKeysToTitles() {
		return routeTitles;
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			Selection selection, int maxStops, double centerLatitude,
			double centerLongitude, ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool,
			Directions directions, Locations locations) throws IOException, ParserConfigurationException, SAXException {
		for (TransitSource source : transitSources)
		{
			source.refreshData(routeConfig, selection, maxStops, centerLatitude,
					centerLongitude, busMapping, routePool, directions, locations);
		}
	}


	private static final TimeZone bostonTimeZone = TimeZone.getTimeZone("America/New_York");
	private static final boolean defaultAllRoutesBlue = false;
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
	@Override
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

	@Override
	public StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle, int platformOrder, String branch,
			String route) {
		TransitSource source = getTransitSource(route);
		
		return source.createStop(latitude, longitude, stopTag, stopTitle, platformOrder, branch, route);
	}

	@Override
	public AlertsMapping getAlertsMapping() {
		return alertsMapping;
	}

	public static String[] getEmails() {
		return emails;
	}
	
	public static String getEmailSubject() {
		return emailSubject;
	}

	public static boolean isDefaultAllRoutesBlue() {
		return defaultAllRoutesBlue;
	}

	public static boolean hasReportProblem() {
		return true;
	}

}
