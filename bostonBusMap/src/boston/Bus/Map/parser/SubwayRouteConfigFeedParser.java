package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;

import android.graphics.drawable.Drawable;

public class SubwayRouteConfigFeedParser
{
	private final Drawable busStop;
	private final HashMap<String, String> routeKeysToTitles;
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	
	private final HashMap<String, Integer> indexes = new HashMap<String, Integer>();
	private final Directions directions;

	public SubwayRouteConfigFeedParser(Drawable busStop,
			HashMap<String, String> routeKeysToTitles, Directions directions) {
		this.busStop = busStop;
		this.routeKeysToTitles = routeKeysToTitles;
		this.directions = directions;
	}

	public void runParse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String[] definitions = reader.readLine().split(",");
		
		for (int i = 0; i < definitions.length; i++)
		{
			indexes.put(definitions[i], i);
		}
		
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
				routeConfig = new RouteConfig(routeName);
				map.put(routeName, routeConfig);
			}
			
			//create stop location
			float latitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lat")]);
			float longitudeAsDegrees = Float.parseFloat(elements[indexes.get("stop_lon")]);
			String tag = elements[indexes.get("PlatformKey")];
			String title = elements[indexes.get("stop_name")];

			StopLocation stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees,
					busStop, tag, title, routeKeysToTitles);
			stopLocation.addRoute(routeConfig);
			routeConfig.addStop(tag, stopLocation);
		}
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe) throws IOException {
		routeMapping.writeToDatabase(map, wipe);
	}
}
