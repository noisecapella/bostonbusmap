package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.SubwayRouteConfigFeedParser;

public class SubwayTransitSource implements TransitSource {
	private final Drawable busStop;
	
	public SubwayTransitSource(Drawable busStop)
	{
		this.busStop = busStop;
	}
	
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions,
			HashMap<String, String> routeKeysToTitles)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude, HashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj, HashMap<String, String> routeKeysToTitles)
			throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	

	public static String getSubwayRouteConfigUrl() {
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

	private static final String[] subwayRoutes = new String[] {"Red", "Orange", "Blue"};
	
	private static final String[] subwayColors = new String[] {"ff0000", "f88017", "0000ff"};
	
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
		return null;
	}

	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, HashMap<String, String> routeKeysToTitles,
			RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException {
		//download subway data
		final String subwayUrl = getSubwayRouteConfigUrl();
		URL url = new URL(subwayUrl);
		InputStream in = Locations.downloadStream(url, task, "Downloading subway info: ", "");
		
		SubwayRouteConfigFeedParser subwayParser = new SubwayRouteConfigFeedParser(busStop, routeKeysToTitles, directions);
		
		task.publish("Parsing route data...");
		
		subwayParser.runParse(in);
		
		subwayParser.writeToDatabase(routeMapping, false);
		
	}
}
