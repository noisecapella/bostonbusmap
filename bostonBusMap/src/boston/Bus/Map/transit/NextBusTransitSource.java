package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
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
	private final int initialRouteResource;

	private final Drawable busStop;
	private final Drawable busStopUpdated;
	private final Drawable bus;
	private final Drawable arrow;

	public NextBusTransitSource(TransitSystem transitSystem, 
			Drawable busStop, Drawable busStopUpdated, Drawable bus, Drawable arrow, String agency, int initialRouteResource)
	{
		this.transitSystem = transitSystem;
		
		this.busStop = busStop;
		this.bus = bus;
		this.busStopUpdated = busStopUpdated;
		this.arrow = arrow;

		mbtaLocationsDataUrlOneRoute = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaLocationsDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaRouteConfigDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=";
		mbtaRouteConfigDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency;
		mbtaPredictionsDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=" + agency;
		this.initialRouteResource = initialRouteResource;
		
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

	private final String[] routes;
	private ArrayList<String> tempRoutes;
	private final HashMap<String, String> routeKeysToTitles = new HashMap<String, String>();


	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions, UpdateAsyncTask task) 
	throws ClientProtocolException, IOException, ParserConfigurationException, SAXException 
	{
		final String urlString = getRouteConfigUrl(routeToUpdate);

		DownloadHelper downloadHelper = new DownloadHelper(urlString);

		downloadHelper.connect();
		//just initialize the route and then end for this round

		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, busStopUpdated, directions, oldRouteConfig,
				this);

		parser.runParse(downloadHelper.getResponseData()); 

		parser.writeToDatabase(routeMapping, false, task);

	}


	@Override
	public void refreshData(RouteConfig routeConfig, int selectedBusPredictions, int maxStops,
			double centerLatitude, double centerLongitude, ConcurrentHashMap<String, BusLocation> busMapping, 
			String selectedRoute, RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		DownloadHelper downloadHelper;
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		case  Main.BUS_PREDICTIONS_STAR:
		case  Main.BUS_PREDICTIONS_ALL:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			for (Location location : locations)
			{
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;
					stopLocation.clearRecentlyUpdated();
				}
			}
			
			//ok, do predictions now
			String routeName = selectedBusPredictions == Main.BUS_PREDICTIONS_ONE ? routeConfig.getRouteName() : null;
			String url = getPredictionsUrl(locations, maxStops, routeName);

			if (url == null)
			{
				return;
			}

			downloadHelper = new DownloadHelper(url);
		}
		break;

		case Main.VEHICLE_LOCATIONS_ONE:
		{
			final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), routeConfig.getRouteName());
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

			VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(bus, arrow, directions, routeKeysToTitles);
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
					BusLocation busLocation = busMapping.get(id);
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
		TransitSource transitSource = transitSystem.getTransitSource(routeConfig.getRouteName());
		if (transitSource instanceof NextBusTransitSource)
		{
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

	protected String getPredictionsUrl(List<Location> locations, int maxStops, String route)
	{
		//TODO: technically we should be checking that it is a bus route, not that it's not a subway route
		//but this is probably more efficient
		TransitSource transitSource = transitSystem.getTransitSource(route);
		if (!(transitSource instanceof NextBusTransitSource))
		{
			//there should only be one instance of a source in memory at a time, but just in case...
			return null;
		}
		
		StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);

		for (Location location : locations)
		{
			if ((location instanceof StopLocation) && !(location instanceof SubwayStopLocation))
			{
				StopLocation stopLocation = (StopLocation)location;
				stopLocation.createBusPredictionsUrl(transitSystem, urlString, route);
			}
		}

		//TODO: hard limit this to 150 requests

		Log.v("BostonBusMap", "urlString for bus predictions, all: " + urlString);

		return urlString.toString();
	}


	@Override
	public void bindPredictionElementsForUrl(StringBuilder urlString,
			String routeName, String stopId, String direction) {
		urlString.append("&stops=").append(routeName).append("%7C");
		if (direction != null)
		{
			urlString.append(direction);
		}

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
	public boolean hasPaths() {
		return true;
	}


	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context, Directions directions,
			RoutePool routeMapping)
	throws IOException, ParserConfigurationException, SAXException {
		task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Decompressing route data", null));

		final int contentLength = getInitialContentLength();

		InputStream in = new StreamCounter(context.getResources().openRawResource(initialRouteResource),
				task, contentLength); 

		GZIPInputStream stream = new GZIPInputStream(in); 

		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, busStopUpdated, directions, null, this);

		parser.runParse(stream);



		parser.writeToDatabase(routeMapping, true, task);


	}

	/**
	 * This is the size of the file at R.res.raw.routeconfig
	 * @return
	 */
	protected abstract int getInitialContentLength();

	@Override
	public String[] getRoutes() {
		return routes;
	}


	@Override
	public HashMap<String, String> getRouteKeysToTitles() {
		return routeKeysToTitles;
	}


	@Override
	public Drawable getBusStopDrawable() {
		return busStop;
	}

	@Override
	public Drawable getBusStopUpdatedDrawable() {
		return busStopUpdated;
	}

	@Override
	public StopLocation createStop(float lat, float lon, String stopTag,
			String title, int platformOrder, String branch, String route, String dirTag)
	{
		StopLocation stop = new StopLocation(lat, lon, busStop, busStopUpdated, stopTag, title);
		stop.addRouteAndDirTag(route, dirTag);
		return stop;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routes, routeKeysToTitles);
	}
}
