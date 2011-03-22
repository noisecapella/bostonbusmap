package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.SubwayRouteConfigFeedParser;
import boston.Bus.Map.util.DownloadHelper;

public class CommuterRailTransitSource implements TransitSource {
	private final Drawable busStop;
	private final Drawable rail;
	private final Drawable railArrow;
	
	public CommuterRailTransitSource(Drawable busStop, Drawable rail, Drawable railArrow)
	{
		this.busStop = busStop;
		this.rail = rail;
		this.railArrow = railArrow;
	}
	
	public static String getRouteConfigUrl() {
		return "http://developer.mbta.com/RT_Archive/RealTimeHeavyRailKeys.csv";
	}


	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions,
			UpdateAsyncTask task) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException
	{
		
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
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude,
			ConcurrentHashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj) throws IOException,
			ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasPaths() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getRoutes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getRouteKeysToTitles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Drawable getBusStopDrawable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StopLocation createStop(float lat, float lon, String stopTag,
			String title, int platformOrder, String branch, String route,
			String dirTag) {
		// TODO Auto-generated method stub
		return null;
	}

}
