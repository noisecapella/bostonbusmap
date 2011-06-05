package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.parser.SubwayRouteConfigFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;

public class SubwayTransitSource implements TransitSource {
	private final Drawable busStop;
	private final Drawable railArrow;
	private final Drawable rail;
	
	public SubwayTransitSource(Drawable busStop, Drawable rail, Drawable railArrow)
	{
		this.busStop = busStop;
		this.railArrow = railArrow;
		this.rail = rail;
		
		for (String route : subwayRoutes)
		{
			subwayRouteKeysToTitles.put(route, route);
		}
	}
	
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions, UpdateAsyncTask task)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		
		//this will probably never be executed
		final String urlString = getRouteConfigUrl();

		DownloadHelper downloadHelper = new DownloadHelper(urlString);
		
		downloadHelper.connect();
		//just initialize the route and then end for this round
		
		SubwayRouteConfigFeedParser parser = new SubwayRouteConfigFeedParser(busStop,
				directions, oldRouteConfig, this);

		parser.runParse(downloadHelper.getResponseData()); 

		parser.writeToDatabase(routeMapping, false, task);

	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude, ConcurrentHashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		
		Log.v("BostonBusMap", "refreshing subway data");
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

		Log.v("BostonBusMap", "refreshing subway data for " + outputRoutes.size() + " routes");

		for (String route : outputRoutes)
		{
			String url = getPredictionsUrl(route);
			DownloadHelper downloadHelper = new DownloadHelper(url);
			
			downloadHelper.connect();

			InputStream data = downloadHelper.getResponseData();

			//bus prediction

			SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser(route, routePool, directions, rail, railArrow, busMapping);

			parser.runParse(data);
		}
	}

	private static String getPredictionsUrl(String route)
	{
		final String dataUrlPrefix = "http://developer.mbta.com/Data/";

		return dataUrlPrefix + route + ".txt";
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

	public static boolean isSubway(String route) {
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
	
	public static final int RedColor = Color.RED;
	public static final int OrangeColor = 0xf88017; //orange isn't a built in color?
	public static final int BlueColor = Color.BLUE;
	
	private static final int[] subwayColors = new int[] {RedColor, OrangeColor, BlueColor};
	private final HashMap<String, String> subwayRouteKeysToTitles = new HashMap<String, String>();
	
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
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions,
			RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException {
		//download subway data
		
		task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Downloading subway info", null));
		final String subwayUrl = getRouteConfigUrl();
		URL url = new URL(subwayUrl);
		InputStream in = Locations.downloadStream(url, task);
		
		SubwayRouteConfigFeedParser subwayParser = new SubwayRouteConfigFeedParser(busStop, directions, null, this);
		
		subwayParser.runParse(in);
		
		subwayParser.writeToDatabase(routeMapping, false, task);
		
	}


	@Override
	public String[] getRoutes() {
		return subwayRoutes;
	}


	@Override
	public HashMap<String, String> getRouteKeysToTitles() {
		return subwayRouteKeysToTitles;
	}


	@Override
	public Drawable getBusStopDrawable() {
		return busStop;
	}


	@Override
	public StopLocation createStop(float lat, float lon, String stopTag, String title,
			int platformOrder, String branch, String route, String dirTag) {
		SubwayStopLocation stop = new SubwayStopLocation(lat, lon, busStop, stopTag, title, platformOrder, branch);
		stop.addRouteAndDirTag(route, dirTag);
		return stop;
	}
}
