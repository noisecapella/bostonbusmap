package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.MbtaRealtimePredictionsParser;
import com.schneeloch.bostonbusmap_library.parser.MbtaRealtimeVehicleParser;
import com.schneeloch.bostonbusmap_library.util.DownloadHelper;
import com.schneeloch.bostonbusmap_library.util.SearchHelper;

public class MbtaRealtimeTransitSource implements TransitSource {
	private static final String dataUrlPrefix = "http://realtime.mbta.com/developer/api/v2/";
	private static final String apiKey = "gmozilm-CkSCh8CE53wvsw";

	private final ITransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final ITransitSystem transitSystem;
	
	public static final ImmutableMap<String, String> gtfsNameToRouteName;
	public static final ImmutableMultimap<String, String> routeNameToGtfsName;
	public static final ImmutableMap<String, Integer> routeNameToTransitSource;

    private final ConcurrentHashMap<String, Long> lastUpdates;

    private static final long fetchDelay = 15000;

	public MbtaRealtimeTransitSource(ITransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem) {
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;

        lastUpdates = new ConcurrentHashMap<>();
	}

	static {
		String greenRoute = "Green";
		String blueRoute = "Blue";
		String orangeRoute = "Orange";
		String redRoute = "Red";

		// workaround for the quick and dirty way things are done in this app
		// TODO: fix local names to match field names
		ImmutableMap.Builder<String, Integer> routeToTransitSourceIdBuilder =
				ImmutableMap.builder();
		
		ImmutableMap.Builder<String, String> gtfsNameToRouteNameBuilder =
				ImmutableMap.builder();
		
		gtfsNameToRouteNameBuilder.put("810_", greenRoute);
		gtfsNameToRouteNameBuilder.put("813_", greenRoute);
		gtfsNameToRouteNameBuilder.put("823_", greenRoute);
		gtfsNameToRouteNameBuilder.put("830_", greenRoute);
		gtfsNameToRouteNameBuilder.put("831_", greenRoute);
		gtfsNameToRouteNameBuilder.put("840_", greenRoute);
		gtfsNameToRouteNameBuilder.put("842_", greenRoute);
		gtfsNameToRouteNameBuilder.put("851_", greenRoute);
		gtfsNameToRouteNameBuilder.put("852_", greenRoute);
		gtfsNameToRouteNameBuilder.put("880_", greenRoute);
		gtfsNameToRouteNameBuilder.put("882_", greenRoute);

		gtfsNameToRouteNameBuilder.put("931_", redRoute);
		gtfsNameToRouteNameBuilder.put("933_", redRoute);

		gtfsNameToRouteNameBuilder.put("903_", orangeRoute);
		gtfsNameToRouteNameBuilder.put("913_", orangeRoute);
		
		gtfsNameToRouteNameBuilder.put("946_", blueRoute);
		gtfsNameToRouteNameBuilder.put("948_", blueRoute);

		String[] commuterRailRoutes = new String[] {
				"CR-Greenbush",
				"CR-Kingston",
				"CR-Middleborough",
				"CR-Fairmount",
				"CR-Providence",
				"CR-Franklin",
				"CR-Needham",
				"CR-Worcester",
				"CR-Fitchburg",
				"CR-Lowell",
				"CR-Haverhill",
				"CR-Newburyport"
		};

		routeToTransitSourceIdBuilder.put(greenRoute, Schema.Routes.enumagencyidSubway);
		routeToTransitSourceIdBuilder.put(redRoute, Schema.Routes.enumagencyidSubway);
		routeToTransitSourceIdBuilder.put(orangeRoute, Schema.Routes.enumagencyidSubway);
		routeToTransitSourceIdBuilder.put(blueRoute, Schema.Routes.enumagencyidSubway);
		
		for (String commuterRailRoute : commuterRailRoutes) {
			gtfsNameToRouteNameBuilder.put(commuterRailRoute, commuterRailRoute);
			routeToTransitSourceIdBuilder.put(commuterRailRoute, Schema.Routes.enumagencyidCommuterRail);
		}

		gtfsNameToRouteName = gtfsNameToRouteNameBuilder.build();
		
		ImmutableMultimap.Builder<String, String> routeNameToGtfsNameBuilder =
				ImmutableMultimap.builder();
		for (String routeName : gtfsNameToRouteName.keySet()) {
			String gtfsName = gtfsNameToRouteName.get(routeName);
			routeNameToGtfsNameBuilder.put(gtfsName, routeName);
		}
		
		routeNameToGtfsName = routeNameToGtfsNameBuilder.build();
		routeNameToTransitSource = routeToTransitSourceIdBuilder.build();
	}
	
	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			VehicleLocations busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		Selection.Mode selectedBusPredictions = selection.getMode();
		List<String> routesInUrl = Lists.newArrayList();
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        long currentMillis = System.currentTimeMillis();

