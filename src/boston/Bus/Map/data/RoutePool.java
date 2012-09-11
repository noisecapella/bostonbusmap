package boston.Bus.Map.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.LinkedList;

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
	
	private final MyHashMap<String, StopLocation> sharedStops = new MyHashMap<String, StopLocation>();
	
	/**
	 * A mapping of stop key to route key. Look in sharedStops for the StopLocation
	 */
	private final HashSet<String> favoriteStops = new HashSet<String>();
	private final TransitSystem transitSystem;
	
	
	
	public RoutePool(Context context, TransitSystem transitSystem) {
		super(50);

		this.context = context;
		this.transitSystem = transitSystem;
		
		populateFavorites(false);
	}
	
	public void saveFavoritesToDatabase() throws RemoteException, OperationApplicationException
	{
		DatabaseAgent.saveFavorites(context.getContentResolver(), favoriteStops, sharedStops);
	}
	
	/**
	 * If you upgraded, favoritesStops.values() only has nulls. Use the information from the database to figure out
	 * what routes each stop is in.
	 * 
	 * Set the favorite status for all StopLocation favorites, and make sure they persist in the route pool.
	 */
	public void fillInFavoritesRoutes()
	{
		MyHashMap<String, StopLocation> stops = getStops(favoriteStops);
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

	
	private MyHashMap<String, StopLocation> getStops(AbstractCollection<String> stopTags) {
		if (stopTags.size() == 0)
		{
			return null;
		}
		
		MyHashMap<String, StopLocation> ret = new MyHashMap<String, StopLocation>();
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
		
		DatabaseAgent.getStops(context.getContentResolver(), stopTagsToRetrieve, transitSystem, ret);
		
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
	
	public void writeToDatabase(MyHashMap<String, RouteConfig> map, boolean wipe, UpdateAsyncTask task, boolean silent) throws IOException, RemoteException, OperationApplicationException {
		if (!silent)
		{
			task.publish(new ProgressMessage(ProgressMessage.PROGRESS_DIALOG_ON, "Saving to database", null));
		}
		
		HashSet<String> stopTags = new HashSet<String>();
		DatabaseAgent.saveMapping(context.getContentResolver(), map, wipe, stopTags, task);
		
		clearAll();
		populateFavorites(true);
		//saveFavoritesToDatabase();
	}

	
	
	private void populateFavorites(boolean lookForOtherStopsAtSameLocation) {
		DatabaseAgent.populateFavorites(context.getContentResolver(), favoriteStops, lookForOtherStopsAtSameLocation);
		fillInFavoritesRoutes();

	}

	protected void clearAll() {
		super.clearAll();
		favoriteStops.clear();
		sharedStops.clear();
	}


	public ArrayList<String> routeInfoNeedsUpdating(String[] supportedRoutes) {
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
	
	public int setFavorite(StopLocation location, boolean isFavorite) throws RemoteException, OperationApplicationException {
		ArrayList<String> stopTags = DatabaseAgent.getAllStopTagsAtLocation(context.getContentResolver(), location.getStopTag());

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

	public MyHashMap<String, StopLocation> getAllStopTagsAtLocation(String stopTag) {
		ArrayList<String> tags = DatabaseAgent.getAllStopTagsAtLocation(context.getContentResolver(), stopTag);
		MyHashMap<String, StopLocation> outputMapping = new MyHashMap<String, StopLocation>();
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
	
	public ArrayList<StopLocation> getClosestStops(double centerLatitude,
			double centerLongitude, int maxStops)
	{
		return DatabaseAgent.getClosestStops(context.getContentResolver(), 
				centerLatitude, centerLongitude, transitSystem, sharedStops, maxStops);

	}

	public ArrayList<StopLocation> getStopsByDirtag(String dirTag) {
		return DatabaseAgent.getStopsByDirtag(context.getContentResolver(), 
				dirTag, transitSystem);
	}

}
