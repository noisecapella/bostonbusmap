package com.schneeloch.bostonbusmap_library.transit;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.CommuterRailStopLocation;
import com.schneeloch.bostonbusmap_library.data.CommuterTrainLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.CommuterRailPredictionsFeedParser;
import com.schneeloch.bostonbusmap_library.util.DownloadHelper;
import com.schneeloch.bostonbusmap_library.util.FeedException;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

public class CommuterRailTransitSource implements TransitSource {
	private final ITransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;
	
	public static final int COLOR = 0x940088;

    private final TransitSourceCache cache;
	
	private final ImmutableMap<String, String> routesToUrls;

	public CommuterRailTransitSource(ITransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem)
	{
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
		
		final String predictionsUrlSuffix = ".json";
		final String dataUrlPrefix = "http://developer.mbta.com/lib/RTCR/RailLine_";
		
		ImmutableMap.Builder<String, String> urlBuilder = ImmutableMap.builder();
		urlBuilder.put("CR-Greenbush", dataUrlPrefix + 1 + predictionsUrlSuffix);
		urlBuilder.put("CR-Kingston", dataUrlPrefix + 2 + predictionsUrlSuffix);
		urlBuilder.put("CR-Middleborough", dataUrlPrefix + 3 + predictionsUrlSuffix);
		urlBuilder.put("CR-Fairmount", dataUrlPrefix + 4 + predictionsUrlSuffix);
		urlBuilder.put("CR-Providence", dataUrlPrefix + 5 + predictionsUrlSuffix);
		urlBuilder.put("CR-Franklin", dataUrlPrefix + 6 + predictionsUrlSuffix);
		urlBuilder.put("CR-Needham", dataUrlPrefix + 7 + predictionsUrlSuffix);
		urlBuilder.put("CR-Worcester", dataUrlPrefix + 8 + predictionsUrlSuffix);
		urlBuilder.put("CR-Fitchburg", dataUrlPrefix + 9 + predictionsUrlSuffix);
		urlBuilder.put("CR-Lowell", dataUrlPrefix + 10 + predictionsUrlSuffix);
		urlBuilder.put("CR-Haverhill", dataUrlPrefix + 11 + predictionsUrlSuffix);
		urlBuilder.put("CR-Newburyport", dataUrlPrefix + 12 + predictionsUrlSuffix);
		routesToUrls = urlBuilder.build();

        cache = new TransitSourceCache();
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			Selection selection, int maxStops, double centerLatitude,
			double centerLongitude,
			final VehicleLocations busMapping,
			final RoutePool routePool, final Directions directions,
			final Locations locationsObj) throws IOException,
            ParserConfigurationException, SAXException
	{
		Selection.Mode selectedBusPredictions = selection.getMode();

		List<RefreshData> outputData = Lists.newArrayList();
        switch (selectedBusPredictions) {
            case BUS_PREDICTIONS_ONE:
            case VEHICLE_LOCATIONS_ONE: {
                ImmutableList<ImmutableList<Location>> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

                //ok, do predictions now
                getPredictionsUrl(locations, maxStops, routeConfig.getRouteName(), outputData, selectedBusPredictions);
                break;
            }
            case BUS_PREDICTIONS_ALL:
            case VEHICLE_LOCATIONS_ALL:
            case BUS_PREDICTIONS_STAR: {
                ImmutableList<ImmutableList<Location>> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

                getPredictionsUrl(locations, maxStops, null, outputData, selectedBusPredictions);

                break;
            }
            default:
                throw new RuntimeException("Unexpected mode");
        }

        // split into up to 12 threads
        final ConcurrentHashMap<String, Exception> exceptions = new ConcurrentHashMap<>();
        List<Thread> threads = Lists.newArrayList();
        for (final RefreshData outputRow : outputData)
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String url = outputRow.url;
                        DownloadHelper downloadHelper = new DownloadHelper(url);
                        try {
                            InputStream stream = downloadHelper.getResponseData();
                            InputStreamReader data = new InputStreamReader(stream);
                            //StringReader data = new StringReader(hardcodedData);

                            //bus prediction

                            String route = outputRow.route;
                            RouteConfig railRouteConfig = routePool.get(route);
                            CommuterRailPredictionsFeedParser parser = new CommuterRailPredictionsFeedParser(railRouteConfig, directions,
                                    busMapping, locationsObj.getRouteTitles());

                            parser.runParse(data);
                            data.close();
                        }
                        finally {
                            downloadHelper.disconnect();
                        }
                    } catch (Exception e) {
                        exceptions.put(outputRow.route, e);
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
            throw new FeedException("Error downloading from commuter rail route " + key + " data feed", exception);
        }

