package boston.Bus.Map.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.android.maps.GeoPoint;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;
import boston.Bus.Map.R;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;

public class RoutePool extends Pool<String, RouteConfig> {
	private final Context context;
	
	private final ConcurrentMap<String, StopLocation> sharedStops = Maps.newConcurrentMap();
	
	/**
	 * A mapping of stop key to route key. Look in sharedStops for the StopLocation
	 */
	private final CopyOnWriteArraySet<String> favoriteStops = Sets.newCopyOnWriteArraySet();
	
	private final ConcurrentMap<String, IntersectionLocation> intersections = Maps.newConcurrentMap();
	
	private final TransitSystem transitSystem;
	
	public RoutePool(Context context, TransitSystem transitSystem) {
		super(50);

		this.context = context;
		this.transitSystem = transitSystem;
		
		populateFavorites(false);
		
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
		if (stops.isEmpty())
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
		
		DatabaseAgent.getStops(context.getContentResolver(), ImmutableList.copyOf(stopTagsToRetrieve), 
				transitSystem, ret);
		
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

	protected RouteConfig create(String routeToUpdate) throws IOException {
		return DatabaseAgent.getRoute(context.getContentResolver(), routeToUpdate, sharedStops, transitSystem);
	}
	
	public void writeToDatabase(ImmutableMap<String, RouteConfig> map, UpdateAsyncTask task, boolean silent) throws IOException, RemoteException, OperationApplicationException {
		if (!silent)
		{
			task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Saving to database", null));
		}
		
		HashSet<String> stopTags = Sets.newHashSet();
		DatabaseAgent.saveMapping(context, map, stopTags, task);
		
		clearAll();
		populateFavorites(true);
		//saveFavoritesToDatabase();
	}

	
	
	private void populateFavorites(boolean lookForOtherStopsAtSameLocation) {
		DatabaseAgent.populateFavorites(context.getContentResolver(), favoriteStops, lookForOtherStopsAtSameLocation);
		fillInFavoritesRoutes();

	}
	
	private void populateIntersections() {
		DatabaseAgent.populateIntersections(context.getContentResolver(), intersections,
				transitSystem, sharedStops);
	}

	protected void clearAll() {
		super.clearAll();
		favoriteStops.clear();
		sharedStops.clear();
		intersections.clear();
	}


	public ImmutableList<String> routeInfoNeedsUpdating(RouteTitles supportedRoutes) {
		//TODO: what if another route gets added later, and we want to download it from the server and add it?
		return DatabaseAgent.routeInfoNeedsUpdating(context.getContentResolver(), supportedRoutes);
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
	
	public int setFavorite(StopLocation location, boolean isFavorite) throws RemoteException {
		Collection<String> stopTags = DatabaseAgent.getAllStopTagsAtLocation(context.getContentResolver(), location.getStopTag());

		DatabaseAgent.saveFavorite(context.getContentResolver(), location.getStopTag(), stopTags, isFavorite);
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

	public ConcurrentMap<String, StopLocation> getAllStopTagsAtLocation(String stopTag) {
		ImmutableList<String> tags = DatabaseAgent.getAllStopTagsAtLocation(context.getContentResolver(), stopTag);
		ConcurrentMap<String, StopLocation> outputMapping = Maps.newConcurrentMap();
		DatabaseAgent.getStops(context.getContentResolver(), tags, transitSystem, outputMapping);
		
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
	
	public Collection<StopLocation> getClosestStops(double centerLatitude,
			double centerLongitude, int maxStops, Set<String> routes)
	{
		return DatabaseAgent.getClosestStops(context.getContentResolver(), 
				centerLatitude, centerLongitude, transitSystem, sharedStops, maxStops, routes);

	}

	public ArrayList<StopLocation> getStopsByDirtag(String dirTag) {
		return DatabaseAgent.getStopsByDirtag(context.getContentResolver(), 
				dirTag, transitSystem);
	}

	/**
	 * Be careful not to alter this object!
	 * @return
	 */
	public ConcurrentMap<String, IntersectionLocation> getIntersectPoints() {
		return intersections;
	}

}
