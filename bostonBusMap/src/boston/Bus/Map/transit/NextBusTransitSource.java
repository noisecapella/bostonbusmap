package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.schneeloch.latransit.R;
import com.schneeloch.latransit.main.Main;
import com.schneeloch.latransit.main.UpdateAsyncTask;

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
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.StreamCounter;

public class NextBusTransitSource implements TransitSource
{
	private static final String agency = "lametro";
	
	/**
	 * The XML feed URL
	 */
	private static final String mbtaLocationsDataUrlOneRoute = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";

	private static final String mbtaLocationsDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=" + agency + "&t=";

	private static final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency + "&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=" + agency;

	private static final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=" + agency;

	private final Drawable busStop;
	private final Drawable bus;
	private final Drawable arrow;

	public NextBusTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		this.busStop = busStop;
		this.bus = bus;
		this.arrow = arrow;

		tempRoutes = new ArrayList<String>();

		addRoute("2", "2");
		addRoute("4", "4");
		addRoute("10", "10");
		addRoute("14", "14");
		addRoute("16", "16");
		addRoute("18", "18");
		addRoute("20", "20");
		addRoute("26", "26");
		addRoute("28", "28");
		addRoute("30", "30");
		addRoute("33", "33");
		addRoute("35", "35");
		addRoute("37", "37");
		addRoute("38", "38");
		addRoute("40", "40");
		addRoute("42", "42");
		addRoute("45", "45");
		addRoute("48", "48");
		addRoute("51", "51");
		addRoute("52", "52");
		addRoute("53", "53");
		addRoute("55", "55");
		addRoute("60", "60");
		addRoute("62", "62");
		addRoute("66", "66");
		addRoute("68", "68");
		addRoute("70", "70");
		addRoute("71", "71");
		addRoute("76", "76");
		addRoute("78", "78");
		addRoute("79", "79");
		addRoute("81", "81");
		addRoute("83", "83");
		addRoute("84", "84");
		addRoute("90", "90");
		addRoute("91", "91");
		addRoute("92", "92");
		addRoute("94", "94");
		addRoute("96", "96");
		addRoute("102", "102");
		addRoute("105", "105");
		addRoute("108", "108");
		addRoute("110", "110");
		addRoute("111", "111");
		addRoute("115", "115");
		addRoute("117", "117");
		addRoute("120", "120");
		addRoute("121", "121");
		addRoute("125", "125");
		addRoute("126", "126");
		addRoute("127", "127");
		addRoute("128", "128");
		addRoute("130", "130");
		addRoute("150", "150");
		addRoute("152", "152");
		addRoute("154", "154");
		addRoute("155", "155");
		addRoute("156", "156");
		addRoute("158", "158");
		addRoute("161", "161");
		addRoute("163", "163");
		addRoute("164", "164");
		addRoute("165", "165");
		addRoute("166", "166");
		addRoute("167", "167");
		addRoute("169", "169");
		addRoute("175", "175");
		addRoute("176", "176");
		addRoute("177", "177");
		addRoute("180", "180");
		addRoute("181", "181");
		addRoute("183", "183");
		addRoute("190", "190");
		addRoute("194", "194");
		addRoute("200", "200");
		addRoute("201", "201");
		addRoute("202", "202");
		addRoute("204", "204");
		addRoute("205", "205");
		addRoute("206", "206");
		addRoute("207", "207");
		addRoute("209", "209");
		addRoute("210", "210");
		addRoute("211", "211");
		addRoute("212", "212");
		addRoute("215", "215");
		addRoute("217", "217");
		addRoute("218", "218");
		addRoute("220", "220");
		addRoute("222", "222");
		addRoute("224", "224");
		addRoute("230", "230");
		addRoute("232", "232");
		addRoute("233", "233");
		addRoute("234", "234");
		addRoute("236", "236");
		addRoute("237", "237");
		addRoute("239", "239");
		addRoute("240", "240");
		addRoute("242", "242");
		addRoute("243", "243");
		addRoute("244", "244");
		addRoute("245", "245");
		addRoute("246", "246");
		addRoute("247", "247");
		addRoute("251", "251");
		addRoute("252", "252");
		addRoute("254", "254");
		addRoute("256", "256");
		addRoute("258", "258");
		addRoute("260", "260");
		addRoute("264", "264");
		addRoute("265", "265");
		addRoute("266", "266");
		addRoute("267", "267");
		addRoute("268", "268");
		addRoute("270", "270");
		addRoute("287", "287");
		addRoute("290", "290");
		addRoute("292", "292");
		addRoute("302", "302");
		addRoute("305", "305");
		addRoute("311", "311");
		addRoute("312", "312");
		addRoute("316", "316");
		addRoute("335", "335");
		addRoute("344", "344");
		addRoute("352", "352");
		addRoute("353", "353");
		addRoute("355", "355");
		addRoute("358", "358");
		addRoute("363", "363");
		addRoute("364", "364");
		addRoute("378", "378");
		addRoute("422", "422");
		addRoute("439", "439");
		addRoute("442", "442");
		addRoute("445", "445");
		addRoute("450", "450");
		addRoute("460", "460");
		addRoute("485", "485");
		addRoute("487", "487");
		addRoute("489", "489");
		addRoute("534", "534");
		addRoute("550", "550");
		addRoute("577", "577");
		addRoute("603", "603");
		addRoute("605", "605");
		addRoute("607", "607");
		addRoute("611", "611");
		addRoute("612", "612");
		addRoute("620", "620");
		addRoute("625", "625");
		addRoute("634", "634");
		addRoute("645", "645");
		addRoute("656", "656");
		addRoute("665", "665");
		addRoute("685", "685");
		addRoute("686", "686");
		addRoute("687", "687");
		addRoute("704", "704");
		addRoute("705", "705");
		addRoute("710", "710");
		addRoute("720", "720");
		addRoute("728", "728");
		addRoute("730", "730");
		addRoute("733", "733");
		addRoute("734", "734");
		addRoute("740", "740");
		addRoute("741", "741");
		addRoute("745", "745");
		addRoute("750", "750");
		addRoute("751", "751");
		addRoute("754", "754");
		addRoute("757", "757");
		addRoute("760", "760");
		addRoute("761", "761");
		addRoute("762", "762");
		addRoute("770", "770");
		addRoute("780", "780");
		addRoute("794", "794");
		addRoute("901", "901");
		addRoute("902", "902");
		addRoute("910", "910");
		


