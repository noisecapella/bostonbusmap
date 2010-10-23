package boston.Bus.Map.transit;

import java.io.IOException;
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

public interface TransitSource {

	void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions)
		throws ClientProtocolException, IOException, ParserConfigurationException, SAXException ;

	void refreshData(RouteConfig routeConfig, int selectedBusPredictions,
			int maxStops, float centerLatitude, float centerLongitude,
			HashMap<Integer, BusLocation> busMapping, String selectedRoute,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException;
	
	public String[] getRoutes();
	
	public HashMap<String, String> getRouteKeysToTitles();

	Drawable getBusStopDrawable();
}