        ImmutableSet<String> routeNames;
        if (selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ONE ||
				selectedBusPredictions == Selection.Mode.BUS_PREDICTIONS_ONE) {
            Long lastUpdate = lastUpdates.get(routeConfig.getRouteName());
            if (lastUpdate == null || lastUpdate + fetchDelay < currentMillis) {
                builder.add(routeConfig.getRouteName());
            }
		}
		else {
			for (String routeName : routeNameToTransitSource.keySet()) {
                Long lastUpdate = lastUpdates.get(routeConfig.getRouteName());
                if (lastUpdate == null || lastUpdate + fetchDelay < currentMillis) {
                    builder.add(routeName);
                }
			}
		}
        routeNames = builder.build();

        if (routeNames.size() == 0) {
            return;
        }

        for (String routeName : routeNames) {
            lastUpdates.put(routeName, currentMillis);
        }

        for (String routeName : routeNames) {
            for (String gtfsRoute : routeNameToGtfsName.get(routeName)) {
                routesInUrl.add(gtfsRoute);
            }
        }

        String routesString = Joiner.on(",").join(routesInUrl);
		 
		String vehiclesUrl = dataUrlPrefix + "vehiclesbyroutes?api_key=" + apiKey + "&format=json&routes=" + routesString;
		String predictionsUrl = dataUrlPrefix + "predictionsbyroutes?api_key=" + apiKey + "&format=json&include_service_alerts=false&routes=" + routesString;

		DownloadHelper vehiclesDownloadHelper = new DownloadHelper(vehiclesUrl);
			
		vehiclesDownloadHelper.connect();
			
		InputStream vehicleStream = vehiclesDownloadHelper.getResponseData();
		InputStreamReader vehicleData = new InputStreamReader(vehicleStream);

		MbtaRealtimeVehicleParser vehicleParser = new MbtaRealtimeVehicleParser(routeTitles, busMapping, directions, routeNames);
		vehicleParser.runParse(vehicleData);
		
		DownloadHelper predictionsDownloadHelper = new DownloadHelper(predictionsUrl);
		
		predictionsDownloadHelper.connect();
			
		InputStream predictionsStream = predictionsDownloadHelper.getResponseData();
		InputStreamReader predictionsData = new InputStreamReader(predictionsStream);

		MbtaRealtimePredictionsParser parser = new MbtaRealtimePredictionsParser(routeNames, routePool, routeTitles);
		parser.runParse(predictionsData);
		
		predictionsData.close();
	}

	@Override
	public boolean hasPaths() {
		return true;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routeTitles);
	}

	@Override
	public ITransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle, String route) {
		StopLocation stop = new StopLocation.Builder(latitude,
				longitude, stopTag, stopTitle).build();
		stop.addRoute(route);
		return stop;

	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public int getLoadOrder() {
		return 2;
	}

	@Override
	public int[] getTransitSourceIds() {
		return new int[] {
				Schema.Routes.enumagencyidSubway,
				Schema.Routes.enumagencyidCommuterRail
		};
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

	@Override
	public IAlerts getAlerts() {
		return transitSystem.getAlerts();
	}

	@Override
	public String getDescription() {
		return "Subway";
	}

}
