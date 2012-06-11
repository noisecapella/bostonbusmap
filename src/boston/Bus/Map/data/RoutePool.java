package boston.Bus.Map.data;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.HashSet;
import java.util.LinkedList;

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
	private final HashSet<String> favoriteStops = new HashSet<String>();

	private final TransitSystem transitSystem;

	private final MyHashMap<LocationGroup, LocationGroup> stopsByLocation;
	private final MyHashMap<String, StopLocation> stopsByTag;
	private final MyHashMap<String, RouteConfig> routesByTag;

	private final Directions directions;

	private final WeightedSqrEuclid<LocationGroup> kdtree;
	
	private static final int MAX_ROUTES = 50;
	
	
	public RoutePool(DatabaseHelper helper, TransitSystem transitSystem) throws IOException {
		this.helper = helper;
		this.transitSystem = transitSystem;
		this.directions = new Directions();

		stopsByLocation = new MyHashMap<LocationGroup, LocationGroup>();
		stopsByTag = new MyHashMap<String, StopLocation>();
		routesByTag = new MyHashMap<String, RouteConfig>();
		
        for (TransitSource transitSource : transitSystem.getTransitSources()) {
        	for (RouteConfig route : transitSource.makeRoutes(directions)) {
        		routesByTag.put(route.getRouteName(), route);
        		for (StopLocation stop : route.getStops()) {
        			stopsByTag.put(stop.getStopTag(), stop);
        			LocationGroup locationGroup = stopsByLocation.get(stop);
        			if (locationGroup != null) {
        				if (locationGroup instanceof MultipleStopLocations) {
        					((MultipleStopLocations)locationGroup).addStop(stop);
        				}
        				else
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
	
	
	public RouteConfig get(String routeToUpdate) throws IOException {
		return routesByTag.get(routeToUpdate);
	}

	
	private void populateFavorites() {
		helper.populateFavorites(favoriteStops);

	}

	public StopLocation[] getFavoriteStops() {
		ArrayList<StopLocation> ret = new ArrayList<StopLocation>(favoriteStops.size());
		
		for (String stopTag : favoriteStops)
		{
			StopLocation stopLocation = stopsByTag.get(stopTag);
			
			ret.add(stopLocation);
		}
		
		return ret.toArray(new StopLocation[0]);
	}

	public boolean isFavorite(StopLocation location)
	{
		return favoriteStops.contains(location.getStopTag());
	}
	
	public int setFavorite(StopLocation location, boolean isFavorite) {
		LocationGroup group = stopsByLocation.get(location);

		helper.saveFavorite(group, isFavorite);
		favoriteStops.clear();
		populateFavorites();
		
		return isFavorite ? R.drawable.full_star : R.drawable.empty_star;
	}

	public LocationGroup getAllStopTagsAtLocation(String stopTag) {
		MyHashMap<String, StopLocation> ret = new MyHashMap<String, StopLocation>();
		StopLocation stop = stopsByTag.get(stopTag);
		LocationGroup group = stopsByLocation.get(stop);
		return group;
	}

	public void clearRecentlyUpdated() {
		for (StopLocation stop : stopsByTag.values()) {
			stop.clearRecentlyUpdated();
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

	public Directions getDirections() {
		return directions;
	}

}
