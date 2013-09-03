package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.SimplePrediction;
import boston.Bus.Map.data.StopLocation;

public class CitibikeParser {

	private final RouteConfig routeConfig;

	public CitibikeParser(RouteConfig routeConfig) throws IOException {
		this.routeConfig = routeConfig;
	}

	public void runParse(InputStreamReader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		List<PredictionStopLocationPair> results = parseTree(root.getAsJsonObject());

		for (StopLocation stop : routeConfig.getStopMapping().values()) {
			stop.clearPredictions(null);
		}
		for (PredictionStopLocationPair pair : results) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}

	private List<PredictionStopLocationPair> parseTree(JsonObject root) {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();
		
		if (root.has("results")) {
			JsonArray results = root.get("results").getAsJsonArray();
			for (JsonElement element : results) {
				JsonObject result = element.getAsJsonObject();
				
				String tag = "citibike_" + Integer.toString(result.get("id").getAsInt());
				StopLocation stop = routeConfig.getStop(tag);
				if (stop != null) {
					String availableBikes = Integer.toString(result.get("availableBikes").getAsInt());
					String availableDocks = Integer.toString(result.get("availableDocks").getAsInt());
					String text = "Available Bikes: " + availableBikes + "<br/>Available Docks: " + availableDocks;
					SimplePrediction prediction = new SimplePrediction("Citibike", "Citibike", text);
					
					pairs.add(new PredictionStopLocationPair(prediction, stop));
				}
			}
		}
		
		return pairs;
	}

}
