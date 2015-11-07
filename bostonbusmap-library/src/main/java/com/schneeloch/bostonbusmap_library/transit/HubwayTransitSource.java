package com.schneeloch.bostonbusmap_library.transit;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.HubwayStopLocation;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.HubwayParser;
import com.schneeloch.bostonbusmap_library.util.DownloadHelper;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayTransitSource implements TransitSource {
	public static final String stopTagPrefix = "hubway_";
	private static final String routeTag = "Hubway";
	private static final String dataUrl = "http://www.thehubway.com/data/stations/bikeStations.xml";

	private final ITransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final ITransitSystem transitSystem;

    private final TransitSourceCache cache;

    private static Schema.Routes.SourceId[] transitSourceIds = new Schema.Routes.SourceId[] {Schema.Routes.SourceId.Hubway};

	public HubwayTransitSource(ITransitDrawables drawables, TransitSourceTitles routeTitles,
							   TransitSystem transitSystem) {

		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;

        cache = new TransitSourceCache();
	}

	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
							int maxStops, double centerLatitude, double centerLongitude,
							VehicleLocations busMapping,
							RoutePool routePool, Directions directions,
							Locations locationsObj) throws IOException, ParserConfigurationException, SAXException {
		Selection.Mode mode = selection.getMode();

        switch (mode) {
            case VEHICLE_LOCATIONS_ALL:
            case VEHICLE_LOCATIONS_ONE:
                // no need for that here
                break;
            case BUS_PREDICTIONS_ALL:
            case BUS_PREDICTIONS_ONE:
            case BUS_PREDICTIONS_STAR:
                if (!cache.canUpdateAllPredictions()) {
                    return;
                }


                RouteConfig hubwayRouteConfig = routePool.get(routeTag);
                DownloadHelper downloadHelper = new DownloadHelper(dataUrl);
                try {
                    InputStream stream = downloadHelper.getResponseData();

                    HubwayParser parser = new HubwayParser(hubwayRouteConfig);
                    parser.runParse(stream);
                    parser.addMissingStops(locationsObj);
                    List<PredictionStopLocationPair> pairs = parser.getPairs();

                    for (PredictionStopLocationPair pair : pairs) {
                        pair.stopLocation.clearPredictions(null);
                        pair.stopLocation.addPrediction(pair.prediction);
                    }

                    cache.updateAllPredictions();
                }
                finally {
                    downloadHelper.disconnect();
                }

                break;
            default:
                throw new RuntimeException("Unknown mode encountered");

        }

	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routeTitles);
	}

	@Override
	public ITransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude, String stopTag, String stopTitle, String route) {
		HubwayStopLocation stop = new HubwayStopLocation.HubwayBuilder(latitude,
				longitude, stopTag, stopTitle).build();
		stop.addRoute(route);
		return stop;
	}

    @Override
    public BusLocation createVehicleLocation(float latitude, float longitude, String id, long lastFeedUpdateInMillis, Optional<Integer> heading, String routeName, String headsign) {
        throw new RuntimeException("Cannot create hubway location");
    }

    @Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public int getLoadOrder() {
		return 4;
	}

	@Override
	public Schema.Routes.SourceId[] getTransitSourceIds() {
		return transitSourceIds;
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
	public String getDescription() {
		return "Hubway";
	}
}
