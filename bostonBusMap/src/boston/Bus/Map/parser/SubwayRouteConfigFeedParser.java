package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSystem;

import android.graphics.drawable.Drawable;

public class SubwayRouteConfigFeedParser
{
	public static final String RedNorthToAlewife = "RedNB0";
	public static final String RedNorthToAlewife2 = "RedNB1";
	public static final String RedSouthToBraintree = "RedSB0";
	public static final String RedSouthToAshmont = "RedSB1";
	public static final String BlueEastToWonderland = "BlueEB0";
	public static final String BlueWestToBowdoin = "BlueWB0";
	public static final String OrangeNorthToOakGrove = "OrangeNB0";
	public static final String OrangeSouthToForestHills = "OrangeSB0";
	private final Drawable busStop;
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	
	private final HashMap<String, Integer> indexes = new HashMap<String, Integer>();
	private final Directions directions;
	
	private final SubwayTransitSource transitSource;

	public SubwayRouteConfigFeedParser(Drawable busStop,
			Directions directions, RouteConfig oldRouteConfig,
			SubwayTransitSource transitSource) {
		this.busStop = busStop;
		this.directions = directions;
		this.transitSource = transitSource;
	}

	private int getOrder(String route, int platformOrder)
	{
		platformOrder <<= 6;
		if (route.equals(SubwayTransitSource.RedLine))
		{
			platformOrder |= 1;
		}
		else if (route.equals(SubwayTransitSource.BlueLine))
		{
			platformOrder |= 2;
		}
		else if (route.equals(SubwayTransitSource.OrangeLine))
		{
			platformOrder |= 4;
		}
		
		return platformOrder;
	}
	
	public void runParse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String[] definitions = reader.readLine().split(",");
		
		for (int i = 0; i < definitions.length; i++)
		{
			indexes.put(definitions[i], i);
		}
		
		/**route to a mapping of
		     direction + branch to a mapping of
		       platform order numbers to stops
		 * 
		 */
		HashMap<String, HashMap<String, TreeMap<Short, StopLocation>>> orderedStations =
			new HashMap<String, HashMap<String, TreeMap<Short, StopLocation>>>();
		
		HashMap<String, String> pathDirection = new HashMap<String, String>();
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] elements = line.split(",");
			if (elements.length != definitions.length)
			{
				break;
			}
			
			//ensure route exists
			String routeName = elements[indexes.get("Line")];
			RouteConfig routeConfig = map.get(routeName);
			if (routeConfig == null)
			{
				routeConfig = new RouteConfig(routeName, SubwayTransitSource.getSubwayColor(routeName),
						SubwayTransitSource.BlueColor,
						transitSource);
				map.put(routeName, routeConfig);
			}
			
			//create stop location
			short platformOrder = Short.parseShort(elements[indexes.get("PlatformOrder")]);
			float latitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lat")]);
			float longitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lon")]);
			String tag = elements[indexes.get("PlatformKey")];
			String title = elements[indexes.get("stop_name")];
			String branch = elements[indexes.get("Branch")];

			StopLocation stopLocation = new SubwayStopLocation(latitudeAsDegrees, longitudeAsDegrees,
					busStop, tag, title, platformOrder, branch);

			String dirTag = routeConfig.getRouteName() + elements[indexes.get("Direction")];
			stopLocation.addRouteAndDirTag(routeConfig.getRouteName(), dirTag);
			routeConfig.addStop(tag, stopLocation);
			
			
			HashMap<String, TreeMap<Short, StopLocation>> innerMapping = orderedStations.get(routeName);
			if (innerMapping == null)
			{
				innerMapping = new HashMap<String, TreeMap<Short, StopLocation>>();
				orderedStations.put(routeName, innerMapping);
			}
			
			//mapping of (direction plus branch plus platform order) to a stop
			//for example, key is NBAshmont3 for fields corner
			
			String combinedDirectionBranch = elements[indexes.get("Direction")] + elements[indexes.get("Branch")];
			TreeMap<Short, StopLocation> innerInnerMapping = innerMapping.get(combinedDirectionBranch);
			if (innerInnerMapping == null)
			{
				innerInnerMapping = new TreeMap<Short, StopLocation>();
				innerMapping.put(combinedDirectionBranch, innerInnerMapping);
			}
			
			innerInnerMapping.put(platformOrder, stopLocation);
		}
		
		//workaround
		directions.add(RedNorthToAlewife, "North toward Alewife", null, SubwayTransitSource.RedLine);
		directions.add(RedNorthToAlewife2, "North toward Alewife", null, SubwayTransitSource.RedLine);
		directions.add(RedSouthToBraintree, "South toward Braintree", null, SubwayTransitSource.RedLine);
		directions.add(RedSouthToAshmont, "South toward Ashmont", null, SubwayTransitSource.RedLine);
		directions.add(BlueEastToWonderland, "East toward Wonderland", null, SubwayTransitSource.BlueLine);
		directions.add(BlueWestToBowdoin, "West toward Bowdoin", null, SubwayTransitSource.BlueLine);
		directions.add(OrangeNorthToOakGrove, "North toward Oak Grove", null, SubwayTransitSource.OrangeLine);
		directions.add(OrangeSouthToForestHills, "South toward Forest Hills", null, SubwayTransitSource.OrangeLine);
		
		//path
		for (String route : orderedStations.keySet())
		{
			
			HashMap<String, TreeMap<Short, StopLocation>> innerMapping = orderedStations.get(route);
			for (String directionHash : innerMapping.keySet())
			{
				TreeMap<Short, StopLocation> stations = innerMapping.get(directionHash);

				ArrayList<Float> floats = new ArrayList<Float>();
				for (Short platformOrder : stations.keySet())
				{
					StopLocation station = stations.get(platformOrder);

					floats.add((float)station.getLatitudeAsDegrees());
					floats.add((float)station.getLongitudeAsDegrees());
				}
				
				//this is kind of a hack. We need to connect the southern branches of the red line to JFK manually
				if (directionHash.equals("NBAshmont") || directionHash.equals("NBBraintree"))
				{
					final short jfkNorthBoundOrder = 5;
					StopLocation jfkStation = innerMapping.get("NBTrunk").get(jfkNorthBoundOrder);
					if (jfkStation != null)
					{
						floats.add((float)jfkStation.getLatitudeAsDegrees());
						floats.add((float)jfkStation.getLongitudeAsDegrees());
					}
				}
				Path path = new Path(floats);
				Path[] paths = new Path[] {path};
				map.get(route).setPaths(paths);
			}
		}
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe, UpdateAsyncTask task) throws IOException {
		routeMapping.writeToDatabase(map, wipe, task);
		directions.writeToDatabase(wipe);
	}
}
