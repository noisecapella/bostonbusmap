package com.schneeloch.bostonbusmap_library.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import com.google.common.base.Optional;
import com.schneeloch.bostonbusmap_library.parser.gson.MbtaRealtimeRoot;
import com.schneeloch.bostonbusmap_library.parser.gson.Mode;
import com.schneeloch.bostonbusmap_library.parser.gson.Route;
import com.schneeloch.bostonbusmap_library.parser.gson.Trip;

import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.CommuterTrainLocation;
import com.schneeloch.bostonbusmap_library.data.Direction;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.SubwayTrainLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations.Key;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.transit.MbtaRealtimeTransitSource;
import com.schneeloch.bostonbusmap_library.transit.TransitSource;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class MbtaRealtimeVehicleParser {
	private final TransitSourceTitles routeKeysToTitles;

	private final VehicleLocations busMapping;
	
	private final long lastFeedUpdateInMillis;
	
	private final Directions directionsObj;

    private final ImmutableSet<String> routeNames;
	
	public MbtaRealtimeVehicleParser(TransitSourceTitles routeKeysToTitles,
			VehicleLocations busMapping, Directions directionsObj, ImmutableSet<String> routeNames)
	{
		this.routeKeysToTitles = routeKeysToTitles;

		this.busMapping = busMapping;
		this.directionsObj = directionsObj;
        this.routeNames = routeNames;
		
		this.lastFeedUpdateInMillis = System.currentTimeMillis();
	}
	
	public void runParse(Reader data, TransitSource transitSource) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

        MbtaRealtimeRoot root = new Gson().fromJson(bufferedReader, MbtaRealtimeRoot.class);
		parseTree(root, transitSource);
	}
	
	private void parseTree(MbtaRealtimeRoot root, TransitSource transitSource) {
        if (root.mode == null) {
            return;
        }

        Map<Key, BusLocation> newSubwayVehicles = Maps.newHashMap();
        Map<Key, BusLocation> newCommuterRailVehicles = Maps.newHashMap();
        for (Mode mode : root.mode) {
            for (Route route : mode.route) {
                String routeName;
                Schema.Routes.SourceId transitSourceId;
                if (MbtaRealtimeTransitSource.gtfsNameToRouteName.containsKey(route.route_id)) {
                    routeName = MbtaRealtimeTransitSource.gtfsNameToRouteName.get(route.route_id);
                    transitSourceId = MbtaRealtimeTransitSource.routeNameToTransitSource.get(routeName);
                }
                else {
                    // this is weird because if we get a route id we would have requested it
                    LogUtil.i("Route id not found: " + route.route_id);
                    continue;
                }

                for (com.schneeloch.bostonbusmap_library.parser.gson.Direction direction : route.direction) {
                    String directionId = direction.direction_name;

                    for (Trip trip : direction.trip) {
                        String tripHeadsign = trip.trip_headsign;

                        if (trip.vehicle != null) {
                            String id = trip.vehicle.vehicle_id;
                            if (trip.vehicle.vehicle_lat == null || trip.vehicle.vehicle_lon == null) {
                                continue;
                            }
                            float latitude = Float.parseFloat(trip.vehicle.vehicle_lat);
                            float longitude = Float.parseFloat(trip.vehicle.vehicle_lon);
                            Optional<Integer> bearing;
                            if (trip.vehicle.vehicle_bearing != null) {
                                bearing = Optional.of(Integer.parseInt(trip.vehicle.vehicle_bearing));
                            }
                            else {
                                bearing = Optional.absent();
                            }

                            VehicleLocations.Key key = new VehicleLocations.Key(transitSourceId, routeName, id);

                            Direction directionObj = new Direction(tripHeadsign, directionId, routeName, true);
                            String newDirectionId = directionId + "_" + tripHeadsign;
                            directionsObj.add(newDirectionId, directionObj);

                            BusLocation location = transitSource.createVehicleLocation(latitude,
                                    longitude, id, lastFeedUpdateInMillis, bearing, routeName, tripHeadsign);

                            if (transitSourceId == Schema.Routes.SourceId.CommuterRail) {
                                newCommuterRailVehicles.put(key, location);
                            }
                            else {
                                newSubwayVehicles.put(key, location);
                            }
                        }
                    }
                }
            }
        }

        busMapping.update(Schema.Routes.SourceId.CommuterRail, routeNames, false, newCommuterRailVehicles);
        busMapping.update(Schema.Routes.SourceId.Subway, routeNames, false, newSubwayVehicles);
	}
}
