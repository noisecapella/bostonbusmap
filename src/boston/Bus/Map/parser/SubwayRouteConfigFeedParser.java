package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.MyTreeMap;
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
	private final MyHashMap<String, RouteConfig> map = new MyHashMap<String, RouteConfig>();
	
	private final MyHashMap<String, Integer> indexes = new MyHashMap<String, Integer>();
	private final Directions directions;
	
	private final SubwayTransitSource transitSource;

	public SubwayRouteConfigFeedParser(Directions directions, RouteConfig oldRouteConfig,
			SubwayTransitSource transitSource) {
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 2048);
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
		MyHashMap<String, MyHashMap<String, MyTreeMap<Short, StopLocation>>> orderedStations =
			new MyHashMap<String, MyHashMap<String, MyTreeMap<Short, StopLocation>>>();
		
		MyHashMap<String, String> routeKeysToTitles = transitSource.getRouteKeysToTitles();
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
				String routeTitle = routeKeysToTitles.get(routeName);
				routeConfig = new RouteConfig(routeName, routeTitle, SubwayTransitSource.getSubwayColor(routeName),
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
					transitSource.getDrawables(), tag, title, platformOrder, branch);

			String dirTag = routeConfig.getRouteName() + elements[indexes.get("Direction")];
			stopLocation.addRouteAndDirTag(routeConfig.getRouteName(), dirTag);
			routeConfig.addStop(tag, stopLocation);
			
			
			MyHashMap<String, MyTreeMap<Short, StopLocation>> innerMapping = orderedStations.get(routeName);
			if (innerMapping == null)
			{
				innerMapping = new MyHashMap<String, MyTreeMap<Short, StopLocation>>();
				orderedStations.put(routeName, innerMapping);
			}
			
			//mapping of (direction plus branch plus platform order) to a stop
			//for example, key is NBAshmont3 for fields corner
			
			String combinedDirectionBranch = elements[indexes.get("Direction")] + elements[indexes.get("Branch")];
			MyTreeMap<Short, StopLocation> innerInnerMapping = innerMapping.get(combinedDirectionBranch);
			if (innerInnerMapping == null)
			{
				innerInnerMapping = new MyTreeMap<Short, StopLocation>();
				innerMapping.put(combinedDirectionBranch, innerInnerMapping);
			}
			
			innerInnerMapping.put(platformOrder, stopLocation);
		}
		
		//workaround
		directions.add(RedNorthToAlewife, new Direction("North toward Alewife", null, SubwayTransitSource.RedLine, true));
		directions.add(RedNorthToAlewife2, new Direction("North toward Alewife", null, SubwayTransitSource.RedLine, true));
		directions.add(RedSouthToBraintree, new Direction("South toward Braintree", null, SubwayTransitSource.RedLine, true));
		directions.add(RedSouthToAshmont, new Direction("South toward Ashmont", null, SubwayTransitSource.RedLine, true));
		directions.add(BlueEastToWonderland, new Direction("East toward Wonderland", null, SubwayTransitSource.BlueLine, true));
		directions.add(BlueWestToBowdoin, new Direction("West toward Bowdoin", null, SubwayTransitSource.BlueLine, true));
		directions.add(OrangeNorthToOakGrove, new Direction("North toward Oak Grove", null, SubwayTransitSource.OrangeLine, true));
		directions.add(OrangeSouthToForestHills, new Direction("South toward Forest Hills", null, SubwayTransitSource.OrangeLine, true));
		
		//path
		for (String route : orderedStations.keySet())
		{
			
			MyHashMap<String, MyTreeMap<Short, StopLocation>> innerMapping = orderedStations.get(route);
			for (String directionHash : innerMapping.keySet())
			{
				MyTreeMap<Short, StopLocation> stations = innerMapping.get(directionHash);

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
				
				map.get(route).addPaths(path);
			}
		}
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe, UpdateAsyncTask task, boolean silent) throws IOException {
		routeMapping.writeToDatabase(map, wipe, task, silent);
		directions.writeToDatabase(wipe);
	}
}
