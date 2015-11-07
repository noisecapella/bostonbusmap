package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import android.os.RemoteException;
import android.util.Log;

import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;

public class RoutePool extends Pool<String, RouteConfig> {
	private final IDatabaseAgent databaseAgent;
	
	private final ConcurrentMap<String, StopLocation> sharedStops = Maps.newConcurrentMap();
	
	/**
	 * A mapping of stop key to route key. Look in sharedStops for the StopLocation
	 */
	private final CopyOnWriteArraySet<String> favoriteStops = Sets.newCopyOnWriteArraySet();
	
	private final ConcurrentMap<String, IntersectionLocation> intersections = Maps.newConcurrentMap();
	
	private final ITransitSystem transitSystem;
	
	private float maximumDistanceFromIntersection;

	private boolean filterStopsFromIntersection;
	
	public RoutePool(IDatabaseAgent databaseAgent, ITransitSystem transitSystem) {
		super(50);

		this.databaseAgent = databaseAgent;
		this.transitSystem = transitSystem;
		//TODO: define these as settings
		maximumDistanceFromIntersection = 1.0f;
		filterStopsFromIntersection = true;

		populateFavorites();
		
		populateIntersections();
		
	}
	
	/**
	 * If you upgraded, favoritesStops.values() only has nulls. Use the information from the database to figure out
	 * what routes each stop is in.
	 * 
	 * Set the favorite status for all StopLocation favorites, and make sure they persist in the route pool.
	 */
	public void fillInFavoritesRoutes()
	{
		Map<String, StopLocation> stops = getStops(favoriteStops);
		for (String stop : stops.keySet())
		{
			StopLocation stopLocation = stops.get(stop);
			if (stopLocation != null)
			{
				Log.v("BostonBusMap", "setting favorite status to true for " + stop);
				stopLocation.setFavorite(true);
				sharedStops.put(stop, stopLocation);
			}
		}
	}

	
	private Map<String, StopLocation> getStops(AbstractCollection<String> stopTags) {
		if (stopTags.size() == 0)
		{
			return Collections.emptyMap();
		}
		
		ConcurrentMap<String, StopLocation> ret = Maps.newConcurrentMap();
		ArrayList<String> stopTagsToRetrieve = new ArrayList<String>();
		
		for (String stopTag : stopTags)
		{
			StopLocation stop = sharedStops.get(stopTag);
			if (stop == null)
			{
				stopTagsToRetrieve.add(stopTag);
			}
			else
			{
				ret.put(stopTag, stop);
			}
		}
		
		databaseAgent.getStops(ImmutableList.copyOf(stopTagsToRetrieve),
				transitSystem, ret);
		
		if (ret != null)
		{
			sharedStops.putAll(ret);
		}

		return ret;
	}

	protected RouteConfig create(String routeToUpdate) throws IOException {
		return databaseAgent.getRoute(routeToUpdate, sharedStops, transitSystem);
	}
	

	private void populateFavorites() {
        databaseAgent.populateFavorites(favoriteStops);
		fillInFavoritesRoutes();

	}
	
	private void populateIntersections() {
        databaseAgent.populateIntersections(intersections,
				transitSystem, sharedStops, maximumDistanceFromIntersection, filterStopsFromIntersection);
	}

	protected void clearAll() {
		super.clearAll();
		favoriteStops.clear();
		sharedStops.clear();
		intersections.clear();
	}


	public StopLocation[] getFavoriteStops() {
		ArrayList<StopLocation> ret = new ArrayList<StopLocation>(favoriteStops.size());
		
		for (String stopTag : favoriteStops)
		{
			StopLocation stopLocation = sharedStops.get(stopTag);
			
			if (stopLocation != null)
			{
				ret.add(stopLocation);
			}
		}
		
		return ret.toArray(new StopLocation[0]);
	}

	public boolean isFavorite(StopLocation location)
	{
		return favoriteStops.contains(location.getStopTag());
	}
	
