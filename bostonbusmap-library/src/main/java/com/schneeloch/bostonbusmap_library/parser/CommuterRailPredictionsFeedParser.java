package com.schneeloch.bostonbusmap_library.parser;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.CommuterRailPrediction;
import com.schneeloch.bostonbusmap_library.data.CommuterTrainLocation;
import com.schneeloch.bostonbusmap_library.data.Direction;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommuterRailPredictionsFeedParser
{
	private final RouteConfig routeConfig;
	private final Directions directions;

	private final VehicleLocations busMapping;
	private final RouteTitles routeKeysToTitles;

	/**
	 * Keep this value consistent while reading data
	 */
	private final long currentTimeMillis;

	public CommuterRailPredictionsFeedParser(RouteConfig routeConfig, Directions directions,
			VehicleLocations busMapping, RouteTitles routeKeysToTitles)
	{
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.busMapping = busMapping;
		this.routeKeysToTitles = routeKeysToTitles;

		this.currentTimeMillis = Now.getMillis();
	}

	private void clearPredictions() throws IOException
	{
		for (StopLocation stopLocation : routeConfig.getStops())
		{
			stopLocation.clearPredictions(routeConfig);
		}
	}

	public void runParse(Reader data) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(data, 2048);

		JsonElement root = new JsonParser().parse(bufferedReader);
		List<PredictionStopLocationPair> pairs = parseTree(root.getAsJsonObject());
		
		clearPredictions();

		for (PredictionStopLocationPair pair : pairs) {
			pair.stopLocation.addPrediction(pair.prediction);
		}
	}

	private List<PredictionStopLocationPair> parseTree(JsonObject root) {
		List<PredictionStopLocationPair> pairs = Lists.newArrayList();
		
		String routeName = routeConfig.getRouteName();
		String routeTitle = routeKeysToTitles.getTitle(routeName);
		long nowMillis = Now.getMillis();
		long nowSeconds = nowMillis / 1000;

        Map<VehicleLocations.Key, BusLocation> newLocations = Maps.newHashMap();
        JsonArray messages = root.get("Messages").getAsJsonArray();
		for (JsonElement element : messages) {
			JsonObject message = element.getAsJsonObject();
			String dirTag = message.get("Destination").getAsString();

			// add vehicle if exists
			
			String vehicle = message.get("Vehicle").getAsString();
			
			// not related to GTFS trip
			String trip = message.get("Trip").getAsString();
			String timestampString = message.get("TimeStamp").getAsString();
			long timestamp = Long.parseLong(timestampString);
			long timestampMillis = timestamp * 1000;

			String latenessString = message.get("Lateness").getAsString();
			int lateness;
			if (latenessString.length() != 0) {
				lateness = Integer.parseInt(latenessString);
			}
			else
			{
				lateness = 0;
			}
			if (vehicle.length() != 0) {
				String latitudeString = message.get("Latitude").getAsString();
				String longitudeString = message.get("Longitude").getAsString();
				String headingString = message.get("Heading").getAsString();
                Optional<Integer> heading;
                if (headingString == null || headingString.length() == 0) {
                    heading = Optional.absent();
                }
                else {
                    heading = Optional.of(Integer.parseInt(headingString));
                }
				
				if (longitudeString.length() != 0 && latitudeString.length() != 0) {
					float lat = Float.parseFloat(latitudeString);
					float lon = Float.parseFloat(longitudeString);

					CommuterTrainLocation location = new CommuterTrainLocation(lat,
							lon, trip, timestampMillis,
							heading, routeName, dirTag);
                    newLocations.put(new VehicleLocations.Key(Schema.Routes.SourceId.CommuterRail, routeName, trip), location);
				}
			}
			
			// handle predictions
			String flag = message.get("Flag").getAsString();
			String stopId = message.get("Stop").getAsString();
			String scheduledString = message.get("Scheduled").getAsString();
			long scheduled = Long.parseLong(scheduledString);
			if (stopId.length() != 0 && scheduledString.length() != 0) {
				StopLocation stop = routeConfig.getStop(stopId);
				if (stop != null) {
					int seconds = (int)(scheduled - nowSeconds);
					int minutes = seconds / 60;
					long arrivalTimeMillis = currentTimeMillis + minutes * 60 * 1000;


					CommuterRailPrediction prediction = new CommuterRailPrediction(arrivalTimeMillis,
							trip, dirTag, routeName,
							routeTitle, false,
							lateness > 5*60, lateness, "", stopId,
							CommuterRailPrediction.Flag.toFlagEnum(flag)
							);
					PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction,
							stop);
					pairs.add(pair);
				}
				else
				{
					LogUtil.w("Commuter rail stop missing: " + stopId);
				}
			}
		}
        busMapping.update(Schema.Routes.SourceId.CommuterRail, ImmutableSet.of(routeName), false, newLocations);

		return pairs;
	}
}
