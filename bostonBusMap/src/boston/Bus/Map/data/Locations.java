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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;

import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.FeedException;
import boston.Bus.Map.util.StreamCounter;

public final class Locations
{
	/**
	 * A mapping of the bus number to bus location
	 */
	private HashMap<Integer, BusLocation> busMapping = new HashMap<Integer, BusLocation>();
	
	/**
	 * A mapping of a route id to a RouteConfig object.
	 */
	private final RoutePool routeMapping;
	
	private final Directions directions;
	
	private double lastUpdateTime = 0;
	

	/**
	 * in millis
	 */
	private final double tenMinutes = 10 * 60 * 1000;
	
	
	private final Drawable bus;
	private final Drawable arrow;
	private final Drawable locationDrawable;
	private final Drawable busStop;
	
	private String selectedRoute;
	private int selectedBusPredictions;
	private final TransitSystem transitSystem;
	
	public Locations(Drawable bus, Drawable arrow, Drawable locationDrawable,
			Drawable busStop, DatabaseHelper helper, 
			TransitSystem transitSystem)
	{
		this.bus = bus;
		this.arrow = arrow;
		this.locationDrawable = locationDrawable;
		this.busStop = busStop;
		this.transitSystem = transitSystem;
		routeMapping = new RoutePool(helper, transitSystem);
		directions = new Directions(helper);
	}
	
	/**
	 * Download all stop locations
	 * 
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws SAXException
	 * @throws IOException
	 */
	public void initializeAllRoutes(UpdateAsyncTask task, Context context, String[] routesToCheck)
		throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException
	{
		ArrayList<String> routesThatNeedUpdating = routeInfoNeedsUpdating(routesToCheck); 
		boolean hasNoMissingData = routesThatNeedUpdating == null || routesThatNeedUpdating.size() == 0;
		
		if (hasNoMissingData == false)
		{
			final String prepend = "Downloading route info (this may take a short while): ";
			
			//download everything at once
			//NOTE: for now, I'm including a copy of the routeConfig with the app
			/*final String urlString = TransitSystem.getRouteConfigUrl();
			URL url = new URL(urlString);
			
			InputStream stream = downloadStream(url, task, prepend, "of approx " + TransitSystem.getSizeOfRouteConfigUrl());
			*/
			
			
			HashSet<TransitSource> systems = new HashSet<TransitSource>();
			for (String route : routesThatNeedUpdating)
			{
				systems.add(transitSystem.getTransitSource(route));
			}
			
			task.publish("Downloading route info...");
			for (TransitSource system : systems)
			{
				system.initializeAllRoutes(task, context, directions, routeMapping);
			}
			routeMapping.fillInFavoritesRoutes();
			//TODO: fill routeMapping somehow
			
			task.publish("Done!");
		}
		else
		{
			/*if (routesThatNeedUpdating != null)
			{
				//this code probably isn't executed right now
				for (String route : routesThatNeedUpdating)
				{
					final String prepend = "Downloading route info for " + route + " (this may take a short while): ";

					//populate stops
					final String urlString = TransitSystem.getRouteConfigUrl(route);
					URL url = new URL(urlString);

					//just initialize the route and then end for this round
					InputStream stream = downloadStream(url, task, prepend, null);
					RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, routeKeysToTitles, null);

					parser.runParse(stream);

					parser.writeToDatabase(routeMapping, false);
				}
			}*/
			//TODO: what happens here? I don't think this is ever executed
		}
	}
	
