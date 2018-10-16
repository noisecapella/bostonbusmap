package com.schneeloch.bostonbusmap_library.transit;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
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
import com.schneeloch.bostonbusmap_library.util.IDownloadHelper;
import com.schneeloch.bostonbusmap_library.util.IDownloader;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayTransitSource implements TransitSource {
	public static final String stopTagPrefix = "hubway_";
	private static final String routeTag = "Hubway";
	private static final String infoUrl = "https://gbfs.thehubway.com/gbfs/en/station_information.json";
	private static final String statusUrl = "https://gbfs.thehubway.com/gbfs/en/station_status.json";

	private final ITransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final ITransitSystem transitSystem;

    private final TransitSourceCache cache;
    private final IDownloader downloader;

	public HubwayTransitSource(ITransitDrawables drawables, TransitSourceTitles routeTitles,
							   TransitSystem transitSystem, IDownloader downloader) {

		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
		this.downloader = downloader;

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
                IDownloadHelper infoHelper = downloader.create(infoUrl);
				IDownloadHelper statusHelper = downloader.create(statusUrl);
                try {
                    InputStream infoStream = infoHelper.getResponseData();
					InputStream statusStream = statusHelper.getResponseData();

                    HubwayParser parser = new HubwayParser(hubwayRouteConfig);
                    parser.runParse(new InputStreamReader(infoStream), new InputStreamReader(statusStream));
                    parser.addMissingStops(locationsObj);
                    List<PredictionStopLocationPair> pairs = parser.getPairs();

                    for (PredictionStopLocationPair pair : pairs) {
                        pair.stopLocation.clearPredictions(null);
                        pair.stopLocation.addPrediction(pair.prediction);
                    }

                    cache.updateAllPredictions();
                }
                finally {
                    infoHelper.disconnect();
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
	public ITransitDrawables getDrawables(Schema.Routes.SourceId sourceId) {
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
}
