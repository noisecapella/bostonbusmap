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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


import android.content.Context;
import android.util.Log;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.FeedException;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StreamCounter;

public final class Locations
{
	/**
	 * A mapping of the bus number to bus location
	 */
	private ConcurrentHashMap<String, VehicleLocation> busMapping = new ConcurrentHashMap<String, VehicleLocation>();
	
	/**
	 * A mapping of a route id to a RouteConfig object.
	 */
	private final RoutePool routeMapping;
	
	private double lastUpdateTime = 0;
	

	private String selectedRoute;
	private int selectedBusPredictions;
	private final TransitSystem transitSystem;
	
	public Locations(DatabaseHelper helper, 
			TransitSystem transitSystem) throws IOException
	{
		this.transitSystem = transitSystem;
		routeMapping = new RoutePool(helper, transitSystem);
	}
	
	public String getRouteName(String key)
	{
		return transitSystem.getTransitSource(key).getRouteKeysToTitles().get(key);
	}
	
	public static InputStream downloadStream(URL url, UpdateAsyncTask task) throws IOException {
		URLConnection connection = url.openConnection();
		int totalDownloadSize = connection.getContentLength();
		InputStream inputStream = connection.getInputStream();

		return new StreamCounter(inputStream, task, totalDownloadSize);
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
	public void refresh(boolean inferBusRoutes, String routeToUpdate,
			int selectedBusPredictions, double centerLatitude, double centerLongitude,
			UpdateAsyncTask updateAsyncTask, boolean showRoute) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError 
	{
		final int maxStops = 35;

		//see if route overlays need to be downloaded
		RouteConfig routeConfig = routeMapping.get(routeToUpdate);
		
		switch (selectedBusPredictions)
		{
		case Main.BUS_PREDICTIONS_STAR:
		case Main.VEHICLE_LOCATIONS_ALL:
			//get data from many transit sources
			transitSystem.refreshData(routeConfig, selectedBusPredictions, maxStops, centerLatitude,
					centerLongitude, busMapping, selectedRoute, routeMapping, this);
			break;
		case Main.BUS_PREDICTIONS_ALL:
		{
			TransitSource transitSource = transitSystem.getTransitSource(null);
			transitSource.refreshData(routeConfig, selectedBusPredictions, maxStops,
					centerLatitude, centerLongitude, busMapping,
					selectedRoute, routeMapping, this);
		}
			break;
		default:
		{
			TransitSource transitSource = routeConfig.getTransitSource();
			transitSource.refreshData(routeConfig, selectedBusPredictions, maxStops,
					centerLatitude, centerLongitude, busMapping,
					selectedRoute, routeMapping, this);
		}
			break;
		}
	}

	public int getSelectedBusPredictions()
	{
		return selectedBusPredictions;
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
	public List<LocationGroup> getLocations(int maxLocations, double centerLatitude, double centerLongitude, 
			boolean doShowUnpredictable) throws IOException {

		ArrayList<LocationGroup> newLocations = new ArrayList<LocationGroup>(maxLocations);
		
		
		if (selectedBusPredictions == Main.VEHICLE_LOCATIONS_ALL || selectedBusPredictions == Main.VEHICLE_LOCATIONS_ONE)
		{
			if (doShowUnpredictable == false)
			{
				for (VehicleLocation busLocation : busMapping.values())
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
					for (VehicleLocation location : busMapping.values())
					{
						if (selectedRoute != null && selectedRoute.equals(location.getRouteId()))
						{
							newLocations.add(location);
						}
					}
				}
			}
			if (maxLocations > newLocations.size()) {
				maxLocations = newLocations.size();
			}
			
			Collections.sort(newLocations, new LocationComparator(centerLatitude, centerLongitude));

		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE)
		{
			RouteConfig routeConfig = routeMapping.get(selectedRoute);
			if (routeConfig != null)
			{
				newLocations.addAll(routeConfig.getStops());
			}
			if (maxLocations > newLocations.size()) {
				maxLocations = newLocations.size();
			}
			
			Collections.sort(newLocations, new LocationComparator(centerLatitude, centerLongitude));
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_ALL)
		{
			ArrayList<LocationGroup> groups = routeMapping.getClosestStops(centerLatitude, centerLongitude, maxLocations);
			for (LocationGroup group : groups) {
				if (group instanceof StopLocationGroup) {
					StopLocationGroup stopLocationGroup = (StopLocationGroup)group;
					TransitSource source = stopLocationGroup.getTransitSource();
					if (source instanceof NextBusTransitSource) {
						newLocations.add(group);
					}
				}
			}
			newLocations.addAll(groups);
		}
		else if (selectedBusPredictions == Main.BUS_PREDICTIONS_STAR)
		{
			newLocations.addAll(routeMapping.getFavoriteStops());

			if (maxLocations > newLocations.size()) {
				maxLocations = newLocations.size();
			}
			
			Collections.sort(newLocations, new LocationComparator(centerLatitude, centerLongitude));

		}

		return newLocations;
	}

	public void select(String newRoute, int busPredictions) {
		selectedRoute = newRoute;

		selectedBusPredictions = busPredictions;
	}

	public Path[] getSelectedPaths() throws IOException {
		RouteConfig routeConfig = routeMapping.get(selectedRoute);
		if (routeConfig != null)
		{
			return routeConfig.getPaths();
		}
		else
		{
			return RouteConfig.nullPaths;
		}
	}

	public RouteConfig getSelectedRoute() throws IOException {
		return routeMapping.get(selectedRoute);
	}
	
	public int toggleFavorite(StopLocationGroup locationGroup)
	{
		boolean isFavorite = routeMapping.isFavorite(locationGroup);
		return routeMapping.setFavorite(locationGroup, !isFavorite);
	}

	public HashSet<StopLocationGroup> getCurrentFavorites()
	{
		return routeMapping.getFavoriteStops();
	}
	
	public void setLastUpdateTime(double lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public long getLastUpdateTime()
	{
		return (long)lastUpdateTime;
	}
	
	public boolean isFavorite(LocationGroup locationGroup) {
		return routeMapping.isFavorite(locationGroup);
	}
}
