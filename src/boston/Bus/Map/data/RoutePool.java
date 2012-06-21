package boston.Bus.Map.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import java.util.HashSet;
import java.util.LinkedList;

import cern.colt.list.IntArrayList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.schneeloch.suffixarray.ObjectWithString;
import com.schneeloch.suffixarray.SuffixArray;

import ags.utils.KdTree.Entry;
import ags.utils.KdTree.WeightedSqrEuclid;
import android.util.Log;
import boston.Bus.Map.R;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;

public class RoutePool {
	private final DatabaseHelper helper;
	
	/**
	 * A mapping of stop key to route key. Look in sharedStops for the StopLocation
	 */
	private final MyHashSet<StopLocationGroup> favoriteStops = new MyHashSet<StopLocationGroup>();

	private static MyHashMap<StopLocationGroup, StopLocationGroup> stopsByLocation;
	private static MyHashMap<String, Collection<StopLocationGroup>> routesByTag;

	private static Directions directions;

	private final WeightedSqrEuclid<LocationGroup> kdtree;
	
	private static MyHashMap<String, StopLocationGroup> stopsByTag;
	private static MyHashMap<String, RouteConfig> routes;
	
	public RoutePool(DatabaseHelper helper, TransitSystem transitSystem) throws IOException {
		this.helper = helper;

		if (stopsByLocation == null)
		{
			directions = new Directions();

			stopsByLocation = new MyHashMap<StopLocationGroup, StopLocationGroup>();
			routesByTag = new MyHashMap<String, Collection<StopLocationGroup>>();
			routes = new MyHashMap<String, RouteConfig>();
        	stopsByTag = new MyHashMap<String, StopLocationGroup>();
			ArrayList<RouteConfig> routeList = new ArrayList<RouteConfig>();

			for (TransitSource transitSource : transitSystem.getTransitSources()) {
				for (RouteConfig route : transitSource.makeRoutes(directions)) {
					ArrayList<StopLocationGroup> collection = new ArrayList<StopLocationGroup>();
					routesByTag.put(route.getRouteName(), collection);
					routes.put(route.getRouteName(), route);
					routeList.add(route);
					for (StopLocation stop : route.getStops()) {
						StopLocationGroup locationGroup = stopsByLocation.get(stop);
						if (locationGroup != null) {
							if (locationGroup instanceof MultipleStopLocations) {
								((MultipleStopLocations)locationGroup).addStop(stop);
							}
							else //must be StopLocation
							{
								MultipleStopLocations multipleStopLocations = new MultipleStopLocations((StopLocation)locationGroup, stop);
								stopsByLocation.put(multipleStopLocations, multipleStopLocations);
							}
						}
						else
						{
							stopsByLocation.put(stop, stop);
						}
					}
				}
			}
			for (RouteConfig route : routes.values()) {
				HashSet<StopLocationGroup> alreadyAdded = new HashSet<StopLocationGroup>();
				for (StopLocation stop : route.getStops()) {
					StopLocationGroup group = stopsByLocation.get(stop);
					alreadyAdded.add(group);
				}
				Collection<StopLocationGroup> collection = routesByTag.get(route.getRouteName());
				collection.addAll(alreadyAdded);
			}
			
        	for (StopLocationGroup stopLocationGroup : stopsByLocation.values()) {
        		if (stopLocationGroup instanceof StopLocation) {
        			stopsByTag.put(stopLocationGroup.getFirstStopTag(), stopLocationGroup);
        		}
        		else
        		{
        			for (StopLocation stop : stopLocationGroup.getStops()) {
        				stopsByTag.put(stop.getStopTag(), stopLocationGroup);
        			}
        		}
        	}
		}
		else
		{
			for (StopLocationGroup stopLocationGroup : stopsByLocation.values()) {
				stopLocationGroup.clearPredictions(null);
			}
		}
        
        kdtree = new WeightedSqrEuclid<LocationGroup>(2, stopsByLocation.size());
        for (LocationGroup group : stopsByLocation.values()) {
        	kdtree.addPoint(new double[]{group.getLatitudeAsDegrees(), group.getLongitudeAsDegrees()},
        			group);
        }
        
		populateFavorites();
		
			
	}
	
