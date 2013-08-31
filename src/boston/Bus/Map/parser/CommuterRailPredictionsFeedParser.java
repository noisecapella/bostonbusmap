package boston.Bus.Map.parser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.data.TimePrediction;
import skylight1.opengl.files.QuickParseUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CommuterTrainLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.LogUtil;

public class CommuterRailPredictionsFeedParser
{
	private final RouteConfig routeConfig;
	private final Directions directions;

	private final ConcurrentHashMap<String, BusLocation> busMapping;
	private final RouteTitles routeKeysToTitles;

	private final Set<String> vehiclesToRemove;

	public CommuterRailPredictionsFeedParser(RouteConfig routeConfig, Directions directions,
			ConcurrentHashMap<String, BusLocation> busMapping, RouteTitles routeKeysToTitles)
	{
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.busMapping = busMapping;
		this.routeKeysToTitles = routeKeysToTitles;

		vehiclesToRemove = Sets.newHashSet(busMapping.keySet());
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

	private class PredictionStopLocationPair {
		private final TimePrediction prediction;
		private final StopLocation stopLocation;
		
		public PredictionStopLocationPair(TimePrediction prediction, StopLocation stopLocation) {
			this.prediction = prediction;
			this.stopLocation = stopLocation;
		}
	}

	public void runParse(Reader data) throws IOException
	{
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

	private List<PredictionStopLocationPair> parseTree(JsonObject root) {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();
		
		String routeName = routeConfig.getRouteName();
		String routeTitle = routeKeysToTitles.getTitle(routeName);
		long nowMillis = System.currentTimeMillis();
		long nowSeconds = nowMillis / 1000;
		
		JsonArray messages = root.get("Messages").getAsJsonArray();
		for (JsonElement element : messages) {
			JsonObject message = element.getAsJsonObject();
			String dirTag = message.get("Destination").getAsString();
			Direction direction = directions.getDirection(dirTag);
			if (direction == null) {
				direction = new Direction(dirTag, "", routeName,
						true);
				directions.add(dirTag, direction);
			}
			
			// add vehicle if exists
			
			String vehicle = message.get("Vehicle").getAsString();
			
			// not related to GTFS trip
			String trip = message.get("Trip").getAsString();
			String timestampString = message.get("TimeStamp").getAsString();
			long timestamp = Long.parseLong(timestampString);
			long timestampMillis = timestamp * 1000;

			String latenessString = message.get("Lateness").getAsString();
			int lateness;
			if (latenessString.length() != 0) {
				lateness = QuickParseUtil.parseInteger(latenessString);
			}
			else
			{
				lateness = 0;
			}
			if (vehicle.length() != 0) {
				String latitudeString = message.get("Latitude").getAsString();
				String longitudeString = message.get("Longitude").getAsString();
				String headingString = message.get("Heading").getAsString();
				
				if (longitudeString.length() != 0 && latitudeString.length() != 0) {
					float lat = QuickParseUtil.parseFloat(latitudeString);
					float lon = QuickParseUtil.parseFloat(longitudeString);

					CommuterTrainLocation location = new CommuterTrainLocation(lat,
							lon, trip, timestampMillis, timestampMillis,
							headingString, true, dirTag, routeName, directions,
							routeTitle);
					busMapping.put(trip, location);
					vehiclesToRemove.remove(trip);
				}
			}
			
			// handle predictions
			String stopId = message.get("Stop").getAsString();
			String scheduledString = message.get("Scheduled").getAsString();
			long scheduled = Long.parseLong(scheduledString);
			if (stopId.length() != 0 && scheduledString.length() != 0) {
				StopLocation stop = routeConfig.getStop(stopId);
				if (stop != null) {
					int seconds = (int)(scheduled - nowSeconds);
					int minutes = seconds / 60;
					TimePrediction prediction = new TimePrediction(minutes,
							trip, dirTag, routeName,
							routeTitle, false,
							lateness > 5*60, lateness);
					PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction,
							stop);
					pairs.add(pair);
				}
				else
				{
					LogUtil.w("Commuter rail stop missing: " + stopId);
				}
			}
		}
		
		return pairs;
	}
}
