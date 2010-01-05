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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.location.LocationListener;

public class BusLocations
{
	/**
	 * This class stores information about the bus. This information is mostly taken from the feed
	 */
	public class BusLocation
	{
		/**
		 * Current latitude of bus 
		 */
		public final double latitude;
		/**
		 * Current longitude of bus
		 */
		public final double longitude;
		
		/**
		 * The bus id. This uniquely identifies a bus
		 */
		public final int id;
		/**
		 * The route number. This may be null if the XML says so
		 */
		public final String route;
		
		/**
		 * seconds since the bus last sent GPS data to the server. This comes from the XML; we don't calculate this
		 */
		public final int seconds;
		/**
		 * Creation time of this bus object
		 */
		public final double lastUpdateInMillis;
		
		/**
		 * Distance in miles of the bus from its previous location, in the x dimension
		 */
		private double distanceFromLastX;
		/**
		 * Distance in miles of the bus from its previous location, in the y dimension
		 */
		private double distanceFromLastY;
		
		/**
		 * What is the heading mentioned for the bus?
		 */
		private String heading;
		
		/**
		 * Does the bus behave predictably?
		 */
		private boolean predictable;
		
		/**
		 * Is the bus inbound, or outbound?
		 * This only makes sense if predictable is true
		 */
		private boolean inBound;
		
		
		/**
		 * Used in calculating the distance between coordinates
		 */
		private final double radiusOfEarthInKilo = 6371.2;
		private final double kilometersPerMile = 1.609344;
		
		private double timeBetweenUpdatesInMillis;
		
		public BusLocation(double latitude, double longitude, int id, String route, int seconds, double lastUpdateInMillis,
				String heading, boolean predictable, boolean inBound)
		{
			this.latitude = latitude;
			this.longitude = longitude;
			this.id = id;
			this.route = route;
			this.seconds = seconds;
			this.lastUpdateInMillis = lastUpdateInMillis;
			this.heading = heading;
			this.predictable = predictable;
			this.inBound = inBound;
		}

		/**
		 * 
		 * @return a String describing the direction of the bus, or "" if it can't be calculated.
		 * For example: E (90 deg)
		 */
		public String getDirection()
		{
			double thetaInRadians = Math.atan2(distanceFromLastY, distanceFromLastX);
			
			if (distanceFromLastY == 0 && distanceFromLastX == 0)
			{
				return "";
			}
			else
			{
				return radiansToCardinalDirections(thetaInRadians);
			}
			
		}
		/**
		 * 
		 * @param thetaBackup direction in radians, where east is 0 and going counterclockwise
		 * @return a descriptive String showing the direction (for example: E (90 deg))
		 */
		private String radiansToCardinalDirections(double thetaAsRadians)
		{
			//NOTE: degrees will be 0 == north, going clockwise
			int degrees = (int)(thetaAsRadians * 180.0 / Math.PI);
			if (degrees < 0)
			{
				degrees += 360;
			}
			
			//convert to usual compass orientation
			degrees = -degrees + 90;
			if (degrees < 0)
			{
				degrees += 360;
			}
			
			return degrees + " deg (" + convertHeadingToCardinal(degrees) + ")";
		}

		/**
		 * @param lat2 latitude in radians
		 * @param lon2 longitude in radians
		 * @return distance in miles
		 */
		private double distanceFrom(double lat2, double lon2)
		{
			double lat1 = latitude * Math.PI / 180.0;
			double lon1 = longitude * Math.PI / 180.0;
			
			
			//great circle distance
			double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
			dist *= radiusOfEarthInKilo;
			dist /= kilometersPerMile;
			
			return dist;
		}
		
