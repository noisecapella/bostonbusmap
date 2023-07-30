package com.schneeloch.bostonbusmap_library.parser;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.schneeloch.bostonbusmap_library.data.HubwayStopData;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.BikeSharePrediction;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.parser.gson.stationInfo.InfoRoot;
import com.schneeloch.bostonbusmap_library.parser.gson.stationInfo.InfoStation;
import com.schneeloch.bostonbusmap_library.parser.gson.stationStatus.StatusRoot;
import com.schneeloch.bostonbusmap_library.parser.gson.stationStatus.StatusStation;
import com.schneeloch.bostonbusmap_library.transit.HubwayTransitSource;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayParser {
	private final RouteConfig routeConfig;
	private final List<HubwayStopData> hubwayStopData = Lists.newArrayList();

	public HubwayParser(RouteConfig routeConfig) {
		this.routeConfig = routeConfig;
	}

	public void runParse(Reader infoReader, Reader statusReader)  {
		BufferedReader bufferedInfoReader = new BufferedReader(infoReader, 2048);
		BufferedReader bufferedStatusReader = new BufferedReader(statusReader, 2048);

		InfoRoot infoRoot = new Gson().fromJson(bufferedInfoReader, InfoRoot.class);
		StatusRoot statusRoot = new Gson().fromJson(bufferedStatusReader, StatusRoot.class);

		Map<String, StatusStation> statusLookup = Maps.newHashMap();
		for (StatusStation station : statusRoot.data.stations) {
			statusLookup.put(station.station_id, station);
		}

		for (InfoStation infoStation : infoRoot.data.stations) {
			StatusStation statusStation = statusLookup.get(infoStation.station_id);
			if (statusStation != null) {
				String tag = HubwayTransitSource.stopTagPrefix + statusStation.station_id;
				boolean installed = statusStation.is_installed == 1;
				boolean locked = !(statusStation.is_renting == 1 && statusStation.is_returning == 1);
				HubwayStopData data = new HubwayStopData(
						tag, String.valueOf(statusStation.station_id), infoStation.lat, infoStation.lon, infoStation.name,
						statusStation.num_bikes_available, statusStation.num_docks_available, locked, installed
				);
				hubwayStopData.add(data);
			}
		}
	}

    /**
     * For Hubway we need to update the stop list here, we don't receive it ahead of time.
     */
    public void addMissingStops(Locations locations) throws IOException {
        ImmutableMap.Builder<String, StopLocation> builder = ImmutableMap.builder();

        for (HubwayStopData data : hubwayStopData) {
            StopLocation.Builder hubwayBuilder = new StopLocation.Builder(data.latitude, data.longitude, data.tag, data.name, Optional.<String>absent());

            StopLocation newStop = hubwayBuilder.build();
            newStop.addRoute(routeConfig.getRouteName());
            builder.put(data.tag, newStop);
        }

        ImmutableMap<String, StopLocation> stopsToReplace = builder.build();
        locations.replaceStops(routeConfig.getRouteName(), stopsToReplace);
    }

	public List<PredictionStopLocationPair> getPairs() {
        List<PredictionStopLocationPair> pairs = Lists.newArrayList();

        for (HubwayStopData data : hubwayStopData) {
            StopLocation stop = routeConfig.getStop(data.tag);

            if (stop != null && data.name.equals(stop.getTitle())) {
                BikeSharePrediction prediction = new BikeSharePrediction(routeConfig.getRouteName(),
                        routeConfig.getRouteTitle(), data.numberBikes, data.numberEmptyDocks, data.locked, data.installed);


                PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stop);
                pairs.add(pair);
            }
        }

        return pairs;
    }
}
