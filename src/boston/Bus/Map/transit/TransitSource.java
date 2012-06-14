package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import boston.Bus.Map.data.VehicleLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.main.UpdateAsyncTask;

public interface TransitSource {

	public RouteConfig[] makeRoutes(Directions directions) throws IOException;
	
	void refreshData(RouteConfig routeConfig, int selectedBusPredictions,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, VehicleLocation> busMapping, String selectedRoute,
			RoutePool routePool, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	public String[] getRoutes();
	
	public MyHashMap<String, String> getRouteKeysToTitles();

	void bindPredictionElementsForUrl(StringBuilder urlString, String route,
			String stopTag);

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	TransitDrawables getDrawables();
}
