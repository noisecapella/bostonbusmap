package boston.Bus.Map.transit;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CitibikeStopLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.parser.CitibikeParser;
import boston.Bus.Map.parser.HubwayParser;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;

/**
 * Created by schneg on 9/2/13.
 */
public class CitibikeTransitSource implements TransitSource {
	public static final String stopTagPrefix = "citibike_";
	private static final String routeTag = "Citibike";
	private static final String dataUrl = "http://appservices.citibikenyc.com//data2/stations.php";

	private final TransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;


	public CitibikeTransitSource(TransitSystem transitSystem,
								 TransitDrawables drawables, TransitSourceTitles routeTitles,
							   RouteTitles allRouteTitles) {

		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
	}

	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
							int maxStops, double centerLatitude, double centerLongitude,
							ConcurrentHashMap<String, BusLocation> busMapping,
							RoutePool routePool, Directions directions,
							Locations locationsObj) throws IOException, ParserConfigurationException, SAXException {
		Selection.Mode mode = selection.getMode();
		if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
				mode == Selection.Mode.VEHICLE_LOCATIONS_ONE) {
			// no need for that here
			return;
		} else if (mode == Selection.Mode.BUS_PREDICTIONS_ALL ||
				mode == Selection.Mode.BUS_PREDICTIONS_ONE ||
				mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
			RouteConfig hubwayRouteConfig = routePool.get(routeTag);
			DownloadHelper downloadHelper = new DownloadHelper(dataUrl);

			downloadHelper.connect();


			InputStream stream = downloadHelper.getResponseData();

			CitibikeParser parser = new CitibikeParser(hubwayRouteConfig);
			parser.runParse(new InputStreamReader(stream));
		} else {
			throw new RuntimeException("Unknown mode encountered");

		}


	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routeTitles);
	}

	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude, String stopTag, String stopTitle, String route) {
		CitibikeStopLocation stop = new CitibikeStopLocation.CitibikeBuilder(latitude,
				longitude, stopTag, stopTitle).build();
		stop.addRoute(route);
		return stop;
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public int getLoadOrder() {
		return 4;
	}

	@Override
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidHubway;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

	@Override
	public IAlerts getAlerts() {
		return transitSystem.getAlerts();
	}

	@Override
	public String getDescription() {
		return "Hubway";
	}
}
