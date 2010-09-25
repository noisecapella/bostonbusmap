package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.transit.TransitSystem;

public class UMichInitialFeedParser extends DefaultHandler {
	/*
	 * example of data:
	 * 
	 * <livefeed>
−
<route>
<name>Commuter Southbound (Nights)</name>
<id>-2</id>
<topofloop>0</topofloop>
<busroutecolor>0000ff</busroutecolor>
−
<stop>
<name>Bonisteel and Beal (Cooley) W</name>
<name2>Cooley Lab</name2>
<name3>None</name3>
<latitude>42.29056</latitude>
<longitude>-83.71385</longitude>
<toa1>3623.32</toa1>
<id1>9</id1>
<toa2>1187.33</toa2>
<id2>119</id2>
<toa3>1997.72</toa3>
<id3>143</id3>
<toacount>3</toacount>
</stop>
	 */

	private final Directions directions;
	private final Drawable busStop;
	
	public UMichInitialFeedParser(Directions directions, HashMap<String, String> routeKeysToTitles, Drawable busStop)
	{
		this.directions = directions;
		this.routeKeysToTitles = routeKeysToTitles;
		this.busStop = busStop;
	}
	
	public void runParse(InputStream data) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(data));
		 
	}

	private boolean inRoute;
	private boolean inStop;
	private boolean inName;
	private boolean inToa;
	private boolean inId;
	private boolean inLat;
	private boolean inLon;
	
	private HashMap<String, RouteConfig> routeMapping = new HashMap<String, RouteConfig>();
	private HashMap<String, String> routeKeysToTitles = new HashMap<String, String>();
	private RouteConfig currentRouteConfig;
	
	private String stopName;
	private float stopLat;
	private float stopLon;
	
	private final ArrayList<Integer> predictionIds = new ArrayList<Integer>();
	private final ArrayList<Float> predictionTimes = new ArrayList<Float>();
	
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equals("route"))
		{
			inRoute = true;
		}
		else if (localName.equals("stop"))
		{
			inStop = true;
		}
		else if (localName.equals("name"))
		{
			inName = true;
		}
		else if (localName.equals("latitude"))
		{
			inLat = true;
		}
		else if (localName.equals("longitude"))
		{
			inLon = true;
		}
		else if (localName.startsWith("toa"))
		{
			inToa = true;
		}
		else if (localName.startsWith("id"))
		{
			inId = true;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String string = new String(ch, start, length);
		if (!inStop)
		{
			if (inName)
			{
				currentRouteConfig = new RouteConfig(string, null, null, TransitSystem.getTransitSource(string));
			}
		}
		else
		{
			if (inName)
			{
				stopName = string;
			}
			else if (inLat)
			{
				stopLat = Float.parseFloat(string);
			}
			else if (inLon)
			{
				stopLon = Float.parseFloat(string);
			}
			else if (inId)
			{
				predictionIds.add(Integer.parseInt(string));
			}
			else if (inToa)
			{
				predictionTimes.add(Float.parseFloat(string));
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("route"))
		{
			inRoute = false;
		}
		else if (localName.equals("stop"))
		{
			inStop = false;
			
			StopLocation currentStopLocation = new StopLocation(stopLat, stopLon, busStop, stopName, stopName, routeKeysToTitles);
			
			for (int i = 0; i < predictionTimes.size(); i++)
			{
				long epochTime = 0;
				String dirTag = null;
				currentStopLocation.addPrediction((int)(predictionTimes.get(i) / 60), epochTime, predictionIds.get(i), dirTag, 
						currentRouteConfig, directions);
			}
			
			currentRouteConfig.addStop(stopName, currentStopLocation);
			predictionTimes.clear();
			predictionIds.clear();

		}
		else if (localName.equals("name"))
		{
			inName = false;
		}
		else if (localName.equals("latitude"))
		{
			inLat = false;
		}
		else if (localName.equals("longitude"))
		{
			inLon = false;
		}
		else if (localName.startsWith("toa"))
		{
			inToa = false;
		}
		else if (localName.startsWith("id"))
		{
			inId = false;
		}

	}
}
