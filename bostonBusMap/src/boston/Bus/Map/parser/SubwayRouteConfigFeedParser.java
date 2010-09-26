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
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSystem;

import android.graphics.drawable.Drawable;

public class SubwayRouteConfigFeedParser
{
	private final Drawable busStop;
	private final HashMap<String, String> routeKeysToTitles;
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	
	private final HashMap<String, Integer> indexes = new HashMap<String, Integer>();
	private final Directions directions;
	
	private final SubwayTransitSource transitSource;

	public SubwayRouteConfigFeedParser(Drawable busStop,
			HashMap<String, String> routeKeysToTitles, Directions directions, RouteConfig oldRouteConfig,
			SubwayTransitSource transitSource) {
		this.busStop = busStop;
		this.routeKeysToTitles = routeKeysToTitles;
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
		
		HashMap<String, TreeMap<Integer, StopLocation>> orderedStations =
			new HashMap<String, TreeMap<Integer, StopLocation>>();
		
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
			float latitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lat")]);
			float longitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lon")]);
			String tag = elements[indexes.get("PlatformKey")];
			String title = elements[indexes.get("stop_name")];

			StopLocation stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees,
					busStop, tag, title, routeKeysToTitles);

			String dirTag = routeConfig.getRouteName() + elements[indexes.get("Direction")];
			stopLocation.addRouteAndDirTag(routeConfig.getRouteName(), dirTag);
			routeConfig.addStop(tag, stopLocation);
			
			int platformOrder = Integer.parseInt(elements[indexes.get("PlatformOrder")]);
			
			TreeMap<Integer, StopLocation> innerMapping = orderedStations.get(routeName);
			if (innerMapping == null)
			{
				innerMapping = new TreeMap<Integer, StopLocation>();
				orderedStations.put(routeName, innerMapping);
			}
			
			innerMapping.put(platformOrder, stopLocation);
		}
		
		//workaround
		directions.add("RedNB0", "North", null, SubwayTransitSource.RedLine);
		directions.add("RedSB0", "South to Braintree", null, SubwayTransitSource.RedLine);
		directions.add("RedSB1", "South to Ashmont", null, SubwayTransitSource.RedLine);
		directions.add("BlueEB0", "East", null, SubwayTransitSource.BlueLine);
		directions.add("BlueWB0", "West", null, SubwayTransitSource.BlueLine);
		directions.add("OrangeNB0", "North", null, SubwayTransitSource.OrangeLine);
		directions.add("OrangeSB0", "South", null, SubwayTransitSource.OrangeLine);
		
		//path
		for (String route : orderedStations.keySet())
		{
			ArrayList<Float> floats = new ArrayList<Float>();
			TreeMap<Integer, StopLocation> stations = orderedStations.get(route);
			for (Integer key : stations.keySet())
			{
				StopLocation stopLocation = stations.get(key);
				floats.add((float)stopLocation.getLatitudeAsDegrees());
				floats.add((float)stopLocation.getLongitudeAsDegrees());
			}
			
			map.get(route).addPath(new Path(floats));
		}
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe) throws IOException {
		routeMapping.writeToDatabase(map, wipe);
	}
}
