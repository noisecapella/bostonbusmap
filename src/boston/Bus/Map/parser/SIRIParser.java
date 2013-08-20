package boston.Bus.Map.parser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.DistancePrediction;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.util.StringUtil;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class SIRIParser {
	protected final Directions directions;
	protected final RoutePool routePool;
	protected long lastUpdatedTime;
	protected final TransitSourceTitles routeTitles;

	protected SIRIParser(RoutePool routePool, Directions directions, TransitSourceTitles routeTitles) {
		this.routePool = routePool;
		this.directions = directions;
		this.routeTitles = routeTitles;
	}
	
	protected void parseStopInformation(JsonObject monitoredCall, String routeName,
			String routeTitle, String vehicleId, 
			String direction, List<PredictionStopLocationPair> ret) throws IOException {
		String stopTag = truncateStopId(monitoredCall.get("StopPointRef").getAsString());

		JsonObject distances = monitoredCall.get("Extensions").getAsJsonObject().get("Distances").getAsJsonObject();
		String presentableDistance = distances.get("PresentableDistance").getAsString();

		RouteConfig routeConfig = routePool.get(routeName);
		if (routeConfig != null) {
			StopLocation stop = routeConfig.getStop(stopTag);
			if (stop != null) {
				float distanceInMeters = distances.get("DistanceFromCall").getAsFloat();
				DistancePrediction prediction = new DistancePrediction(presentableDistance, vehicleId, direction,
						routeName, routeTitle, distanceInMeters);
				PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stop);
				ret.add(pair);
			}
		}
	}
	
	protected static String truncateVehicleId(String vehicleId) {
		String ret = StringUtil.trimPrefix(vehicleId, "MTA NYCT_");
		ret = StringUtil.trimPrefix(ret, "MTABC_");
		return ret;
	}
	
	protected static String truncateRouteId(String routeId) {
		String ret = StringUtil.trimPrefix(routeId, "MTA NYCT_");
		ret = StringUtil.trimPrefix(ret, "MTABC_");
		return ret;
	}
	
	protected static String truncateStopId(String siriStopId) {
		String ret = StringUtil.trimPrefix(siriStopId, "MTA_");
		return ret;
	}
	
	protected static Date parseTime(String dateString) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		// example input:
		// 2013-08-17T12:45:11.598-04:00
		// need to convert -04:00 to -0400
		int lastIndex = dateString.lastIndexOf(':');
		if (lastIndex < 0) {
			throw new RuntimeException("Error parsing dateString " + dateString);
		}
		String newDateString = dateString.substring(0, lastIndex) + dateString.substring(lastIndex + 1);
		
		try {
			return simpleDateFormat.parse(newDateString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	protected SIRIVehicleParsingResults parseTree(JsonObject root) throws IOException {
		this.lastUpdatedTime = System.currentTimeMillis();
		
		JsonObject siri = root.get("Siri").getAsJsonObject();
		JsonObject serviceDelivery = siri.get("ServiceDelivery").getAsJsonObject();
		
		// parse alerts
		Alerts.Builder alertsBuilder = Alerts.builder();
		
		JsonArray situationExchangeDeliveryArray = serviceDelivery.get("SituationExchangeDelivery").getAsJsonArray();
		for (JsonElement situationExchangeDeliveryElement : situationExchangeDeliveryArray) {
			
			JsonObject situationExchangeDelivery = situationExchangeDeliveryElement.getAsJsonObject();
			JsonObject situations = situationExchangeDelivery.get("Situations").getAsJsonObject();
			JsonArray ptSituationElementArray = situations.get("PtSituationElement").getAsJsonArray();
			for (JsonElement ptSituationElement : ptSituationElementArray) {
				JsonObject ptSituation = ptSituationElement.getAsJsonObject();

				// TODO: this should probably use PublicationWindow->StartTime
				String creationTimeString = ptSituation.get("CreationTime").getAsString();
				Date creationTime = parseTime(creationTimeString);
				String description = ptSituation.get("Description").getAsString();
				String summary = ptSituation.get("Summary").getAsString();
				
				JsonObject affects = ptSituation.get("Affects").getAsJsonObject();
				JsonObject vehicleJourneys = affects.get("VehicleJourneys").getAsJsonObject();
				JsonArray affectedVehicleJourneyArray = vehicleJourneys.get("AffectedVehicleJourney").getAsJsonArray();
				
				Alert alert = new Alert(creationTime, summary, description, "");

				for (JsonElement affectedVehicleJourneyElement : affectedVehicleJourneyArray) {
					// TODO: support DirectionRef
					JsonObject affectedVehicleJourney = affectedVehicleJourneyElement.getAsJsonObject();
					String route = truncateRouteId(affectedVehicleJourney.get("LineRef").getAsString());

					alertsBuilder.addAlertForRoute(route, alert);
				}
			}
		}
		
		List<PredictionStopLocationPair> ret = Lists.newArrayList();
		List<BusLocation> busLocations = Lists.newArrayList();
		
		SIRIVehicleParsingResults results = new SIRIVehicleParsingResults(ret, alertsBuilder.build(), busLocations);
		parseSpecialElement(serviceDelivery, results);
		
		return results;
	}

	protected abstract void parseSpecialElement(JsonObject serviceDelivery,
			SIRIVehicleParsingResults results) throws IOException;

	protected void parseVehicleMonitoringDelivery(JsonObject monitoredVehicleJourney, 
			Date responseDate, SIRIVehicleParsingResults results) throws IOException {
		// parse vehicle information
		String vehicleId = truncateVehicleId(monitoredVehicleJourney.get("VehicleRef").getAsString());
		 

		long lastFeedUpdateInMillis = responseDate.getTime();
		long lastUpdateInMillis = lastFeedUpdateInMillis;
		float heading = monitoredVehicleJourney.get("Bearing").getAsFloat();
		String headingString = Integer.toString((int)heading);

		JsonObject vehicleLocation = monitoredVehicleJourney.get("VehicleLocation").getAsJsonObject();
		float latitude = vehicleLocation.get("Latitude").getAsFloat();
		float longitude = vehicleLocation.get("Longitude").getAsFloat();

		String routeName = monitoredVehicleJourney.get("PublishedLineName").getAsString();
		String dirTag = monitoredVehicleJourney.get("DestinationName").getAsString();
		if (!directions.hasDirection(dirTag)) {
			Direction direction = new Direction(dirTag, "", routeName, true);
			directions.add(dirTag, direction);
		}

		if (routeTitles.hasRoute(routeName)) {
			String routeTitle = routeTitles.getKey(routeName);

			BusLocation location = new BusLocation(latitude, longitude,
					vehicleId, lastFeedUpdateInMillis, lastUpdateInMillis, headingString,
					true, dirTag, routeName, directions, routeTitle);
			results.busLocations.add(location);

			String direction = monitoredVehicleJourney.get("DestinationName").getAsString();
			/*
			if (monitoredVehicleJourney.has("OnwardCalls")) {
				JsonObject onwardCalls = monitoredVehicleJourney.get("OnwardCalls").getAsJsonObject();

				if (onwardCalls.has("OnwardCall")) {
					JsonArray onwardCallArray = onwardCalls.get("OnwardCall").getAsJsonArray();
					for (JsonElement onwardCallElement : onwardCallArray) {
						JsonObject onwardCall = onwardCallElement.getAsJsonObject();
						parseStopInformation(onwardCall, routeName, routeTitle, vehicleId, 
								direction, results.pairs);

					}
				}
				// parse stop information
			}
			else */if (monitoredVehicleJourney.has("MonitoredCall")) {
				// parse stop information
				JsonObject monitoredCall = monitoredVehicleJourney.get("MonitoredCall").getAsJsonObject();

				parseStopInformation(monitoredCall, routeName, routeTitle, vehicleId, 
						direction, results.pairs);
			}
		}
	}
}
