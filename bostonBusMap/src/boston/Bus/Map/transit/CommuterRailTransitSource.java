package boston.Bus.Map.transit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
import boston.Bus.Map.parser.AlertParser;
import boston.Bus.Map.parser.CommuterRailPredictionsFeedParser;
import boston.Bus.Map.parser.CommuterRailRouteConfigParser;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.parser.SubwayRouteConfigFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;

public class CommuterRailTransitSource implements TransitSource {
	public static final String stopTagPrefix = "CRK-";
	private final Drawable busStop;
	private final Drawable rail;
	private final Drawable railArrow;
	private final ArrayList<String> routes = new ArrayList<String>(12);
	private final HashMap<String, String> routeKeysToTitles = new HashMap<String, String>(12);
	private static final String predictionsUrlSuffix = ".csv";
	public static final String routeTagPrefix = "CR-";
	private static final String dataUrlPrefix = "http://developer.mbta.com/lib/RTCR/RailLine_";
	private static final String alertUrlPrefix = "http://talerts.com/rssfeed2/alertsrss.aspx?";
	
	/*private static final String hardcodedData = "TimeStamp,Trip,Destination,Stop,Scheduled,Flag,Vehicle,Latitude,Longitude,Heading,Speed,Lateness\n"+
	"1308201549,P517,Worcester / Union Station,South Station,1308249600,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Back Bay,1308249960,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Newtonville,1308250680,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,West Newton,1308250860,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Auburndale,1308251040,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Wellesley Farms,1308251340,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Wellesley Hills,1308251520,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Wellesley Square,1308251760,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Natick,1308252120,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,West Natick,1308252420,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Framingham,1308252780,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Ashland,1308253140,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Southborough,1308253440,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Westborough,1308253980,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Grafton,1308254340,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P517,Worcester / Union Station,Worcester / Union Station,1308255180,sch,1508,41.88866,-71.38521,184,49,\n"+
	"1308201549,P523,Worcester / Union Station,South Station,1308258000,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Back Bay,1308258360,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,West Natick,1308259860,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Framingham,1308260220,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Ashland,1308260580,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Southborough,1308260880,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Westborough,1308261480,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Grafton,1308261900,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P523,Worcester / Union Station,Worcester / Union Station,1308262800,sch,1701,42.26207,-71.79092,255,14,\n"+
	"1308201549,P527,Worcester / Union Station,South Station,1308260100,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Back Bay,1308260460,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Yawkey,1308260760,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Newtonville,1308261360,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,West Newton,1308261600,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Auburndale,1308261780,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Wellesley Farms,1308262080,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Wellesley Hills,1308262260,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Wellesley Square,1308262500,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Natick,1308262860,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,West Natick,1308263160,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Framingham,1308263460,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Ashland,1308263880,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Southborough,1308264180,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Westborough,1308264720,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Grafton,1308265080,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P527,Worcester / Union Station,Worcester / Union Station,1308265980,sch,1715,42.26215,-71.79059,256,18,\n"+
	"1308201549,P533,Worcester / Union Station,South Station,1308266100,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Back Bay,1308266460,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Yawkey,1308266760,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Newtonville,1308267300,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,West Newton,1308267540,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Auburndale,1308267720,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Wellesley Farms,1308268020,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Wellesley Hills,1308268200,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Wellesley Square,1308268440,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Natick,1308268800,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,West Natick,1308269160,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Framingham,1308269460,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Ashland,1308269880,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Southborough,1308270180,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Westborough,1308270720,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Grafton,1308271080,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P533,Worcester / Union Station,Worcester / Union Station,1308271920,sch,1523,42.26177,-71.79212,248,20,\n"+
	"1308201549,P535,Worcester / Union Station,South Station,1308270000,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Back Bay,1308270360,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Yawkey,1308270660,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Newtonville,1308271200,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,West Newton,1308271440,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Auburndale,1308271620,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Wellesley Farms,1308271920,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Wellesley Hills,1308272100,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Wellesley Square,1308272340,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Natick,1308272700,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,West Natick,1308273060,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Framingham,1308273360,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Ashland,1308273720,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Southborough,1308274020,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Westborough,1308274560,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Grafton,1308274920,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P535,Worcester / Union Station,Worcester / Union Station,1308275760,sch,1529,42.26245,-71.7898,253,,\n"+
	"1308201549,P537,Worcester / Union Station,South Station,1308277200,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Back Bay,1308277560,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Yawkey,1308277860,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Newtonville,1308278400,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,West Newton,1308278640,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Auburndale,1308278820,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Wellesley Farms,1308279120,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Wellesley Hills,1308279300,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Wellesley Square,1308279540,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Natick,1308279900,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,West Natick,1308280260,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Framingham,1308280560,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Ashland,1308280920,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Southborough,1308281220,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Westborough,1308281760,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Grafton,1308282120,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P537,Worcester / Union Station,Worcester / Union Station,1308282960,sch,1526,42.34754,-71.08391,90,2,\n"+
	"1308201549,P538,South Station,Framingham,1308288660,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,West Natick,1308288960,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Natick,1308289200,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Wellesley Square,1308289500,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Wellesley Hills,1308289680,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Wellesley Farms,1308289860,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Auburndale,1308290100,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,West Newton,1308290280,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Newtonville,1308290460,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,Back Bay,1308291420,sch,1515,42.34801,-71.07433,26,23,\n"+
	"1308201549,P538,South Station,South Station,1308291780,sch,1515,42.34801,-71.07433,26,23,\n";
	
	private static final String hardcodedAlerts = "<?xml version=\"1.0\" encoding=\"utf-8\"?><rss version=\"2.0\"><channel><title>T-Alerts</title><link>http://www.mbta.com/rider_tools/transit_updates/</link><description>Recent MBTA T-Alerts</description><language>en-us</language><generator>eAlert Messaging System  http://www.ealet.com</generator><webMaster>hostmaster@mis-sciences.com</webMaster><ttl>15</ttl><item><title>Fairmount</title><link>http://www.mbta.com/rider_tools/transit_updates/</link><description>All Southside Lines/Routes experiencing delays due to Boston Bruins Victory Rolling Rally today. Please allow extra time for your commute. 6/18/2011 10:00 AM</description><pubDate>Sat, 18 Jun 2011 14:00:15 GMT</pubDate><guid isPermaLink=\"false\">talerts135794134794152229</guid></item><item><title>Fairmount</title><link>http://www.mbta.com/rider_tools/transit_updates/</link><description>Fairmount Line \n"+
	" Tie Replacement Work \n"+
	" Schedules Affected \n"+
	"\n"+
	" Beginning Saturday June 11th and continuing until mid September tie replacement will be done between Back Bay and Forest Hills. \n"+
	"\n"+
	" This will impact the schedules of Trains on the Fairmount Line. \n"+
	"\n"+
	" Please click on the link below to obtain a printable copy of the temporary schedule which will be in effect during the tie replacement work. \n"+
	"\n"+
	" Updated PDF Schedule - Fairmount\n"+
	"\n"+
	" We apologize for any inconvenience that may result from this project.</description><pubDate>Thu, 09 Jun 2011 20:45:47 GMT</pubDate><guid isPermaLink=\"false\">talerts134354133354150580</guid></item><item><title>Fairmount</title><link>http://www.mbta.com/rider_tools/transit_updates/</link><description>Fairmount Line \n"+
	" Fairmount and Morton Street Stations \n"+
	" Outbound Platforms Removed From Service \n"+
	"\n"+
	" Beginning Monday March 28th and continuing until further notice, the Outbound platforms at Fairmount and Morton Street stations will be closed because of continuing track and station improvements. \n"+
	"\n"+
	" All trains at these stations will be boarded on the Inbound platform. Signs directing passengers to the Inbound platforms have been posted. \n"+
	"\n"+
	" We apologize for any inconvenience this may cause while the improvements to the Fairmount Line continue.</description><pubDate>Fri, 25 Mar 2011 14:35:11 GMT</pubDate><guid isPermaLink=\"false\">talerts122533121533136919</guid></item></channel></rss>\n";
	*/
	private final HashMap<String, String> routeKeysToAlertUrls = new HashMap<String, String>();
	
