package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableSet;

import boston.Bus.Map.data.AlertsFuture;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.SIRIVehicleLocationsFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;
import boston.Bus.Map.util.StringUtil;

public class SIRITransitSource implements TransitSource {
	private final TransitSystem transitSystem;
	
	private final TransitDrawables drawables;

	private final TransitSourceTitles routeTitles;
	private final RouteTitles allRouteTitles;

	private static final String KEY = "b0a3f670-0a8b-43e7-8706-7a4ec985f1ff";

	public SIRITransitSource(TransitSystem transitSystem,
			TransitDrawables drawables, String string,
			TransitSourceTitles routeTitles, RouteTitles allRouteTitles) {
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		
		this.routeTitles = routeTitles;
		this.allRouteTitles = allRouteTitles;
	}

	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		
		// TODO: when we handle multiple transit sources this should return early without download
		// if the stops we're looking at aren't in this transit source

		int mode = selection.getMode();
		DownloadHelper downloadHelper;

		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_STAR:
		case Selection.VEHICLE_LOCATIONS_ALL:
		{
			String urlString = getVehicleLocationsUrl(null);
			downloadHelper = new DownloadHelper(urlString);
			break;
		}
		case Selection.VEHICLE_LOCATIONS_ONE:
		case Selection.BUS_PREDICTIONS_ONE:
		default:
		{
			String urlString = getVehicleLocationsUrl(routeConfig.getRouteName());
			downloadHelper = new DownloadHelper(urlString);
			break;
		}
		}

		downloadHelper.connect();

		InputStream data = downloadHelper.getResponseData();

		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_ONE:
		case Selection.BUS_PREDICTIONS_STAR:
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ONE:
		{
			SIRIVehicleLocationsFeedParser parser = new SIRIVehicleLocationsFeedParser(
					routeConfig, directions, busMapping, allRouteTitles, routePool);
			parser.runParse(new InputStreamReader(data), transitSystem);
			//get the time that this information is valid until
			locationsObj.setLastUpdateTime(parser.getLastUpdateTime());

			// TODO: do I synchronize busMapping everywhere I should?
			// given that this is a ConcurrentHashMap is synchronization even necessary?
			synchronized (busMapping)
			{

				//delete old buses
				List<String> busesToBeDeleted = new ArrayList<String>();
				for (String id : busMapping.keySet())
				{
					BusLocation busLocation = busMapping.get(id);
					if (busLocation.getLastUpdateInMillis() + 180000 < System.currentTimeMillis())
					{
						//put this old dog to sleep
						busesToBeDeleted.add(id);
					}
				}

				for (String id : busesToBeDeleted)
				{
					busMapping.remove(id);
				}
			}
			break;
		}
		}
		data.close();

	}

	private String getVehicleLocationsUrl(String routeName) {
		String ret = "http://bustime.mta.info/api/siri/vehicle-monitoring.json?key=" + KEY;
		if (!StringUtil.isEmpty(routeName)) {
			ret += "&LineRef=" + routeName;
		}
		return ret;
	}

	@Override
	public boolean hasPaths() {
		return true;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, transitSystem.getRouteKeysToTitles());
	}

	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle, String route) {
		StopLocation.Builder builder = new StopLocation.Builder(latitude, longitude, stopTag, stopTitle);
		StopLocation stop = builder.build();
		stop.addRoute(route);
		return stop;
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return this.routeTitles;
	}

	@Override
	public int getLoadOrder() {
		return 1;
	}

	@Override
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidBus;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

	@Override
	public IAlerts getAlerts() {
		return AlertsFuture.EMPTY;
	}

	@Override
	public String getDescription() {
		return "Bus";
	}

}
