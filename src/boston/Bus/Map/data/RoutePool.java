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
	
	private static final int MAX_STOPS = 500;
	
	
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

	public ArrayList<LocationGroup> getFavoriteStops() {
		ArrayList<LocationGroup> ret = new ArrayList<LocationGroup>(favoriteStops.size());
		
		for (String stopTag : favoriteStops)
		{
			ret.add(getAllStopTagsAtLocation(stopTag));
		}
		
		return ret;
	}

	public boolean isFavorite(LocationGroup locationGroup)
	{
		if (locationGroup instanceof StopLocation) {
			StopLocation stopLocation = (StopLocation)locationGroup;
			return favoriteStops.contains(stopLocation.getStopTag());
		}
		else if (locationGroup instanceof MultipleStopLocations) {
			MultipleStopLocations multipleStopLocations = (MultipleStopLocations)locationGroup;
			for (StopLocation stop : multipleStopLocations.getStops()) {
				if (favoriteStops.contains(stop.getStopTag())) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int setFavorite(StopLocationGroup locationGroup, boolean isFavorite) {
		LocationGroup group = stopsByLocation.get(locationGroup);

		helper.saveFavorite(group, isFavorite);
		favoriteStops.clear();
		populateFavorites();
		
		return isFavorite ? R.drawable.full_star : R.drawable.empty_star;
	}

	public LocationGroup getAllStopTagsAtLocation(String stopTag) {
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
