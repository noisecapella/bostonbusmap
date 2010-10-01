package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.parser.SubwayRouteConfigFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.util.DownloadHelper;

public class SubwayTransitSource implements TransitSource {
	private final Drawable busStop;
	private final Drawable arrow;
	private final Drawable bus;
	
	public SubwayTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		this.busStop = busStop;
		this.arrow = arrow;
		this.bus = bus;
		
		for (String route : subwayRoutes)
		{
			subwayRouteKeysToTitles.put(route, route);
		}
	}
	
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions)
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

		parser.writeToDatabase(routeMapping, false);

	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude, HashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		
		HashSet<String> outputUrls = new HashSet<String>();
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			//ok, do predictions now
			getPredictionsUrl(locations, maxStops, routeConfig.getRouteName(), outputUrls);

		}
		break;
		case Main.BUS_PREDICTIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);
			
			getPredictionsUrl(locations, maxStops, null, outputUrls);

		}
		break;

		case Main.VEHICLE_LOCATIONS_ONE:
		case Main.VEHICLE_LOCATIONS_ALL:
		default:
		{
			//TODO
			return;
		}
		}

		for (String url : outputUrls)
		{
			DownloadHelper downloadHelper = new DownloadHelper(url);
			
			downloadHelper.connect();

			InputStream data = downloadHelper.getResponseData();

			if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE || 
					selectedBusPredictions == Main.BUS_PREDICTIONS_ALL ||
					selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
			{
				//bus prediction

				SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser(routePool, directions);

				parser.runParse(data);
			}
			else 
			{
				//TODO
			}		
		}
	}

	private void getPredictionsUrl(List<Location> locations, int maxStops,
			String routeName, HashSet<String> outputUrls) {
		if (routeName != null)
		{
			//we know we're updating only one route
			for (String subwayRoute : subwayRoutes)
			{
				//make sure we don't make an incorrect url
				if (subwayRoute.equals(routeName))
				{
					outputUrls.add("http://developer.mbta.com/Data/" + routeName + ".json");
					return;
				}
			}
		}
		else
		{
			//ok, let's look at the locations and see what we can get
			for (Location location : locations)
			{
				StopLocation stopLocation = (StopLocation)location;
				
				for (String route : stopLocation.getRoutes())
				{
					for (String subwayRoute : subwayRoutes)
					{
						if (subwayRoute.equals(route))
						{
							outputUrls.add("http://developer.mbta.com/Data/" + route + ".json");
						}
					}
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
	public static final String[] subwayRoutes = new String[] {RedLine, OrangeLine, BlueLine};
	
	public static final String RedColor = "ff0000";
	public static final String OrangeColor = "f88017";
	public static final String BlueColor = "0000ff";
	
	private static final String[] subwayColors = new String[] {RedColor, OrangeColor, BlueColor};
	private final HashMap<String, String> subwayRouteKeysToTitles = new HashMap<String, String>();
	
	public static String[] getAllSubwayRoutes() {
		return subwayRoutes;
	}

	public static String getSubwayColor(String subwayRoute)
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
		final String subwayUrl = getRouteConfigUrl();
		URL url = new URL(subwayUrl);
		InputStream in = Locations.downloadStream(url, task, "Downloading subway info: ", "");
		
		SubwayRouteConfigFeedParser subwayParser =
			new SubwayRouteConfigFeedParser(busStop, directions, null, this);
		
		task.publish("Parsing route data...");
		
		subwayParser.runParse(in);
		
		subwayParser.writeToDatabase(routeMapping, false);
		
	}


	@Override
	public String[] getRoutes() {
		return subwayRoutes;
	}


	@Override
	public HashMap<String, String> getRouteKeysToTitles() {
		return subwayRouteKeysToTitles;
	}
}
