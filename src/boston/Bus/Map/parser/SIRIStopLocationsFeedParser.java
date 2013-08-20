package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Set;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.transit.TransitSystem;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SIRIStopLocationsFeedParser extends SIRIParser {

	public SIRIStopLocationsFeedParser(RoutePool routePool,
			Directions directions, TransitSourceTitles routeTitles) {
		super(routePool, directions, routeTitles);
	}

	public void runParse(InputStreamReader data,
			StopLocation stop) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);
		stop.clearPredictions(null);

		JsonElement root = new JsonParser().parse(bufferedReader);
		SIRIVehicleParsingResults results = parseTree(root.getAsJsonObject());

		for (PredictionStopLocationPair pair : results.pairs) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}

	@Override
	protected void parseSpecialElement(JsonObject serviceDelivery,
			SIRIVehicleParsingResults results) throws IOException {

		JsonArray stopMonitoringDeliveryArray = serviceDelivery.get("StopMonitoringDelivery").getAsJsonArray();
		for (JsonElement stopMonitoringDeliveryElement : stopMonitoringDeliveryArray) {
			JsonObject stopMonitoringDelivery = stopMonitoringDeliveryElement.getAsJsonObject();
		
			String dateString = stopMonitoringDelivery.get("ResponseTimestamp").getAsString();

			Date responseDate = parseTime(dateString);
			JsonArray vehicleActivity = stopMonitoringDelivery.get("MonitoredStopVisit").getAsJsonArray();

			for (JsonElement element : vehicleActivity) {
				JsonObject monitoredVehicleJourney = element.getAsJsonObject().get("MonitoredVehicleJourney").getAsJsonObject();
				parseVehicleMonitoringDelivery(monitoredVehicleJourney, responseDate, results);
			}
		}
		
	}
}