		/**
		 * calculate the distance from the old location
		 * 
		 * @param oldBusLocation
		 */
		public void movedFrom(BusLocation oldBusLocation)
		{
			if (oldBusLocation.latitude == latitude && oldBusLocation.longitude == longitude)
			{
				//ignore
				return;
			}
			distanceFromLastX = distanceFrom(latitude * Math.PI / 180.0, oldBusLocation.longitude * Math.PI / 180.0);
			distanceFromLastY = distanceFrom(oldBusLocation.latitude * Math.PI / 180.0, longitude * Math.PI / 180.0);
			
			if (oldBusLocation.latitude > latitude)
			{
				distanceFromLastX *= -1;
			}
			if (oldBusLocation.longitude > longitude)
			{
				distanceFromLastY *= -1;
			}
			
			timeBetweenUpdatesInMillis = lastUpdateInMillis - oldBusLocation.lastUpdateInMillis;
		}

		public String getSpeed() {
			//time in hours
			//distance in miles
			
			double time = ((timeBetweenUpdatesInMillis / 1000.0) / 60.0) / 60.0;
			double distance = Math.sqrt(distanceFromLastX * distanceFromLastX + distanceFromLastY * distanceFromLastY);
			if (time == 0)
			{
				return "";
			}
			else
			{
				return String.format("%f", distance/time) + " MPH";
			}
			
		}

		public String makeTitle() {
        	String title = "Id: " + id + ", route: " + (route != null ? route : "null");
        	title += "\nSeconds since update: " + (int)(seconds + (System.currentTimeMillis() - lastUpdateInMillis) / 1000);
        	String direction = getDirection();
        	if (direction.length() != 0 && predictable == false)
        	{
        		title += "\nEstimated direction: " + direction;
        	}
        	
        	if (predictable)
        	{
        		title += "\nHeading: " + heading + " deg (" + convertHeadingToCardinal(Integer.parseInt(heading)) + ")";

        		title += "\n";
        		if (inBound)
        		{
        			title += "Inbound";
        		}
        		else
        		{
        			title += "Outbound";
        		}
        	}
        	return title;
		}

		/**
		 * Converts a heading to a cardinal direction string
		 * 
		 * @param degree heading in degrees, where 0 is north and 90 is east
		 * @return a direction (for example "N" for north)
		 */
		private String convertHeadingToCardinal(double degree)
		{
			//shift degree so all directions line up nicely
			degree += 360.0 / 16; //22.5
			if (degree >= 360)
			{
				degree -= 360;
			}
			
			//get an index into the directions array
			int index = (int)(degree / (360.0 / 8.0));
			if (index < 0 || index >= 8)
			{
				return "calculation error";
			}
			
			String[] directions = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
			return directions[index];
		}
	}
	
	/**
	 * A mapping of the bus number to bus location
	 */
	private Map<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	/**
	 * The XML feed URL
	 */
	private final String mbtaDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private double lastTime = 0;
	
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
		String urlString = mbtaDataUrl + (long)lastTime; 
		url = new URL(urlString);
		
		DataInputStream dataInputStream;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
		InputStream stream = url.openStream();
		byte[] bytes = ReadToEnd(stream);
		
		stream.read(bytes, 0, bytes.length);
		
		String debuggingString = new String(bytes);
		
		//parse the data into an XML document
		Document document = builder.parse(new ByteArrayInputStream(bytes));
		
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
	
	private class BusComparator implements Comparator<BusLocation>
	{
		private final double centerLatitude;
		private final double centerLongitude;
		
		public BusComparator(double centerLatitude, double centerLongitude)
		{
			this.centerLatitude = centerLatitude;
			this.centerLongitude = centerLongitude;
		}
		
		/**
		 * Sort by distance to the center
		 */
		@Override
		public int compare(BusLocation arg0, BusLocation arg1)
		{
			double dist = arg0.distanceFrom(centerLatitude * (Math.PI / 180.0), centerLongitude * (Math.PI / 180.0));
			double otherDist = arg1.distanceFrom(centerLatitude * (Math.PI / 180.0), centerLongitude * (Math.PI / 180.0));
			
			return Double.compare(dist, otherDist);
		}
	}
}
