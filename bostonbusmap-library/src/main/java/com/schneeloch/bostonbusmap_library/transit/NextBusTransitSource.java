package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.google.common.collect.Lists;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteStopPair;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.BusPredictionsFeedParser;
import com.schneeloch.bostonbusmap_library.parser.VehicleLocationsFeedParser;
import com.schneeloch.bostonbusmap_library.util.IDownloadHelper;
import com.schneeloch.bostonbusmap_library.util.IDownloader;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

/**
 * A transit source which accesses a NextBus webservice. Override for a specific agency
 * @author schneg
 *
 */
public abstract class NextBusTransitSource implements TransitSource
{
	private final ITransitSystem transitSystem;
	
	private static final String prefix = "webservices";
	/**
	 * The XML feed URL
	 */
	private final String mbtaLocationsDataUrlOneRoute;
	private final String mbtaLocationsDataUrlAllRoutes;
	private final String mbtaRouteConfigDataUrl;
	private final String mbtaRouteConfigDataUrlAllRoutes;
	private final String mbtaPredictionsDataUrl;

	private final ITransitDrawables drawables;

	private final TransitSourceTitles routeTitles;

    private final TransitSourceCache cache;
    private final IDownloader downloader;

	public NextBusTransitSource(TransitSystem transitSystem,
                                ITransitDrawables drawables, String agency, TransitSourceTitles routeTitles,
                                RouteTitles allRouteTitles, IDownloader downloader)
	{
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		this.downloader = downloader;

		mbtaLocationsDataUrlOneRoute = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaLocationsDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaRouteConfigDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=";
		mbtaRouteConfigDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency;
		mbtaPredictionsDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=" + agency;
		
		this.routeTitles = routeTitles;
        cache = new TransitSourceCache();
	}


	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection, int maxStops,
			double centerLatitude, double centerLongitude, VehicleLocations busMapping, 
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException {
        //read data from the URL
        ITransitSystem transitSystem = locationsObj.getTransitSystem();
        IDownloadHelper downloadHelper;
        Selection.Mode mode = selection.getMode();
        switch (mode) {
            case BUS_PREDICTIONS_ONE:
            case BUS_PREDICTIONS_STAR:
            case BUS_PREDICTIONS_ALL:

                List<Location> locations = Lists.newArrayList();
                for (ImmutableList<Location> group : locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection)) {
                    for (Location location : group) {
                        for (String route : location.getRoutes()) {
                            if (transitSystem.getSourceId(route) == Schema.Routes.SourceId.Bus) {
                                locations.add(location);
                                break;
                            }
                        }
                    }
                }

                //ok, do predictions now
                ImmutableSet<String> routes;
                if (mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
                    routes = ImmutableSet.of(routeConfig.getRouteName());
                } else {
                    routes = ImmutableSet.of();
                }
                String url = getPredictionsUrl(locations, routes);

                if (url == null) {
                    return;
                }

                downloadHelper = downloader.create(url);
                break;
            case VEHICLE_LOCATIONS_ONE: {
                final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), routeConfig.getRouteName());
                if (urlString == null) {
                    return;
                }
                downloadHelper = downloader.create(urlString);
                break;
            }
            case VEHICLE_LOCATIONS_ALL: {
                final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), null);
                if (urlString == null) {
                    return;
                }
                downloadHelper = downloader.create(urlString);
                break;
            }
            default:
                throw new RuntimeException("Unexpected enum");
        }

        try {
            InputStream data = downloadHelper.getResponseData();

            switch (mode) {
                case BUS_PREDICTIONS_ONE:
                case BUS_PREDICTIONS_ALL:
                case BUS_PREDICTIONS_STAR: {
                    //bus prediction

                    BusPredictionsFeedParser parser = new BusPredictionsFeedParser(routePool, directions);

                    parser.runParse(data);

                    // set last update time for downloaded stops
                    ImmutableList<ImmutableList<Location>> groups = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);
                    List<Location> locations = Lists.newArrayList();
                    for (ImmutableList<Location> group : groups) {
                        locations.addAll(group);
                    }

                    ImmutableSet<String> routes;
                    if (mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
                        routes = ImmutableSet.of(routeConfig.getRouteName());
                    } else {
                        routes = ImmutableSet.of();
                    }
                    ImmutableList<RouteStopPair> pairs = getStopPairs(locations, routes);
                    for (RouteStopPair pair : pairs) {
                        cache.updatePredictionForStop(pair);
                    }

                    break;
                }
                case VEHICLE_LOCATIONS_ALL:
                case VEHICLE_LOCATIONS_ONE: {
                    //vehicle locations
                    VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(directions);
                    parser.runParse(data);

                    //get the time that this information is valid until
                    locationsObj.setLastUpdateTime(parser.getLastUpdateTime());

                    Map<VehicleLocations.Key, BusLocation> newBuses = parser.getNewBuses();

                    busMapping.update(Schema.Routes.SourceId.Bus, routeTitles.routeTags(), true, newBuses);

                    // now that we've succeeded, update last download times
                    switch (mode) {
                        case VEHICLE_LOCATIONS_ONE:
                            cache.updateVehiclesForRoute(routeConfig.getRouteName());
                            break;
                        case VEHICLE_LOCATIONS_ALL:
                            cache.updateAllVehicles();
                            break;
                        default:
                            throw new RuntimeException("Unexpected mode");
                    }

                    break;
                }
                default:
                    throw new RuntimeException("Unexpected enum");
            }
        }
        finally {
            downloadHelper.disconnect();
        }
    }

    protected ImmutableList<RouteStopPair> getStopPairs(Collection<Location> locations, Collection<String> routes) {
        ImmutableList.Builder<RouteStopPair> builder = ImmutableList.builder();

        for (Location location : locations)
        {
            if (location instanceof StopLocation) {
                StopLocation stopLocation = (StopLocation) location;
                boolean matchesBus = false;
                for (String route : stopLocation.getRoutes()) {
                    if (transitSystem.getSourceId(route) == Schema.Routes.SourceId.Bus) {
                        matchesBus = true;
                        break;
                    }
                }
                if (matchesBus) {

                    if (routes.isEmpty() == false) {
                        for (String route : routes) {
                            if (stopLocation.hasRoute(route)) {
                                RouteStopPair pair = new RouteStopPair(route, stopLocation.getStopTag());
                                if (cache.canUpdatePredictionForStop(pair)) {
                                    builder.add(pair);
                                }
                            }
                        }
                    } else {
                        for (String stopRoute : stopLocation.getRoutes()) {
                            RouteStopPair pair = new RouteStopPair(stopRoute, stopLocation.getStopTag());
                            if (cache.canUpdatePredictionForStop(pair)) {
                                builder.add(pair);
                            }
                        }
                    }
                }
            }
        }

        return builder.build();
    }

	protected String getPredictionsUrl(Collection<Location> locations, Collection<String> routes)
	{
		StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);

		//TODO: hard limit this to 150 requests

        ImmutableList<RouteStopPair> pairs = getStopPairs(locations, routes);

        if (pairs.size() == 0) {
            return null;
        }

        for (RouteStopPair pair : pairs) {
            if (transitSystem.getSourceId(pair.getRoute()) == Schema.Routes.SourceId.Bus) {
                urlString.append("&stops=").append(pair.getRoute()).append("%7C");
                urlString.append("%7C").append(pair.getStopId());
            }
        }

		return urlString.toString();
	}

	protected String getVehicleLocationsUrl(long time, String route)
	{
        String url = null;
        if (route != null)
        {
            if (cache.canUpdateVehiclesForRoute(route)) {
                url = mbtaLocationsDataUrlOneRoute + time + "&r=" + route;
            }
        }
        else
        {
            if (cache.canUpdateAllVehicles()) {
                url = mbtaLocationsDataUrlAllRoutes + time;
            }
        }

        return url;
	}


	@Override
	public boolean hasPaths() {
		return true;
	}


	@Override
	public StopLocation createStop(float lat, float lon, String stopTag,
			String title, String route, Optional<String> parent)
	{
		StopLocation stop = new StopLocation.Builder(lat, lon, stopTag, title, parent).build();
		stop.addRoute(route);
		return stop;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, transitSystem.getRouteKeysToTitles());
	}
	
	@Override
	public ITransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public int getLoadOrder() {
		return 1;
	}
	
	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}
	
	@Override
	public IAlerts getAlerts() {
		return transitSystem.getAlerts();
	}
	
    @Override
    public BusLocation createVehicleLocation(float latitude, float longitude, String id, long lastFeedUpdateInMillis, Optional<Integer> heading, String routeName, String headsign) {
        return new BusLocation(latitude, longitude, id, lastFeedUpdateInMillis, heading, routeName, headsign);
    }
}
