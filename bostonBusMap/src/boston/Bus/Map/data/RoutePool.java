package boston.Bus.Map.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.schneeloch.latransit.R;
import boston.Bus.Map.main.UpdateAsyncTask;

import android.util.Log;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;

public class RoutePool {
	private final DatabaseHelper helper;
	
	
	private final LinkedList<String> priorities = new LinkedList<String>();
	private final HashMap<String, RouteConfig> pool = new HashMap<String, RouteConfig>();
	private final HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
	
	/**
	 * A mapping of stop key to route key. Look in sharedStops for the StopLocation
	 */
	private final HashSet<String> favoriteStops = new HashSet<String>();

	private final TransitSystem transitSystem;
	
	private static final int MAX_ROUTES = 50;
	
	
	public RoutePool(DatabaseHelper helper, TransitSystem transitSystem) {
		this.helper = helper;
		this.transitSystem = transitSystem;
		
		helper.upgradeIfNecessary();
		populateFavorites(false);
	}
	
	public void saveFavoritesToDatabase()
	{
		helper.saveFavorites(favoriteStops, sharedStops);
	}
	
	/**
	 * If you upgraded, favoritesStops.values() only has nulls. Use the information from the database to figure out
	 * what routes each stop is in.
	 * 
	 * Set the favorite status for all StopLocation favorites, and make sure they persist in the route pool.
	 */
	public void fillInFavoritesRoutes()
	{
		HashMap<String, StopLocation> stops = getStops(favoriteStops);
		if (stops == null)
		{
			return;
		}
		
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

	
	private HashMap<String, StopLocation> getStops(AbstractCollection<String> stopTags) {
		if (stopTags.size() == 0)
		{
			return null;
		}
		
		HashMap<String, StopLocation> ret = new HashMap<String, StopLocation>();
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
		
		helper.getStops(stopTagsToRetrieve, transitSystem, ret);
		
		if (ret != null)
		{
			sharedStops.putAll(ret);
		}

		return ret;
	}

	/**
	 * In the future, this may be necessary to implement. Currently all route data is shipped with the app
	 * 
	 * @param route
	 * @return
	 */
	public boolean isMissingRouteInfo(String route) {
		return false;
	}

	public RouteConfig get(String routeToUpdate) throws IOException {
		debugStateOfPool();
		
		RouteConfig routeConfig = pool.get(routeToUpdate);
		if (routeConfig != null)
		{
			return routeConfig;
		}
		else
		{
			synchronized (helper)
			{
				routeConfig = helper.getRoute(routeToUpdate, sharedStops, transitSystem);
				if (routeConfig == null)
				{
					return null;
				}
				else
				{
					if (priorities.size() >= MAX_ROUTES)
					{
						removeARoute(priorities.get(0));
					}

					addARoute(routeToUpdate, routeConfig);

					return routeConfig;
				}
			}
		}
	}

	private void debugStateOfPool() {
		//commenting this out since it seems to have caused a crash on some computer
		/*ArrayList<String> routes = new ArrayList<String>(pool.size());
		routes.addAll(pool.keySet());
		Collections.sort(routes);
		
		StringBuffer joinable = new StringBuffer();
		for (String route : routes)
		{
			joinable.append(route).append(", ");
		}
		
		Log.v("BostonBusMap", "routes currently in pool: " + joinable);*/
	}

	private void addARoute(String routeToUpdate, RouteConfig routeConfig) {
		priorities.add(routeToUpdate);
		pool.put(routeToUpdate, routeConfig);
	}

	private void removeARoute(String routeToRemove) {
		RouteConfig routeConfig = pool.get(routeToRemove);
		priorities.remove(routeToRemove);
		pool.remove(routeToRemove);
		
		//TODO: can this be done faster?
		if (routeConfig == null)
		{
			return;
		}
		//remove all stops from sharedStops,
		//unless that stop is owned by another route also which is currently in the pool, or it's a favorite
		for (StopLocation stopLocation : routeConfig.getStops())
		{
			boolean keepStop = false;
			for (String route : stopLocation.getRoutes())
			{
				if (pool.containsKey(route))
				{
					//keep this stop
					keepStop = true;
					break;
				}
			}
			
			if (keepStop == false && favoriteStops.contains(stopLocation.getStopTag()))
			{
				keepStop = true;
			}
			
			if (keepStop == false)
			{
				sharedStops.remove(stopLocation.getStopTag());
			}
		}
	}

	public void writeToDatabase(HashMap<String, RouteConfig> map, boolean wipe, UpdateAsyncTask task) throws IOException {
		task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Saving to database", null));
		
		HashSet<String> stopTags = new HashSet<String>();
		helper.saveMapping(map, wipe, stopTags, task);
		
		clearAll();
		populateFavorites(true);
		//saveFavoritesToDatabase();
	}

	
	
	private void populateFavorites(boolean lookForOtherStopsAtSameLocation) {
		helper.populateFavorites(favoriteStops, lookForOtherStopsAtSameLocation);
		fillInFavoritesRoutes();

	}

	private void clearAll() {
		priorities.clear();
		pool.clear();
		favoriteStops.clear();
		sharedStops.clear();
	}


	public ArrayList<String> routeInfoNeedsUpdating(String[] supportedRoutes) {
		//TODO: what if another route gets added later, and we want to download it from the server and add it?
		return helper.routeInfoNeedsUpdating(supportedRoutes);
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
	
	public int setFavorite(StopLocation location, boolean isFavorite) {
		ArrayList<String> stopTags = helper.getAllStopTagsAtLocation(location.getStopTag());

		helper.saveFavorite(location.getStopTag(), stopTags, isFavorite);
		favoriteStops.clear();
		populateFavorites(false);
		
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
		
		return isFavorite ? R.drawable.full_star : R.drawable.empty_star;
	}

	public HashMap<String, StopLocation> getAllStopTagsAtLocation(String stopTag) {
		ArrayList<String> tags = helper.getAllStopTagsAtLocation(stopTag);
		HashMap<String, StopLocation> outputMapping = new HashMap<String, StopLocation>();
		helper.getStops(tags, transitSystem, outputMapping);
		
		return outputMapping;
	}

	public void clearRecentlyUpdated() {
		for (StopLocation stop : sharedStops.values())
		{
			stop.clearRecentlyUpdated();
		}
		
		for (RouteConfig route : pool.values())
		{
			for (StopLocation stop : route.getStopMapping().values())
			{
				stop.clearRecentlyUpdated();
			}
		}
	}
	
	public ArrayList<StopLocation> getClosestStops(double centerLatitude,
			double centerLongitude, int maxStops)
	{
		return helper.getClosestStops(centerLatitude, centerLongitude, transitSystem, sharedStops, maxStops);

	}

}
