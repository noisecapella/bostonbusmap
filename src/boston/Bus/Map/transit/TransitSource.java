package boston.Bus.Map.transit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.ITransitDrawables;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.data.VehicleLocations;

public interface TransitSource {

	void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			VehicleLocations busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	ITransitDrawables getDrawables();

	StopLocation createStop(float latitude, float longitude, String stopTag,
			String stopTitle, String route);
	
	TransitSourceTitles getRouteTitles();
	
	/**
	 * The order in which to load transit sources. Lower numbers go first. Must be unique!
	 * @return
	 */
	int getLoadOrder();

	/**
	 * Returns corresponding values in Schema.Routes.enumagency*
	 */
	int[] getTransitSourceIds();

	/**
	 * Do we need to look at the Schema.Subway table to get branch
	 * and platform information?
	 */
	boolean requiresSubwayTable();

	IAlerts getAlerts();

	String getDescription();
}
