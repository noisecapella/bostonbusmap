package com.schneeloch.bostonbusmap_library.transit;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.MbtaV3PredictionsParser;
import com.schneeloch.bostonbusmap_library.parser.MbtaV3VehiclesParser;
import com.schneeloch.bostonbusmap_library.util.IDownloadHelper;
import com.schneeloch.bostonbusmap_library.util.IDownloader;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by schneg on 12/14/17.
 */

public class MbtaV3TransitSource implements TransitSource {
    private final TransitSourceCache cache;
    private final ITransitDrawables drawables;
    private final TransitSourceTitles routeTitles;
    private final ITransitSystem transitSystem;
    private final IDownloader downloader;
    public static final String apiKey = "109fafba79a848e792e8e7c584f6d1f1";
    public static final String dataUrlPrefix = "https://api-v3.mbta.com";

    public MbtaV3TransitSource(ITransitDrawables drawables,
                               TransitSourceTitles routeTitles,
                               ITransitSystem transitSystem,
                               IDownloader downloader) {
        this.drawables = drawables;
        this.routeTitles = routeTitles;
        this.transitSystem = transitSystem;
        this.downloader = downloader;
        cache = new TransitSourceCache();
    }


    @Override
    public void refreshData(RouteConfig routeConfig, Selection selection, int maxStops, double centerLatitude, double centerLongitude, VehicleLocations busMapping, RoutePool routePool, Directions directions, Locations locationsObj) throws IOException, ParserConfigurationException, SAXException {
        //read data from the URL
        ImmutableList<ImmutableList<Location>> groups = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

        ITransitSystem transitSystem = locationsObj.getTransitSystem();
        IDownloadHelper downloadHelper;
        Selection.Mode mode = selection.getMode();
        switch (mode) {
            case BUS_PREDICTIONS_ONE:
            case BUS_PREDICTIONS_STAR:
            case BUS_PREDICTIONS_ALL: {

                List<String> stopIds = Lists.newArrayList();
                for (ImmutableList<Location> group : groups) {
                    for (Location location : group) {
                        if (!(location instanceof StopLocation)) {
                            // might happen if there's a race condition somewhere
                            continue;
                        }
                        StopLocation stopLocation = (StopLocation) location;
                        for (String route : location.getRoutes()) {
                            Schema.Routes.SourceId sourceId = transitSystem.getSourceId(route);
                            if (sourceId != Schema.Routes.SourceId.Hubway) {
                                stopIds.add(stopLocation.getStopTag());
                                break;
                            }
                        }
                    }
                }

                //ok, do predictions now

                String url = dataUrlPrefix + "/predictions?api_key=" + apiKey + "&filter[stop]=" + Joiner.on(",").join(stopIds) + "&include=vehicle,trip";
                downloadHelper = downloader.create(url);
                break;
            }
            case VEHICLE_LOCATIONS_ONE: {
                String url = dataUrlPrefix + "/vehicles?api_key=" + apiKey + "&filter[route]=" + routeConfig.getRouteName() + "&include=trip";
                downloadHelper = downloader.create(url);
                break;
            }
            case VEHICLE_LOCATIONS_ALL: {
                String url = dataUrlPrefix + "/vehicles?api_key=" + apiKey + "&include=trip";
                downloadHelper = downloader.create(url);
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

                    try {
                        MbtaV3PredictionsParser.runParse(locationsObj.getRouteTitles(), cache, routePool, groups, data);
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    break;
                }
                case VEHICLE_LOCATIONS_ALL:
                case VEHICLE_LOCATIONS_ONE: {
                    //vehicle locations
                    Map<VehicleLocations.Key, BusLocation> newBuses = MbtaV3VehiclesParser.runParse(data, locationsObj.getRouteTitles());

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

    private ImmutableList<RouteStopPair> getStopPairs(List<Location> locations) {
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

                    for (String stopRoute : stopLocation.getRoutes()) {
                        RouteStopPair pair = new RouteStopPair(stopRoute, stopLocation.getStopTag());
                        if (cache.canUpdatePredictionForStop(pair)) {
                            builder.add(pair);
                        }
                    }
                }
            }
        }

        return builder.build();

    }

    @Override
    public boolean hasPaths() {
        return true;
    }

    @Override
    public String searchForRoute(String indexingQuery, String lowercaseQuery) {
        return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, transitSystem.getRouteKeysToTitles());
    }

    @Override
    public ITransitDrawables getDrawables() {
        return drawables;
    }

    @Override
    public TransitSourceTitles getRouteTitles() {
        return routeTitles;
    }

    @Override
    public IAlerts getAlerts() {
        return transitSystem.getAlerts();
    }

    public static String translateRoute(String routeId) {
        if (routeId.equals("Green-B") || routeId.equals("Green-C") || routeId.equals("Green-D") || routeId.equals("Green-E")) {
            return "Green";
        }
        return routeId;
    }
}
