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
package boston.Bus.Map.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.BusPredictionsFeedParser;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.StreamCounter;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.util.Log;

public final class Locations
{
	/**
	 * No change to mode we're in. 0 is vehicle locations, and others are indexes to route numbers for bus predictions, as index into
	 * routesSupported. (In this class, routesSupported is defined such that 0 is null, 1 is route 1, 2 is route 4, etc, so -1
	 * should be the only special case to deal with)
	 */
	public static final int NO_CHANGE = -1;

	/**
	 * A mapping of the bus number to bus location
	 */
	private HashMap<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	/**
	 * A mapping of a route id to a RouteConfig object. This should probably be renamed routeMapping
	 */
	private final HashMap<String, RouteConfig> stopMapping = new HashMap<String, RouteConfig>();
	
	/**
	 * A list of all stoplocations
	 */
	private final ArrayList<StopLocation> allStops = new ArrayList<StopLocation>();
	
	/**
	 * The XML feed URL
	 */
	private final String mbtaLocationsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=mbta&t=";

	private final String mbtaRouteConfigDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=";
	private final String mbtaRouteConfigDataUrlAllRoutes = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta";
	
	private final String mbtaPredictionsDataUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops&a=mbta";

	
	private final HashMap<Integer, String> vehiclesToRouteNames = new HashMap<Integer, String>();

	private double lastInferBusRoutesTime = 0;
	
	private double lastUpdateTime = 0;
	
	/**
	 * This should let us know if the user checked or unchecked the Infer bus routes checkbox. If inferBusRoutes in Refresh()
	 * is true and this is false, we should do a refresh, and if inferBusRoutes is false and this is true, we should
	 * clear the bus information 
	 */
	private boolean lastInferBusRoutes;

	/**
	 * in millis
	 */
	private final double tenMinutes = 10 * 60 * 1000;
	
	
	private final Drawable bus;
	private final Drawable arrow;
	private final Drawable locationDrawable;
	private final Drawable busStop;
	
	private final String[] supportedRoutes;
	
	private String selectedRoute;
	private int selectedBusPredictions;
	
	public Locations(Drawable bus, Drawable arrow, Drawable locationDrawable,
			Drawable busStop, String[] supportedRoutes)
	{
		this.bus = bus;
		this.arrow = arrow;
		this.locationDrawable = locationDrawable;
		this.busStop = busStop;
		this.supportedRoutes = supportedRoutes;
		
		
	}
	
