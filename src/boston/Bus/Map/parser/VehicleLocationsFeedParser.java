package boston.Bus.Map.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import skylight1.opengl.files.QuickParseUtil;


import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.VehicleLocations;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.transit.TransitSystem;

public class VehicleLocationsFeedParser extends DefaultHandler
{
	private final TransitDrawables drawables;
	private final Directions directions;
	private final RouteTitles routeKeysToTitles;
    private final Map<VehicleLocations.Key, BusLocation> newBuses = Maps.newHashMap();
	
	public VehicleLocationsFeedParser(TransitDrawables drawables,
			Directions directions, RouteTitles routeKeysToTitles)
	{
		this.drawables = drawables;
		this.directions = directions;
		this.routeKeysToTitles = routeKeysToTitles;
	}
	
	public void runParse(InputStream data)
		throws SAXException, ParserConfigurationException, IOException
	{
		android.util.Xml.parse(data, Encoding.UTF_8, this);
		data.close();
	}

	private long lastUpdateTime;
	private final VehicleLocations busMapping = new VehicleLocations();
	private final Map<String, Integer> tagCache = Maps.newHashMap();
	
	
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
	private static final String tripTagKey = "tripTag";
	private static final String speedKmHrKey = "speedKmHr";

    public Map<VehicleLocations.Key, BusLocation> getNewBuses() {
        return newBuses;
    }

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (localName.equals(vehicleKey))
		{
			clearAttributes(attributes);
			
			float lat = Float.parseFloat(getAttribute(latKey, attributes));
			float lon = Float.parseFloat(getAttribute(lonKey, attributes));
			String id = getAttribute(idKey, attributes);
			String route = getAttribute(routeTagKey, attributes);
			int seconds = Integer.parseInt(getAttribute(secsSinceReportKey, attributes));
			String heading = getAttribute(headingKey, attributes);
			boolean predictable = Boolean.parseBoolean(getAttribute(predictableKey, attributes)); 
			String dirTag = getAttribute(dirTagKey, attributes);
			
			long lastFeedUpdate = System.currentTimeMillis() - (seconds * 1000);
			
			BusLocation newBusLocation = new BusLocation(lat, lon, id, lastFeedUpdate,
					heading, predictable, dirTag, true, route);

			VehicleLocations.Key key = new VehicleLocations.Key(Schema.Routes.enumagencyidBus, route, id);

			newBuses.put(key, newBusLocation);
		}
		else if (localName.equals(lastTimeKey))
		{
			lastUpdateTime = Long.parseLong(attributes.getValue(timeKey));
		}

	}
	
	private String getAttribute(String key, Attributes attributes)
	{
		return XmlParserHelper.getAttribute(key, attributes, tagCache);
	}

	private void clearAttributes(Attributes attributes)
	{
		XmlParserHelper.clearAttributes(attributes, tagCache);
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
}
