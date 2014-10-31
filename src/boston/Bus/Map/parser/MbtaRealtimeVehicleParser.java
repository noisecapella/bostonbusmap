package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.parser.gson.MbtaRealtimeRoot;
import boston.Bus.Map.parser.gson.Mode;
import boston.Bus.Map.parser.gson.Route;
import boston.Bus.Map.parser.gson.Trip;
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
import com.google.gson.Gson;
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

        MbtaRealtimeRoot root = new Gson().fromJson(bufferedReader, MbtaRealtimeRoot.class);
		parseTree(root);
		
		for (VehicleLocations.Key vehicleId : vehiclesToRemove) {
			busMapping.remove(vehicleId);
		}
	}
	
	private void parseTree(MbtaRealtimeRoot root) {
        for (Mode mode : root.mode) {
            for (Route route : mode.route) {
                String routeName;
                int transitSourceId;
                if (MbtaRealtimeTransitSource.gtfsNameToRouteName.containsKey(route.route_id)) {
                    routeName = MbtaRealtimeTransitSource.gtfsNameToRouteName.get(route.route_id);
                    transitSourceId = MbtaRealtimeTransitSource.routeNameToTransitSource.get(routeName);
                }
                else {
                    // this is weird because if we get a route id we would have requested it
                    LogUtil.i("Route id not found: " + route.route_id);
                    continue;
                }

                for (boston.Bus.Map.parser.gson.Direction direction : route.direction) {
                    String directionId = direction.direction_name;

                    for (Trip trip : direction.trip) {
                        String tripHeadsign = trip.trip_headsign;
                        String tripName = trip.trip_name;

                        if (trip.vehicle != null) {
                            String id = trip.vehicle.vehicle_id;
                            if (trip.vehicle.vehicle_lat == null || trip.vehicle.vehicle_lon == null) {
                                continue;
                            }
                            float latitude = Float.parseFloat(trip.vehicle.vehicle_lat);
                            float longitude = Float.parseFloat(trip.vehicle.vehicle_lon);
                            long timestamp = Long.parseLong(trip.vehicle.vehicle_timestamp);
                            String bearing = trip.vehicle.vehicle_bearing;

                            VehicleLocations.Key key = new VehicleLocations.Key(transitSourceId, id);
                            vehiclesToRemove.remove(key);

                            String routeTitle = routeKeysToTitles.getTitle(routeName);

                            Direction directionObj = new Direction(tripHeadsign, directionId, routeName, true);
                            String newDirectionId = directionId + "_" + tripHeadsign;
                            directionsObj.add(newDirectionId, directionObj);

                            BusLocation location;
                            if (transitSourceId == Schema.Routes.enumagencyidCommuterRail) {
                                location = new CommuterTrainLocation(latitude, longitude, tripName,
                                        lastFeedUpdateInMillis, timestamp, bearing, true,
                                        newDirectionId, routeName, directionsObj, routeTitle);
                            }
                            else {
                                location = new SubwayTrainLocation(latitude, longitude, id,
                                        lastFeedUpdateInMillis, timestamp, bearing, true,
                                        newDirectionId, routeName, directionsObj, routeTitle);
                            }
                            busMapping.put(key, location);
                        }
                    }
                }
            }
        }
	}
}
