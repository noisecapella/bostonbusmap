package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.data.VehicleLocations;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.parser.HeavyRailPredictionsFeedParser;
import boston.Bus.Map.parser.MbtaRealtimePredictionsParser;
import boston.Bus.Map.parser.MbtaRealtimeVehicleParser;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;

public class MbtaRealtimeTransitSource implements TransitSource {
	private static final String dataUrlPrefix = "http://realtime.mbta.com/developer/api/v2/";
	private static final String apiKey = "gmozilm-CkSCh8CE53wvsw";

	private final TransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;
	
	public static final ImmutableMap<String, String> gtfsNameToRouteName;
	public static final ImmutableMultimap<String, String> routeNameToGtfsName;
	public static final ImmutableMap<String, Integer> routeNameToTransitSource;

	public MbtaRealtimeTransitSource(TransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem) {
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
		
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
		routeToTransitSourceIdBuilder.put(greenRoute, Schema.Routes.enumagencyidSubway);
		
		ImmutableMultimap.Builder<String, String> gtfsNameToRouteNameBuilder =
				ImmutableMultimap.builder();
		
		ImmutableBiMap.Builder<String, String> routeIdMapBuilder =
				ImmutableBiMap.builder();
		routeIdMapBuilder.put("810_", greenRoute);
		routeIdMapBuilder.put("813_", greenRoute);
		routeIdMapBuilder.put("823_", greenRoute);
		routeIdMapBuilder.put("830_", greenRoute);
		routeIdMapBuilder.put("831_", greenRoute);
		routeIdMapBuilder.put("840_", greenRoute);
		routeIdMapBuilder.put("842_", greenRoute);
		routeIdMapBuilder.put("851_", greenRoute);
		routeIdMapBuilder.put("852_", greenRoute);
		routeIdMapBuilder.put("880_", greenRoute);
		routeIdMapBuilder.put("882_", greenRoute);

		routeIdMapBuilder.put("931_", redRoute);
		routeIdMapBuilder.put("933_", redRoute);

		routeIdMapBuilder.put("903_", orangeRoute);
		routeIdMapBuilder.put("913_", orangeRoute);
		
		routeIdMapBuilder.put("946_", blueRoute);
		routeIdMapBuilder.put("948_", blueRoute);

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

		for (String commuterRailRoute : commuterRailRoutes) {
			routeIdMapBuilder.put(commuterRailRoute, commuterRailRoute);
			routeToTransitSourceIdBuilder.put(commuterRailRoute, Schema.Routes.enumagencyidCommuterRail);
		}

		gtfsNameToRouteName = routeIdMapBuilder.build();
		
		for (String routeName : gtfsNameToRouteName.keySet()) {
			String gtfsName = gtfsNameToRouteName.get(routeName);
			gtfsNameToRouteNameBuilder.put(gtfsName, routeName);
		}
		
		routeNameToGtfsName = gtfsNameToRouteNameBuilder.build();
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
		ImmutableMap<String, RouteConfig> routeConfigs;
		if (selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ONE ||
				selectedBusPredictions == Selection.Mode.BUS_PREDICTIONS_ONE) {
			for (String gtfsRoute : routeNameToGtfsName.get(routeConfig.getRouteName())) {
				routesInUrl.add(gtfsRoute);
			}
			routeConfigs = ImmutableMap.of(routeConfig.getRouteName(), routeConfig);
		}
		else {
			routesInUrl.addAll(routeNameToGtfsName.values());
			
			ImmutableMap.Builder<String, RouteConfig> builder = 
				ImmutableMap.builder();
			
			for (String routeName : routeNameToTransitSource.keySet()) {
				RouteConfig aRouteConfig = routePool.get(routeName);
				builder.put(aRouteConfig.getRouteName(), aRouteConfig);
			}
			routeConfigs = builder.build();
		}
		
		String routesString = Joiner.on(",").join(routesInUrl);
		 
		String url;
		if (selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
				selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ONE)
		{
			url = dataUrlPrefix + "vehiclesbyroutes?api_key=" + apiKey + "&format=json&routes=" + routesString;
		}
		else {
			url = dataUrlPrefix + "predictionsbyroutes?api_key=" + apiKey + "&format=json&routes=" + routesString;
		}
		
		DownloadHelper downloadHelper = new DownloadHelper(url);
			
		downloadHelper.connect();
			
		InputStream stream = downloadHelper.getResponseData();
		InputStreamReader data = new InputStreamReader(stream);

		if (selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
				selectedBusPredictions == Selection.Mode.VEHICLE_LOCATIONS_ONE)
		{
			MbtaRealtimeVehicleParser parser = new MbtaRealtimeVehicleParser(routeTitles, busMapping, directions);
			parser.runParse(data);
		}
		else {
			MbtaRealtimePredictionsParser parser = new MbtaRealtimePredictionsParser(routeConfigs, routeTitles);
			parser.runParse(data);
		}
		
		data.close();
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
	public TransitDrawables getDrawables() {
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
