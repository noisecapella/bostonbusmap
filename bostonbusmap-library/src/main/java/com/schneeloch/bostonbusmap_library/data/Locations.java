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
package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.TransitSource;
import com.schneeloch.bostonbusmap_library.util.FeedException;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

public final class Locations
{
	/**
	 * A mapping of the bus number to bus location
	 */
	private final VehicleLocations busMapping = new VehicleLocations();
	
	/**
	 * A mapping of a route id to a RouteConfig object.
	 */
	private final RoutePool routeMapping;
	
	private final Directions directions;
	
	private long lastUpdateTime = 0;
	
	private Selection mutableSelection;
	private final ITransitSystem transitSystem;

	public Locations(IDatabaseAgent databaseAgent,
			ITransitSystem transitSystem, Selection selection)
	{
		this.transitSystem = transitSystem;
		routeMapping = new RoutePool(databaseAgent, transitSystem);
		directions = new Directions(databaseAgent);
		mutableSelection = selection;
	}
	
	public String getRouteTitle(String key)
	{
		return transitSystem.getRouteKeysToTitles().getTitle(key);
	}
	
	public int getRouteAsIndex(String key) {
		return transitSystem.getRouteKeysToTitles().getIndexForTag(key);
	}
	
	/**
	 * Update the bus locations based on data from the XML feed 
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 * @throws FeedException 
	 */
	public void refresh(IDatabaseAgent databaseAgent, Selection selection,
			double centerLatitude, double centerLongitude,
			boolean showRoute, Runnable refreshRunnable) throws SAXException, IOException,
			ParserConfigurationException, FactoryConfigurationError, RemoteException, OperationApplicationException 
	{
		final int maxStops = 15;

		//see if route overlays need to be downloaded
		String routeToUpdate = selection.getRoute();
		RouteConfig routeConfig = routeMapping.get(routeToUpdate);
		transitSystem.startObtainAlerts(databaseAgent, refreshRunnable);

		Selection.Mode mode = selection.getMode();
		if (mode == Selection.Mode.BUS_PREDICTIONS_ALL ||
				mode == Selection.Mode.BUS_PREDICTIONS_ONE ||
				mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
				routeMapping.clearRecentlyUpdated();
		}
		if (mode == Selection.Mode.BUS_PREDICTIONS_STAR ||
				mode == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
				mode == Selection.Mode.BUS_PREDICTIONS_ALL)
		{
			//get data from many transit sources
			transitSystem.refreshData(routeConfig, selection, maxStops, centerLatitude,
					centerLongitude, busMapping, routeMapping, directions, this);
		}
		else
		{
			TransitSource transitSource = routeConfig.getTransitSource();
			transitSource.refreshData(routeConfig, selection, maxStops,
					centerLatitude, centerLongitude, busMapping,
					routeMapping, directions, this);
		}
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
	public ImmutableList<Location> getLocations(int maxLocations, double centerLatitude, double centerLongitude, 
			boolean doShowUnpredictable, Selection selection) throws IOException {

		ArrayList<Location> newLocations = Lists.newArrayListWithCapacity(maxLocations);
		
		String selectedRoute = selection.getRoute();
		Selection.Mode mode = selection.getMode();
		if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL ||
				mode == Selection.Mode.VEHICLE_LOCATIONS_ONE)
		{
			if (doShowUnpredictable == false)
			{
				for (BusLocation busLocation : busMapping.values())
				{
					if (busLocation.predictable == true)
					{
						if (mode == Selection.Mode.VEHICLE_LOCATIONS_ONE)
						{
							if (selectedRoute != null && selectedRoute.equals(busLocation.getRouteId()))
							{
								newLocations.add(busLocation);
							}
						}
						else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL) {
							newLocations.add(busLocation);
						}
						else
						{
							throw new RuntimeException("selectedBusPredictions is invalid");
						}
					}
				}
			}
			else
			{
				if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL)
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
		else if (mode == Selection.Mode.BUS_PREDICTIONS_ONE)
		{
			RouteConfig routeConfig = routeMapping.get(selection.getRoute());
			if (routeConfig != null)
			{
				newLocations.addAll(routeConfig.getStops());
			}
		}
		else if (mode == Selection.Mode.BUS_PREDICTIONS_ALL)
		{
			Collection<StopLocation> stops = routeMapping.getClosestStops(centerLatitude, centerLongitude, maxLocations);

			for (StopLocation stop : stops)
			{
				if (stop.supportsBusPredictionsAllMode())
				{
					newLocations.add(stop);
				}
			}
		}
		else if (mode == Selection.Mode.BUS_PREDICTIONS_STAR)
		{
			for (StopLocation stopLocation : routeMapping.getFavoriteStops())
			{
				newLocations.add(stopLocation);
			}
		}
		
		if (mode == Selection.Mode.BUS_PREDICTIONS_ALL ||
				mode == Selection.Mode.BUS_PREDICTIONS_ONE ||
				mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
			newLocations.addAll(routeMapping.getIntersections());
		}
		
		if (maxLocations > newLocations.size())
		{
			maxLocations = newLocations.size();
		}
		

		Collections.sort(newLocations, new LocationComparator(centerLatitude, centerLongitude));
		
		ImmutableList.Builder<Location> ret = ImmutableList.builder();
		//add the first n-th locations, where n is the maximum number of icons we can display on screen without slowing things down
		//however, we shouldn't cut two locations off where they would get combined into one icon anyway
		int count = 0;
		Location lastLocation = null;
		for (Location location : newLocations)
		{
			if (count >= maxLocations)
			{
				if (lastLocation != null && 
					lastLocation.getLatitudeAsDegrees() == location.getLatitudeAsDegrees() && 
					lastLocation.getLongitudeAsDegrees() == location.getLongitudeAsDegrees())
				{
					//ok, let's add one more since it won't affect the framerate at all (and because things would get weird without it)
				}
				else
				{
					break;
				}
			}
			ret.add(location);
			count++;
			lastLocation = location;
		}
		
		return ret.build();
	}

