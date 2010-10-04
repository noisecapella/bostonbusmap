package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;

public class VehicleLocationsFeedParser extends DefaultHandler
{
	private final RoutePool stopMapping;
	private final Drawable bus;
	private final Drawable arrow;
	private final Directions directions;
	private final HashMap<String, String> routeKeysToTitles;
	
	public VehicleLocationsFeedParser(RoutePool stopMapping, Drawable bus, Drawable arrow,
			Directions directions, HashMap<String, String> routeKeysToTitles)
	{
		this.stopMapping = stopMapping;
		this.bus = bus;
		this.arrow = arrow;
		this.directions = directions;
		this.routeKeysToTitles = routeKeysToTitles;
	}
	
	public void runParse(InputStream data)
		throws SAXException, ParserConfigurationException, IOException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(this);
		InputSource source = new InputSource(data);
		xmlReader.parse(source);
		data.close();
	}

	private double lastUpdateTime;
	private final HashMap<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	private static final String vehicleKey = "vehicle";
	private static final String latKey = "lat";
	private static final String lonKey = "lon";
	private static final String idKey = "id";
	private static final String routeTagKey = "routeTag";
	private static final String secsSinceReportKey = "secsSinceReport";
	private static final String headingKey = "heading";
	private static final String predictableKey = "predictable";
	private static final String dirTagKey = "dirTag";
	private static final String lastTimeKey = "lastTime";
	private static final String timeKey = "time";
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (localName.equals(vehicleKey))
		{
			double lat = Double.parseDouble(attributes.getValue(latKey));
			double lon = Double.parseDouble(attributes.getValue(lonKey));
			int id = Integer.parseInt(attributes.getValue(idKey));
			String route = attributes.getValue(routeTagKey);
			int seconds = Integer.parseInt(attributes.getValue(secsSinceReportKey));
			String heading = attributes.getValue(headingKey);
			boolean predictable = Boolean.parseBoolean(attributes.getValue(predictableKey)); 
			String dirTag = attributes.getValue(dirTagKey);


			String inferBusRoute = null;

			BusLocation newBusLocation = new BusLocation(lat, lon, id, seconds, lastUpdateTime, 
					heading, predictable, dirTag, inferBusRoute, bus, arrow, route, directions, routeKeysToTitles.get(route), false);

			Integer idInt = new Integer(id);
			if (busMapping.containsKey(idInt))
			{
				//calculate the direction of the bus from the current and previous locations
				newBusLocation.movedFrom(busMapping.get(idInt));
			}

			busMapping.put(idInt, newBusLocation);
		}
		else if (localName.equals(lastTimeKey))
		{
			lastUpdateTime = Double.parseDouble(attributes.getValue(timeKey));
			
			for (Integer key : busMapping.keySet())
			{
				busMapping.get(key).lastUpdateInMillis = lastUpdateTime;
			}
		}

	}
	
	public double getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void fillMapping(HashMap<Integer, BusLocation> outputBusMapping) {
		outputBusMapping.putAll(busMapping);
	}
	
	
}
