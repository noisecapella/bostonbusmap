package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
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

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.FeedException;


import android.graphics.drawable.Drawable;

public class RouteConfigFeedParser extends DefaultHandler
{
	private final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
	
	public RouteConfigFeedParser(Drawable busStop)
	{
		this.busStop = busStop;
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

	private static final String tagKey = "tag";
	private static final String latitudeKey = "lat";
	private static final String longitudeKey = "lon";
	private static final String titleKey = "title";
	private static final String dirTagKey = "dirTag";
	private static final String nameKey = "name";
	private static final String pathKey = "path";
	private static final String pointKey = "point";
	
	
	private boolean inRoute;
	private boolean inDirection;
	private boolean inStop;
	private boolean inPath;
	
	private RouteConfig currentRouteConfig;
	private String currentRoute;
	
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
					int id = Integer.parseInt(tag);

					double latitudeAsDegrees = Double.parseDouble(attributes.getValue(latitudeKey));
					double longitudeAsDegrees = Double.parseDouble(attributes.getValue(longitudeKey));

					String title = attributes.getValue(titleKey);
					String dirTag = attributes.getValue(dirTagKey);

					StopLocation stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees, busStop, id,
							title, dirTag, currentRouteConfig);

					currentRouteConfig.addStop(id, stopLocation);
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
				
				
				currentRouteConfig.addDirection(tag, title, name);
			}
		}
		else if (routeKey.equals(localName))
		{
			inRoute = true;
			
			currentRoute = attributes.getValue(tagKey);
			currentRouteConfig = new RouteConfig(currentRoute);
		}
		else if (pathKey.equals(localName))
		{
			inPath = true;
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
		}
		
	}
	
	
	
	
	public void fillMapping(HashMap<String, RouteConfig> stopMapping) {
		for (String route : map.keySet())
		{
			stopMapping.put(route, map.get(route));
		}
	}
	
}
