package com.schneeloch.bostonbusmap_library.parser;

import android.util.Xml;

import com.google.common.collect.Lists;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.schneeloch.bostonbusmap_library.data.HubwayStopLocation;
import com.schneeloch.bostonbusmap_library.data.PredictionStopLocationPair;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.SimplePrediction;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.transit.HubwayTransitSource;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayParser extends DefaultHandler {
	private final RouteConfig routeConfig;

	private static final String idKey = "id";
	private static final String numberBikesKey = "nbBikes";
	private static final String numberEmptyDocksKey = "nbEmptyDocks";
	private static final String lockedKey = "locked";
	private static final String installedKey = "installed";
	private static final String nameKey = "name";
	private static final String stationKey = "station";
    private static final String longitudeKey = "long";
    private static final String latitudeKey = "lat";

	private String id;
	private String numberBikes;
	private String numberEmptyDocks;
	private boolean locked;
	private boolean installed;
    private float latitude;
    private float longitude;
	private String name;

    private final HashMap<String, StopLocation> lookup = new HashMap<String, StopLocation>();

	private final StringBuilder chars = new StringBuilder();

	private final List<PredictionStopLocationPair> pairs = Lists.newArrayList();

	public HubwayParser(RouteConfig routeConfig) {

		this.routeConfig = routeConfig;
	}

	public void runParse(InputStream data)  throws ParserConfigurationException, SAXException, IOException {
		android.util.Xml.parse(data, Xml.Encoding.UTF_8, this);
		data.close();

	}

	public List<PredictionStopLocationPair> getPairs() {
		return pairs;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (stationKey.equals(localName)) {
			id = null;
			numberBikes = null;
			numberEmptyDocks = null;
			locked = true;
			installed = false;
			name = null;
            latitude = 0;
            longitude = 0;
		}

		chars.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// technically this can be called repeatedly but I don't think there's any inner elements
		// or surprises in this feed
		String string = new String(ch, start, length);
		chars.append(string);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String string = chars.toString();

		if (idKey.equals(localName)) {
			id = string;
		}
		else if (nameKey.equals(localName)) {
			name = string;
		}
		else if (numberBikesKey.equals(localName)) {
			numberBikes = string;
		}
		else if (numberEmptyDocksKey.equals(localName)) {
			numberEmptyDocks = string;
		}
		else if (lockedKey.equals(localName)) {
			locked = Boolean.parseBoolean(string);
		}
		else if (installedKey.equals(localName)) {
			installed = Boolean.parseBoolean(string);
		}
        else if (longitudeKey.equals(localName)) {
            longitude = Float.parseFloat(string);
        }
        else if (latitudeKey.equals(localName)) {
            latitude = Float.parseFloat(string);
        }
		else if (stationKey.equals(localName)) {
			String text = makeText();
			SimplePrediction prediction = new SimplePrediction(routeConfig.getRouteName(),
					routeConfig.getRouteTitle(), text);
            String tag = HubwayTransitSource.stopTagPrefix + id;
			StopLocation stop = routeConfig.getStop(tag);
			if (stop != null && name.equals(stop.getTitle())) {
				PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, stop);
				pairs.add(pair);
			}
			else
			{
				HubwayStopLocation.HubwayBuilder builder = new HubwayStopLocation.HubwayBuilder(latitude, longitude, tag, name);

                HubwayStopLocation newStop = builder.build();

                PredictionStopLocationPair pair = new PredictionStopLocationPair(prediction, newStop);
                pairs.add(pair);
			}
		}
	}

	private String makeText() {
		StringBuilder ret = new StringBuilder();

		ret.append("Bikes: ").append(numberBikes).append("<br />");
		ret.append("Empty Docks: ").append(numberEmptyDocks).append("<br />");
		if (locked) {
			ret.append("<b>Locked</b><br />");
		}
		if (!installed) {
			ret.append("<b>Not installed</b><br />");
		}

		return ret.toString();
	}
}
