package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.database.Schema;
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

		JsonElement root = new JsonParser().parse(bufferedReader);
		List<PredictionStopLocationPair> pairs = parseTree(root.getAsJsonObject());
		
		clearPredictions();

		for (PredictionStopLocationPair pair : pairs) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}
	
	private List<PredictionStopLocationPair> parseTree(JsonObject root) {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();

		if (root.has("mode")) {
			JsonArray modes = root.get("mode").getAsJsonArray();

			for (JsonElement modeElem : modes) {
				JsonObject modeObj = modeElem.getAsJsonObject();
				if (modeObj.has("route")) {
					JsonArray routes = modeObj.get("route").getAsJsonArray();
					
					for (JsonElement routeElem : routes) {
						JsonObject routeObj = routeElem.getAsJsonObject();
						String routeId = routeObj.get("route_id").getAsString();
						
						String routeName;
						if (MbtaRealtimeTransitSource.gtfsNameToRouteName.containsKey(routeId)) {
							routeName = MbtaRealtimeTransitSource.gtfsNameToRouteName.get(routeId);
						}
						else {
							LogUtil.i("Route id not found: " + routeId);
							continue;
						}
						RouteConfig routeConfig = routeConfigs.get(routeName);
						
						if (routeObj.has("direction")) {
							JsonArray directions = routeObj.get("direction").getAsJsonArray();
							
							for (JsonElement directionElem : directions) {
								JsonObject directionObj = directionElem.getAsJsonObject();
								String directionId = directionObj.get("direction_name").getAsString();
								
								if (directionObj.has("trip")) {
									JsonArray trips = directionObj.get("trip").getAsJsonArray();
									
									for (JsonElement tripElem : trips) {
										JsonObject tripObj = tripElem.getAsJsonObject();
										
										String vehicleId = null;
										if (tripObj.has("vehicle")) {
											JsonObject vehicleObj = tripObj.get("vehicle").getAsJsonObject();
											vehicleId = vehicleObj.get("vehicle_id").getAsString();
										}
										
										String directionName = null;
										if (tripObj.has("trip_headsign")) {
											directionName = tripObj.get("trip_headsign").getAsString();
										}
                                        String tripName = tripObj.get("trip_name").getAsString();
										
										if (tripObj.has("stop")) {
											JsonArray stops = tripObj.get("stop").getAsJsonArray();
											
											for (JsonElement stopElem : stops) {
												JsonObject stopObj = stopElem.getAsJsonObject();
												String stopId = stopObj.get("stop_id").getAsString();


												StopLocation stop = routeConfig.getStop(stopId);
												if (stop != null) {

                                                    if (stopObj.has("pre_dt")) {
                                                        // TODO: make this more thorough
														long arrivalTimeSeconds = Long.parseLong(stopObj.get("pre_dt").getAsString());
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

														PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stop);
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
						}
					}
				}
			}
		}
		else {
			// no predictions
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
