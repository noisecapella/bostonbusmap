package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.RouteConfig;

public class VehicleLocationsFeedParser extends DefaultHandler
{
	/**
	 * NOTE: this is read only here
	 */
	private final HashMap<Integer,String> vehiclesToRouteNames;
	private final HashMap<String, RouteConfig> stopMapping;
	private final Drawable bus;
	private final Drawable arrow;
	
	public VehicleLocationsFeedParser(HashMap<Integer, String> vehiclesToRouteNames,
			HashMap<String, RouteConfig> stopMapping, Drawable bus, Drawable arrow)
	{
		this.vehiclesToRouteNames = vehiclesToRouteNames;
		this.stopMapping = stopMapping;
		this.bus = bus;
		this.arrow = arrow;
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
			synchronized (vehiclesToRouteNames)
			{
				if (vehiclesToRouteNames.containsKey(id))
				{
					String value = vehiclesToRouteNames.get(id);
					if (value != null && value.length() != 0)
					{
						inferBusRoute = value;
					}
				}
			}

			RouteConfig routeConfig;
			if (stopMapping.containsKey(route))
			{
				routeConfig = stopMapping.get(route);
			}
			else
			{
				routeConfig = new RouteConfig(route);
			}


			BusLocation newBusLocation = new BusLocation(lat, lon, id, routeConfig, seconds, lastUpdateTime, 
					heading, predictable, dirTag, inferBusRoute, bus, arrow);

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
