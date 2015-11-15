package com.schneeloch.bostonbusmap_library.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.schneeloch.bostonbusmap_library.data.CommuterRailPrediction;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.gson.MbtaRealtimeRoot;
import com.schneeloch.bostonbusmap_library.parser.gson.Mode;
import com.schneeloch.bostonbusmap_library.parser.gson.Route;
import com.schneeloch.bostonbusmap_library.parser.gson.Stop;
import com.schneeloch.bostonbusmap_library.parser.gson.Trip;
import com.schneeloch.bostonbusmap_library.transit.MbtaRealtimeTransitSource;

import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TimePrediction;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class MbtaRealtimePredictionsParser {
	private final TransitSourceTitles routeKeysToTitles;

	private final ImmutableSet<String> routeNames;
    private final RoutePool routePool;
	
	public MbtaRealtimePredictionsParser(ImmutableSet<String> routeNames,
                                         RoutePool routePool,
			TransitSourceTitles routeKeysToTitles)
	{
		this.routeKeysToTitles = routeKeysToTitles;
		this.routeNames = routeNames;
        this.routePool = routePool;
	}

	public void runParse(Reader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

        MbtaRealtimeRoot root = new Gson().fromJson(bufferedReader, MbtaRealtimeRoot.class);

		clearPredictions();

		for (PredictionStopLocationPair pair : parseTree(root)) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}
	
	private List<PredictionStopLocationPair> parseTree(MbtaRealtimeRoot root) throws IOException {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();

        if (root.mode == null) {
            return pairs;
        }
        for (Mode mode : root.mode) {
            for (Route route : mode.route) {
                String routeName;
                if (MbtaRealtimeTransitSource.gtfsNameToRouteName.containsKey(route.route_id)) {
                    routeName = MbtaRealtimeTransitSource.gtfsNameToRouteName.get(route.route_id);
                }
                else {
                    LogUtil.i("Route id not found: " + route.route_id);
                    continue;
                }
                RouteConfig routeConfig = routePool.get(routeName);


                for (com.schneeloch.bostonbusmap_library.parser.gson.Direction direction : route.direction) {
                    for (Trip trip : direction.trip) {
                        String vehicleId = null;
                        if (trip.vehicle != null) {
                            vehicleId = trip.vehicle.vehicle_id;
                        }

                        String directionName = trip.trip_headsign;
                        String tripName = trip.trip_name;
                        for (Stop stop : trip.stop) {
                            String stopId = stop.stop_id;

                            if ("N/A".equals(stopId)) {
                                // not sure whether this value is coming from feed or Gson parser
                                continue;
                            }

                            StopLocation stopLocation = routeConfig.getStop(stopId);
                            if (stopLocation != null) {
                                // TODO: make this more thorough

                                boolean isCommuterRail = MbtaRealtimeTransitSource.routeNameToTransitSource.get(routeName) == Schema.Routes.SourceId.CommuterRail;
                                String routeTitle = this.routeKeysToTitles.getTitle(routeName);

                                if (stop.sch_arr_dt != null && isCommuterRail) {
                                    // Show scheduled time and delay
                                    long scheduledArrivalMillis = Long.parseLong(stop.sch_arr_dt) * 1000;

                                    long arrivalTimeMillis;
                                    if (stop.pre_dt != null) {
                                        long arrivalTimeSeconds = Long.parseLong(stop.pre_dt);
                                        arrivalTimeMillis = arrivalTimeSeconds * 1000;
                                    }
                                    else {
                                        arrivalTimeMillis = scheduledArrivalMillis;
                                    }
                                    int lateness = (int)(arrivalTimeMillis - scheduledArrivalMillis) / 1000;
                                    if (lateness < 0) {
                                        lateness = 0;
                                    }

                                    TimePrediction prediction = new CommuterRailPrediction(scheduledArrivalMillis,
                                            tripName, directionName, routeName,
                                            routeTitle, false,
                                            lateness > 5*60, lateness, "", stopId,
                                            CommuterRailPrediction.Flag.Arr
                                    );

                                    PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stopLocation);
                                    pairs.add(pair);
                                } else if (stop.pre_dt != null) {
                                    long arrivalTimeSeconds = Long.parseLong(stop.pre_dt);
                                    long arrivalTimeMillis = arrivalTimeSeconds * 1000;

                                    TimePrediction prediction;
                                    if (isCommuterRail) {
                                        prediction = new CommuterRailPrediction(arrivalTimeMillis,
                                                tripName, directionName, routeName,
                                                routeTitle, false,
                                                false, 0, "", stopId,
                                                CommuterRailPrediction.Flag.Arr
                                        );
                                    }
                                    else {
                                        prediction = new TimePrediction(arrivalTimeMillis, vehicleId, directionName,
                                                routeName, routeTitle, false, false, 0, null, stopId);
                                    }

                                    PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stopLocation);
                                    pairs.add(pair);

                                }
                            }
                            else {
                                LogUtil.i("Unable to find stop " + stopId + " in database");
                            }
                        }
                    }
                }
            }
        }

		return pairs;
	}
	private void clearPredictions() throws IOException
	{
		for (String route : routeNames) {
            RouteConfig routeConfig = routePool.get(route);
            if (routeConfig != null) {
                for (StopLocation stopLocation : routeConfig.getStops()) {
                    stopLocation.clearPredictions(routeConfig);
                }
            }
		}
	}
}
