package boston.Bus.Map.parser;

import java.io.IOException;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.CommuterRailTransitSource;

/**
 * This is a fake parser until something more automated is implemented
 * @author schneg
 *
 */
public class CommuterRailRouteConfigParser
{
	private final Directions directions;
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	private final HashMap<String, StopLocation> stops = new HashMap<String, StopLocation>();
	private final CommuterRailTransitSource source;
	private RouteConfig tempCurrentRoute;
	
	public CommuterRailRouteConfigParser(Drawable busStop, Directions directions, RouteConfig oldRouteConfig,
			CommuterRailTransitSource source) 
	{
		this.directions = directions;
		this.source = source;
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe, UpdateAsyncTask task) throws IOException
	{
		routeMapping.writeToDatabase(map, wipe, task);
		directions.writeToDatabase(wipe);
	}

	private void populateStops()
	{
		tempCurrentRoute = map.get("CR-4");

		addStop("Readville", 42.237750,-71.132376);
		addStop("Fairmount", 42.253638,-71.119270);
		addStop("Morton Street", 42.280994,-71.085475);
		addStop("Uphams Corner", 42.318670,-71.069072);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-9");

		addStop("Fitchburg", 42.581739,-71.792750);
		addStop("North Leominster", 42.540577,-71.739402);
		addStop("Shirley", 42.544726,-71.648363);
		addStop("Ayer", 42.560047,-71.590117);
		addStop("Littleton / Rte 495", 42.519236,-71.502643);
		addStop("South Acton", 42.461575,-71.455322);
		addStop("West Concord", 42.456372,-71.392371);
		addStop("Concord", 42.457147,-71.358051);
		addStop("Lincoln", 42.414229,-71.325344);
		addStop("Silver Hill", 42.395625,-71.302357);
		addStop("Hastings", 42.385755,-71.289203);
		addStop("Kendal Green", 42.378970,-71.282411);
		addStop("Brandeis/ Roberts", 42.361728,-71.260854);
		addStop("Waltham", 42.374424,-71.236595);
		addStop("Waverley", 42.387489,-71.189864);
		addStop("Belmont", 42.398420,-71.174499);
		addStop("Porter Square", 42.388353,-71.119159);
		addStop("North Station", 42.365577,-71.06129);


		tempCurrentRoute = map.get("CR-8");

		addStop("Worcester / Union Station", 42.261796,-71.793881);
		addStop("Grafton", 42.246291,-71.684614);
		addStop("Westborough", 42.269184,-71.652005);
		addStop("Southborough", 42.267518,-71.523621);
		addStop("Ashland", 42.261694,-71.478813);
		addStop("Framingham", 42.276719,-71.416792);
		addStop("West Natick", 42.281855,-71.390548);
		addStop("Natick", 42.285239,-71.347641);
		addStop("Wellesley Square", 42.296427,-71.294311);
		addStop("Wellesley Hills", 42.310027,-71.276769);
		addStop("Wellesley Farms", 42.323608,-71.272288);
		addStop("Auburndale", 42.346087,-71.246658);
		addStop("West Newton", 42.348599,-71.229010);
		addStop("Newtonville", 42.351603,-71.207338);
		addStop("Yawkey", 42.346796,-71.098937);
		addStop("Back Bay", 42.347158,-71.075769);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-6");

		addStop("Forge Park / 495", 42.090704,-71.430342);
		addStop("Franklin", 42.083591,-71.396735);
		addStop("Norfolk", 42.120694,-71.325217);
		addStop("Walpole", 42.144192,-71.259016);
		addStop("Plimptonville", 42.159123,-71.236125);
		addStop("Windsor Gardens", 42.172192,-71.220704);
		addStop("Norwood Central", 42.190776,-71.199748);
		addStop("Norwood Depot", 42.195668,-71.196784);
		addStop("Islington", 42.220714,-71.183406);
		addStop("Dedham Corp. Center", 42.225896,-71.173806);
		addStop("Endicott", 42.232881,-71.160413);
		addStop("Readville", 42.237750,-71.132376);
		addStop("Hyde Park", 42.255121,-71.125022);
		addStop("Ruggles", 42.335545,-71.090524);
		addStop("Back Bay", 42.347158,-71.075769);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-1");

		addStop("Greenbush", 42.178100,-70.746200);
		addStop("North Scituate", 42.219700,-70.787700);
		addStop("Cohasset", 42.242400,-70.837000);
		addStop("Nantasket Junction", 42.245200,-70.869800);
		addStop("West Hingham", 42.236700,-70.903100);
		addStop("East Weymouth", 42.219100,-70.921400);
		addStop("Weymouth Landing/ East Braintree", 42.220800,-70.968200);
		addStop("Quincy Center", 42.250862,-71.004843);
		addStop("JFK/UMASS", 42.321123,-71.052555);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-11");

		addStop("Haverhill", 42.772684,-71.085962);
		addStop("Bradford", 42.768899,-71.085998);
		addStop("Lawrence", 42.700094,-71.159797);
		addStop("Andover", 42.657798,-71.144513);
		addStop("Ballardvale", 42.626449,-71.159653);
		addStop("North Wilmington", 42.568462,-71.159724);
		addStop("Wilmington", 42.546368,-71.173569);
		addStop("Anderson/ Woburn", 42.518082,-71.138650);
		addStop("Reading", 42.521480,-71.107440);
		addStop("Wakefield", 42.501811,-71.075000);
		addStop("Greenwood", 42.483473,-71.067233);
		addStop("Melrose Highlands", 42.468793,-71.068270);
		addStop("Melrose Cedar Park", 42.459128,-71.069448);
		addStop("Wyoming Hill", 42.452097,-71.069518);
		addStop("Malden Center", 42.426407,-71.074227);
		addStop("North Station", 42.365577,-71.06129);


		tempCurrentRoute = map.get("CR-2");

		addStop("Kingston", 41.978548,-70.720315);
		addStop("Plymouth", 41.981184,-70.692514);
		addStop("Halifax", 42.012867,-70.820832);
		addStop("Hanson", 42.043262,-70.881553);
		addStop("Whitman", 42.083563,-70.923204);
		addStop("Abington", 42.108034,-70.935296);
		addStop("South Weymouth", 42.153747,-70.952490);
		addStop("Braintree", 42.208550,-71.000850);
		addStop("Quincy Center", 42.250862,-71.004843);
		addStop("JFK/UMASS", 42.321123,-71.052555);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-10");

		addStop("Lowell", 42.638402,-71.314916);
		addStop("North Billerica", 42.592881,-71.280869);
		addStop("Haverhill", 42.772684,-71.085962);
		addStop("Wilmington", 42.546368,-71.173569);
		addStop("Anderson/ Woburn", 42.518082,-71.138650);
		addStop("Mishawum", 42.503595,-71.137511);
		addStop("Winchester Center", 42.452650,-71.137041);
		addStop("Wedgemere", 42.445284,-71.140909);
		addStop("West Medford", 42.421184,-71.132468);
		addStop("North Station", 42.365577,-71.06129);


		tempCurrentRoute = map.get("CR-3");

		addStop("Kingston", 41.978548,-70.720315);
		addStop("Plymouth", 41.981184,-70.692514);
		addStop("Halifax", 42.012867,-70.820832);
		addStop("Hanson", 42.043262,-70.881553);
		addStop("Whitman", 42.083563,-70.923204);
		addStop("Abington", 42.108034,-70.935296);
		addStop("South Weymouth", 42.153747,-70.952490);
		
		addStop("Middleboro/ Lakeville", 41.878210,-70.918444);
		addStop("Bridgewater", 41.986355,-70.966625);
		addStop("Campello", 42.060038,-71.012460);
		addStop("Brockton", 42.085720,-71.016860);
		addStop("Montello", 42.106047,-71.021078);
		addStop("Holbrook/ Randolph", 42.155314,-71.027518);
		addStop("Braintree", 42.208550,-71.000850);
		addStop("Quincy Center", 42.250862,-71.004843);
		addStop("JFK/UMASS", 42.321123,-71.052555);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-7");

		addStop("Needham Heights", 42.293139,-71.235087);
		addStop("Needham Center", 42.280274,-71.238089);
		addStop("Needham Junction", 42.273327,-71.238007);
		addStop("Hersey", 42.275842,-71.214853);
		addStop("West Roxbury", 42.281600,-71.159932);
		addStop("Highland", 42.284869,-71.154700);
		addStop("Bellevue", 42.287138,-71.146060);
		addStop("Roslindale Village", 42.287206,-71.129610);
		addStop("Forest Hills", 42.300023,-71.113377);
		addStop("Ruggles", 42.335545,-71.090524);
		addStop("Back Bay", 42.347158,-71.075769);
		addStop("South Station", 42.352271,-71.055242);


		tempCurrentRoute = map.get("CR-12");

		addStop("Rockport", 42.656173,-70.625616);
		addStop("Gloucester", 42.616069,-70.668767);
		addStop("West Gloucester", 42.610928,-70.706456);
		addStop("Manchester", 42.573570,-70.770473);
		addStop("Beverly Farms", 42.561403,-70.812745);
		addStop("Prides Crossing", 42.559513,-70.824813);
		addStop("Montserrat", 42.561483,-70.870035);
		addStop("Newburyport", 42.800292,-70.880262);
		addStop("Rowley", 42.725351,-70.859436);
		addStop("Ipswich", 42.678355,-70.840024);
		addStop("Hamilton/ Wenham", 42.610756,-70.874005);
		addStop("North Beverly", 42.582471,-70.884501);
		addStop("Beverly", 42.546907,-70.885168);
		addStop("Salem", 42.523927,-70.898903);
		addStop("Swampscott", 42.473739,-70.922036);
		addStop("Lynn", 42.462293,-70.947794);
		addStop("River Works", 42.453804,-70.975698);
		addStop("Chelsea", 42.395661,-71.034826);
		addStop("North Station", 42.365577,-71.06129);


		tempCurrentRoute = map.get("CR-5");

		addStop("TF Green Airport", 41.726599,-71.442453);
		addStop("Providence", 41.829641,-71.413332);
		addStop("South Attleboro", 41.897943,-71.354621);
		addStop("Attleboro", 41.942097,-71.284897);
		addStop("Mansfield", 42.032734,-71.219318);
		addStop("Sharon", 42.124804,-71.183213);
		addStop("Stoughton", 42.123818,-71.103090);
		addStop("Canton Center", 42.156769,-71.145530);
		addStop("Canton Junction", 42.163423,-71.153374);
		addStop("Route 128", 42.209884,-71.147100);
		addStop("Hyde Park", 42.255121,-71.125022);
		addStop("Ruggles", 42.335545,-71.090524);
		addStop("Back Bay", 42.347158,-71.075769);
		addStop("South Station", 42.352271,-71.055242);

		tempCurrentRoute = null;
	}
	
	private void addStop(String stopTitle, double lat, double lon)
	{
		String key = "CRK-" + stopTitle;
		StopLocation stopLocation = stops.get(key);
		if (stopLocation == null)
		{
			stopLocation = source.createStop((float)lat, (float)lon, key, stopTitle, 0, null, tempCurrentRoute.getRouteName(), null);
			stops.put(key, stopLocation);
		}
		else
		{
			stopLocation.addRouteAndDirTag(tempCurrentRoute.getRouteName(), null);
		}
		tempCurrentRoute.addStop(stopLocation.getStopTag(), stopLocation);
	}

	public void runParse() throws IOException
	{
		
		//StopLocation stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees, busStop, tag, title);
		
		for (String route : source.getRoutes())
		{
			map.put(route, new RouteConfig(route, 0, 0, source));
		}
		populateStops();

		
	}
	
}
