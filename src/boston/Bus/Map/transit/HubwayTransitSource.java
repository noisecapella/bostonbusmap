package boston.Bus.Map.transit;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.PredictionStopLocationPair;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.parser.HubwayParser;
import boston.Bus.Map.util.DownloadHelper;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayTransitSource implements TransitSource {
	public static final String stopTagPrefix = "hubway_";
	private static final String routeTag = "Hubway";
	private static final String dataUrl = "http://www.thehubway.com/data/stations/bikeStations.xml";

	private final TransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;


	public HubwayTransitSource(TransitDrawables drawables, TransitSourceTitles routeTitles,
							   TransitSystem transitSystem) {

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
		int mode = selection.getMode();
		switch (mode) {
			case Selection.VEHICLE_LOCATIONS_ALL:
			case Selection.VEHICLE_LOCATIONS_ONE:
				// no need for that here
				return;
			case Selection.BUS_PREDICTIONS_ALL:
			case Selection.BUS_PREDICTIONS_ONE:
			case Selection.BUS_PREDICTIONS_STAR:
				RouteConfig hubwayRouteConfig = routePool.get(routeTag);
				DownloadHelper downloadHelper = new DownloadHelper(dataUrl);

				downloadHelper.connect();


				InputStream stream = downloadHelper.getResponseData();

				HubwayParser parser = new HubwayParser(hubwayRouteConfig);
				parser.runParse(stream);
				List<PredictionStopLocationPair> pairs = parser.getPairs();

				for (PredictionStopLocationPair pair : pairs) {
					pair.stopLocation.clearPredictions(null);
					pair.stopLocation.addPrediction(pair.prediction);
				}

				break;
			default:
				throw new RuntimeException("Unknown mode encountered");

		}


	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return null;
	}

	@Override
	public TransitDrawables getDrawables() {
		return null;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude, String stopTag, String stopTitle, String route) {
		return null;
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return null;
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public int getTransitSourceId() {
		return 0;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

	@Override
	public IAlerts getAlerts() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}
}
