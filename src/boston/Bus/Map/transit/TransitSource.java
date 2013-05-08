package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.main.UpdateAsyncTask;

public interface TransitSource {

	void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	TransitDrawables getDrawables();

	StopLocation createStop(float latitude, float longitude, String stopTag,
			String stopTitle, int platformOrder, String branch, String route);
	
	TransitSourceTitles getRouteTitles();
	
	/**
	 * The order in which to load transit sources. Lower numbers go first. Must be unique!
	 * @return
	 */
	int getLoadOrder();

	/**
	 * Returns corresponding value in Schema.Routes.enumagency*
	 */
	int getTransitSourceId();

	/**
	 * Do we need to look at the Schema.Subway table to get branch
	 * and platform information?
	 */
	boolean requiresSubwayTable();
}
