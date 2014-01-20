package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayTrainLocation;
import boston.Bus.Map.util.LogUtil;

public class HeavyRailPredictionsFeedParser {
	private final RouteConfig routeConfig;
	private final Directions directions;
	private final ConcurrentHashMap<String, BusLocation> busMapping;
	private final RouteTitles routeTitles;
	
	private final Set<String> vehiclesToRemove;
	
	public HeavyRailPredictionsFeedParser(RouteConfig routeConfig,
			Directions directions,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RouteTitles routeTitles) {
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.busMapping = busMapping;
		this.routeTitles = routeTitles;
		
		vehiclesToRemove = Sets.newHashSet(busMapping.keySet());
	}


	/**
	 * When this is done old vehicles should be removed from busMapping,
	 * new vehicles should be added and existing ones updated,
	 * and predictions should be cleared and updated for all stops on this route
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void runParse(InputStreamReader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		List<PredictionStopLocationPair> pairs = parseTree(root.getAsJsonObject());
		
		String route = routeConfig.getRouteName();
		clearPredictions(route);

		for (PredictionStopLocationPair pair : pairs) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
		
		for (String vehicleId : vehiclesToRemove) {
			busMapping.remove(vehicleId);
		}
	}
	private void clearPredictions(String route) throws IOException
	{
		if (routeConfig != null)
		{
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
	}

	/**
	 * Parse JSON received from subway real time feed into vehicle locations
	 * and predictions
	 * @param tripList
	 * @return
	 */
	private List<PredictionStopLocationPair> parseTree(JsonObject root) {

		List<PredictionStopLocationPair> pairs = Lists.newArrayList();
		JsonObject tripList = root.get("TripList").getAsJsonObject();
		long vehicleUpdateTime = tripList.get("CurrentTime").getAsInt();
		JsonArray trips = tripList.get("Trips").getAsJsonArray();
		String routeName = routeConfig.getRouteName();
		String routeTitle = routeConfig.getRouteTitle();
		
		for (JsonElement tripElement : trips) {
			JsonObject trip = tripElement.getAsJsonObject();
			
			// get direction of train, or add it if it isn't there already
			String dirTag = trip.get("Destination").getAsString();
			Direction direction = directions.getDirection(dirTag);
			if (direction == null) {
				direction = new Direction(dirTag,
						"", routeConfig.getRouteName(), true);
				directions.add(dirTag, direction);
			}
			
			// extract position information if it exists
			JsonObject position = null;
			String vehicleId = null;
			if (trip.has("Position")) {
				position = trip.get("Position").getAsJsonObject();
				vehicleId = position.get("Train").getAsString();
				float lat = position.get("Lat").getAsFloat();
				float lon = position.get("Long").getAsFloat();
				long positionTimestamp = position.get("Timestamp").getAsInt();
				int heading = position.get("Heading").getAsInt();
				
				long nowEpochTime = positionTimestamp * 1000;
				BusLocation vehicleLocation = new SubwayTrainLocation(lat, lon,
						vehicleId, nowEpochTime, vehicleUpdateTime,
						Integer.valueOf(heading).toString(), true, dirTag, 
						routeName, directions, routeTitle);
				busMapping.put(vehicleId, vehicleLocation);
				vehiclesToRemove.remove(vehicleId);
			}

			// add new predictions
			JsonArray predictions = trip.get("Predictions").getAsJsonArray();
			
			for (JsonElement predictionElement : predictions) {
				JsonObject prediction = predictionElement.getAsJsonObject();
				int seconds = prediction.get("Seconds").getAsInt();
				String stopId = prediction.get("StopID").getAsString();
				
				if (routeConfig.getStopMapping().containsKey(stopId)) {
					StopLocation stop = routeConfig.getStop(stopId);
					int minutes = seconds / 60;
					
					// TODO: should we define lateness here?
					TimePrediction predictionObj = new TimePrediction(minutes,
							vehicleId, dirTag, routeName, routeTitle,
							false, false, 0, stopId);
					PredictionStopLocationPair pair = new PredictionStopLocationPair(predictionObj, stop);
					pairs.add(pair);
				}
				else
				{
					LogUtil.w("StopId mentioned but not found in database: " + stopId);
				}
			}
		}
		
		return pairs;
	}

}