		routes = tempRoutes.toArray(new String[0]);
		tempRoutes.clear();
		tempRoutes = null;
	}

	private void addRoute(String key, String title) {
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

		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, oldRouteConfig,
				this);

		parser.runParse(downloadHelper.getResponseData()); 

		parser.writeToDatabase(routeMapping, false, task);

	}


	@Override
	public void refreshData(RouteConfig routeConfig, int selectedBusPredictions, int maxStops,
			double centerLatitude, double centerLongitude, ConcurrentHashMap<Integer, BusLocation> busMapping, 
			String selectedRoute, RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException {
		//read data from the URL
		DownloadHelper downloadHelper;
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			//ok, do predictions now
			String url = getPredictionsUrl(locations, maxStops, routeConfig.getRouteName());

			if (url == null)
			{
				return;
			}

			downloadHelper = new DownloadHelper(url);
		}
		break;
		case Main.BUS_PREDICTIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			String url = getPredictionsUrl(locations, maxStops, null);

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

			VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(routePool,
					bus, arrow, directions, routeKeysToTitles);
			parser.runParse(data);

			//get the time that this information is valid until
			locationsObj.setLastUpdateTime(parser.getLastUpdateTime());

			synchronized (busMapping)
			{
				parser.fillMapping(busMapping);

				//delete old buses
				List<Integer> busesToBeDeleted = new ArrayList<Integer>();
				for (Integer id : busMapping.keySet())
				{
					BusLocation busLocation = busMapping.get(id);
					if (busLocation.getLastUpdateInMillis() + 180000 < TransitSystem.currentTimeMillis())
					{
						//put this old dog to sleep
						busesToBeDeleted.add(id);
					}
				}

				for (Integer id : busesToBeDeleted)
				{
					busMapping.remove(id);
				}
			}
		}
	}

	private String getPredictionsUrl(List<Location> locations, int maxStops, String route)
	{
		//TODO: technically we should be checking that it is a bus route, not that it's not a subway route
		//but this is probably more efficient

		if (SubwayTransitSource.isSubway(route))
		{
			return null;
		}

		StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);

		for (Location location : locations)
		{
			if (location instanceof StopLocation)
			{
				StopLocation stopLocation = (StopLocation)location;
				stopLocation.createBusPredictionsUrl(urlString, route);
			}
		}

		//TODO: hard limit this to 150 requests

		Log.v("BostonBusMap", "urlString for bus predictions, all: " + urlString);

		return urlString.toString();
	}



	public static void bindPredictionElementsForUrl(StringBuilder urlString,
			String routeName, String stopId, String direction) {
		urlString.append("&stops=").append(routeName).append("%7C");
		if (direction != null)
		{
			urlString.append(direction);
		}

		urlString.append("%7C").append(stopId);

	}

	private static String getVehicleLocationsUrl(long time, String route)
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

	private static String getRouteConfigUrl(String route)
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

		final int contentLength = 341522;

		InputStream in = new StreamCounter(context.getResources().openRawResource(R.raw.routeconfig),
				task, contentLength); 

		GZIPInputStream stream = new GZIPInputStream(in); 

		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, null, this);

		parser.runParse(stream);



		parser.writeToDatabase(routeMapping, true, task);


	}


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
	public StopLocation createStop(float lat, float lon, String stopTag,
			String title, int platformOrder, String branch, String route,
			String dirTag) {
		StopLocation stop = new StopLocation(lat, lon, busStop, stopTag, title);
		stop.addRouteAndDirTag(route, dirTag);
		return stop;
	}


}