	public Path[] getPaths(String route) {
		try
		{
			RouteConfig routeConfig = routeMapping.get(route);
			if (routeConfig != null)
			{
				return routeConfig.getPaths();
			}
			else
			{
				return RouteConfig.nullPaths;
			}
		}
		catch (IOException e) {
			LogUtil.e(e);
			return RouteConfig.nullPaths;
		}
	}

	
	public Favorite toggleFavorite(StopLocation location) throws RemoteException
	{
		Favorite isFavorite = routeMapping.isFavorite(location);
        Favorite isNotFavorite = isFavorite == Favorite.IsFavorite ? Favorite.IsNotFavorite : Favorite.IsFavorite;

		return routeMapping.setFavorite(location, isNotFavorite);
	}

	public boolean addIntersection(IntersectionLocation.Builder builder) {
		return routeMapping.addIntersection(builder);
	}

	
	public StopLocation[] getCurrentFavorites()
	{
		return routeMapping.getFavoriteStops();
	}
	
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public ConcurrentMap<String, StopLocation> getAllStopsAtStop(String stopTag)
	{
		return routeMapping.getAllStopTagsAtLocation(stopTag);
	}

	public StopLocation setSelectedStop(String route, String stopTag)
	{
		try
		{
			RouteConfig routeConfig = routeMapping.get(route);
			if (routeConfig != null)
			{
				StopLocation stopLocation = routeConfig.getStop(stopTag);
				Selection newSelection = new Selection(Selection.Mode.BUS_PREDICTIONS_ONE, route);
				mutableSelection = newSelection;
				return stopLocation;
			}
			else
			{
				Log.e("BostonBusMap", "bizarre... route doesn't exist: " + (route != null ? route : ""));
			}
		}
		catch (IOException e)
		{
			LogUtil.e(e);
		}
		
		return null;
	}

	/**
	 * Do not modify return value!
	 * @return
	 */
	public String makeNewIntersectionName() {
		int count = 1;
		while (true) {
			String name = "Place " + count;
			if (routeMapping.hasIntersection(name) == false) {
				return name;
			}
			count++;
		}
	}

	public Selection getSelection() {
		return mutableSelection;
	}
	
	public void setSelection(Selection selection) {
		mutableSelection = selection;
	}

	public RouteConfig getRoute(String route) throws IOException {
		return routeMapping.get(route);
	}

	public RouteTitles getRouteTitles() {
		return transitSystem.getRouteKeysToTitles();
	}
	
	public void removeIntersection(String name) {
		routeMapping.removeIntersection(name);
	}
	
	public void editIntersection(String oldName, String newName) {
		routeMapping.editIntersectionName(oldName, newName);
	}

	public boolean containsIntersection(String intersection) {
		return routeMapping.hasIntersection(intersection);
	}
	
	public IntersectionLocation getIntersection(String name) {
		return routeMapping.getIntersection(name);
	}

	public Collection<String> getIntersectionNames() {
		return routeMapping.getIntersectionNames();
	}
	
	public ITransitSystem getTransitSystem() {
		return routeMapping.getTransitSystem();
	}

    public void replaceStops(String route, ImmutableMap<String, StopLocation> values) throws IOException {
        routeMapping.replaceStops(route, values);
    }
}
