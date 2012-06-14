package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import boston.Bus.Map.data.VehicleLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.LocationGroup;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopLocationGroup;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.prepopulated.NextbusPrepopulatedData;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.SearchHelper;
import boston.Bus.Map.util.StreamCounter;

/**
 * A transit source which accesses a NextBus webservice. Override for a specific agency
 * @author schneg
 *
 */
public abstract class NextBusTransitSource implements TransitSource
{
	private final TransitSystem transitSystem;
	
	private static final String prefix = "webservices";
	/**
	 * The XML feed URL
	 */
	private final String mbtaLocationsDataUrlOneRoute;
	private final String mbtaLocationsDataUrlAllRoutes;
	private final String mbtaRouteConfigDataUrl;
	private final String mbtaRouteConfigDataUrlAllRoutes;
	private final String mbtaPredictionsDataUrl;

	private final TransitDrawables drawables;
	private final String[] routes;
	private ArrayList<String> tempRoutes;
	private final MyHashMap<String, String> routeKeysToTitles = new MyHashMap<String, String>();

	

	public NextBusTransitSource(TransitSystem transitSystem, 
			TransitDrawables drawables, String agency)
	{
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		
		mbtaLocationsDataUrlOneRoute = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaLocationsDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaRouteConfigDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=";
		mbtaRouteConfigDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency;
		mbtaPredictionsDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=" + agency;
		
		tempRoutes = new ArrayList<String>();

		addRoutes();

		routes = tempRoutes.toArray(new String[0]);
		tempRoutes.clear();
		tempRoutes = null;
	}

	protected abstract void addRoutes();
	
	protected void addRoute(String key, String title) {
		tempRoutes.add(key);

		routeKeysToTitles.put(key, title);

	}


	@Override
	public void refreshData(int selectedBusPredictions, int maxStops,
			double centerLatitude, double centerLongitude, ConcurrentHashMap<String, VehicleLocation> busMapping, 
			String routeToUpdate, RoutePool routePool, Locations locationsObj) throws IOException, ParserConfigurationException, SAXException {
		Directions directions = routePool.getDirections();
		//read data from the URL
		DownloadHelper downloadHelper;
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		case  Main.BUS_PREDICTIONS_STAR:
		case  Main.BUS_PREDICTIONS_ALL:
		{

			routePool.clearRecentlyUpdated();

			List<LocationGroup> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			//ok, do predictions now
			String routeName = selectedBusPredictions == Main.BUS_PREDICTIONS_ONE ? routeToUpdate : null;
			String url = getPredictionsUrl(locations, maxStops, routeName, directions);

			if (url == null)
			{
				return;
			}

			downloadHelper = new DownloadHelper(url);
		}
		break;

		case Main.VEHICLE_LOCATIONS_ONE:
		{
			final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), routeToUpdate);
			downloadHelper = new DownloadHelper(urlString);
		}
		break;
		case Main.VEHICLE_LOCATIONS_ALL:
		default:
		{
			final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), null);
			downloadHelper = new DownloadHelper(urlString);
		}
		break;
		}

		downloadHelper.connect();

		InputStream data = downloadHelper.getResponseData();

		if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE || 
				selectedBusPredictions == Main.BUS_PREDICTIONS_ALL ||
				selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
		{
			//bus prediction

			BusPredictionsFeedParser parser = new BusPredictionsFeedParser(routePool, directions);

			parser.runParse(data);
		}
		else 
		{
			//vehicle locations
			//VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(stream);

			//lastUpdateTime = parser.getLastUpdateTime();

			VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(drawables, directions, routeKeysToTitles);
			parser.runParse(data);

			//get the time that this information is valid until
			locationsObj.setLastUpdateTime(parser.getLastUpdateTime());

			synchronized (busMapping)
			{
				parser.fillMapping(busMapping);

				//delete old buses
				List<String> busesToBeDeleted = new ArrayList<String>();
				for (String id : busMapping.keySet())
				{
					VehicleLocation busLocation = busMapping.get(id);
					if (busLocation.getLastUpdateInMillis() + 180000 < TransitSystem.currentTimeMillis())
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
		}
		
		//alerts
		TransitSource transitSource = transitSystem.getTransitSource(routeToUpdate);
		if (transitSource instanceof NextBusTransitSource)
		{
			RouteConfig routeConfig = routePool.getRoute(routeToUpdate);
			if (routeConfig.obtainedAlerts() == false)
			{
				try
				{
					parseAlert(routeConfig);
				}
				catch (Exception e)
				{
					LogUtil.e(e);
					//I'm silencing these since alerts aren't necessary to use the rest of the app
				}
			}
		}
	}

	protected abstract void parseAlert(RouteConfig routeConfig) throws ClientProtocolException, IOException, SAXException;

	protected String getPredictionsUrl(List<LocationGroup> locationGroups, int maxStops, String route, Directions directions)
	{
		StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);

		for (LocationGroup locationGroup : locationGroups)
		{
			if (locationGroup instanceof StopLocationGroup)
			{
				StopLocationGroup stopLocationGroup = (StopLocationGroup)locationGroup;
				for (StopLocation stop : stopLocationGroup.getStops()) {
					if (stop.getTransitSource() == this) {
						stop.createBusPredictionsUrl(transitSystem, urlString, stop.getFirstRoute(), directions);
					}
				}
			}
		}

		//TODO: hard limit this to 150 requests

		return urlString.toString();
	}


	@Override
	public void bindPredictionElementsForUrl(StringBuilder urlString,
			String routeName, String stopId) {
		urlString.append("&stops=").append(routeName).append("%7C");

		urlString.append("%7C").append(stopId);

	}

	protected String getVehicleLocationsUrl(long time, String route)
	{
		if (route != null)
		{
			return mbtaLocationsDataUrlOneRoute + time + "&r=" + route;
		}
		else
		{
			return mbtaLocationsDataUrlAllRoutes + time;
		}
	}

	protected String getRouteConfigUrl(String route)
	{
		if (route == null)
		{
			return mbtaRouteConfigDataUrlAllRoutes;
		}
		else
		{
			return mbtaRouteConfigDataUrl + route;
		}
	}



	@Override
	public String[] getRoutes() {
		return routes;
	}


	@Override
	public MyHashMap<String, String> getRouteKeysToTitles() {
		return routeKeysToTitles;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routes, routeKeysToTitles);
	}
	
	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}
	
	@Override
	public RouteConfig[] makeRoutes(Directions directions) throws IOException {
		return new NextbusPrepopulatedData(this).getAllRoutes(directions);
	}
}
