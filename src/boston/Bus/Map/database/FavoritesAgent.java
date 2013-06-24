package boston.Bus.Map.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.os.RemoteException;
import android.util.Log;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.provider.FavoritesContentProvider;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;

/**
 * The names are a bit confusing. This handles interactions with the mutable database,
 * which currently handles favorites and places. The immutable stuff is in
 * DatabaseAgent and in-memory stuff is in InMemoryAgent
 * 
 * @author schneg
 *
 */
public class FavoritesAgent {
	/**
	 * Fill the given HashSet with all stop tags that are favorites
	 * @param favorites
	 */
	public static void populateFavorites(ContentResolver contentResolver, 
			CopyOnWriteArraySet<String> favorites)
	{
		Cursor cursor = null;
		try
		{
			cursor = contentResolver.query(FavoritesContentProvider.FAVORITES_URI, new String[]{Schema.Favorites.tagColumn},
					null, null, null);

			cursor.moveToFirst();
			List<String> toWrite = Lists.newArrayList();
			while (cursor.isAfterLast() == false)
			{
				String favoriteStopKey = cursor.getString(0);

				toWrite.add(favoriteStopKey);

				cursor.moveToNext();
			}
			favorites.addAll(toWrite);
		}
		finally
		{
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * 
	 * @param resolver
	 * @param build
	 * @return true for success, false for failure
	 */
	public static boolean addIntersection(ContentResolver resolver,
			IntersectionLocation.Builder build, TransitSourceTitles routeTitles) {
		// temporary throwaway location. We still need to attach nearby routes to it,
		// that gets done in populateIntersections
		IntersectionLocation location = build.build(routeTitles);
		ContentValues values = new ContentValues();
		values.put(Schema.Locations.nameColumn, location.getName());
		values.put(Schema.Locations.latColumn, location.getLatitudeAsDegrees());
		values.put(Schema.Locations.lonColumn, location.getLongitudeAsDegrees());
		try
		{
			resolver.insert(FavoritesContentProvider.LOCATIONS_URI, values);
			return true;
		}
		catch (SQLException e) {
			LogUtil.e(e);
			return false;
		}
	}

	public static void populateIntersections(
			ContentResolver resolver,
			ConcurrentMap<String, IntersectionLocation> intersections,
			TransitSystem transitSystem, ConcurrentMap<String, StopLocation> sharedStops,
			float miles, boolean filterByDistance) {

		Map<String, IntersectionLocation.Builder> ret = Maps.newHashMap();

		String[] projectionIn = new String[]{Schema.Locations.nameColumn,
				Schema.Locations.latColumn, Schema.Locations.lonColumn};
		Cursor cursor = resolver.query(FavoritesContentProvider.LOCATIONS_URI, projectionIn, null, null, null);
		try
		{
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				String title = cursor.getString(0);

				float lat = cursor.getFloat(1);
				float lon = cursor.getFloat(2);
				IntersectionLocation.Builder builder = 
						new IntersectionLocation.Builder(title, lat, lon);
				ret.put(title, builder);

				cursor.moveToNext();
			}
		}
		finally
		{
			if (cursor != null) {
				cursor.close();
			}
		}

		for (String key : ret.keySet()) {
			// get 35 closest stops for each intersection point, and
			// get all routes mentioned in that list
			// then eliminate those which are farther than a mile
			IntersectionLocation.Builder builder = ret.get(key);

			int limit = 35;

			Collection<StopLocation> stops = DatabaseAgent.getClosestStops(resolver, 
					builder.getLatitudeAsDegrees(), builder.getLongitudeAsDegrees(),
					transitSystem, sharedStops, limit);
			Set<String> routes = Sets.newHashSet();
			for (StopLocation stop : stops) {
				float lat = (float) (builder.getLatitudeAsDegrees() * Geometry.degreesToRadians);
				float lon = (float) (builder.getLongitudeAsDegrees() * Geometry.degreesToRadians);
				if (filterByDistance) {
					float distance = stop.distanceFromInMiles(lat, lon);
					if (distance < miles) {
						routes.addAll(stop.getRoutes());
					}
				}
				else
				{
					routes.addAll(stop.getRoutes());
				}
			}

			for (String route : routes) {
				builder.addRoute(route);
			}

			intersections.put(key, builder.build(transitSystem.getRouteKeysToTitles()));
		}

	}


	public static void saveFavorite(ContentResolver resolver, 
			Collection<String> allStopTagsAtLocation, boolean isFavorite) throws RemoteException {
		if (isFavorite)
		{
			storeFavorite(resolver, allStopTagsAtLocation);
		}
		else
		{
			//delete all stops at location
			resolver.delete(FavoritesContentProvider.FAVORITES_URI, Schema.Favorites.tagColumn + " IN (" + StringUtil.quotedJoin(allStopTagsAtLocation) + ")", null);
		}
	}

	private static void storeFavorite(ContentResolver resolver, Collection<String> stopTags) throws RemoteException
	{
		if (stopTags == null || stopTags.size() == 0)
		{
			return;
		}

		List<ContentValues> allValues = Lists.newArrayList();
		for (String tag : stopTags)
		{
			ContentValues values = new ContentValues();
			values.put(Schema.Favorites.tagColumn, tag);
			allValues.add(values);
		}

		resolver.bulkInsert(FavoritesContentProvider.FAVORITES_URI, allValues.toArray(new ContentValues[0]));
	}


	public static void removeIntersection(ContentResolver contentResolver,
			String name) {
		int result = contentResolver.delete(FavoritesContentProvider.LOCATIONS_URI, Schema.Locations.nameColumn + "= ?", new String[] {name});
		if (result == 0) {
			Log.e("BostonBusMap", "Failed to delete intersection " + name);
		}
	}

	public static void editIntersectionName(
			ContentResolver contentResolver, String oldName, String newName) {
		if (oldName.equals(newName))
		{
			return;
		}

		ContentValues values = new ContentValues();
		values.put(Schema.Locations.nameColumn, newName);
		int result = contentResolver.update(FavoritesContentProvider.LOCATIONS_URI, values, Schema.Locations.nameColumn + "= ?", new String[]{oldName});
		if (result == 0) {
			Log.e("BostonBusMap", "Failed to update intersection");
		}
	}


}
