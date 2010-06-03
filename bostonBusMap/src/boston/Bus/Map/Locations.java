/*
    BostonBusMap
 
    Copyright (C) 2009  George Schneeloch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package boston.Bus.Map;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.graphics.drawable.Drawable;
import android.location.LocationListener;

public final class Locations
{
	/**
	 * A mapping of the bus number to bus location
	 */
	private Map<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	private Map<String, ArrayList<StopLocation>> stopMapping = new HashMap<String, ArrayList<StopLocation>>();
	
	/**
	 * The XML feed URL
	 */
	private final String mbtaLocationsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	
	private double lastUpdateTime = 0;
	
	
	private final Drawable bus;
	private final Drawable arrow;
	private final Drawable tooltip;
	private final Drawable locationDrawable;
	private final Drawable busStop;
	
	public Locations(Drawable bus, Drawable arrow, Drawable tooltip, Drawable locationDrawable, Drawable busStop)
	{
		this.bus = bus;
		this.arrow = arrow;
		this.tooltip = tooltip;
		this.locationDrawable = locationDrawable;
		this.busStop = busStop;
	}
	
	public void InitializeStopInfo(String route, String xml) throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException 
	{
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		
		Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		
		//first check for errors
		if (document.getElementsByTagName("Error").getLength() != 0)
		{
			throw new RuntimeException("The feed is reporting an error"); 
			
		}
		
		ArrayList<StopLocation> stopLocations = new ArrayList<StopLocation>();
		
		NodeList routeList = document.getElementsByTagName("stop");
		for (int i = 0; i < routeList.getLength(); i++)
		{
			Element stop = (Element)routeList.item(i);
			
			float latitudeAsDegrees = Float.parseFloat(stop.getAttribute("lat"));
			float longitudeAsDegrees = Float.parseFloat(stop.getAttribute("lon"));
			int id = Integer.parseInt(stop.getAttribute("stopId"));
			String title = stop.getAttribute("title");
			boolean inBound = "in".equals(stop.getAttribute("dirTag"));
			
			StopLocation stopLocation = new StopLocation(latitudeAsDegrees, longitudeAsDegrees, busStop, tooltip, id, title, inBound);
			stopLocations.add(stopLocation);
		}
		
		stopMapping.put(route, stopLocations);
	}
	
	/**
	 * Update the bus locations based on data from the XML feed 
	 * 
	 * @param centerLat
	 * @param centerLon
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public void Refresh() throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError 
	{
		//read data from the URL
		URL url;
		String urlString = mbtaLocationsDataUrl + (long)lastUpdateTime; 
		url = new URL(urlString);
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
		InputStream stream = url.openStream();
		
		//parse the data into an XML document
		Document document = builder.parse(stream);
		
		//first check for errors
		if (document.getElementsByTagName("Error").getLength() != 0)
		{
			throw new RuntimeException("The feed is reporting an error"); 
			
		}
		
		//get the time that this information is valid until
		Element lastTimeElement = (Element)document.getElementsByTagName("lastTime").item(0);
		lastUpdateTime = Double.parseDouble(lastTimeElement.getAttribute("time"));

		//iterate through each vehicle mentioned
		NodeList nodeList = document.getElementsByTagName("vehicle");
		
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Element element = (Element)nodeList.item(i);
			
			double lat = Double.parseDouble(element.getAttribute("lat"));
			double lon = Double.parseDouble(element.getAttribute("lon"));
			int id = Integer.parseInt(element.getAttribute("id"));
			String route = element.getAttribute("routeTag");
			int seconds = Integer.parseInt(element.getAttribute("secsSinceReport"));
			String heading = element.getAttribute("heading");
			boolean predictable = Boolean.parseBoolean(element.getAttribute("predictable")); 
			boolean inBound = false;
			if (element.getAttribute("dirTag").equals("in"))
			{
				inBound = true;
			}
			
			BusLocation newBusLocation = new BusLocation(lat, lon, id, route, seconds, lastUpdateTime, 
					heading, predictable, inBound, bus, arrow, tooltip);
			
			Integer idInt = new Integer(id);
			if (busMapping.containsKey(idInt))
			{
				//calculate the direction of the bus from the current and previous locations
				newBusLocation.movedFrom(busMapping.get(idInt));
			}

			busMapping.put(idInt, newBusLocation);
		}
			

		//delete old buses
		List<Integer> busesToBeDeleted = new ArrayList<Integer>();
		for (Integer id : busMapping.keySet())
		{
			BusLocation busLocation = busMapping.get(id);
			if (busLocation.lastUpdateInMillis + 180000 < System.currentTimeMillis())
			{
				//put this old dog to sleep
				busesToBeDeleted.add(id);
			}
		}
		
		for (Integer id : busesToBeDeleted)
		{
			busMapping.remove(id);
		}
	}

	/**
	 * Return the 20 (or whatever maxLocations is) closest buses to the center
	 * 
	 * @param maxLocations
	 * @return
	 */
	public List<BusLocation> getBusLocations(int maxLocations, double centerLatitude, double centerLongitude, boolean doShowUnpredictable) {

		ArrayList<BusLocation> newLocations = new ArrayList<BusLocation>();
		
		if (doShowUnpredictable == false)
		{
			for (BusLocation busLocation : busMapping.values())
			{
				if (busLocation.predictable == true)
				{
					newLocations.add(busLocation);
				}
			}
		}
		else
		{
			newLocations.addAll(busMapping.values());
		}
		
		if (maxLocations > newLocations.size())
		{
			maxLocations = newLocations.size();
		}
		
		
		
		Collections.sort(newLocations, new LocationComparator(centerLatitude, centerLongitude));
		
		return newLocations.subList(0, maxLocations);
	}

	private int latitudeAsDegreesE6;
	private int longitudeAsDegreesE6;
	private boolean showCurrentLocation;
	
	public void setCurrentLocation(int latitudeAsDegreesE6, int longitudeAsDegreesE6) {
		this.latitudeAsDegreesE6 = latitudeAsDegreesE6;
		this.longitudeAsDegreesE6 = longitudeAsDegreesE6;
		showCurrentLocation = true;
	}
	
	public void clearCurrentLocation()
	{
		showCurrentLocation = false;
	}
	
	public CurrentLocation getCurrentLocation()
	{
		if (showCurrentLocation)
		{
			CurrentLocation location = new CurrentLocation(locationDrawable, latitudeAsDegreesE6, longitudeAsDegreesE6);
			return location;
		}
		else
		{
			return null;
		}
	}
}
