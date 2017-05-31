package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;
import com.schneeloch.bostonbusmap_library.data.AlertsFuture;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.MbtaAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.FeedException;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
public class TransitSystem implements ITransitSystem {
	private static final double bostonLatitude = 42.3583333;
	private static final double bostonLongitude = -71.0602778;
	
	private static final String website = "http://www.georgeschneeloch.com/bostonbusmap";

	private static final String[] emails = new String[]{"bostonbusmap@gmail.com"};
	private static final String emailSubject = "BostonBusMap error report";
    private static final String feedbackUrl = "http://www.mbta.com/customer_support/feedback/";

	private static final boolean showRunNumber = false;

	private RouteTitles routeTitles;
	
	/**
	 * This will be null when alerts haven't been read yet
	 */
	private AlertsFuture alertsFuture;
    private static final String agencyName = "MBTA";

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

    public static String getAgencyName() {
        return agencyName;
    }

    /**
	 * Only call this on the UI thread!
	 * @param busDrawables
	 * @param subwayDrawables
	 * @param commuterRailDrawables
	 */
	@Override
	public void setDefaultTransitSource(ITransitDrawables busDrawables, ITransitDrawables subwayDrawables,
			ITransitDrawables commuterRailDrawables, ITransitDrawables hubwayDrawables, IDatabaseAgent databaseAgent)
	{
		if (defaultTransitSource == null)
		{
			routeTitles = databaseAgent.getRouteTitles();

			TransitSourceTitles busTransitRoutes = routeTitles.getMappingForSource(Schema.Routes.SourceId.Bus);
			TransitSourceTitles hubwayTransitRoutes = routeTitles.getMappingForSource(Schema.Routes.SourceId.Hubway);
			
			defaultTransitSource = new BusTransitSource(this, busDrawables, busTransitRoutes, routeTitles);
			
			ImmutableMap.Builder<String, TransitSource> mapBuilder = ImmutableMap.builder();
			MbtaRealtimeTransitSource subwayTransitSource = new MbtaRealtimeTransitSource(
					subwayDrawables,
                    routeTitles.getMappingForSources(new Schema.Routes.SourceId[]{Schema.Routes.SourceId.Subway, Schema.Routes.SourceId.CommuterRail}), this);
			for (String route : subwayTransitSource.getRouteTitles().routeTags()) {
				mapBuilder.put(route, subwayTransitSource);
			}

			HubwayTransitSource hubwayTransitSource = new HubwayTransitSource(hubwayDrawables, hubwayTransitRoutes,
					this);

			for (String route : hubwayTransitSource.getRouteTitles().routeTags()) {
				mapBuilder.put(route, hubwayTransitSource);
			}
			transitSourceMap = mapBuilder.build();

			transitSources = ImmutableList.of(subwayTransitSource,
					defaultTransitSource, hubwayTransitSource);
		
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
	public void refreshData(final RouteConfig routeConfig,
                            final Selection selection, final int maxStops, final double centerLatitude,
                            final double centerLongitude, final VehicleLocations busMapping,
                            final RoutePool routePool,
                            final Directions directions, final Locations locations) throws IOException, ParserConfigurationException, SAXException {
        List<Thread> threads = Lists.newArrayList();
        final ConcurrentHashMap<String, Exception> exceptions = new ConcurrentHashMap<>();

		for (final TransitSource source : transitSources)
		{
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        source.refreshData(routeConfig, selection, maxStops, centerLatitude,
                                centerLongitude, busMapping, routePool, directions, locations);
                    } catch (Exception e) {
                        exceptions.put(source.getDescription(), e);
                    }
                }
            });
            threads.add(thread);
            thread.start();
		}


        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LogUtil.e(e);
            }
        }

        if (exceptions.size() > 0) {
            // hopefully no more than one. We can't throw more than one exception at a time
            String key = exceptions.keySet().iterator().next();
            Exception exception = exceptions.get(key);
            throw new FeedException("Error downloading from " + key + " data feed", exception);
        }
	}


	private static final TimeZone bostonTimeZone = TimeZone.getTimeZone("America/New_York");
	private static final boolean defaultAllRoutesBlue = false;

	public static final String ALERTS_URL = "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb";
	
	private static DateFormat defaultTimeFormat;
	private static DateFormat defaultDateFormat;
		
	/**
	 * TODO: Time handling in this app should be cleaned up to be all
	 * UTC, but I don't want to risk breaking something that works 
	 * @return
	 */
	public static TimeZone getTimeZone()
	{
		return bostonTimeZone;
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
			String stopTag, String stopTitle,
			String route, Optional<String> parent) {
		TransitSource source = getTransitSource(route);
		
		return source.createStop(latitude, longitude, stopTag, stopTitle, route, parent);
	}

	@Override
	public IAlerts getAlerts() {
		if (alertsFuture != null) {
			return alertsFuture.getAlerts();
		}
		else
		{
			// this shouldn't happen but maybe some code might change
			// to cause alerts to be read before they're set
			return AlertsFuture.EMPTY;
		}
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

	public static boolean showRunNumber() {
		return showRunNumber;
	}

	public static boolean hasReportProblem() {
		return true;
	}

	public TransitSource getTransitSourceByRouteType(Schema.Routes.SourceId routeType) {
		for (TransitSource source : transitSources) {
			for (Schema.Routes.SourceId otherRouteType : source.getTransitSourceIds()) {
				if (routeType == otherRouteType) {
					return source;
				}
			}
		}
		return defaultTransitSource;
	}

	/**
	 * This downloads alerts in a background thread. If alerts are
	 * not available when getAlerts() is called, empty alerts are returned
	 */
	public void startObtainAlerts(IDatabaseAgent databaseAgent, Runnable runnable) {
        final long oneMinuteInMillis = 1000 * 60;

		if (alertsFuture == null) {
			// this runs the alerts code in the background,
			// providing empty alerts until the data is ready
			
			alertsFuture = new AlertsFuture(databaseAgent, new MbtaAlertsParser(this), runnable);
		}
        else if (alertsFuture.getCreationTime() + oneMinuteInMillis < System.currentTimeMillis()) {
            // refresh alerts
            alertsFuture = new AlertsFuture(databaseAgent, new MbtaAlertsParser(this), runnable);
        }
	}

    public static String getFeedbackUrl() {
        return feedbackUrl;
    }

    @Override
    public boolean hasVehicles(Schema.Routes.SourceId transitSourceType) {
        if (transitSourceType == Schema.Routes.SourceId.Hubway) {
            return false;
        }
        return true;
    }
}
