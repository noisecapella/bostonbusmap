package com.schneeloch.bostonbusmap_library.parser;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Relationship;
import com.schneeloch.bostonbusmap_library.parser.apiv3.RelationshipDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Relationships;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Resource;
import com.schneeloch.bostonbusmap_library.parser.apiv3.ResourceDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Root;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Timestamp;
import com.schneeloch.bostonbusmap_library.parser.apiv3.TimestampDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.TripAttributes;
import com.schneeloch.bostonbusmap_library.parser.apiv3.VehicleAttributes;
import com.schneeloch.bostonbusmap_library.transit.MbtaV3TransitSource;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.transform.Source;

/**
 * Created by schneg on 12/15/17.
 */

public class MbtaV3VehiclesParser {
    public static Map<VehicleLocations.Key, BusLocation> runParse(InputStream data, RouteTitles routeTitles) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data), 2048);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
        gsonBuilder.registerTypeAdapter(Resource.class, new ResourceDeserializer());
        gsonBuilder.registerTypeAdapter(Relationship.class, new RelationshipDeserializer());
        Root root = gsonBuilder.create().fromJson(bufferedReader, Root.class);

        Map<VehicleLocations.Key, BusLocation> vehicles = Maps.newHashMap();
        Map<String, TripAttributes> trips = Maps.newHashMap();

        if (root.included == null) {
            LogUtil.w("Strange, included is empty");
            return Maps.newHashMap();
        }

        for (Resource resource : root.included) {
            if (resource.tripAttributes != null) {
                trips.put(resource.id, resource.tripAttributes);
            }
        }

        for (Resource resource : root.data) {
            if (!resource.type.equals("vehicle")) {
                LogUtil.w("Vehicle feed should only contain vehicles");
                continue;
            }
            String id = resource.id;

            VehicleAttributes attributes = resource.vehicleAttributes;
            if (attributes == null) {
                LogUtil.w("Missing attributes in vehicle " + id);
                continue;
            }

            Relationships relationships = resource.relationships;
            if (relationships == null) {
                LogUtil.w("Missing relationship for vehicle " + id);
                continue;
            }

            String tripId = null, routeId;
            if (relationships.route == null) {
                LogUtil.w("Vehicle " + id + " has no route");
                continue;
            }
            routeId = MbtaV3TransitSource.translateRoute(relationships.route.id);

            if (relationships.trip != null) {
                tripId = resource.relationships.trip.id;
            }
            TripAttributes trip = trips.get(tripId);
            Schema.Routes.SourceId sourceId = routeTitles.getTransitSourceId(routeId);

            if (sourceId == null) {
                LogUtil.w("Route " + routeId + " has no source id");
                continue;
            }

            VehicleLocations.Key key = new VehicleLocations.Key(
                    sourceId,
                    routeId,
                    id
            );

            BusLocation busLocation = new BusLocation(
                    attributes.latitude,
                    attributes.longitude,
                    id,
                    attributes.last_updated.millis,
                    Optional.of((int) attributes.bearing),
                    routeId,
                    trip != null ? trip.headsign : null,
                    sourceId
            );
            vehicles.put(key, busLocation);
        }
        return vehicles;
    }
}
