package com.schneeloch.bostonbusmap_library.parser;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.transit.realtime.GtfsRealtime;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.gson.Vehicle;
import com.schneeloch.bostonbusmap_library.transit.TransitSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by georgeandroid on 9/14/15.
 */
public class GtfsRealtimeVehicleParser {

    public void parse(InputStream data, VehicleLocations busMapping, ImmutableMap<String, Schema.Routes.SourceId> routeNameToTransitSource, ImmutableMap<String, String> gtfsNameToRouteName, TransitSource transitSource, Directions directions) throws IOException {
        GtfsRealtime.FeedMessage message = GtfsRealtime.FeedMessage.parseFrom(data);

        long timestamp = message.getHeader().getTimestamp() * 1000;

        Map<VehicleLocations.Key, BusLocation> newItems = Maps.newHashMap();

        for (GtfsRealtime.FeedEntity entity : message.getEntityList()) {
            GtfsRealtime.VehiclePosition vehiclePosition = entity.getVehicle();
            if (vehiclePosition == null) {
                continue;
            }
            GtfsRealtime.TripDescriptor tripDescriptor = vehiclePosition.getTrip();
            if (tripDescriptor == null) {
                continue;
            }
            String route;
            if (gtfsNameToRouteName.containsKey(tripDescriptor.getRouteId())) {
                route = gtfsNameToRouteName.get(tripDescriptor.getRouteId());
            }
            else {
                route = tripDescriptor.getRouteId();
            }
            Schema.Routes.SourceId sourceId = routeNameToTransitSource.get(route);
            if (sourceId == null) {
                sourceId = Schema.Routes.SourceId.Bus;
            }
            GtfsRealtime.VehicleDescriptor vehicleDescriptor = vehiclePosition.getVehicle();
            if (vehicleDescriptor == null) {
                continue;
            }
            GtfsRealtime.Position position = vehiclePosition.getPosition();
            if (position == null) {
                continue;
            }
            VehicleLocations.Key key = new VehicleLocations.Key(sourceId, route, vehicleDescriptor.getId());
            newItems.put(key, transitSource.createVehicleLocation(
                    position.getLatitude(), position.getLongitude(), vehicleDescriptor.getId(),
                    timestamp, Optional.of((int)position.getBearing()), route, directions.getTitleAndName(tripDescriptor.getTripId())));
        }

        for (Map.Entry<String, Schema.Routes.SourceId> entry : routeNameToTransitSource.entrySet()) {
            busMapping.update(entry.getValue(), ImmutableSet.of(entry.getKey()), false, newItems);

        }
    }
}
