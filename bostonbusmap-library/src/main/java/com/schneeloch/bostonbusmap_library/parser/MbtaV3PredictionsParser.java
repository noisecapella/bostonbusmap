package com.schneeloch.bostonbusmap_library.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteStopPair;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TimePrediction;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.parser.apiv3.PredictionAttributes;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Relationship;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Relationships;
import com.schneeloch.bostonbusmap_library.parser.apiv3.RelationshipDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Timestamp;
import com.schneeloch.bostonbusmap_library.parser.apiv3.TimestampDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Resource;
import com.schneeloch.bostonbusmap_library.parser.apiv3.ResourceDeserializer;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Root;
import com.schneeloch.bostonbusmap_library.parser.apiv3.TripAttributes;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Created by schneg on 12/15/17.
 */

public class MbtaV3PredictionsParser {
    public static void runParse(TransitSourceTitles routeTitles, TransitSourceCache cache, RoutePool routePool, ImmutableList<ImmutableList<Location>> groups, InputStream data) throws IOException, ParseException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data), 2048);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
        gsonBuilder.registerTypeAdapter(Resource.class, new ResourceDeserializer());
        gsonBuilder.registerTypeAdapter(Relationship.class, new RelationshipDeserializer());
        Root root = gsonBuilder.create().fromJson(bufferedReader, Root.class);

        clearPredictions(groups);

        List<PredictionStopLocationPair> predictionPairs = parseTree(routePool, routeTitles, root);
        for (PredictionStopLocationPair pair : predictionPairs) {
            pair.stopLocation.addPrediction(pair.prediction);
        }

        ImmutableList<RouteStopPair> pairs = getStopPairs(groups);
        for (RouteStopPair pair : pairs) {
            cache.updatePredictionForStop(pair);
        }
    }

    private static ImmutableList<RouteStopPair> getStopPairs(ImmutableList<ImmutableList<Location>> groups) {
        ImmutableList.Builder<RouteStopPair> builder = ImmutableList.builder();
        for (ImmutableList<Location> group : groups) {
            for (Location location : group) {
                if (location instanceof StopLocation) {
                    StopLocation stopLocation = (StopLocation)location;
                    for (String route : location.getRoutes()) {
                        builder.add(new RouteStopPair(route, stopLocation.getStopTag()));
                    }
                }
            }
        }
        return builder.build();
    }

    private static void clearPredictions(ImmutableList<ImmutableList<Location>> groups) {
        for (ImmutableList<Location> group: groups) {
            for (Location location : group) {
                if (location instanceof StopLocation) {
                    StopLocation stopLocation = (StopLocation)location;
                    stopLocation.clearPredictions(null);
                }
            }
        }
    }

    private static ImmutableList<PredictionStopLocationPair> parseTree(RoutePool routePool, TransitSourceTitles routeTitles, Root root) throws IOException, ParseException {
        ImmutableList.Builder<PredictionStopLocationPair> builder = ImmutableList.builder();

        Map<String, TripAttributes> trips = Maps.newHashMap();
        for (Resource resource : root.included) {
            if (resource.tripAttributes != null) {
                trips.put(resource.id, resource.tripAttributes);
            }
        }

        for (Resource resource : root.data) {
            String id = resource.id;
            PredictionAttributes attributes = resource.predictionAttributes;
            if (attributes == null) {
                LogUtil.w("Expecting prediction but got " + resource.type);
                continue;
            }

            Relationships relationships = resource.relationships;
            if (relationships == null) {
                LogUtil.w("Expecting relationships for prediction " + id);
                continue;
            }

            String stopId = relationships.stop.id;
            String routeId = relationships.route.id;
            String vehicleId = relationships.vehicle.id;
            String tripId = relationships.trip.id;
            StopLocation location = routePool.get(routeId).getStop(stopId);

            long arrivalTimeMillis = attributes.arrival_time.millis;
            // TODO: departure time

            TripAttributes tripAttributes = trips.get(tripId);
            String headsign = null;
            String block = null;
            if (tripAttributes != null) {
                headsign = tripAttributes.headsign;
                block = tripAttributes.block_id;
            }

            TimePrediction prediction = new TimePrediction(
                    arrivalTimeMillis,
                    vehicleId,
                    headsign,
                    routeId,
                    routeTitles.getTitle(routeId),
                    false, // TODO
                    false, // TODO
                    0, // TODO
                    block,
                    stopId
            );

            PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, location);
            builder.add(pair);
        }
        return builder.build();
    }
}
