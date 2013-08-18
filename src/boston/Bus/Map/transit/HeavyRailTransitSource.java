package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.parser.HeavyRailPredictionsFeedParser;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.SearchHelper;

public class HeavyRailTransitSource implements TransitSource {

	private static final String dataUrlPrefix = "http://developer.mbta.com/lib/rthr/";

	private static final String predictionsUrlSuffix = ".json";
	
	private final TransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;

	public HeavyRailTransitSource(TransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem) {
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
	}
	
	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		int selectedBusPredictions = selection.getMode();
		if (selectedBusPredictions == Selection.VEHICLE_LOCATIONS_ALL)
		{
			//for now I'm only refreshing data for buses if this is checked
			return;
		}
		
		List<RefreshData> outputData = Lists.newArrayList();
		switch (selectedBusPredictions)
		{
		case  Selection.BUS_PREDICTIONS_ONE:
		case Selection.VEHICLE_LOCATIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

			//ok, do predictions now
			getPredictionsUrl(locations, maxStops, routeConfig.getRouteName(), outputData, selectedBusPredictions);
			break;
		}
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);
			
			getPredictionsUrl(locations, maxStops, null, outputData, selectedBusPredictions);

		}
		break;

		}

		for (RefreshData outputRow : outputData)
		{
			String url = outputRow.url;
			DownloadHelper downloadHelper = new DownloadHelper(url);
			
			downloadHelper.connect();
			

			InputStream stream = downloadHelper.getResponseData();
			InputStreamReader data = new InputStreamReader(stream);
			//StringReader data = new StringReader(hardcodedData);

			//bus prediction

			String route = outputRow.route;
			RouteConfig railRouteConfig = routePool.get(route);
			HeavyRailPredictionsFeedParser parser = new HeavyRailPredictionsFeedParser(railRouteConfig, directions,
					busMapping, locationsObj.getRouteTitles());

			parser.runParse(data);
			data.close();
		}
		
		for (RefreshData outputRow : outputData)
		{
			String route = outputRow.route;
			RouteConfig railRouteConfig = routePool.get(route);

		}
		
	}

	private static class RefreshData {
		private final String url;
		private final String route;
		
		public RefreshData(String url, String route) {
			this.url = url;
			this.route = route;
		}
	}
	
	private boolean isSubwayRoute(String name) {
		for (String route : routeTitles.routeTags()) {
			if (name.equals(route)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean containsRoute(String route, List<RefreshData> outputData) {
		boolean containsRoute = false;
		for (RefreshData row : outputData) {
			if (row.route.equals(route)) {
				containsRoute = true;
				break;
			}
		}
		return containsRoute;
	}

	private void getPredictionsUrl(List<Location> locations, int maxStops,
			String routeName, List<RefreshData> outputData, int mode)
	{
		//http://developer.mbta.com/lib/RTCR/RailLine_1.csv
		
		//BUS_PREDICTIONS_ONE or VEHICLE_LOCATIONS_ONE
		if (routeName != null)
		{
			//we know we're updating only one route
			if (isSubwayRoute(routeName))
			{
				// this is kind of a hack, but it happens to work for subway routes
				String url = dataUrlPrefix + routeName + predictionsUrlSuffix;
				
				outputData.add(new RefreshData(url, routeName));
				return;
			}
		}
		else
		{
			if (mode == Selection.BUS_PREDICTIONS_STAR)
			{
				//ok, let's look at the locations and see what we can get
				for (Location location : locations)
				{
					if (location instanceof StopLocation)
					{
						StopLocation stopLocation = (StopLocation)location;


						for (String route : stopLocation.getRoutes())
						{
							if (isSubwayRoute(route) && containsRoute(route, outputData) == false)
							{
								// this is kind of a hack, but it happens to work for subway routes
								String url = dataUrlPrefix + route + predictionsUrlSuffix;
								outputData.add(new RefreshData(url, route));
							}
						}
					}
					else if (location instanceof BusLocation)
					{
						//bus location
						BusLocation busLocation = (BusLocation)location;
						String route = busLocation.getRouteId();

						if (isSubwayRoute(route) && containsRoute(route, outputData) == false)
						{
							// this is kind of a hack, but it happens to work for subway routes
							String url = dataUrlPrefix + route + predictionsUrlSuffix;
							outputData.add(new RefreshData(url, route));
						}
					}
				}
			}
			else
			{
				//add all of them
				
				for (String routeKey : routeTitles.routeTags())
				{
					String url = dataUrlPrefix + routeKey + predictionsUrlSuffix;
					
					outputData.add(new RefreshData(url, routeKey));
				}
			}
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
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle,
			String route) {
		SubwayStopLocation stop = new SubwayStopLocation.SubwayBuilder(latitude,
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
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidSubway;
	}

	@Override
	public boolean requiresSubwayTable() {
		return true;
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
