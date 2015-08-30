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

import com.schneeloch.bostonbusmap_library.data.CommuterRailStopLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.SubwayStopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceCache;
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
	public static final ImmutableMap<String, Schema.Routes.SourceId> routeNameToTransitSource;

    private final TransitSourceCache cache;

    private static final Schema.Routes.SourceId[] transitSourceIds = new Schema.Routes.SourceId[] {
        Schema.Routes.SourceId.Subway,
        Schema.Routes.SourceId.CommuterRail
    };

	public MbtaRealtimeTransitSource(ITransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem) {
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;

        cache = new TransitSourceCache();
	}

	static {
		String greenRoute = "Green";
		String blueRoute = "Blue";
		String orangeRoute = "Orange";
		String redRoute = "Red";

		// workaround for the quick and dirty way things are done in this app
		// TODO: fix local names to match field names
		ImmutableMap.Builder<String, Schema.Routes.SourceId> routeToTransitSourceIdBuilder =
				ImmutableMap.builder();
		
		ImmutableMap.Builder<String, String> gtfsNameToRouteNameBuilder =
				ImmutableMap.builder();
		
		gtfsNameToRouteNameBuilder.put("Green-B", greenRoute);
        gtfsNameToRouteNameBuilder.put("Green-C", greenRoute);
        gtfsNameToRouteNameBuilder.put("Green-D", greenRoute);
        gtfsNameToRouteNameBuilder.put("Green-E", greenRoute);

		gtfsNameToRouteNameBuilder.put("Red", redRoute);

		gtfsNameToRouteNameBuilder.put("Orange", orangeRoute);
		
		gtfsNameToRouteNameBuilder.put("Blue", blueRoute);

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

		routeToTransitSourceIdBuilder.put(greenRoute, Schema.Routes.SourceId.Subway);
		routeToTransitSourceIdBuilder.put(redRoute, Schema.Routes.SourceId.Subway);
		routeToTransitSourceIdBuilder.put(orangeRoute, Schema.Routes.SourceId.Subway);
		routeToTransitSourceIdBuilder.put(blueRoute, Schema.Routes.SourceId.Subway);

		/*
		TODO: disabled because this functionality is provided by CommuterRailTransitSource
		for (String commuterRailRoute : commuterRailRoutes) {
			gtfsNameToRouteNameBuilder.put(commuterRailRoute, commuterRailRoute);
			routeToTransitSourceIdBuilder.put(commuterRailRoute, Schema.Routes.SourceId.CommuterRail);
		}*/

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
		Selection.Mode mode = selection.getMode();
		List<String> routesInUrl = Lists.newArrayList();
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        ImmutableSet<String> routeNames;
        switch (mode) {
            case VEHICLE_LOCATIONS_ONE:
            case BUS_PREDICTIONS_ONE: {
                String routeName = routeConfig.getRouteName();
                if (routeNameToTransitSource.containsKey(routeName)) {
                    if (cache.canUpdateVehiclesForRoute(routeName) && cache.canUpdatePredictionForRoute(routeName)) {
                        builder.add(routeName);
                    }
                }

                break;
            }
            case VEHICLE_LOCATIONS_ALL:
            case BUS_PREDICTIONS_ALL:
            case BUS_PREDICTIONS_STAR: {
                for (String routeName : routeNameToTransitSource.keySet()) {
                    if (cache.canUpdateVehiclesForRoute(routeName) && cache.canUpdatePredictionForRoute(routeName)) {
                        builder.add(routeName);
                    }
                }
                break;
            }
            default:
                throw new RuntimeException("Unexpected mode");
        }
        routeNames = builder.build();

        if (routeNames.size() == 0) {
            return;
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
        try {
            InputStream vehicleStream = vehiclesDownloadHelper.getResponseData();
            InputStreamReader vehicleData = new InputStreamReader(vehicleStream);

            MbtaRealtimeVehicleParser vehicleParser = new MbtaRealtimeVehicleParser(routeTitles, busMapping, directions, routeNames);
            vehicleParser.runParse(vehicleData);
        }
        finally {
            vehiclesDownloadHelper.disconnect();
        }
		
		DownloadHelper predictionsDownloadHelper = new DownloadHelper(predictionsUrl);
        try {
            InputStream predictionsStream = predictionsDownloadHelper.getResponseData();
            InputStreamReader predictionsData = new InputStreamReader(predictionsStream);

            MbtaRealtimePredictionsParser parser = new MbtaRealtimePredictionsParser(routeNames, routePool, routeTitles);
            parser.runParse(predictionsData);

            predictionsData.close();
        }
        finally {
            predictionsDownloadHelper.disconnect();
        }

        for (String route : routeNames) {
            cache.updatePredictionForRoute(route);
            cache.updateVehiclesForRoute(route);
        }
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
        Schema.Routes.SourceId transitSourceId = routeNameToTransitSource.get(route);
        StopLocation stop;
        if (transitSourceId == Schema.Routes.SourceId.Subway) {
            stop = new SubwayStopLocation.SubwayBuilder(latitude,
                    longitude, stopTag, stopTitle).build();
        }
        else if (transitSourceId == Schema.Routes.SourceId.CommuterRail) {
            stop = new CommuterRailStopLocation.CommuterRailBuilder(latitude, longitude, stopTag, stopTitle).build();
        }
        else {
            throw new RuntimeException("Unexpected transit source " + transitSourceId);
        }
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
	public Schema.Routes.SourceId[] getTransitSourceIds() {
		return transitSourceIds;
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
