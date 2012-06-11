package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Color;
import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.prepopulated.SubwayPrepopulatedData;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.AlertParser;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;

public class SubwayTransitSource implements TransitSource {
	private static String predictionsUrlSuffix = ".txt";
	private final TransitDrawables drawables;
	
	public SubwayTransitSource(TransitDrawables drawables, AlertsMapping alertsMapping)
	{
		this.drawables = drawables;
		
		alertKeys = alertsMapping.getAlertNumbers(subwayRoutes, subwayRouteKeysToTitles);
		for (String route : subwayRoutes)
		{
			subwayRouteKeysToTitles.put(route, route);

		}
	}
	
	
	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude, ConcurrentHashMap<String, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool,
			Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		Directions directions = routePool.getDirections();
		//read data from the URL
		if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ALL)
		{
			//for now I'm only refreshing data for buses if this is checked
			return;
		}
		
		
		HashSet<String> outputRoutes = new HashSet<String>();
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		case Main.VEHICLE_LOCATIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			//ok, do predictions now
			getPredictionsRoutes(locations, maxStops, routeConfig.getRouteName(), outputRoutes, selectedBusPredictions);
			break;
		}
		case Main.BUS_PREDICTIONS_ALL:
		case Main.VEHICLE_LOCATIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);
			
			getPredictionsRoutes(locations, maxStops, null, outputRoutes, selectedBusPredictions);

		}
		break;

		}

		for (String route : outputRoutes)
		{
			{
				String url = getPredictionsUrl(route);
				DownloadHelper downloadHelper = new DownloadHelper(url);

				downloadHelper.connect();

				InputStream data = downloadHelper.getResponseData();

				//bus prediction

				SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser(route, routePool, directions, drawables, busMapping, subwayRouteKeysToTitles);

				parser.runParse(data);
			}
			
			//get alerts if necessary
			int alertKey = alertKeys.get(route);
			
			RouteConfig railRouteConfig = routePool.get(route);
			if (railRouteConfig.obtainedAlerts() == false)
			{
				String url = AlertsMapping.alertUrlPrefix + alertKey;
				DownloadHelper downloadHelper = new DownloadHelper(url);
				downloadHelper.connect();

				InputStream stream = downloadHelper.getResponseData();
				InputStreamReader data = new InputStreamReader(stream);

				AlertParser parser = new AlertParser();
				parser.runParse(data);
				railRouteConfig.setAlerts(parser.getAlerts());
				data.close();

			}
			
		}
	}

	private static String getPredictionsUrl(String route)
	{
		final String dataUrlPrefix = "http://developer.mbta.com/Data/";

		return dataUrlPrefix + route + predictionsUrlSuffix ;
	}
	
	private static void getPredictionsRoutes(List<Location> locations, int maxStops,
			String routeName, HashSet<String> outputRoutes, int mode) {
		
		//BUS_PREDICTIONS_ONE or VEHICLE_LOCATIONS_ONE
		if (routeName != null)
		{
			//we know we're updating only one route
			if (isSubway(routeName))
			{
				outputRoutes.add(routeName);
				return;
			}
		}
		else
		{
			if (mode == Main.BUS_PREDICTIONS_STAR)
			{
				//ok, let's look at the locations and see what we can get
				for (Location location : locations)
				{
					if (location instanceof StopLocation)
					{
						StopLocation stopLocation = (StopLocation)location;


						for (String route : stopLocation.getRoutes())
						{
							if (isSubway(route))
							{
								outputRoutes.add(route);
							}
						}
					}
					else
					{
						//bus location
						BusLocation busLocation = (BusLocation)location;
						String route = busLocation.getRouteId();

						if (isSubway(route))
						{
							outputRoutes.add(route);
						}
					}
				}
			}
			else
			{
				//add all three
				for (String route : subwayRoutes)
				{
					outputRoutes.add(route);
				}
			}
		}
	}


	@Override
	public boolean hasPaths() {
		return false;
	}

	

	public static String getRouteConfigUrl() {
		return "http://developer.mbta.com/RT_Archive/RealTimeHeavyRailKeys.csv";
	}

	private static boolean isSubway(String route) {
		for (String subwayRoute : subwayRoutes)
		{
			if (subwayRoute.equals(route))
			{
				return true;
			}
		}
		return false;
	}

	public static final String RedLine = "Red";
	public static final String OrangeLine = "Orange";
	public static final String BlueLine = "Blue";
	private static final String[] subwayRoutes = new String[] {RedLine, OrangeLine, BlueLine};
	private final MyHashMap<String, Integer> alertKeys;
	
	
	public static final int RedColor = Color.RED;
	public static final int OrangeColor = 0xf88017; //orange isn't a built in color?
	public static final int BlueColor = Color.BLUE;
	
	private static final int[] subwayColors = new int[] {RedColor, OrangeColor, BlueColor};
	private final MyHashMap<String, String> subwayRouteKeysToTitles = new MyHashMap<String, String>();
	
	public static String[] getAllSubwayRoutes() {
		return subwayRoutes;
	}

	public static int getSubwayColor(String subwayRoute)
	{
		for (int i = 0; i < subwayRoutes.length; i++)
		{
			if (subwayRoutes[i].equals(subwayRoute))
			{
				return subwayColors[i];
			}
		}
		return BlueColor;
	}


	@Override
	public String[] getRoutes() {
		return subwayRoutes;
	}


	@Override
	public MyHashMap<String, String> getRouteKeysToTitles() {
		return subwayRouteKeysToTitles;
	}

	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public void bindPredictionElementsForUrl(StringBuilder urlString,
			String route, String stopTag)
	{
		//do nothing
		
	}


	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, subwayRoutes, subwayRouteKeysToTitles);
	}
	
	@Override
	public RouteConfig[] makeRoutes(Directions directions) throws IOException {
		return new SubwayPrepopulatedData(this, directions).getAllRoutes();
	}
}
