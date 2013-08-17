package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import skylight1.opengl.files.QuickParseUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.DistancePrediction;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;

public class SIRIVehicleLocationsFeedParser {
	private final RouteConfig routeConfig;
	private final Directions directions;
	private final ConcurrentHashMap<String, BusLocation> busMapping;
	private final RouteTitles routeTitles;
	private final RoutePool routePool;
	
	private final Set<String> vehiclesToRemove;

	public SIRIVehicleLocationsFeedParser(RouteConfig routeConfig,
			Directions directions,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RouteTitles routeTitles, RoutePool routePool) {
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.busMapping = busMapping;
		this.routeTitles = routeTitles;
		this.routePool = routePool;
		
		vehiclesToRemove = Sets.newHashSet(busMapping.keySet());
	}

	public void runParse(InputStreamReader data) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		List<PredictionStopLocationPair> pairs = parseTree(root.getAsJsonObject());
		
		for (String vehicleId : vehiclesToRemove) {
			busMapping.remove(vehicleId);
		}
		
		String route = routeConfig.getRouteName();
		clearPredictions();

		for (PredictionStopLocationPair pair : pairs) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
		
	}
	private void clearPredictions() throws IOException
	{
		if (routeConfig != null)
		{
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
		else {
			routePool.clearAllPredictions();
		}
	}

	public double getLastUpdateTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<PredictionStopLocationPair> parseTree(JsonObject root) throws IOException {
		List<PredictionStopLocationPair> ret = Lists.newArrayList();
		
		JsonObject siri = root.get("Siri").getAsJsonObject();
		JsonObject serviceDelivery = siri.get("ServiceDelivery").getAsJsonObject();
		// serviceDelivery.get("SituationExchange") will have the alerts
		JsonObject vehicleMonitoringDelivery = serviceDelivery.get("VehicleMonitoringDelivery").getAsJsonArray().get(0).getAsJsonObject();
		JsonArray vehicleActivity = vehicleMonitoringDelivery.get("VehicleActivity").getAsJsonArray();

		for (JsonElement element : vehicleActivity) {
			
			JsonObject monitoredVehicleJourney = ((JsonObject)element).get("MonitoredVehicleJourney").getAsJsonObject();
			String vehicleId = monitoredVehicleJourney.get("VehicleRef").getAsString();
			long lastFeedUpdateInMillis = System.currentTimeMillis();
			long lastUpdateInMillis = System.currentTimeMillis();
			float heading = monitoredVehicleJourney.get("Bearing").getAsFloat();
			String headingString = Integer.toString((int)heading);
			
			JsonObject vehicleLocation = monitoredVehicleJourney.get("VehicleLocation").getAsJsonObject();
			float latitude = vehicleLocation.get("Latitude").getAsFloat();
			float longitude = vehicleLocation.get("Longitude").getAsFloat();
			
			String dirTag = monitoredVehicleJourney.get("DestinationRef").getAsString();
			
			String routeName = monitoredVehicleJourney.get("PublishedLineName").getAsString();
			String routeTitle = routeName;
			
			BusLocation location = new BusLocation(latitude, longitude,
					vehicleId, lastFeedUpdateInMillis, lastUpdateInMillis, headingString,
					true, dirTag, routeName, directions, routeTitle);
			busMapping.put(vehicleId, location);
			vehiclesToRemove.remove(vehicleId);
			
			JsonObject monitoredCall = monitoredVehicleJourney.get("MonitoredCall").getAsJsonObject();
			String stopTag = monitoredCall.get("StopPointRef").getAsString();
			JsonObject distances = monitoredCall.get("Extensions").getAsJsonObject().get("Distances").getAsJsonObject();
			String presentableDistance = distances.get("PresentableDistance").getAsString();
			
			RouteConfig routeConfig = routePool.get(routeName);
			StopLocation stop = routeConfig.getStop(stopTag);
			String direction = monitoredVehicleJourney.get("DestinationName").getAsString();
			float distanceInMeters = distances.get("DistanceFromCall").getAsFloat();
			DistancePrediction prediction = new DistancePrediction(presentableDistance, vehicleId, direction,
					routeName, routeTitle, distanceInMeters);
			PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stop);
			ret.add(pair);
		}
		
		return ret;
	}
}
