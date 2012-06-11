package boston.Bus.Map.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.transit.TransitSource;

public class FakeTransitSource implements TransitSource {

	@Override
	public RouteConfig[] makeRoutes(Directions directions) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasPaths() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getRoutes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyHashMap<String, String> getRouteKeysToTitles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindPredictionElementsForUrl(StringBuilder urlString,
			String route, String stopTag) {
		// TODO Auto-generated method stub

	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransitDrawables getDrawables() {
		// TODO Auto-generated method stub
		return null;
	}

}
