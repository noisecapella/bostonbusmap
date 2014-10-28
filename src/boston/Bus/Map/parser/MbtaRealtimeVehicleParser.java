package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import skylight1.opengl.files.QuickParseUtil;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CommuterTrainLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.SubwayTrainLocation;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.data.VehicleLocations;
import boston.Bus.Map.data.VehicleLocations.Key;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.transit.MbtaRealtimeTransitSource;
import boston.Bus.Map.util.LogUtil;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MbtaRealtimeVehicleParser {
	private final TransitSourceTitles routeKeysToTitles;

	private final Set<VehicleLocations.Key> vehiclesToRemove;

	private final VehicleLocations busMapping;
	
	private final long lastFeedUpdateInMillis;
	
	private final Directions directionsObj;
	
	public MbtaRealtimeVehicleParser(TransitSourceTitles routeKeysToTitles,
			VehicleLocations busMapping, Directions directionsObj)
	{
		this.routeKeysToTitles = routeKeysToTitles;

		vehiclesToRemove = busMapping.copyVehicleIds();
		this.busMapping = busMapping;
		this.directionsObj = directionsObj;
		
		this.lastFeedUpdateInMillis = System.currentTimeMillis();
	}
	
	public void runParse(Reader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		parseTree(root.getAsJsonObject());
		
		for (VehicleLocations.Key vehicleId : vehiclesToRemove) {
			busMapping.remove(vehicleId);
		}
	}
	
	private void parseTree(JsonObject root) {
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
						int transitSourceId;
						if (MbtaRealtimeTransitSource.gtfsNameToRouteName.containsKey(routeId)) {
							routeName = MbtaRealtimeTransitSource.gtfsNameToRouteName.get(routeId);
							transitSourceId = MbtaRealtimeTransitSource.routeNameToTransitSource.get(routeName);
						}
						else {
							// this is weird because if we get a route id we would have requested it
							LogUtil.i("Route id not found: " + routeId);
							continue;
						}
						//RouteConfig routeConfig = routeConfigs.get(routeInfo.routeName);
						
						if (routeObj.has("direction")) {
							JsonArray directions = routeObj.get("direction").getAsJsonArray();
							
							for (JsonElement directionElem : directions) {
								JsonObject directionObj = directionElem.getAsJsonObject();
								String directionId = directionObj.get("direction_name").getAsString();
								
								if (directionObj.has("trip")) {
									JsonArray trips = directionObj.get("trip").getAsJsonArray();
									
									for (JsonElement tripElem : trips) {
										JsonObject tripObj = tripElem.getAsJsonObject();
										String tripHeadsign = tripObj.get("trip_headsign").getAsString();
										
										if (tripObj.has("vehicle")) {
											JsonObject vehicleObj = tripObj.get("vehicle").getAsJsonObject();
											String id = vehicleObj.get("vehicle_id").getAsString();
											float latitude = QuickParseUtil.parseFloat(vehicleObj.get("vehicle_lat").getAsString());
											float longitude = QuickParseUtil.parseFloat(vehicleObj.get("vehicle_lon").getAsString());
											long timestamp = Long.parseLong(vehicleObj.get("vehicle_timestamp").getAsString());
											String bearing = vehicleObj.get("vehicle_bearing").getAsString();
											
											VehicleLocations.Key key = new VehicleLocations.Key(transitSourceId, id);
											vehiclesToRemove.remove(key);
											
											String routeTitle = routeKeysToTitles.getTitle(routeName);
											
											Direction direction = new Direction(tripHeadsign, directionId, routeName, true); 
											directionsObj.add(directionId, direction);
											
											BusLocation location;
											if (transitSourceId == Schema.Routes.enumagencyidCommuterRail) {
												location = new CommuterTrainLocation(latitude, longitude, id,
														lastFeedUpdateInMillis, timestamp, bearing, true,
														directionId, routeName, directionsObj, routeTitle);
											}
											else if (transitSourceId == Schema.Routes.enumagencyidSubway) {
												location = new SubwayTrainLocation(latitude, longitude, id,
														lastFeedUpdateInMillis, timestamp, bearing, true,
														directionId, routeName, directionsObj, routeTitle);
											}
											else {
												throw new RuntimeException("Unexpected transit id");
											}
											busMapping.put(key, location);
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
			// no vehicles
		}
	}
}
