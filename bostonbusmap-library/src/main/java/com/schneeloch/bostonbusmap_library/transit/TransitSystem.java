package com.schneeloch.bostonbusmap_library.transit;

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
import java.util.concurrent.CopyOnWriteArrayList;

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
	private static final double laLatitude = 34.0522222;
	private static final double laLongitude = -118.2427778;
	
	private static final String website = "http://www.terribleinformation.org/george/bostonbusmap";
	
    /**
     * Unimplemented for Los Angelbus
     */
	public static String ALERTS_URL = null;
    private AlertsFuture alertsFuture = null;

	private static final String[] emails = new String[]{"bostonbusmap@gmail.com"};
	private static final String emailSubject = "Los Angelbus error report";
    private static final String feedbackUrl = "http://webapps2.metro.net/customercomments/";

	private static final boolean showRunNumber = false;

	private RouteTitles routeTitles;
	

	public static double getCenterLat() {
        return laLatitude;
    }

    private static final String agencyName = "MBTA";


	public static double getCenterLon() {
		return laLongitude;
	}

	public static int getCenterLatAsInt()
	{
		return (int)(laLatitude * Constants.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(laLongitude * Constants.E6);
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

			defaultTransitSource = new LABusTransitSource(this, busDrawables, busTransitRoutes, routeTitles);
			
			ImmutableMap.Builder<String, TransitSource> mapBuilder = ImmutableMap.builder();

			transitSourceMap = mapBuilder.build();

			transitSources = ImmutableList.of(defaultTransitSource);

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
							final Directions directions, final Locations locations) {
		List<Thread> threads = Lists.newArrayList();
		final CopyOnWriteArrayList<Exception> exceptions = new CopyOnWriteArrayList<>();

		for (final TransitSource source : transitSources)
		{
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        source.refreshData(routeConfig, selection, maxStops, centerLatitude,
                                centerLongitude, busMapping, routePool, directions, locations);
                    } catch (Exception e) {
                        exceptions.add(e);
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


	private static final TimeZone laTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
	private static final boolean defaultAllRoutesBlue = true;
	private static DateFormat defaultTimeFormat;
	private static DateFormat defaultDateFormat;
		
	/**
	 * TODO: Time handling in this app should be cleaned up to be all
	 * UTC, but I don't want to risk breaking something that works 
	 * @return
	 */
	public static TimeZone getTimeZone()
	{
		return laTimeZone;
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

        // Los Angelbus currently doesn't provide alerts
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
