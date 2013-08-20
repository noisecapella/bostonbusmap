package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import skylight1.opengl.files.QuickParseUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.DistancePrediction;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.StringUtil;

public class SIRIVehicleLocationsFeedParser extends SIRIParser {
	private final RouteConfig routeConfig;

	private long lastUpdatedTime;

	public SIRIVehicleLocationsFeedParser(RouteConfig routeConfig,
			Directions directions,
			RouteTitles routeTitles, RoutePool routePool) {
		super(routePool, directions, routeTitles);
		this.routeConfig = routeConfig;
	}

	public void runParse(InputStreamReader data, TransitSystem transitSystem, ConcurrentHashMap<String,BusLocation> busMapping) throws IOException {
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
		
		transitSystem.setAlerts(results.alerts);

		
	}
	public double getLastUpdateTime() {
		return lastUpdatedTime;
	}


}