        for (RefreshData refreshData : outputData) {
            cache.updateVehiclesForRoute(refreshData.route);
            cache.updatePredictionForRoute(refreshData.route);
        }
	}

	private static class RefreshData {
		private final String url;
		private final String route;
		
		public RefreshData(String url, String route) {
			this.url = url;
			this.route = route;
		}
	}
	
	private void getPredictionsUrl(ImmutableList<ImmutableList<Location>> locationGroups, int maxStops,
			String routeName, List<RefreshData> outputData, Selection.Mode mode)
	{
		//http://developer.mbta.com/lib/RTCR/RailLine_1.csv
		
		//BUS_PREDICTIONS_ONE or VEHICLE_LOCATIONS_ONE
		if (routeName != null)
		{
			//we know we're updating only one route
			if (isCommuterRail(routeName))
			{
				
				String url = routesToUrls.get(routeName);

                if (cache.canUpdatePredictionForRoute(routeName) && cache.canUpdateVehiclesForRoute(routeName)) {
                    outputData.add(new RefreshData(url, routeName));
                }
				return;
			}
		}
		else
		{
			if (mode == Selection.Mode.BUS_PREDICTIONS_STAR)
			{
				//ok, let's look at the locations and see what we can get
                for (ImmutableList<Location> group : locationGroups) {
                    for (Location location : group) {
                        if (location instanceof StopLocation) {
                            StopLocation stopLocation = (StopLocation) location;


                            for (String route : stopLocation.getRoutes()) {
                                if (isCommuterRail(route) && containsRoute(route, outputData) == false) {
                                    String url = routesToUrls.get(route);
                                    if (cache.canUpdatePredictionForRoute(route) && cache.canUpdateVehiclesForRoute(route)) {
                                        outputData.add(new RefreshData(url, route));
                                    }
                                }
                            }
                        } else if (location instanceof BusLocation) {
                            //bus location
                            BusLocation busLocation = (BusLocation) location;
                            String route = busLocation.getRouteId();

                            if (isCommuterRail(route) && containsRoute(route, outputData) == false) {
                                String url = routesToUrls.get(route);
                                if (cache.canUpdatePredictionForRoute(route) && cache.canUpdateVehiclesForRoute(route)) {
                                    outputData.add(new RefreshData(url, route));
                                }
                            }
                        }
                    }
                }
			}
			else
			{
				//add all 12 of them

				for (String route : routesToUrls.keySet())
				{
					String url = routesToUrls.get(route);

                    if (cache.canUpdatePredictionForRoute(route) && cache.canUpdateVehiclesForRoute(route)) {
                        outputData.add(new RefreshData(url, route));
                    }
				}
			}
		}
	}

	private static boolean containsRoute(String route, List<RefreshData> outputData) {
		boolean containsRoute = false;
		for (RefreshData row : outputData) {
			if (row.route.equals(route)) {
				containsRoute = true;
				break;
			}
		}
		return containsRoute;
	}

	private boolean isCommuterRail(String routeName) {
		return routeTitles.hasRoute(routeName);
	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	@Override
	public ITransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		//try splitting up the route keys along the diagonal and see if they match one piece of it
		for (String route : routeTitles.routeTags())
		{
			String title = routeTitles.getTitle(route);
			if (title.contains("/"))
			{
				String[] pieces = title.split("/");
				for (int i = 0; i < pieces.length; i++)
				{
					if (lowercaseQuery.equals(pieces[i].toLowerCase(Locale.US)))
					{
						return route;
					}
				}
			}
		}
		
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routeTitles);
		
	}

	@Override
	public CommuterRailStopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle,
			String route, Optional<String> parent) {
		CommuterRailStopLocation stop = new CommuterRailStopLocation.CommuterRailBuilder(
				latitude, longitude, stopTag, stopTitle, parent).build();
		stop.addRoute(route);
		return stop;
	}

    @Override
    public BusLocation createVehicleLocation(float latitude, float longitude, String id, long lastFeedUpdateInMillis, Optional<Integer> heading, String routeName, String headsign) {
        return new CommuterTrainLocation(latitude, longitude, id, lastFeedUpdateInMillis, heading, routeName, headsign);
    }

    @Override
	public int getLoadOrder() {
		return 3;
	}

    @Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public boolean requiresSubwayTable() {
		return true;
	}

	@Override
	public IAlerts getAlerts() {
		return transitSystem.getAlerts();
	}
}