	/**
	 * Download all stop locations
	 * 
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws SAXException
	 * @throws IOException
	 */
	public void initializeAllRoutes(DatabaseHelper helper, UpdateAsyncTask task)
		throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException
	{
		boolean hasMissingData = routeInfoNeedsUpdating();
		
		if (hasMissingData)
		{
			final String prepend = "Downloading route info (this may take a short while): ";
			
			//download everything at once
			final String urlString = mbtaRouteConfigDataUrlAllRoutes;
			URL url = new URL(urlString);
			
			InputStream stream = downloadStream(url, task, prepend, "of approx 7.5MB");
			
			RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop);
			
			parser.runParse(stream);
			
			task.publish("Parsing route data...");
			parser.fillMapping(stopMapping);
			
			task.publish("Saving route data to database...");
			helper.saveMapping(stopMapping, true);
			task.publish("Done!");
		}
		else
		{
			
		

			for (int i = 0; i < supportedRoutes.length; i++)
			{
				String route = supportedRoutes[i];

				if (stopMapping.containsKey(route) == false || stopMapping.get(route) == null || 
						stopMapping.get(route).getStops().size() == 0)
				{
					final String prepend = "Downloading route info for " + route + " (this may take a short while): ";

					//populate stops
					final String urlString = mbtaRouteConfigDataUrl + route;
					URL url = new URL(urlString);

					//just initialize the route and then end for this round
					InputStream stream = downloadStream(url, task, prepend, null);
					RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop);
					
					parser.runParse(stream);
					
					parser.fillMapping(stopMapping);
					
					helper.saveMapping(stopMapping, false);
				}


			}
		}
	}
	
	private InputStream downloadStream(URL url, UpdateAsyncTask task, String prepend, String ifContentLengthMissing) throws IOException {
		URLConnection connection = url.openConnection();
		int totalDownloadSize = connection.getContentLength();
		InputStream inputStream = connection.getInputStream();

		return new StreamCounter(inputStream, task, totalDownloadSize, ifContentLengthMissing, prepend);
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
	 * @throws FeedException 
	 */
	public void Refresh(DatabaseHelper helper, boolean inferBusRoutes, int routeIndexToUpdate,
			int selectedBusPredictions, double centerLatitude, double centerLongitude) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError 
			{
		String routeToUpdate = supportedRoutes[routeIndexToUpdate];

		try
		{
			updateInferRoutes(inferBusRoutes);
		}
		catch (IOException e)
		{
			//don't let a problem with the mit website stop everything from working
			Log.e("BostonBusMap", e.toString());
		}

		
		if (allStops.size() == 0)
		{
			for (String route : stopMapping.keySet())
			{
				RouteConfig routeConfig = stopMapping.get(route);
				
				allStops.addAll(routeConfig.getStops());
			}
		}

		//read data from the URL
		DownloadHelper downloadHelper;
		switch (selectedBusPredictions)
		{
		case  Main.BUS_PREDICTIONS_ONE:
		{
			if (stopMapping.containsKey(routeToUpdate))
			{
				RouteConfig routeConfig = stopMapping.get(routeToUpdate);
				if (routeConfig.getStops().size() != 0)
				{
					//ok, do predictions now
					StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);// + "&stops=39|null|6570&stops=39|null|6571";

					for (StopLocation location : routeConfig.getStops())
					{
						urlString.append("&stops=").append(routeToUpdate).append("%7Cnull%7C").append(location.getStopNumber());
					}
					downloadHelper = new DownloadHelper(urlString.toString());
				}
				else
				{
					//populate stops (just in case we didn't already)
					populateStops(routeToUpdate, helper);
					return;
				}
			}
			else
			{
				//populate stops (just in case we didn't already)
				populateStops(routeToUpdate, helper);
				return;
			}
		}
		break;
		case Main.BUS_PREDICTIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		{
			
			StringBuilder urlString = new StringBuilder(mbtaPredictionsDataUrl);
			
			List<Location> locations = getLocations(30, centerLatitude, centerLongitude, false);
			for (Location location : locations)
			{
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;
					for (RouteConfig routeConfig : stopLocation.getRoutes())
					{
						urlString.append("&stops=").append(routeConfig.getRouteName()).append("%7Cnull%7C").append(stopLocation.getStopNumber());
					}
				}
			}
			
			Log.v("BostonBusMap", "urlString for bus predictions, all: " + urlString);
			
			downloadHelper = new DownloadHelper(urlString.toString());
		}
		break;

		case Main.VEHICLE_LOCATIONS_ALL:
		case Main.VEHICLE_LOCATIONS_ONE:
		default:
		{
			//for now, we download and update all buses, whether the user chooses one route or all routes
			//we only make the distinction when we display the icons
			final String urlString = mbtaLocationsDataUrl + (long)lastUpdateTime;
			downloadHelper = new DownloadHelper(urlString);
		}
		break;
		}

		downloadHelper.connect();

		InputStream data = downloadHelper.getResponseData();

		if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE || 
				selectedBusPredictions == Main.BUS_PREDICTIONS_ALL ||
				selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
		{
			//bus prediction

			BusPredictionsFeedParser parser = new BusPredictionsFeedParser(stopMapping);

			parser.runParse(data);
		}
		else 
		{
			//vehicle locations
			//VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(stream);

			//lastUpdateTime = parser.getLastUpdateTime();

			VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(vehiclesToRouteNames, stopMapping, bus, arrow);
			parser.runParse(data);

			//get the time that this information is valid until
			lastUpdateTime = parser.getLastUpdateTime();

			parser.fillMapping(busMapping);

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
	}

	
	private void populateStops(String routeToUpdate, DatabaseHelper helper) 
		throws IOException, ParserConfigurationException, SAXException
	{
		final String urlString = mbtaRouteConfigDataUrl + routeToUpdate;
		URL url = new URL(urlString);

		//just initialize the route and then end for this round
		InputStream stream = url.openStream();
		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop);

		parser.runParse(stream); 

		parser.fillMapping(stopMapping);

		helper.saveMapping(stopMapping, false);
	}

	private void updateInferRoutes(boolean inferBusRoutes)
			throws MalformedURLException, ParserConfigurationException,
			FactoryConfigurationError, IOException, SAXException {
		//if Infer bus routes is checked and either:
		//(a) 10 minutes have passed
		//(b) the checkbox wasn't checked before, which means we should refresh anyway
		if (inferBusRoutes && ((System.currentTimeMillis() - lastInferBusRoutesTime > tenMinutes) || (lastInferBusRoutes == false)))
		{
			//if we can't read from this feed, it'll throw an exception
			//set last time we read from site to 5 minutes ago, so it won't try to read for another 5 minutes
			//(currently it will check inferred route info every 10 minutes)
			lastInferBusRoutesTime = System.currentTimeMillis() - tenMinutes / 2;
			
			
			vehiclesToRouteNames.clear();
			
			//thanks Nickolai Zeldovich! http://people.csail.mit.edu/nickolai/
			final String vehicleToRouteNameUrl = "http://kk.csail.mit.edu/~nickolai/bus-infer/vehicle-to-routename.xml";
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
			
			lastInferBusRoutesTime = System.currentTimeMillis();
		}
		else if (inferBusRoutes == false && lastInferBusRoutes == true)
		{
			//clear vehicle mapping if checkbox is false
			vehiclesToRouteNames.clear();
		}
		
		lastInferBusRoutes = inferBusRoutes;
	}


	/**
	 * Return the 20 (or whatever maxLocations is) closest buses to the center
	 * 
	 * NOTE: this is run in the UI thread, so be speedy
	 * 
	 * @param maxLocations
	 * @return
	 */
	public List<Location> getLocations(int maxLocations, double centerLatitude, double centerLongitude, boolean doShowUnpredictable) {

		ArrayList<Location> newLocations = new ArrayList<Location>();

		if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ALL || selectedBusPredictions == Main.VEHICLE_LOCATIONS_ONE)
		{
			
			if (doShowUnpredictable == false)
			{
				for (BusLocation busLocation : busMapping.values())
				{
					if (busLocation.predictable == true)
					{
						if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ONE)
						{
							if (busLocation.route != null && busLocation.route.getRouteName().equals(selectedRoute))
							{
								newLocations.add(busLocation);
							}
						}
						else
						{
							newLocations.add(busLocation);
						}
					}
				}
			}
			else
			{
				if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ALL)
				{
					newLocations.addAll(busMapping.values());
				}
				else
				{
					for (BusLocation location : busMapping.values())
					{
						if (location.route != null && location.route.getRouteName().equals(selectedRoute))
						{
							newLocations.add(location);
						}
					}
				}
			}
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE)
		{
			if (stopMapping.containsKey(selectedRoute))
			{
				newLocations.addAll(stopMapping.get(selectedRoute).getStops());
			}
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ALL)
		{
			//Log.v("BostonBusMap", "allStops size is " + allStops.size());
			
			newLocations.addAll(allStops);
			/*for (StopLocation location : allStops)
			{
				if (location.distanceFrom(centerLatitude * LocationComparator.degreesToRadians,
						centerLongitude * LocationComparator.degreesToRadians) < 1)
				{
					newLocations.add(location);
				}
			}*/
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
		{
			for (StopLocation location : allStops)
			{
				if (location.getIsFavorite() == Location.IS_FAVORITE)
				{
					newLocations.add(location);
				}
			}
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

	public void select(int position, int busPredictions) {
		if (position == Locations.NO_CHANGE)
		{
			//-1 means don't change this
		}
		else
		{
			selectedRoute = supportedRoutes[position];

			selectedBusPredictions = busPredictions;
		}
	}

	public ArrayList<Path> getSelectedPaths() {
		ArrayList<Path> ret = new ArrayList<Path>();

		RouteConfig routeConfig = stopMapping.get(selectedRoute);
		if (routeConfig != null)
		{
			ret.addAll(routeConfig.getPaths().values());
		}
		
		return ret;
	}

	/**
	 * Fills stopMapping with information from the database
	 * @param helper
	 * @param favorites
	 * @return
	 * @throws IOException 
	 */
	public void getRouteDataFromDatabase(DatabaseHelper helper) throws IOException {
		if (routeInfoNeedsUpdating() == false)
		{
			return;
		}
		
		final HashMap<String, RouteConfig> map = new HashMap<String, RouteConfig>();
		
		for (String route : supportedRoutes)
		{
			map.put(route, null);
		}
		
		try
		{
			ArrayList<Integer> initialFavorites = new ArrayList<Integer>();
			helper.populateMap(map, initialFavorites, supportedRoutes);
			//we can skip buses since they have no favorites, so everything will be in the map
			if (initialFavorites.size() != 0)
			{
				for (RouteConfig route : map.values())
				{
					if (route != null)
					{
						for (StopLocation location : route.getStops())
						{
							if (initialFavorites.contains(location.getId()))
							{
								location.toggleFavorite();
							}
						}
					}
				}
			}
			initialFavorites.clear();
		}
		catch (IOException e)
		{
			Log.e("BostonBusMap", e.toString());
		}

		stopMapping.putAll(map);
	}
	
	private boolean routeInfoNeedsUpdating()
	{
		boolean needsUpdating = false;
		for (String route : supportedRoutes)
		{
			if (stopMapping.get(route) == null || stopMapping.get(route).getStops().size() == 0)
			{
				needsUpdating = true;
				break;
			}
		}
		
		return needsUpdating;
	}

	/**
	 * Is there enough space available, if we need any?
	 * @return
	 */
	public boolean checkFreeSpace(DatabaseHelper helper) {
		if (routeInfoNeedsUpdating() == false)
		{
			//everything is already in the database
			return true;
		}
		else
		{
			return helper.checkFreeSpace();
		}
	}
}
