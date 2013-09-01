package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.transit.TransitSystem;

public class SIRIVehicleLocationsFeedParser extends SIRIParser {
	private final RouteConfig routeConfig;

	private long lastUpdatedTime;

	public SIRIVehicleLocationsFeedParser(RouteConfig routeConfig,
			Directions directions,
			TransitSourceTitles routeTitles, RoutePool routePool) {
		super(routePool, directions, routeTitles);
		this.routeConfig = routeConfig;
	}

	public SIRIVehicleParsingResults runParse(InputStreamReader data, ConcurrentHashMap<String,BusLocation> busMapping) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		SIRIVehicleParsingResults results = parseTree(root.getAsJsonObject());

		Set<String> vehiclesToRemove = Sets.newHashSet(busMapping.keySet());
		for (BusLocation busLocation : results.busLocations) {
			vehiclesToRemove.remove(busLocation.getBusNumber());
			busMapping.put(busLocation.getBusNumber(), busLocation);
		}
		
		for (String vehicleToRemove : vehiclesToRemove) {
			busMapping.remove(vehicleToRemove);
		}

		return results;
	}
	public double getLastUpdateTime() {
		return lastUpdatedTime;
	}

	@Override
	protected void parseSpecialElement(JsonObject serviceDelivery,
			SIRIVehicleParsingResults results) throws IOException {

		JsonArray vehicleMonitoringDeliveryArray = serviceDelivery.get("VehicleMonitoringDelivery").getAsJsonArray();
		for (JsonElement vehicleMonitoringDeliveryElement : vehicleMonitoringDeliveryArray) {
			JsonObject vehicleMonitoringDelivery = vehicleMonitoringDeliveryElement.getAsJsonObject();
		
			String dateString = vehicleMonitoringDelivery.get("ResponseTimestamp").getAsString();

			Date responseDate = parseTime(dateString);
			JsonArray vehicleActivity = vehicleMonitoringDelivery.get("VehicleActivity").getAsJsonArray();

			for (JsonElement element : vehicleActivity) {
				JsonObject monitoredVehicleJourney = element.getAsJsonObject().get("MonitoredVehicleJourney").getAsJsonObject();
				parseVehicleMonitoringDelivery(monitoredVehicleJourney, responseDate, results);
			}
		}
		
	}


}
