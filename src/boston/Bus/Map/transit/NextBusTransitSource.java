package boston.Bus.Map.transit;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
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

	private final TransitDrawables drawables;

	private final TransitSourceTitles routeTitles;
	private final RouteTitles allRouteTitles;

	public NextBusTransitSource(TransitSystem transitSystem, 
			TransitDrawables drawables, String agency, TransitSourceTitles routeTitles,
			RouteTitles allRouteTitles)
	{
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		
		mbtaLocationsDataUrlOneRoute = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaLocationsDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";
		mbtaRouteConfigDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=";
		mbtaRouteConfigDataUrlAllRoutes = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency;
		mbtaPredictionsDataUrl = "http://" + prefix + ".nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=" + agency;
		
		this.routeTitles = routeTitles;
		this.allRouteTitles = allRouteTitles;
	}

	@Override
	public void populateStops(Context context, RoutePool routeMapping, String routeToUpdate,
			Directions directions, UpdateAsyncTask task, boolean silent) 
	throws ClientProtocolException, IOException, ParserConfigurationException, SAXException, RemoteException, OperationApplicationException 
	{
		final String urlString = getRouteConfigUrl(routeToUpdate);

		DownloadHelper downloadHelper = new DownloadHelper(urlString);

		downloadHelper.connect();
		//just initialize the route and then end for this round

		RouteConfigFeedParser parser = new RouteConfigFeedParser(context,
				this, allRouteTitles);
		parser.runParse(downloadHelper.getResponseData());
		parser.writeToDatabase(context);

	}


	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection, int maxStops,
			double centerLatitude, double centerLongitude, ConcurrentHashMap<String, BusLocation> busMapping, 
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		DownloadHelper downloadHelper;
		int selectedBusPredictions = selection.getMode();
		switch (selectedBusPredictions)
		{
		case  Selection.BUS_PREDICTIONS_ONE:
		case  Selection.BUS_PREDICTIONS_STAR:
		case  Selection.BUS_PREDICTIONS_ALL:
		case  Selection.BUS_PREDICTIONS_INTERSECT:
		{

			routePool.clearRecentlyUpdated();

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

			//ok, do predictions now
			String routeName = selectedBusPredictions == Selection.BUS_PREDICTIONS_ONE ? routeConfig.getRouteName() : null;
			String url = getPredictionsUrl(locations, maxStops, routeName);

			if (url == null)
			{
				return;
			}

			downloadHelper = new DownloadHelper(url);
		}
		break;

		case Selection.VEHICLE_LOCATIONS_ONE:
		{
			final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), routeConfig.getRouteName());
			downloadHelper = new DownloadHelper(urlString);
		}
		break;
		case Selection.VEHICLE_LOCATIONS_ALL:
		default:
		{
			final String urlString = getVehicleLocationsUrl(locationsObj.getLastUpdateTime(), null);
			downloadHelper = new DownloadHelper(urlString);
		}
		break;
		}

		downloadHelper.connect();

		InputStream data = downloadHelper.getResponseData();

		if (selectedBusPredictions == Selection.BUS_PREDICTIONS_ONE || 
				selectedBusPredictions == Selection.BUS_PREDICTIONS_ALL ||
				selectedBusPredictions == Selection.BUS_PREDICTIONS_STAR ||
				selectedBusPredictions == Selection.BUS_PREDICTIONS_INTERSECT)
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

			VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(drawables, directions, transitSystem.getRouteKeysToTitles());
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
					parseAlert(routeConfig, transitSystem.getAlertsMapping());
				}
				catch (Exception e)
				{
					LogUtil.e(e);
					//I'm silencing these since alerts aren't necessary to use the rest of the app
				}
			}
		}
	}

	protected abstract void parseAlert(RouteConfig routeConfig, AlertsMapping alertMapping) throws ClientProtocolException, IOException, SAXException;

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
				if (route != null) {
					urlString.append("&stops=").append(route).append("%7C");
					urlString.append("%7C").append(stopLocation.getStopTag());
				}
				else
				{
					for (String stopRoute : stopLocation.getRoutes()) {
						urlString.append("&stops=").append(stopRoute).append("%7C");
						urlString.append("%7C").append(stopLocation.getStopTag());
						
					}
				}
			}
		}

		//TODO: hard limit this to 150 requests

		return urlString.toString();
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
	throws IOException, ParserConfigurationException, SAXException, RemoteException, OperationApplicationException {
		// this intentially left blank
	}

	@Override
	public StopLocation createStop(float lat, float lon, String stopTag,
			String title, int platformOrder, String branch, String route)
	{
		StopLocation stop = new StopLocation.Builder(lat, lon, drawables, stopTag, title).build();
		stop.addRoute(route);
		return stop;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, transitSystem.getRouteKeysToTitles());
	}
	
	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}
	
	@Override
	public int getLoadOrder() {
		return 1;
	}
	
	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}
	
	@Override
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidBus;
	}
	
	@Override
	public boolean requiresSubwayTable() {
		return false;
	}
}
