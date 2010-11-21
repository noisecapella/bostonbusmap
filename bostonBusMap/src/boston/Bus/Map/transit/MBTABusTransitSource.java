package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private static final String mbtaLocationsDataUrlOneRoute = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaLocationsDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private static final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	private static final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta";
	
	private static final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";

	private final Drawable busStop;
	private final Drawable bus;
	private final Drawable arrow;
	
	public MBTABusTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		this.busStop = busStop;
		this.bus = bus;
		this.arrow = arrow;
		
		routes = new String[]{
				"741","742","746","751","749","701","747","748","708",
				"1","4","5","7","8","9","10","11","14",
				"15","16","17","18","19","21","22","23","24",
				"26","27","28","29","30","31","32","33","34",
				"34E","35","36","37","38","39","40","41","42",
				"43","44","45","47","48","50","51","52","55",
				"57","59","60","62","64","65","66","67","68",
				"69","70","70A","71","72","73","74","75","76",
				"77","78","79","80","83","84","85","86","87",
				"88","89","90","91","92","93","94","95","96",
				"97","99","100","101","104","105","106","108","109",
				"110","111","111C","112","114","116","117","119","120",
				"121","131","132","134","136","137","170","171","191",
				"192","193","194","201","202","210","211","212","214",
				"215","216","217","220","221","222","225","225C","230",
				"236","238","240","245","274","275","276","277","325",
				"326","350","351","352","354","355","411","424","424W",
				"426","426W","428","429","430","430G","431","434","435",
				"436","439","441","441W","442","442W","448","449","450",
				"450W","451","455","455W","456","459","465","468","500",
				"501","502","503","504","505","553","554","555","556",
				"558","9109","9111","9501","9507","9701","9702","9703"};
		
		
		routeKeysToTitles.put("741", "SL1");
		routeKeysToTitles.put("742", "SL2");
		routeKeysToTitles.put("746", "SL");
		routeKeysToTitles.put("751", "SL4");
		routeKeysToTitles.put("749", "SL5");
		routeKeysToTitles.put("701", "CT1");
		routeKeysToTitles.put("747", "CT2-S");
		routeKeysToTitles.put("748", "CT2-N");
		routeKeysToTitles.put("708", "CT3");
		routeKeysToTitles.put("1", "1");
		routeKeysToTitles.put("4", "4");
		routeKeysToTitles.put("5", "5");
		routeKeysToTitles.put("7", "7");
		routeKeysToTitles.put("8", "8");
		routeKeysToTitles.put("9", "9");
		routeKeysToTitles.put("10", "10");
		routeKeysToTitles.put("11", "11");
		routeKeysToTitles.put("14", "14");
		routeKeysToTitles.put("15", "15");
		routeKeysToTitles.put("16", "16");
		routeKeysToTitles.put("17", "17");
		routeKeysToTitles.put("18", "18");
		routeKeysToTitles.put("19", "19");
		routeKeysToTitles.put("21", "21");
		routeKeysToTitles.put("22", "22");
		routeKeysToTitles.put("23", "23");
		routeKeysToTitles.put("24", "24");
		routeKeysToTitles.put("26", "26");
		routeKeysToTitles.put("27", "27");
		routeKeysToTitles.put("28", "28");
		routeKeysToTitles.put("29", "29");
		routeKeysToTitles.put("30", "30");
		routeKeysToTitles.put("31", "31");
		routeKeysToTitles.put("32", "32");
		routeKeysToTitles.put("33", "33");
		routeKeysToTitles.put("34", "34");
		routeKeysToTitles.put("34E", "34E");
		routeKeysToTitles.put("35", "35");
		routeKeysToTitles.put("36", "36");
		routeKeysToTitles.put("37", "37");
		routeKeysToTitles.put("38", "38");
		routeKeysToTitles.put("39", "39");
		routeKeysToTitles.put("40", "40");
		routeKeysToTitles.put("41", "41");
		routeKeysToTitles.put("42", "42");
		routeKeysToTitles.put("43", "43");
		routeKeysToTitles.put("44", "44");
		routeKeysToTitles.put("45", "45");
		routeKeysToTitles.put("47", "47");
		routeKeysToTitles.put("48", "48");
		routeKeysToTitles.put("50", "50");
		routeKeysToTitles.put("51", "51");
		routeKeysToTitles.put("52", "52");
		routeKeysToTitles.put("55", "55");
		routeKeysToTitles.put("57", "57");
		routeKeysToTitles.put("59", "59");
		routeKeysToTitles.put("60", "60");
		routeKeysToTitles.put("62", "62");
		routeKeysToTitles.put("64", "64");
		routeKeysToTitles.put("65", "65");
		routeKeysToTitles.put("66", "66");
		routeKeysToTitles.put("67", "67");
		routeKeysToTitles.put("68", "68");
		routeKeysToTitles.put("69", "69");
		routeKeysToTitles.put("70", "70");
		routeKeysToTitles.put("70A", "70A");
		routeKeysToTitles.put("71", "71");
		routeKeysToTitles.put("72", "72");
		routeKeysToTitles.put("73", "73");
		routeKeysToTitles.put("74", "74");
		routeKeysToTitles.put("75", "75");
		routeKeysToTitles.put("76", "76");
		routeKeysToTitles.put("77", "77");
		routeKeysToTitles.put("78", "78");
		routeKeysToTitles.put("79", "79");
		routeKeysToTitles.put("80", "80");
		routeKeysToTitles.put("83", "83");
		routeKeysToTitles.put("84", "84");
		routeKeysToTitles.put("85", "85");
		routeKeysToTitles.put("86", "86");
		routeKeysToTitles.put("87", "87");
		routeKeysToTitles.put("88", "88");
		routeKeysToTitles.put("89", "89");
		routeKeysToTitles.put("90", "90");
		routeKeysToTitles.put("91", "91");
		routeKeysToTitles.put("92", "92");
		routeKeysToTitles.put("93", "93");
		routeKeysToTitles.put("94", "94");
		routeKeysToTitles.put("95", "95");
		routeKeysToTitles.put("96", "96");
		routeKeysToTitles.put("97", "97");
		routeKeysToTitles.put("99", "99");
		routeKeysToTitles.put("100", "100");
		routeKeysToTitles.put("101", "101");
		routeKeysToTitles.put("104", "104");
		routeKeysToTitles.put("105", "105");
		routeKeysToTitles.put("106", "106");
		routeKeysToTitles.put("108", "108");
		routeKeysToTitles.put("109", "109");
		routeKeysToTitles.put("110", "110");
		routeKeysToTitles.put("111", "111");
		routeKeysToTitles.put("111C", "111C");
		routeKeysToTitles.put("112", "112");
		routeKeysToTitles.put("114", "114");
		routeKeysToTitles.put("116", "116");
		routeKeysToTitles.put("117", "117");
		routeKeysToTitles.put("119", "119");
		routeKeysToTitles.put("120", "120");
		routeKeysToTitles.put("121", "121");
		routeKeysToTitles.put("131", "131");
		routeKeysToTitles.put("132", "132");
		routeKeysToTitles.put("134", "134");
		routeKeysToTitles.put("136", "136");
		routeKeysToTitles.put("137", "137");
		routeKeysToTitles.put("170", "170");
		routeKeysToTitles.put("171", "171");
		routeKeysToTitles.put("191", "191");
		routeKeysToTitles.put("192", "192");
		routeKeysToTitles.put("193", "193");
		routeKeysToTitles.put("194", "194");
		routeKeysToTitles.put("201", "201");
		routeKeysToTitles.put("202", "202");
		routeKeysToTitles.put("210", "210");
		routeKeysToTitles.put("211", "211");
		routeKeysToTitles.put("212", "212");
		routeKeysToTitles.put("214", "214");
		routeKeysToTitles.put("215", "215");
		routeKeysToTitles.put("216", "216");
		routeKeysToTitles.put("217", "217");
		routeKeysToTitles.put("220", "220");
		routeKeysToTitles.put("221", "221");
		routeKeysToTitles.put("222", "222");
		routeKeysToTitles.put("225", "225");
		routeKeysToTitles.put("225C", "225C");
		routeKeysToTitles.put("230", "230");
		routeKeysToTitles.put("236", "236");
		routeKeysToTitles.put("238", "238");
		routeKeysToTitles.put("240", "240");
		routeKeysToTitles.put("245", "245");
		routeKeysToTitles.put("274", "274");
		routeKeysToTitles.put("275", "275");
		routeKeysToTitles.put("276", "276");
		routeKeysToTitles.put("277", "277");
		routeKeysToTitles.put("325", "325");
		routeKeysToTitles.put("326", "326");
		routeKeysToTitles.put("350", "350");
		routeKeysToTitles.put("351", "351");
		routeKeysToTitles.put("352", "352");
		routeKeysToTitles.put("354", "354");
		routeKeysToTitles.put("355", "355");
		routeKeysToTitles.put("411", "411");
		routeKeysToTitles.put("424", "424");
		routeKeysToTitles.put("424W", "424W");
		routeKeysToTitles.put("426", "426");
		routeKeysToTitles.put("426W", "426W");
		routeKeysToTitles.put("428", "428");
		routeKeysToTitles.put("429", "429");
		routeKeysToTitles.put("430", "430");
		routeKeysToTitles.put("430G", "430G");
		routeKeysToTitles.put("431", "431");
		routeKeysToTitles.put("434", "434");
		routeKeysToTitles.put("435", "435");
		routeKeysToTitles.put("436", "436");
		routeKeysToTitles.put("439", "439");
		routeKeysToTitles.put("441", "441");
		routeKeysToTitles.put("441W", "441W");
		routeKeysToTitles.put("442", "442");
		routeKeysToTitles.put("442W", "442W");
		routeKeysToTitles.put("448", "448");
		routeKeysToTitles.put("449", "449");
		routeKeysToTitles.put("450", "450");
		routeKeysToTitles.put("450W", "450W");
		routeKeysToTitles.put("451", "451");
		routeKeysToTitles.put("455", "455");
		routeKeysToTitles.put("455W", "455W");
		routeKeysToTitles.put("456", "456");
		routeKeysToTitles.put("459", "459");
		routeKeysToTitles.put("465", "465");
		routeKeysToTitles.put("468", "468");
		routeKeysToTitles.put("500", "500");
		routeKeysToTitles.put("501", "501");
		routeKeysToTitles.put("502", "502");
		routeKeysToTitles.put("503", "503");
		routeKeysToTitles.put("504", "504");
		routeKeysToTitles.put("505", "505");
		routeKeysToTitles.put("553", "553");
		routeKeysToTitles.put("554", "554");
		routeKeysToTitles.put("555", "555");
		routeKeysToTitles.put("556", "556");
		routeKeysToTitles.put("558", "558");
		routeKeysToTitles.put("9109", "9109");
		routeKeysToTitles.put("9111", "9111");
		routeKeysToTitles.put("9501", "9501");
		routeKeysToTitles.put("9507", "9507");
		routeKeysToTitles.put("9701", "9701");
		routeKeysToTitles.put("9702", "9702");
		routeKeysToTitles.put("9703", "9703");

	}
	
	private final String[] routes;
	private final HashMap<String, String> routeKeysToTitles = new HashMap<String, String>();
	
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions) 
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException 
	{
		final String urlString = getRouteConfigUrl(routeToUpdate);

		DownloadHelper downloadHelper = new DownloadHelper(urlString);
		
		downloadHelper.connect();
		//just initialize the route and then end for this round
		
		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, oldRouteConfig,
				this);

		parser.runParse(downloadHelper.getResponseData()); 

		parser.writeToDatabase(routeMapping, false);
		
	}


	@Override
	public void refreshData(RouteConfig routeConfig, int selectedBusPredictions, int maxStops,
			float centerLatitude, float centerLongitude, HashMap<Integer, BusLocation> busMapping, 
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
		task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Decompressing route data...", null));
		
		final int contentLength = 453754;
		
		InputStream in = new StreamCounter(context.getResources().openRawResource(boston.Bus.Map.R.raw.routeconfig),
				task, contentLength); 
		
		GZIPInputStream stream = new GZIPInputStream(in); 
		
		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, null, this);
		
		parser.runParse(stream);
		

		
		parser.writeToDatabase(routeMapping, true);
		
		
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
	

}
