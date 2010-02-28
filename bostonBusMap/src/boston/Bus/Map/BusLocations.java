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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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


import android.location.LocationListener;

public class BusLocations
{
	/**
	 * A mapping of the bus number to bus location
	 */
	private Map<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	/**
	 * The XML feed URL
	 */
	private final String mbtaDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private double lastTime = 0;

	private boolean postVehicleRouteEstimate;

	private final HashMap<Integer, String> vehiclesToRouteNames = new HashMap<Integer, String>();
	
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
		//NOTE: disable this code for now
		if (postVehicleRouteEstimate && false)
		{
			vehiclesToRouteNames.clear();
			
			String vehicleToRouteNameUrl = "http://kk.csail.mit.edu/~nickolai/bus-infer/vehicle-to-routename.xml";
			URL url = new URL(vehicleToRouteNameUrl);
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			InputStream stream = url.openStream();
			
			//parse the data into an XML document
			Document document = builder.parse(stream);
			
			NodeList nodeList = document.getElementsByTagName("vehicle");
			
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Element node = (Element)nodeList.item(i);
				vehiclesToRouteNames.put(Integer.parseInt(node.getAttribute("id")), node.getAttribute("routeTag"));
			}
			
			postVehicleRouteEstimate = false;
		}
		
		
		//read data from the URL
		URL url;
		String urlString = mbtaDataUrl + (long)lastTime; 
		url = new URL(urlString);
		
		DataInputStream dataInputStream;
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
		lastTime = Double.parseDouble(lastTimeElement.getAttribute("time"));

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
			
			if (vehiclesToRouteNames.containsKey(id))
			{
				String value = vehiclesToRouteNames.get(id);
				if (value != null && value.length() != 0)
				{
					route = value;
				}
			}
			
			
			BusLocation newBusLocation = new BusLocation(lat, lon, id, route, seconds, lastTime, 
					heading, predictable, inBound);
			
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
	 * Read a stream and return a byte array of its contents
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	private byte[] ReadToEnd(InputStream stream) throws IOException {
		byte[] ret = new byte[0];
		
		byte[] bytesRead = new byte[4096];

		int amountRead;
		while ((amountRead = stream.read(bytesRead, 0, bytesRead.length)) > 0)
		{
			byte[] newRet = new byte[ret.length + amountRead];
			
			//concat ret + temp
			System.arraycopy(ret, 0, newRet, 0, ret.length);
			System.arraycopy(bytesRead, 0, newRet, ret.length, amountRead);
			
			ret = newRet;
		}
		return ret;
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
		
		
		
		Collections.sort(newLocations, new BusComparator(centerLatitude, centerLongitude));
		
		return newLocations.subList(0, maxLocations);
	}

	public void postVehicleRouteEstimate() {
		// TODO Auto-generated method stub
		postVehicleRouteEstimate = true;
	}
	
}