	public static InputStream downloadStream(URL url, UpdateAsyncTask task, String prepend, String ifContentLengthMissing) throws IOException {
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
	public void Refresh(boolean inferBusRoutes, String routeToUpdate,
			int selectedBusPredictions, float centerLatitude, float centerLongitude,
			UpdateAsyncTask updateAsyncTask, boolean showRoute) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError 
	{
		final int maxStops = 15;

		//see if route overlays need to be downloaded
		RouteConfig routeConfig = routeMapping.get(routeToUpdate);
		if (routeConfig != null)
		{
			if (routeConfig.getStops().size() != 0 && (showRoute == false || routeConfig.getPaths().size() != 0 || 
					routeConfig.hasPaths() == false))
			{
				//everything's ok
			}
			else
			{
				//populate route overlay (just in case we didn't already)
				updateAsyncTask.publish("Downloading data for route " + routeToUpdate + "...");
				populateStops(routeToUpdate, routeConfig);
				updateAsyncTask.publish("Finished download");
				
				return;
			}
		}
		else
		{
			//populate route overlay (just in case we didn't already)
			updateAsyncTask.publish("Downloading data for route " + routeToUpdate + "...");
			populateStops(routeToUpdate, routeConfig);
			updateAsyncTask.publish("Finished download");
			return;
		}

		switch (selectedBusPredictions)
		{
		case Main.VEHICLE_LOCATIONS_ALL:
		case Main.VEHICLE_LOCATIONS_ONE:
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			for (Integer id : busMapping.keySet())
			{
				BusLocation busLocation = busMapping.get(id);
				if (busLocation.isDisappearAfterRefresh())
				{
					toRemove.add(id);
				}
			}
			
			for (int id : toRemove)
			{
				busMapping.remove(id);
			}
		}
		
		switch (selectedBusPredictions)
		{
		case Main.BUS_PREDICTIONS_ALL:
		case Main.BUS_PREDICTIONS_STAR:
		case Main.VEHICLE_LOCATIONS_ALL:
			//get data from many transit sources
			transitSystem.refreshData(routeConfig, selectedBusPredictions, maxStops, centerLatitude,
					centerLongitude, busMapping, selectedRoute, routeMapping, directions, this);
			break;
		default:
			TransitSource transitSource = routeConfig.getTransitSource();
			transitSource.refreshData(routeConfig, selectedBusPredictions, maxStops,
					centerLatitude, centerLongitude, busMapping,
					selectedRoute, routeMapping, directions, this);
		}
	}

	private void populateStops(String routeToUpdate, RouteConfig oldRouteConfig) 
		throws IOException, ParserConfigurationException, SAXException
	{
		
		TransitSource transitSource;
		if (oldRouteConfig != null)
		{
			transitSource = oldRouteConfig.getTransitSource();
		}
		else
		{
			transitSource = transitSystem.getTransitSource(routeToUpdate);
		}
		
		transitSource.populateStops(routeMapping, routeToUpdate, oldRouteConfig, directions);
	}

	/**
	 * Return the 20 (or whatever maxLocations is) closest buses to the center
	 * 
	 * NOTE: this is run in the UI thread, so be speedy
	 * 
	 * @param maxLocations
	 * @return
	 * @throws IOException 
	 */
	public List<Location> getLocations(int maxLocations, float centerLatitude, float centerLongitude, 
			boolean doShowUnpredictable) throws IOException {

		TreeSet<Location> newLocations = new TreeSet<Location>(new LocationComparator(centerLatitude, centerLongitude));
		
		HashSet<Integer> locationKeys = new HashSet<Integer>();

		if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ALL || selectedBusPredictions == Main.VEHICLE_LOCATIONS_ONE)
		{
			synchronized (busMapping)
			{
				if (doShowUnpredictable == false)
				{
					for (BusLocation busLocation : busMapping.values())
					{
						if (busLocation.predictable == true)
						{
							if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ONE)
							{
								if (selectedRoute != null && selectedRoute.equals(busLocation.getRouteId()))
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
							if (selectedRoute != null && selectedRoute.equals(location.getRouteId()))
							{
								newLocations.add(location);
							}
						}
					}
				}
			}
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE)
		{
			RouteConfig routeConfig = routeMapping.get(selectedRoute);
			if (routeConfig != null)
			{
				newLocations.addAll(routeConfig.getStops());
			}
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ALL)
		{
			//Log.v("BostonBusMap", "allStops size is " + allStops.size());
			
			for (StopLocation location : routeMapping.getAllStops())
			{
				newLocations.add(location);
				locationKeys.add(location.getId());
			}
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
		{
			for (StopLocation stopLocation : routeMapping.getFavoriteStops())
			{
				newLocations.add(stopLocation);
				locationKeys.add(stopLocation.getId());
			}
		}
		
		if (maxLocations > newLocations.size())
		{
			maxLocations = newLocations.size();
		}
		
		
		
		return new ArrayList<Location>(newLocations).subList(0, maxLocations);
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
	
	public void select(String newRoute, int busPredictions) {
		selectedRoute = newRoute;

		selectedBusPredictions = busPredictions;
	}

	public ArrayList<Path> getSelectedPaths() throws IOException {
		ArrayList<Path> ret = new ArrayList<Path>();

		RouteConfig routeConfig = routeMapping.get(selectedRoute);
		if (routeConfig != null)
		{
			ret.addAll(routeConfig.getPaths());
		}
		
		return ret;
	}

	
	private ArrayList<String> routeInfoNeedsUpdating(String[] routesToCheck) throws IOException
	{
		return routeMapping.routeInfoNeedsUpdating(routesToCheck);
	}

	/**
	 * Is there enough space available, if we need any?
	 * @return
	 * @throws IOException 
	 */
	public boolean checkFreeSpace(DatabaseHelper helper, String[] routesToCheck) throws IOException {
		ArrayList<String> routesThatNeedUpdating = routeInfoNeedsUpdating(routesToCheck);
		if (routesThatNeedUpdating == null || routesThatNeedUpdating.size() == 0)
		{
			//everything is already in the database
			return true;
		}
		else
		{
			return helper.checkFreeSpace();
		}
	}

	public RouteConfig getSelectedRoute() throws IOException {
		return routeMapping.get(selectedRoute);
	}
	
	public int toggleFavorite(StopLocation location)
	{
		Collection<StopLocation> sharedSnippetStops = location.getSharedSnippetStops();

		//isFavorite will be true if any of the individual stops are favorited
		boolean isFavorite = routeMapping.isFavorite(location);
		if (sharedSnippetStops != null)
		{
			synchronized (sharedSnippetStops)
			{
				for (StopLocation stopLocation : sharedSnippetStops)
				{
					if (routeMapping.isFavorite(stopLocation))
					{
						isFavorite = true;
					}
				}

				//ok, now start setting favorite statuses
				for (StopLocation stopLocation : sharedSnippetStops)
				{
					routeMapping.setFavorite(stopLocation, !isFavorite);
				}
			}
		}
		return routeMapping.setFavorite(location, !isFavorite);
		                            	   
	}

	public void setLastUpdateTime(double lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public long getLastUpdateTime()
	{
		return (long)lastUpdateTime;
	}
}