	public void saveFavoritesToDatabase()
	{
		helper.saveFavorites(favoriteStops);
	}
	
	
	public static Collection<StopLocationGroup> getStopsForRoute(String routeToUpdate) {
		return routesByTag.get(routeToUpdate);
	}

	
	private void populateFavorites() {
		HashSet<String> stopTags = new HashSet<String>();
		helper.populateFavorites(stopTags);
		
		for (StopLocationGroup locationGroup : stopsByLocation.values()) {
			if (locationGroup instanceof MultipleStopLocations) {
				MultipleStopLocations multipleStopLocations = (MultipleStopLocations)locationGroup;
				for (StopLocation stop : multipleStopLocations.getStops()) {
					if (stopTags.contains(stop.getStopTag())) {
						favoriteStops.add(locationGroup);
						break;
					}
				}
			}
			else
			{
				StopLocation stopLocation = (StopLocation)locationGroup;
				if (stopTags.contains(stopLocation.getStopTag())) {
					favoriteStops.add(locationGroup);
				}
			}
		}
	}

	public MyHashSet<StopLocationGroup> getFavoriteStops() {
		return favoriteStops;
	}

	public boolean isFavorite(StopLocationGroup locationGroup)
	{
		return favoriteStops.contains(locationGroup);
	}
	
	public int setFavorite(StopLocationGroup locationGroup, boolean isFavorite) {
		LocationGroup group = stopsByLocation.get(locationGroup);

		helper.saveFavorite(group, isFavorite);
		favoriteStops.clear();
		populateFavorites();
		
		return isFavorite ? R.drawable.full_star : R.drawable.empty_star;
	}

	public void clearRecentlyUpdated() {
		for (LocationGroup stop : stopsByLocation.values()) {
			if (stop instanceof StopLocationGroup) {
				((StopLocationGroup)stop).clearRecentlyUpdated();
			}
		}
	}
	
	public ArrayList<LocationGroup> getClosestStops(double centerLatitude,
			double centerLongitude, int maxStops)
	{
		ArrayList<LocationGroup> ret = new ArrayList<LocationGroup>();
		List<Entry<LocationGroup>> list = kdtree.nearestNeighbor(new double[]{centerLatitude, centerLongitude},
				maxStops, false);
		for (Entry<LocationGroup> entry : list) {
			ret.add(entry.value);
		}
		return ret;

	}

	public static Directions getDirections() {
		return directions;
	}

	public static StopLocationGroup getStop(String stopTag) {
		return stopsByTag.get(stopTag);
	}

	public static RouteConfig getRoute(String selectedRoute) {
		return routes.get(selectedRoute);
	}

	public static <T extends ObjectWithString> Iterable<T> findStuff(String search, final Collection<T> list) {
		final String searchLower = search.toLowerCase();
		Predicate<T> filter = new Predicate<T>() {
			@Override
			public boolean apply(T arg0) {
				return arg0.getString().toLowerCase().contains(searchLower);
			}
		};
		return Iterables.filter(list, filter);
	}
	
	public static Iterable<RouteConfig> findRoutes(String search) {
		return findStuff(search, routes.values());
	}
	
	public static Iterable<StopLocationGroup> findStops(String search) {
		return findStuff(search, stopsByTag.values());
	}

	public static StopLocationGroup getStopByTitleIgnoreCase(
			String search) {
		String searchLower = search.toLowerCase();
		for (StopLocationGroup group : stopsByLocation.values()) {
			if (group.getFirstTitle().toLowerCase().equals(searchLower)) {
				return group;
			}
		}
		return null;
	}
}
