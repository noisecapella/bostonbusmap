package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.main.UpdateAsyncTask;

public interface TransitSource {

	void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions, UpdateAsyncTask task, boolean silent)
		throws ClientProtocolException, IOException, ParserConfigurationException, SAXException ;

	void refreshData(RouteConfig routeConfig, int selectedBusPredictions,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping, String selectedRoute,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException;
	
	public String[] getRoutes();
	
	public MyHashMap<String, String> getRouteKeysToTitles();
	
	/**
	 * Create a StopLocation using the parameters
	 * @param lat
	 * @param lon
	 * @param stopTag
	 * @param title
	 * @param platformOrder
	 * @param branch
	 * @param route
	 * @param dirTag
	 */
	StopLocation createStop(float lat, float lon, String stopTag, String title,
			int platformOrder, String branch, String route, String dirTag);

	void bindPredictionElementsForUrl(StringBuilder urlString, String route,
			String stopTag, String dirTag);

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	TransitDrawables getDrawables();
}
