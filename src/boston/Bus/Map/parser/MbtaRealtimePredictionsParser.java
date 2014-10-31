package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.database.Schema;
import boston.Bus.Map.parser.gson.MbtaRealtimeRoot;
import boston.Bus.Map.parser.gson.Mode;
import boston.Bus.Map.parser.gson.Route;
import boston.Bus.Map.parser.gson.Stop;
import boston.Bus.Map.parser.gson.Trip;
import boston.Bus.Map.transit.MbtaRealtimeTransitSource;
import skylight1.opengl.files.QuickParseUtil;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CommuterRailPrediction;
import boston.Bus.Map.data.CommuterTrainLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.util.LogUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MbtaRealtimePredictionsParser {
	private final TransitSourceTitles routeKeysToTitles;

	private final ImmutableMap<String, RouteConfig> routeConfigs;
	
	public MbtaRealtimePredictionsParser(ImmutableMap<String, RouteConfig> routeConfigs,
			TransitSourceTitles routeKeysToTitles)
	{
		this.routeKeysToTitles = routeKeysToTitles;
		this.routeConfigs = routeConfigs;
	}

	public void runParse(Reader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

        MbtaRealtimeRoot root = new Gson().fromJson(bufferedReader, MbtaRealtimeRoot.class);

		clearPredictions();

		for (PredictionStopLocationPair pair : parseTree(root)) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}
	
	private List<PredictionStopLocationPair> parseTree(MbtaRealtimeRoot root) {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();

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
                RouteConfig routeConfig = routeConfigs.get(routeName);


                for (boston.Bus.Map.parser.gson.Direction direction : route.direction) {
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
                                if (stop.pre_dt != null) {
                                    long arrivalTimeSeconds = Long.parseLong(stop.pre_dt);
                                    long arrivalTimeMillis = arrivalTimeSeconds * 1000;

                                    String routeTitle = this.routeKeysToTitles.getTitle(routeName);

                                    String id;
                                    if (MbtaRealtimeTransitSource.routeNameToTransitSource.get(routeName) == Schema.Routes.enumagencyidCommuterRail) {
                                        id = tripName;
                                    }
                                    else {
                                        id = vehicleId;
                                    }
                                    TimePrediction prediction = new TimePrediction(arrivalTimeMillis, id, directionName,
                                            routeName, routeTitle, false, false, 0, null, stopId);

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
		for (RouteConfig routeConfig : routeConfigs.values()) {
            for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
	}
}
