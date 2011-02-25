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
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.StreamCounter;

public class MBTABusTransitSource implements TransitSource
{
	/**
	 * The XML feed URL
	 */
	private static final String mbtaLocationsDataUrlOneRoute = "http://test.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta-test&t=";

	private static final String mbtaLocationsDataUrlAllRoutes = "http://test.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta-test&t=";

	private static final String mbtaRouteConfigDataUrl = "http://test.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta-test&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://test.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta-test";
	
	private static final String mbtaPredictionsDataUrl = "http://test.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta-test";

	private final Drawable busStop;
	private final Drawable bus;
	private final Drawable arrow;
	
	public MBTABusTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		this.busStop = busStop;
		this.bus = bus;
		this.arrow = arrow;
		
		tempRoutes = new ArrayList<String>();
		
		addRoute("741", "SL1");
		addRoute("742", "SL2");
		addRoute("746", "SL");
		addRoute("751", "SL4");
		addRoute("749", "SL5");
		addRoute("701", "CT1");
		addRoute("747", "CT2");
		addRoute("708", "CT3");
		
		addRoute("1", "1");
		addRoute("4", "4");
		addRoute("5", "5");
		addRoute("7", "7");
		addRoute("8", "8");
		addRoute("9", "9");
		addRoute("10", "10");
		addRoute("11", "11");
		addRoute("14", "14");
		addRoute("15", "15");
		addRoute("16", "16");
		addRoute("17", "17");
		addRoute("18", "18");
		addRoute("19", "19");
		addRoute("21", "21");
		addRoute("22", "22");
		addRoute("23", "23");
		addRoute("24", "24");
		addRoute("26", "26");
		addRoute("27", "27");
		addRoute("28", "28");
		addRoute("29", "29");
		addRoute("30", "30");
		addRoute("31", "31");
		addRoute("32", "32");
		addRoute("33", "33");
		addRoute("34", "34");
		addRoute("34E", "34E");
		addRoute("35", "35");
		addRoute("36", "36");
		addRoute("37", "37");
		addRoute("38", "38");
		addRoute("39", "39");
		addRoute("40", "40");
		addRoute("41", "41");
		addRoute("42", "42");
		addRoute("43", "43");
		addRoute("44", "44");
		addRoute("45", "45");
		addRoute("47", "47");
		addRoute("48", "48");
		addRoute("50", "50");
		addRoute("51", "51");
		addRoute("52", "52");
		addRoute("55", "55");
		addRoute("57", "57");
		addRoute("57A", "57A");
		addRoute("59", "59");
		addRoute("60", "60");
		addRoute("62", "62");
		addRoute("64", "64");
		addRoute("65", "65");
		addRoute("66", "66");
		addRoute("67", "67");
		addRoute("68", "68");
		addRoute("69", "69");
		addRoute("70", "70");
		addRoute("70A", "70A");
		addRoute("71", "71");
		addRoute("72", "72");
		addRoute("73", "73");
		addRoute("74", "74");
		addRoute("75", "75");
		addRoute("76", "76");
		addRoute("77", "77");
		addRoute("78", "78");
		addRoute("79", "79");
		addRoute("80", "80");
		addRoute("83", "83");
		addRoute("84", "84");
		addRoute("85", "85");
		addRoute("86", "86");
		addRoute("87", "87");
		addRoute("88", "88");
		addRoute("89", "89");
		addRoute("90", "90");
		addRoute("91", "91");
		addRoute("92", "92");
		addRoute("93", "93");
		addRoute("94", "94");
		addRoute("95", "95");
		addRoute("96", "96");
		addRoute("97", "97");
		addRoute("99", "99");
		addRoute("100", "100");
		addRoute("101", "101");
		addRoute("104", "104");
		addRoute("105", "105");
		addRoute("106", "106");
		addRoute("108", "108");
		addRoute("109", "109");
		addRoute("110", "110");
		addRoute("111", "111");
		addRoute("112", "112");
		addRoute("114", "114");
		addRoute("116", "116");
		addRoute("117", "117");
		addRoute("119", "119");
		addRoute("120", "120");
		addRoute("121", "121");
		addRoute("131", "131");
		addRoute("132", "132");
		addRoute("134", "134");
		addRoute("136", "136");
		addRoute("137", "137");
		addRoute("170", "170");
		addRoute("171", "171");
		addRoute("191", "191");
		addRoute("192", "192");
		addRoute("193", "193");
		addRoute("194", "194");
		addRoute("201", "201");
		addRoute("202", "202");
		addRoute("210", "210");
		addRoute("211", "211");
		addRoute("212", "212");
		addRoute("214", "214");
		addRoute("215", "215");
		addRoute("216", "216");
		addRoute("217", "217");
		addRoute("220", "220");
		addRoute("221", "221");
		addRoute("222", "222");
		addRoute("225", "225");
		addRoute("230", "230");
		addRoute("236", "236");
		addRoute("238", "238");
		addRoute("240", "240");
		addRoute("245", "245");
		addRoute("274", "274");
		addRoute("275", "275");
		addRoute("276", "276");
		addRoute("277", "277");
		addRoute("325", "325");
		addRoute("326", "326");
		addRoute("350", "350");
		addRoute("351", "351");
		addRoute("352", "352");
		addRoute("354", "354");
		addRoute("355", "355");
		addRoute("411", "411");
		addRoute("424", "424");
		addRoute("426", "426");
		addRoute("428", "428");
		addRoute("429", "429");
		addRoute("430", "430");
		addRoute("431", "431");
		addRoute("434", "434");
		addRoute("435", "435");
		addRoute("436", "436");
		addRoute("439", "439");
		addRoute("441", "441");
		addRoute("442", "442");
		addRoute("448", "448");
		addRoute("449", "449");
		addRoute("450", "450");
		addRoute("451", "451");
		addRoute("455", "455");
		addRoute("456", "456");
		addRoute("459", "459");
		addRoute("465", "465");
		addRoute("468", "468");
		addRoute("500", "500");
		addRoute("501", "501");
		addRoute("502", "502");
		addRoute("503", "503");
		addRoute("504", "504");
		addRoute("505", "505");
		addRoute("553", "553");
		addRoute("554", "554");
		addRoute("555", "555");
		addRoute("556", "556");
		addRoute("558", "558");
		addRoute("627", "62/76");

		addRoute("725", "72/75");





		addRoute("3233", "32/33");
		addRoute("3738", "37/38");
		addRoute("4050", "40/50");
		addRoute("4265", "426/455");
		addRoute("4269", "426/439");
		addRoute("4412", "441/442");
		addRoute("9109", "9109");
		addRoute("9111", "9111");
		addRoute("9501", "9501");
		addRoute("9507", "9507");
		addRoute("9701", "9701");
		addRoute("9702", "9702");
		addRoute("9703", "9703");

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
			float centerLatitude, float centerLongitude, ConcurrentHashMap<Integer, BusLocation> busMapping, 
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
					if (busLocation.lastUpdateInMillis + 180000 < System.currentTimeMillis())
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
		
		final int contentLength = 855212;
		
		InputStream in = new StreamCounter(context.getResources().openRawResource(boston.Bus.Map.R.raw.routeconfig),
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