	public CommuterRailTransitSource(Drawable busStop, Drawable rail, Drawable railArrow)
	{
		this.busStop = busStop;
		this.rail = rail;
		this.railArrow = railArrow;
		
		addRoute(routeTagPrefix + "1","Greenbush Line");
		addRoute(routeTagPrefix + "2","Kingston/Plymouth Line");
		addRoute(routeTagPrefix + "3","Middleborough/Lakeville Line");
		addRoute(routeTagPrefix + "4","Fairmount Line");
		addRoute(routeTagPrefix + "5","Providence/Stoughton Line");
		addRoute(routeTagPrefix + "6","Franklin Line");
		addRoute(routeTagPrefix + "7","Needham Line");
		addRoute(routeTagPrefix + "8","Framingham/Worcester Line");
		addRoute(routeTagPrefix + "9","Fitchburg/South Acton Line");
		addRoute(routeTagPrefix + "10","Lowell Line");
		addRoute(routeTagPrefix + "11","Haverhill Line");
		addRoute(routeTagPrefix + "12","Newburyport/Rockport Line");
		
		addAlert(routeTagPrefix + "1", 232);
		addAlert(routeTagPrefix + "2", 12);
		addAlert(routeTagPrefix + "3", 9);
		addAlert(routeTagPrefix + "4", 1);
		addAlert(routeTagPrefix + "5", 14);
		addAlert(routeTagPrefix + "6", 5);
		addAlert(routeTagPrefix + "7", 10);
		addAlert(routeTagPrefix + "8", 4);
		addAlert(routeTagPrefix + "9", 2);
		addAlert(routeTagPrefix + "10", 8);
		addAlert(routeTagPrefix + "11", 7);
		addAlert(routeTagPrefix + "12", 11);
	}
	