	public Favorite setFavorite(StopLocation location, boolean isFavorite) throws RemoteException {
		Collection<String> stopTags = databaseAgent.getAllStopTagsAtLocation(location.getStopTag());

        databaseAgent.saveFavorite(stopTags, isFavorite);
		favoriteStops.clear();
		populateFavorites();
		
		if (isFavorite == false)
		{
			//make sure setFavorite(false) is called for each stop
			for (String tag : stopTags)
			{
				StopLocation stop = sharedStops.get(tag);
				if (stop != null)
				{
					stop.setFavorite(false);
				}
			}
		}
		
		return isFavorite ? Favorite.IsFavorite : Favorite.IsNotFavorite;
	}

	public boolean addIntersection(IntersectionLocation.Builder build) {
		boolean success = databaseAgent.addIntersection(build, transitSystem.getRouteKeysToTitles());
		if (success) {
			populateIntersections();
		}
		return success;
	}
	
	public ConcurrentMap<String, StopLocation> getAllStopTagsAtLocation(String stopTag) {
		ImmutableList<String> tags = databaseAgent.getAllStopTagsAtLocation(stopTag);
		ConcurrentMap<String, StopLocation> outputMapping = Maps.newConcurrentMap();
        databaseAgent.getStops(tags, transitSystem, outputMapping);
		
		return outputMapping;
	}

	public void clearRecentlyUpdated() {
		for (StopLocation stop : sharedStops.values())
		{
			stop.clearRecentlyUpdated();
		}
		
		for (RouteConfig route : values())
		{
			for (StopLocation stop : route.getStopMapping().values())
			{
				stop.clearRecentlyUpdated();
			}
		}
	}
	
	private static class ClosestCacheKey {
		private final double lat;
		private final double lon;
		private final int maxStops;
		private final Set<String> routes;
		private final boolean usesRoutes;
		
		public ClosestCacheKey(double lat, double lon, int maxStops, Set<String> routes, boolean usesRoutes) {
			this.lat = lat;
			this.lon = lon;
			this.maxStops = maxStops;
			this.routes = routes;
			this.usesRoutes = usesRoutes;
		}
		
		public boolean equals(double lat, double lon, int maxStops, Set<String> routes, boolean usesRoutes) {
			return this.usesRoutes == usesRoutes &&
					this.lat == lat &&
					this.lon == lon &&
					this.maxStops == maxStops &&
					this.routes.equals(routes);
		}
	}
	
	private ClosestCacheKey previousKey;
	private Collection<StopLocation> previousValue;
	
	public Collection<StopLocation> getClosestStops(double centerLatitude,
			double centerLongitude, int maxStops)
	{
		return databaseAgent.getClosestStops(centerLatitude, centerLongitude, transitSystem, sharedStops, maxStops);

	}

	public IntersectionLocation getIntersection(String name) {
		if (name == null) {
			return null;
		}
		else
		{
			return intersections.get(name);
		}
	}
	
	public boolean hasIntersection(String name) {
		if (name == null) {
			return false;
		}
		else
		{
			return intersections.containsKey(name);
		}
	}
	
	public void removeIntersection(String name) {
        databaseAgent.removeIntersection(name);
		
		intersections.remove(name);
	}

	public void editIntersectionName(String oldName, String newName) {
        databaseAgent.editIntersectionName(oldName, newName);
		
		intersections.remove(oldName);
		
		populateIntersections();
	}

	public Collection<String> getIntersectionNames() {
		return intersections.keySet();
	}

	public Collection<IntersectionLocation> getIntersections() {
		return intersections.values();
	}
	
	public ITransitSystem getTransitSystem() {
		return transitSystem;
	}

    public void replaceStops(String route, ImmutableMap<String, StopLocation> stops) throws IOException {
        sharedStops.clear();

        RouteConfig routeConfig = get(route);
        routeConfig.replaceStops(stops);
        databaseAgent.replaceStops(stops.values());
    }
}
