package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.MBTABusTransitSource;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.FeedException;


import android.graphics.drawable.Drawable;

public class RouteConfigFeedParser extends DefaultHandler
{
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	private final Directions directions;
	private final HashMap<String, String> routeKeysToTitles;
	private final RouteConfig oldRouteConfig;
	
	public RouteConfigFeedParser(Drawable busStop, Directions directions, HashMap<String, String> routeKeysToTitles,
			RouteConfig oldRouteConfig)
	{
		this.busStop = busStop;
		this.directions = directions;
		this.routeKeysToTitles = routeKeysToTitles;
		this.oldRouteConfig = oldRouteConfig;
		
		if (oldRouteConfig != null)
		{
			allStops.putAll(oldRouteConfig.getStopMapping());
		}
		transitSource = TransitSystem.getTransitSource(oldRouteConfig != null ? oldRouteConfig.getRouteName() : null);
	}

	public void runParse(InputStream inputStream)  throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(inputStream));
	}
	
	private final Drawable busStop;
	
	private static final String routeKey = "route";
	private static final String directionKey = "direction";
	private static final String stopKey = "stop";

	private static final String stopIdKey = "stopId";
	private static final String tagKey = "tag";
	private static final String latitudeKey = "lat";
	private static final String longitudeKey = "lon";
	private static final String titleKey = "title";
	private static final String dirTagKey = "dirTag";
	private static final String nameKey = "name";
	private static final String pathKey = "path";
	private static final String pointKey = "point";
	private static final String latKey = "lat";
	private static final String lonKey = "lon";
	
	private static final String colorKey = "color";
	private static final String oppositeColorKey = "oppositeColor";
	
	
	private HashMap<String, StopLocation> allStops = new HashMap<String, StopLocation>();
	
	private boolean inRoute;
	private boolean inDirection;
	private boolean inStop;
	private boolean inPath;
	
	private RouteConfig currentRouteConfig;
	private String currentRoute;
	
	private ArrayList<Float> currentPathPoints;
	private final TransitSource transitSource;
	
	 
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (stopKey.equals(localName))
		{
			inStop = true;
			
			if (inRoute)
			{

				if (inDirection == false)
				{
					String tag = attributes.getValue(tagKey);

					float latitudeAsDegrees = Float.parseFloat(attributes.getValue(latitudeKey));
					float longitudeAsDegrees = Float.parseFloat(attributes.getValue(longitudeKey));

					String title = attributes.getValue(titleKey);

					StopLocation stopLocation = allStops.get(tag);
					if (stopLocation == null)
					{
						stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees, busStop, tag,
								title, routeKeysToTitles);
						allStops.put(tag, stopLocation);
					}

					currentRouteConfig.addStop(tag, stopLocation);
					stopLocation.addRouteAndDirTag(currentRouteConfig.getRouteName(), attributes.getValue(dirTagKey));
				}
				else
				{
					//ignore for now
				}
			}
		}
		else if (directionKey.equals(localName))
		{
			inDirection = true;
			
			if (inRoute)
			{
				String tag = attributes.getValue(tagKey);
				String title = attributes.getValue(titleKey);
				String name = attributes.getValue(nameKey);
				
				directions.add(tag, name, title, currentRoute);
			}
		}
		else if (routeKey.equals(localName))
		{
			inRoute = true;
			
			currentRoute = attributes.getValue(tagKey);
			String color = attributes.getValue(colorKey);
			String oppositeColor = attributes.getValue(oppositeColorKey);
			currentRouteConfig = new RouteConfig(currentRoute, color, oppositeColor, transitSource);
			
		}
		else if (pathKey.equals(localName))
		{
			inPath = true;
			
			currentPathPoints = new ArrayList<Float>();
		}
		else if (pointKey.equals(localName))
		{
			float lat = Float.parseFloat(attributes.getValue(latKey));
			float lon = Float.parseFloat(attributes.getValue(lonKey));
			currentPathPoints.add(lat);
			currentPathPoints.add(lon);
		}
		else if (localName.equals("Error"))
		{
			//i hate checked exceptions
			throw new RuntimeException(new FeedException());
		}
		
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (stopKey.equals(localName))
		{
			inStop = false;
		}
		else if (directionKey.equals(localName))
		{
			inDirection = false;
		}
		else if (routeKey.equals(localName))
		{
			inRoute = false;
			
			map.put(currentRoute, currentRouteConfig);

			currentRoute = null;
			currentRouteConfig = null;
		}
		else if (pathKey.equals(localName))
		{
			inPath = false;
			
			if (currentRouteConfig != null)
			{
				Path path = new Path(currentPathPoints);
				currentRouteConfig.addPath(path);
			}
		}
		
	}
	
	public void fillMapping(HashMap<String, RouteConfig> stopMapping) {
		for (String route : map.keySet())
		{
			stopMapping.put(route, map.get(route));
		}
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe) throws IOException {
		routeMapping.writeToDatabase(map, wipe);
		directions.writeToDatabase(wipe);
	}
	
}
