package com.schneeloch.bostonbusmap_library.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;



import android.util.Xml.Encoding;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.util.Now;

public class VehicleLocationsFeedParser extends DefaultHandler
{
	private final Directions directions;
    private final Map<VehicleLocations.Key, BusLocation> newBuses = Maps.newHashMap();
	
	public VehicleLocationsFeedParser(Directions directions)
	{
		this.directions = directions;
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
            boolean predictable = Boolean.parseBoolean(getAttribute(predictableKey, attributes));
            if (predictable) {
                int seconds = Integer.parseInt(getAttribute(secsSinceReportKey, attributes));
                String headingString = getAttribute(headingKey, attributes);
                Optional<Integer> heading;
                if (headingString == null) {
                    heading = Optional.absent();
                } else {
                    heading = Optional.of(Integer.parseInt(headingString));
                }

                String dirTag = getAttribute(dirTagKey, attributes);

                long lastFeedUpdate = Now.getMillis() - (seconds * 1000);

                BusLocation newBusLocation = new BusLocation(lat, lon, id, lastFeedUpdate,
                        heading, route, directions.getTitleAndName(dirTag));

                VehicleLocations.Key key = new VehicleLocations.Key(Schema.Routes.SourceId.Bus, route, id);

                newBuses.put(key, newBusLocation);
            }
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
