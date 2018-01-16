package com.schneeloch.bostonbusmap_library.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteStopPair;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TimePrediction;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.database.Schema;
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
import com.schneeloch.bostonbusmap_library.parser.apiv3.VehicleAttributes;
import com.schneeloch.bostonbusmap_library.transit.MbtaV3TransitSource;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

/**
 * Created by schneg on 12/15/17.
 */

public class MbtaV3PredictionsParser {
    public static void runParse(RouteTitles routeTitles, TransitSourceCache cache, RoutePool routePool, ImmutableList<ImmutableList<Location>> groups, InputStream data) throws IOException, ParseException {
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

    private static ImmutableList<PredictionStopLocationPair> parseTree(RoutePool routePool, RouteTitles routeTitles, Root root) throws IOException, ParseException {
        ImmutableList.Builder<PredictionStopLocationPair> builder = ImmutableList.builder();

        Map<String, TripAttributes> trips = Maps.newHashMap();
        Map<String, VehicleAttributes> vehicles = Maps.newHashMap();
        if (root.included == null) {
            LogUtil.w("Strange, predictions list is empty");
            return ImmutableList.of();
        }

        for (Resource resource : root.included) {
            if (resource.tripAttributes != null) {
                trips.put(resource.id, resource.tripAttributes);
            }
            if (resource.vehicleAttributes != null) {
                vehicles.put(resource.id, resource.vehicleAttributes);
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

            if (relationships.route == null) {
                LogUtil.w("Unable to find route for " + id);
                continue;
            }

            if (relationships.stop == null) {
                LogUtil.w("Unable to find stop for " + id);
                continue;
            }

            String routeId = MbtaV3TransitSource.translateRoute(relationships.route.id);
            Schema.Routes.SourceId sourceId = routeTitles.getTransitSourceId(routeId);
            String stopId = relationships.stop.id;
            String vehicleId = relationships.vehicle != null ? relationships.vehicle.id : null;
            String tripId = relationships.trip != null ? relationships.trip.id : null;
            StopLocation location = routePool.get(routeId).getStop(stopId);

            long arrivalTimeMillis, departureTimeMillis;
            if (attributes.arrival_time != null) {
                arrivalTimeMillis = attributes.arrival_time.millis;
                if (attributes.departure_time != null) {
                    departureTimeMillis = attributes.departure_time.millis;
                } else {
                    departureTimeMillis = arrivalTimeMillis;
                }
            } else {
                if (attributes.departure_time != null) {
                    departureTimeMillis = attributes.departure_time.millis;
                    arrivalTimeMillis = departureTimeMillis;
                } else {
                    LogUtil.w("Stop " + id + " has a null arrival and departure time");
                    continue;
                }
            }


            TripAttributes tripAttributes = trips.get(tripId);
            String headsign = null;
            String block = null;
            int delay = 0;
            if (tripAttributes != null) {
                headsign = tripAttributes.headsign;
                block = tripAttributes.block_id;

                if (tripAttributes.departure_time != null && attributes.departure_time != null) {
                    delay = (int)((attributes.departure_time.millis - tripAttributes.departure_time.millis) / 1000);
                }
            }

            String vehicleLabel = vehicleId;
            VehicleAttributes vehicleAttributes = vehicles.get(vehicleId);
            if (vehicleAttributes != null) {
                vehicleLabel = vehicleAttributes.label;
            }

            TimePrediction prediction = new TimePrediction(
                    arrivalTimeMillis,
                    departureTimeMillis,
                    vehicleId,
                    vehicleLabel,
                    headsign,
                    routeId,
                    routeTitles.getTitle(routeId),
                    arrivalTimeMillis != departureTimeMillis,
                    false, // TODO
                    delay,
                    block,
                    stopId,
                    sourceId
            );

            PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, location);
            builder.add(pair);
        }
        return builder.build();
    }
}
