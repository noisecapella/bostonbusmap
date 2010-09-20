package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.main.UpdateAsyncTask;

public interface TransitSource {

	void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions, HashMap<String, String> routeKeysToTitles)
		throws ClientProtocolException, IOException, ParserConfigurationException, SAXException ;

	void refreshData(RouteConfig routeConfig, int selectedBusPredictions,
			int maxStops, float centerLatitude, float centerLongitude,
			HashMap<Integer, BusLocation> busMapping, String selectedRoute,
			RoutePool routePool, Directions directions, Locations locationsObj,
			HashMap<String, String> routeKeysToTitles) throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	public abstract void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, HashMap<String, String> routeKeysToTitles, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException;
}
