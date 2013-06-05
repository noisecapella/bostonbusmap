package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;

import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.DownloadHelper;

public class MbtaAlertsParser {
	private final TransitSystem transitSystem;
	private final RouteTitles routeTitles;
	
	public MbtaAlertsParser(TransitSystem transitSystem) {
		this.transitSystem = transitSystem;
		this.routeTitles = transitSystem.getRouteKeysToTitles();
	}
	
	public Alerts obtainAlerts() throws IOException {
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
			//TODO: we don't handle route_type at all
			//TODO: currently it doesn't discriminate alerts for 
			// a stop on one route vs the same stop on another
			List<String> stops = Lists.newArrayList();
			List<String> routes = Lists.newArrayList();
			List<Integer> sources = Lists.newArrayList();
			boolean isSystemWide = false;
			for (EntitySelector selector : alert.getInformedEntityList()) {
				if (selector.hasStopId()) {
					String stopId = selector.getStopId();
					stops.add(stopId);
				}
				else if (selector.hasRouteId()) {
					String routeId = selector.getRouteId();
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
			
			String description = "";
			TranslatedString headerText = alert.getHeaderText();
			if (headerText.getTranslationCount() > 0) {
				Translation translation = headerText.getTranslation(0);
				description = translation.getText();
			}
			
			// now construct alert and add for each stop, route, and systemwide
			if (isSystemWide) {
				Alert systemWideAlert = new Alert(now, "Systemwide",
						description, "");
				builder.addSystemWideAlert(systemWideAlert);
			}
			for (Integer routeType : sources) {
				TransitSource source = transitSystem.getTransitSourceByRouteType(routeType);
				if (source != null) {
					String sourceDescription = source.getDescription();
					Alert routeTypeAlert = new Alert(now, "All " + sourceDescription,
							description, "");
					builder.addAlertForRouteType(routeType, routeTypeAlert);
				}
			}
			for (String route : routes) {
				String routeTitle = routeTitles.getTitle(route);
				Alert routeAlert = new Alert(now, "Route " + routeTitle, description, "");
				builder.addAlertForRoute(route, routeAlert);
			}
			for (String stop : stops) {
				Alert stopAlert = new Alert(now, "Stop " + stop, description, "");
				builder.addAlertForStop(stop, stopAlert);
			}
		}
		
		
		
		return builder.build();
	}

}
