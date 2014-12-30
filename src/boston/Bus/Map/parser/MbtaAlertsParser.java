package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;

import android.content.ContentResolver;
import android.content.Context;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.VehicleLocations;
import boston.Bus.Map.provider.DatabaseAgent;
import boston.Bus.Map.provider.ResolverWrapper;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.DownloadHelper;

public class MbtaAlertsParser implements IAlertsParser {
	private final TransitSystem transitSystem;
	private final RouteTitles routeTitles;

	/**
	 * Mapping of gtfs route id to a Nextbus route id. If key doesn't exist,
	 * there is no difference between gtfs route id and Nextbus route id
	 */
	private static final ImmutableMap<String, String> gtfsRoutes;

	static {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		
		builder.put("01", "1");
		builder.put("04", "4");
		builder.put("05", "5");
		builder.put("07", "7");
		builder.put("08", "8");
		builder.put("09", "9");
		builder.put("931_", "Red");
		builder.put("933_", "Red");
		builder.put("946_", "Blue");
		builder.put("9462", "Blue");
		builder.put("948_", "Blue");
		builder.put("9482", "Blue");
		builder.put("903_", "Orange");
		builder.put("913_", "Orange");
		
		gtfsRoutes = builder.build();
	}
	
	public MbtaAlertsParser(TransitSystem transitSystem) {
		this.transitSystem = transitSystem;
		this.routeTitles = transitSystem.getRouteKeysToTitles();
	}
	
	@Override
	public IAlerts obtainAlerts(Context context) throws IOException {
		Alerts.Builder builder = Alerts.builder();
		
		Date now = new Date();
		
		String alertsUrl = TransitSystem.ALERTS_URL;
		DownloadHelper downloadHelper = new DownloadHelper(alertsUrl);
		downloadHelper.connect();
		InputStream data = downloadHelper.getResponseData();
		
		FeedMessage message = FeedMessage.parseFrom(data);
		for (FeedEntity entity : message.getEntityList()) {
			GtfsRealtime.Alert alert = entity.getAlert();
			//TODO: handle active_period, cause, effect
			
			
			//TODO: we don't handle trip-specific alerts yet
			//TODO: currently it doesn't discriminate alerts for 
			// a stop on one route vs the same stop on another
			ImmutableList.Builder<String> stopsBuilder = ImmutableList.builder();
			List<String> routes = Lists.newArrayList();
			List<Integer> sources = Lists.newArrayList();
			List<String> commuterRailTripIds = Lists.newArrayList();
			boolean isSystemWide = false;
			for (EntitySelector selector : alert.getInformedEntityList()) {
				// this should be a logical AND inside an EntitySelector
				// and logical OR between EntitySelectors. This is a little
				// looser than that, but shouldn't cause any harm
				
				if (selector.hasTrip() && selector.getTrip().hasTripId()) {
					// this is a hack since it relies on the commuter rail
					// GTFS trip id having a similar id as the train number
					// which isn't true for subway or bus
					
					String tripId = selector.getTrip().getTripId();
					if (tripId.startsWith("CR-")) {
						String[] pieces = tripId.split("-");
						commuterRailTripIds.add(pieces[pieces.length - 1]);
					}
				}
				
				if (selector.hasStopId()) {
					String stopId = selector.getStopId();
					stopsBuilder.add(stopId);
				}
				else if (selector.hasRouteId()) {
					String gtfsRouteId = selector.getRouteId();
					String routeId = translateGtfsRoute(gtfsRouteId);
					
					routes.add(routeId);
				}
				else if (selector.hasRouteType()) {
					int routeType = selector.getRouteType();
					sources.add(routeType);
				}
				else
				{
					isSystemWide = true;
				}
			}
			ImmutableList<String> stops = stopsBuilder.build();
			
			String description = "";
			if (alert.hasDescriptionText() &&
				alert.getDescriptionText().getTranslationCount() > 0) {
					Translation translation = alert.getDescriptionText().getTranslation(0);
					description = translation.getText();
			}
			else if (alert.hasHeaderText() &&
					alert.getHeaderText().getTranslationCount() > 0) {
					Translation translation = alert.getHeaderText().getTranslation(0);
					description = translation.getText();
			}
			
			// now construct alert and add for each stop, route, and systemwide
			if (isSystemWide) {
				Alert systemWideAlert = new Alert(now, "Systemwide",
						description);
				builder.addSystemWideAlert(systemWideAlert);
			}
			for (String commuterRailTripId : commuterRailTripIds) {
				Alert commuterRailAlert = new Alert(now, "Commuter Rail Trip " + commuterRailTripId,
						description);
				builder.addAlertForCommuterRailTrip(commuterRailTripId, commuterRailAlert);
			}
			for (Integer routeType : sources) {
				TransitSource source = transitSystem.getTransitSourceByRouteType(routeType);
				if (source != null) {
					String sourceDescription = source.getDescription();
					Alert routeTypeAlert = new Alert(now, "All " + sourceDescription,
							description);
					builder.addAlertForRouteType(routeType, routeTypeAlert);
				}
			}
			for (String route : routes) {
				String routeTitle = routeTitles.getTitle(route);
				Alert routeAlert = new Alert(now, "Route " + routeTitle, description);
				builder.addAlertForRoute(route, routeAlert);
			}
			ContentResolver resolver = context.getContentResolver();
			
			ConcurrentMap<String, StopLocation> stopMapping = Maps.newConcurrentMap();
			DatabaseAgent.getStops(new ResolverWrapper(resolver), stops,
					transitSystem, stopMapping);
			for (String stop : stops) {
				String stopTitle = stop;
				StopLocation stopLocation = stopMapping.get(stop);
				if (stopLocation != null) {
					stopTitle = stopLocation.getTitle();
				}
				Alert stopAlert = new Alert(now, "Stop " + stopTitle, description);
				builder.addAlertForStop(stop, stopAlert);
			}
		}
		
		
		
		return builder.build();
	}

	/**
	 * GTFS Routes are slightly different from NextBus routes for the MBTA 
	 * @param routeId
	 * @return
	 */
	private String translateGtfsRoute(String routeId) {
		String newRoute = gtfsRoutes.get(routeId);
		if (newRoute != null) {
			return newRoute;
		}
		else
		{
			return routeId;
		}
	}

}