	private void addAlert(String routeKey, int alertNum) {
		routeKeysToAlertUrls.put(routeKey, alertUrlPrefix + alertNum);
	}

	private void addRoute(String key, String title) {
		routeKeysToTitles.put(key, title);
		routes.add(key);
	}

	public static String getRouteConfigUrl()
	{

		return null;
	}


	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions,
			UpdateAsyncTask task) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException
	{
		
		//this will probably never be executed
		//final String urlString = getRouteConfigUrl();

		//DownloadHelper downloadHelper = new DownloadHelper(urlString);
		
		//downloadHelper.connect();
		//just initialize the route and then end for this round
		
		CommuterRailRouteConfigParser parser = new CommuterRailRouteConfigParser(busStop,
				directions, oldRouteConfig, this);

		//parser.runParse(downloadHelper.getResponseData()); 
		parser.runParse(new StringReader(CommuterRailRouteConfigParser.temporaryInputData));

		parser.writeToDatabase(routeMapping, false, task);
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, double centerLatitude,
			double centerLongitude,
			ConcurrentHashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj) throws IOException,
			ParserConfigurationException, SAXException
	{
		ArrayList<String> outputUrls = new ArrayList<String>();
		ArrayList<String> outputAlertUrls = new ArrayList<String>();
		ArrayList<String> outputRoutes = new ArrayList<String>();
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		case Main.VEHICLE_LOCATIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);

			//ok, do predictions now
			getPredictionsUrl(locations, maxStops, routeConfig.getRouteName(), outputUrls, outputAlertUrls, outputRoutes, selectedBusPredictions);
			break;
		}
		case Main.BUS_PREDICTIONS_ALL:
		case Main.VEHICLE_LOCATIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false);
			
			getPredictionsUrl(locations, maxStops, null, outputUrls, outputAlertUrls, outputRoutes, selectedBusPredictions);

		}
		break;

		}

		Log.v("BostonBusMap", "refreshing commuter data for " + outputUrls.size() + " routes");

		for (int i = 0; i < outputUrls.size(); i++)
		{
			String url = outputUrls.get(i);
			DownloadHelper downloadHelper = new DownloadHelper(url);
			
			downloadHelper.connect();
			

			InputStream stream = downloadHelper.getResponseData();
			InputStreamReader data = new InputStreamReader(stream);
			//StringReader data = new StringReader(hardcodedData);

			//bus prediction

			String route = outputRoutes.get(i);
			RouteConfig railRouteConfig = routePool.get(route);
			CommuterRailPredictionsFeedParser parser = new CommuterRailPredictionsFeedParser(railRouteConfig, directions,
					rail, railArrow, busMapping);

			parser.runParse(data);
			data.close();
		}
		
		for (int i = 0; i < outputAlertUrls.size(); i++)
		{
			String url = outputAlertUrls.get(i);
			DownloadHelper downloadHelper = new DownloadHelper(url);
			downloadHelper.connect();
			
			InputStream stream = downloadHelper.getResponseData();
			InputStreamReader data = new InputStreamReader(stream);
			
			String route = outputRoutes.get(i);
			RouteConfig railRouteConfig = routePool.get(route);
			AlertParser parser = new AlertParser();
			parser.runParse(data);
			railRouteConfig.setAlerts(parser.getAlerts());
			data.close();
		}
		
	}

	private void getPredictionsUrl(List<Location> locations, int maxStops,
			String routeName, ArrayList<String> outputUrls, ArrayList<String> outputAlertUrls,
			ArrayList<String> outputRoutes, int mode)
	{
		//http://developer.mbta.com/lib/RTCR/RailLine_1.csv
		
		//BUS_PREDICTIONS_ONE or VEHICLE_LOCATIONS_ONE
		if (routeName != null)
		{
			//we know we're updating only one route
			if (isCommuterRail(routeName))
			{
				String index = routeName.substring(routeTagPrefix.length()); //snip off beginning "CR-"
				outputUrls.add(dataUrlPrefix + index + predictionsUrlSuffix);
				String alertUrl = routeKeysToAlertUrls.get(routeName);
				outputAlertUrls.add(alertUrl);
				outputRoutes.add(routeName);
				return;
			}
		}
		else
		{
			if (mode == Main.BUS_PREDICTIONS_STAR)
			{
				//ok, let's look at the locations and see what we can get
				for (Location location : locations)
				{
					if (location instanceof StopLocation)
					{
						StopLocation stopLocation = (StopLocation)location;


						for (String route : stopLocation.getRoutes())
						{
							if (isCommuterRail(route) && outputRoutes.contains(route) == false)
							{
								String index = route.substring(routeTagPrefix.length());
								outputUrls.add(dataUrlPrefix + index + predictionsUrlSuffix);
								String alertUrl = routeKeysToAlertUrls.get(route);
								outputAlertUrls.add(alertUrl);
								outputRoutes.add(route);
							}
						}
					}
					else
					{
						//bus location
						BusLocation busLocation = (BusLocation)location;
						String route = busLocation.getRouteId();

						if (isCommuterRail(route) && outputRoutes.contains(route) == false)
						{
							String index = route.substring(3);
							outputUrls.add(dataUrlPrefix + index + predictionsUrlSuffix);
							String alertUrl = routeKeysToAlertUrls.get(route);
							outputAlertUrls.add(alertUrl);
							outputRoutes.add(route);
						}
					}
				}
			}
			else
			{
				//add all 12 of them
				
				for (int i = 1; i <= 12; i++)
				{
					outputUrls.add(dataUrlPrefix + i + predictionsUrlSuffix);
					String routeKey = routeTagPrefix + i;
					String alertUrl = routeKeysToAlertUrls.get(routeKey);
					
					outputAlertUrls.add(alertUrl);
					outputRoutes.add(routeKey);
				}
			}
		}
	}

	private boolean isCommuterRail(String routeName) {
		for (String route : routes)
		{
			if (route.equals(routeName))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasPaths() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException {
		task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Downloading commuter info", null));
		//final String subwayUrl = getRouteConfigUrl();
		//URL url = new URL(subwayUrl);
		//InputStream in = Locations.downloadStream(url, task);
		
		CommuterRailRouteConfigParser subwayParser = new CommuterRailRouteConfigParser(busStop, directions, null, this);
		
		subwayParser.runParse(new StringReader(CommuterRailRouteConfigParser.temporaryInputData));
		
		subwayParser.writeToDatabase(routeMapping, false, task);
		
		
	}

	@Override
	public String[] getRoutes() {
		return routes.toArray(new String[0]);
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
		SubwayStopLocation stopLocation = new SubwayStopLocation(lat, lon, busStop, stopTag, title,
				platformOrder, branch);
		stopLocation.addRouteAndDirTag(route, dirTag);
		return stopLocation;
	}

	@Override
	public void bindPredictionElementsForUrl(StringBuilder urlString,
			String route, String stopTag, String dirTag) {
		//do nothing
	}

}
