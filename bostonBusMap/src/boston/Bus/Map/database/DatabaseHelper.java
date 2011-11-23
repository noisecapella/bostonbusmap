package boston.Bus.Map.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.schneeloch.sftransit.main.UpdateAsyncTask;

import boston.Bus.Map.data.Path;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.StringUtil;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.os.Debug;
import android.os.Parcel;
import android.os.StatFs;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Handles the database which stores route information
 * 
 * @author schneg
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String dbName = "bostonBusMap";
	 
	private final static String verboseRoutes = "routes";
	private final static String verboseStops = "stops";
	private final static String stopsRoutesMap = "stopmapping";
	private final static String stopsRoutesMapIndexTag = "IDX_stopmapping";
	private final static String stopsRoutesMapIndexRoute = "IDX_routemapping";
	private final static String subwaySpecificTable = "subway";
	
	
	private final static String directionsTable = "directions";
	private final static String stopsTable = "stops";
	private final static String routesTable = "routes";
	private final static String pathsTable = "paths";
	private final static String blobsTable = "blobs";
	private final static String oldFavoritesTable = "favs";
	private final static String newFavoritesTable = "favs2";
	
	private final static String verboseFavorites = "favorites";
	
	private final static String routeKey = "route";
	private final static String routeTitleKey = "routetitle";
	private final static String newFavoritesTagKey = "tag";
	private final static String latitudeKey = "lat";
	private final static String longitudeKey = "lon";

	private final static String colorKey = "color";
	private final static String oppositeColorKey = "oppositecolor";
	private final static String pathsBlobKey = "pathblob";
	private final static String stopTagKey = "tag";
	private final static String branchKey = "branch";
	private final static String stopTitleKey = "title";
	private final static String platformOrderKey = "platformorder";
	
	
	private static final String dirTagKey = "dirTag";
	private static final String dirNameKey = "dirNameKey";
	private static final String dirTitleKey = "dirTitleKey";
	private static final String dirRouteKey = "dirRouteKey";
	
	/**
	 * The first version where we serialize as bytes, not necessarily the first db version
	 */
	public final static int FIRST_DB_VERSION = 5;
	public final static int ADDED_FAVORITE_DB_VERSION = 6;
	public final static int NEW_ROUTES_DB_VERSION = 7;	
	public final static int ROUTE_POOL_DB_VERSION = 8;
	public final static int STOP_LOCATIONS_STORE_ROUTE_STRINGS = 9;
	public final static int STOP_LOCATIONS_ADD_DIRECTIONS = 10;
	public final static int SUBWAY_VERSION = 11;
	public final static int ADDED_PLATFORM_ORDER = 12;
	public final static int VERBOSE_DB = 13;
	public final static int VERBOSE_DB_2 = 14;
	public final static int VERBOSE_DB_3 = 15;
	public final static int VERBOSE_DB_4 = 16;
	public final static int VERBOSE_DB_5 = 17;
	public final static int VERBOSE_DB_6 = 18;
	public final static int VERBOSE_DB_7 = 19;

	public final static int VERBOSE_DB_8 = 20;
	public final static int VERBOSE_DB_9 = 21;
	public final static int VERBOSE_DB_10 = 22;
	public final static int VERBOSE_DB_11 = 23;

	public final static int VERBOSE_DBV2_1 = 24;
	
	public final static int CURRENT_DB_VERSION = VERBOSE_DBV2_1;
	
	public static final int ALWAYS_POPULATE = 3;
	public static final int POPULATE_IF_UPGRADE = 2;
	public static final int MAYBE = 1;

	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, CURRENT_DB_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		/*db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING PRIMARY KEY, " + blobKey + " BLOB)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + routePoolTable + " (" + routeKey + " STRING PRIMARY KEY)");*/
		db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseFavorites + " (" + stopTagKey + " STRING PRIMARY KEY)");
						
		db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + dirTagKey + " STRING PRIMARY KEY, " + 
				dirNameKey + " STRING, " + dirTitleKey + " STRING, " + dirRouteKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseRoutes + " (" + routeKey + " STRING PRIMARY KEY, " + colorKey + 
				" INTEGER, " + oppositeColorKey + " INTEGER, " + pathsBlobKey + " BLOB, " + routeTitleKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
				latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + stopsRoutesMap + " (" + routeKey + " STRING, " + stopTagKey + " STRING, " +
				dirTagKey + " STRING, PRIMARY KEY (" + routeKey + ", " + stopTagKey + "))");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + subwaySpecificTable + " (" + stopTagKey + " STRING PRIMARY KEY, " +
				platformOrderKey + " INTEGER, " + 
				branchKey + " STRING)");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexRoute + " ON " + stopsRoutesMap + " (" + routeKey + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexTag + " ON " + stopsRoutesMap + " (" + stopTagKey + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("BostonBusMap", "upgrading database from " + oldVersion + " to " + newVersion);
		HashSet<String> favorites = null;

		db.beginTransaction();
		if (oldVersion > STOP_LOCATIONS_STORE_ROUTE_STRINGS && oldVersion < VERBOSE_DB)
		{
			favorites = readOldFavorites(db);
		}
		else if (oldVersion >= VERBOSE_DB)
		{
			favorites = new HashSet<String>();
			populateFavorites(favorites, true, db);
		}
		
		if (oldVersion < VERBOSE_DBV2_1)
		{
			db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
			db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
			db.execSQL("DROP TABLE IF EXISTS " + routesTable);
			db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
			db.execSQL("DROP TABLE IF EXISTS " + blobsTable);
			db.execSQL("DROP TABLE IF EXISTS " + verboseRoutes);
			db.execSQL("DROP TABLE IF EXISTS " + verboseStops);
			db.execSQL("DROP TABLE IF EXISTS " + stopsRoutesMap);

			db.execSQL("DROP TABLE IF EXISTS " + oldFavoritesTable);
			db.execSQL("DROP TABLE IF EXISTS " + newFavoritesTable);
		}

		//if it's verboseFavorites, we want to save it since it's user specified data

		onCreate(db);

		if (favorites != null)
		{
			writeVerboseFavorites(db, favorites);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		
	}

	private void writeVerboseFavorites(SQLiteDatabase db, HashSet<String> favorites) {
		for (String favorite : favorites) {
			ContentValues values = new ContentValues();
			values.put(stopTagKey, favorite);
			
			db.replace(verboseFavorites, null, values);
		}
	}
	
	private HashSet<String> readOldFavorites(SQLiteDatabase database)
	{
		Cursor cursor = null;
		try
		{
			HashSet<String> ret = new HashSet<String>();
			
			cursor = database.query(newFavoritesTable, new String[]{newFavoritesTagKey}, null, null, null, null, null);
			
			cursor.moveToFirst();
			
			while (cursor.isAfterLast() == false)
			{
				String tag = cursor.getString(0);
				ret.add(tag);
				
				cursor.moveToNext();
			}
			
			return ret;
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
	
	public synchronized void populateFavorites(HashSet<String> favorites, boolean lookForOtherStopsAtSameLocation)
	{
		SQLiteDatabase database = null;
		try
		{
			database = getReadableDatabase();
			populateFavorites(favorites, lookForOtherStopsAtSameLocation, database);
		}
		finally
		{
			database.close();
		}
	}
	
	/**
	 * Fill the given HashSet with all stop tags that are favorites
	 * @param favorites
	 */
	public synchronized void populateFavorites(HashSet<String> favorites, boolean lookForOtherStopsAtSameLocation, 
			SQLiteDatabase database)
	{
		Cursor cursor = null;

		try
		{
			if (lookForOtherStopsAtSameLocation)
			{
				//get all stop tags which are at the same location as stop tags in the database
				//this is a relatively expensive query but it should only be done once, when the
				//database needs it
				SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
				builder.setTables(verboseFavorites + " JOIN stops as s1 ON " + verboseFavorites + "." + stopTagKey +
						" = s1." + stopTagKey + " JOIN stops as s2 ON s1." +  latitudeKey + 
						" = s2." + latitudeKey + " AND s1." + longitudeKey + " = s2." + longitudeKey + "");
				builder.setDistinct(true);
				
				cursor = builder.query(database, new String[]{"s2." + stopTagKey}, null, null, null, null, null);
			}
			else
			{
				SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
				builder.setTables(verboseFavorites);

				cursor = builder.query(database, new String[]{stopTagKey}, 
						null, null, null, null, null);

			}
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String favoriteStopKey = cursor.getString(0);
				
				favorites.add(favoriteStopKey);

				cursor.moveToNext();
			}
		}		
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
	
	public synchronized void saveMapping(HashMap<String, RouteConfig> mapping,
			boolean wipe, HashSet<String> sharedStops, UpdateAsyncTask task) throws IOException
	{
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();

			if (wipe)
			{
				//database.delete(stopsTable, null, null);
				//database.delete(directionsTable, null, null);
				//database.delete(pathsTable, null, null);
				//database.delete(blobsTable, null, null);
				
				database.delete(verboseStops, null, null);
				database.delete(verboseRoutes, null, null);
			}

			int total = mapping.keySet().size();
			task.publish(new ProgressMessage(ProgressMessage.SET_MAX, total));
			
			int count = 0;
			for (String route : mapping.keySet())
			{
				RouteConfig routeConfig = mapping.get(route);
				if (routeConfig != null)
				{
					String routeTitle = routeConfig.getRouteTitle();
					saveMappingKernel(database, route, routeTitle, routeConfig, sharedStops);
				}
				
				count++;
				task.publish(count);
			}

			database.setTransactionSuccessful();
			database.endTransaction();

		}
		finally
		{
			database.close();
		}
	}
	
	/**
	 * 
	 * @param database
	 * @param route
	 * @param routeConfig
	 * @param useInsert insert all rows, don't replace them. I assume this is faster since there's no lookup involved
	 * @throws IOException 
	 */
	private void saveMappingKernel(SQLiteDatabase database, String route, String routeTitle, RouteConfig routeConfig,
			HashSet<String> sharedStops) throws IOException
	{
		Box serializedPath = new Box(null, CURRENT_DB_VERSION);
		
		routeConfig.serializePath(serializedPath);
		
		byte[] serializedPathBlob = serializedPath.getBlob();
		
		{
			ContentValues values = new ContentValues();
			values.put(routeKey, route);
			values.put(routeTitleKey, routeTitle);
			values.put(pathsBlobKey, serializedPathBlob);
			values.put(colorKey, routeConfig.getColor());
			values.put(oppositeColorKey, routeConfig.getOppositeColor());

			database.replace(verboseRoutes, null, values);
		}
		
		//add all stops associated with the route, if they don't already exist
		

		database.delete(stopsRoutesMap, routeKey + "=?", new String[]{route});
		
		
		for (StopLocation stop : routeConfig.getStops())
		{
			/*"CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
			latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING, " +
			branchKey + " STRING, " + platformOrderKey + " SHORT)"*/
			String stopTag = stop.getStopTag();
			
			if (sharedStops.contains(stopTag) == false)
			{
			
				sharedStops.add(stopTag);

				{
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopTag);
					values.put(latitudeKey, stop.getLatitudeAsDegrees());
					values.put(longitudeKey, stop.getLongitudeAsDegrees());
					values.put(stopTitleKey, stop.getTitle());

					database.replace(verboseStops, null, values);
				}

				if (stop instanceof SubwayStopLocation)
				{
					SubwayStopLocation subwayStop = (SubwayStopLocation)stop;
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopTag);
					values.put(platformOrderKey, subwayStop.getPlatformOrder());
					values.put(branchKey, subwayStop.getBranch());

					database.replace(subwaySpecificTable, null, values);
				}
			}
			
			{
				//show that there's a relationship between the stop and this route
				ContentValues values = new ContentValues();
				values.put(routeKey, route);
				values.put(stopTagKey, stopTag);
				values.put(dirTagKey, stop.getDirTagForRoute(route));
				database.replace(stopsRoutesMap, null, values);
			}
		}
	}

	public synchronized boolean checkFreeSpace() {
		SQLiteDatabase database = getReadableDatabase();
		try
		{
			String path = database.getPath();
			
			StatFs statFs = new StatFs(path);
			long freeSpace = (long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize(); 
		
			Log.v("BostonBusMap", "free database space: " + freeSpace);
			return freeSpace >= 1024 * 1024 * 4;
		}
		catch (Exception e)
		{
			//if for some reason we don't have permission to check free space available, just hope that everything's ok
			return true;
		}
		finally
		{
			database.close();
		}
	}
	

	public synchronized ArrayList<String> getAllStopTagsAtLocation(String stopTag)
	{
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			if (database.isOpen() == false)
			{
				Log.e("BostonBusMap", "SERIOUS ERROR: database didn't save data properly");
				return null;
			}

			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(verboseStops + " as s1, " + verboseStops + " as s2");
			cursor = builder.query(database, new String[] {"s2." + stopTagKey},
					"s1." + stopTagKey + " = ? AND s1." + latitudeKey + " = s2." + latitudeKey +
					" AND s1." + longitudeKey + " = s2." + longitudeKey + "", new String[]{stopTag}, null, null, null);
			
			ArrayList<String> ret = new ArrayList<String>();
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String tag = cursor.getString(0);
				ret.add(tag);
				
				cursor.moveToNext();
			}
			
			return ret;
		}
		finally
		{
			database.close();
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
	
	private void storeFavorite(ArrayList<String> stopTags)
	{
		if (stopTags == null || stopTags.size() == 0)
		{
			return;
		}
		
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();
			for (String tag : stopTags)
			{
				ContentValues values = new ContentValues();
				values.put(stopTagKey, tag);
				
				database.replace(verboseFavorites, null, values);
			}
			
			database.setTransactionSuccessful();
			database.endTransaction();
		}
		finally
		{
			database.close();
		}
	}
	
	
	public synchronized void saveFavorite(String stopTag, ArrayList<String> stopTags, boolean isFavorite) {
		//Log.v("BostonBusMap", "Saving favorite " + lat + ", " + lon + " as " + isFavorite);

		if (isFavorite)
		{
			storeFavorite(stopTags);
		}
		else
		{
			//delete all stops at location
			
			SQLiteDatabase database = getWritableDatabase();
			
			try
			{
				if (database.isOpen() == false)
				{
					Log.e("BostonBusMap", "SERIOUS ERROR: database didn't save data properly");
					return;
				}

				database.beginTransaction();
				//delete all tags from favorites where the lat/lon of stopTag matches those tags
				database.delete(verboseFavorites, verboseFavorites + "." + stopTagKey + 
						" IN (SELECT s2." + stopTagKey + " FROM " + verboseStops + " as s1, " + verboseStops + " as s2 WHERE " +
						"s1." + latitudeKey + " = s2." + latitudeKey + " AND s1." + longitudeKey +
						" = s2." + longitudeKey + " AND s1." + stopTagKey + " = ?)", new String[]{stopTag});
				database.setTransactionSuccessful();
				database.endTransaction();
			}
			finally
			{
				database.close();
			}
		}
	}

	public synchronized RouteConfig getRoute(String routeToUpdate, HashMap<String, StopLocation> sharedStops,
			TransitSystem transitSystem) throws IOException {
		SQLiteDatabase database = getReadableDatabase();
		Cursor routeCursor = null;
		Cursor stopCursor = null;
		try
		{
			/*db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseRoutes + " (" + routeKey + " STRING PRIMARY KEY, " + colorKey + 
					" INTEGER, " + oppositeColorKey + " INTEGER, " + pathsBlobKey + " BLOB)");*/

			//get the route-specific information, like the path outline and the color
			routeCursor = database.query(verboseRoutes, new String[]{colorKey, oppositeColorKey, pathsBlobKey, routeTitleKey}, routeKey + "=?",
					new String[]{routeToUpdate}, null, null, null);
			if (routeCursor.getCount() == 0)
			{
				return null;
			}
			
			routeCursor.moveToFirst();

			TransitSource source = transitSystem.getTransitSource(routeToUpdate);

			int color = routeCursor.getInt(0);
			int oppositeColor = routeCursor.getInt(1);
			byte[] pathsBlob = routeCursor.getBlob(2);
			String routeTitle = routeCursor.getString(3);
			Box pathsBlobBox = new Box(pathsBlob, CURRENT_DB_VERSION);

			RouteConfig routeConfig = new RouteConfig(routeToUpdate, routeTitle, color, oppositeColor, source, pathsBlobBox);

			
			
			//get all stops, joining in the subway stops, making sure that the stop references the route we're on
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			String tables = verboseStops +
			" JOIN " + stopsRoutesMap + " AS sm1 ON (" + verboseStops + "." + stopTagKey + " = sm1." + stopTagKey + ")" +
			" JOIN " + stopsRoutesMap + " AS sm2 ON (" + verboseStops + "." + stopTagKey + " = sm2." + stopTagKey + ")" +
			" LEFT OUTER JOIN " + subwaySpecificTable + " ON (" + verboseStops + "." + stopTagKey + " = " + 
			subwaySpecificTable + "." + stopTagKey + ")";

			
			/* select stops.tag, lat, lon, title, platformorder, branch, stopmapping1.dirTag, stopmapping2.route 
			 * from stops inner join stopmapping as stopmapping1 on (stops.tag = stopmapping1.tag) 
			 * inner join stopmapping as stopmapping2 on (stops.tag = stopmapping2.tag)
			 * left outer join subway on (stops.tag = subway.tag) 
			 * where stopmapping1.route=71;*/ 
			builder.setTables(tables);
			
			String[] projectionIn = new String[] {verboseStops + "." + stopTagKey, latitudeKey, longitudeKey, 
					stopTitleKey, platformOrderKey, branchKey, "sm2." + dirTagKey, "sm2." + routeKey};
			String select = "sm1." + routeKey + "=?";
			String[] selectArray = new String[]{routeToUpdate};
			
			Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, "sm1." + routeKey + "=\"" + routeToUpdate + "\"",
					null, null, null, null));
			
			stopCursor = builder.query(database, projectionIn, select, selectArray, null, null, null);
			
			
			stopCursor.moveToFirst();
			while (stopCursor.isAfterLast() == false)
			{
				String stopTag = stopCursor.getString(0);
				String dirTag = stopCursor.getString(6);
				String route = stopCursor.getString(7);

				//we need to ensure this stop is in the sharedstops and the route
				StopLocation stop = sharedStops.get(stopTag);
				if (stop != null)
				{
					//make sure it exists in the route too
					StopLocation stopInRoute = routeConfig.getStop(stopTag);
					if (stopInRoute == null)
					{
						routeConfig.addStop(stopTag, stop);
					}
					stop.addRouteAndDirTag(route, dirTag);
				}
				else
				{
					stop = routeConfig.getStop(stopTag);
					
					if (stop == null)
					{
						float latitude = stopCursor.getFloat(1);
						float longitude = stopCursor.getFloat(2);
						String stopTitle = stopCursor.getString(3);
						String branch = stopCursor.getString(5);

						int platformOrder = stopCursor.getInt(4);

						stop = transitSystem.createStop(latitude, longitude, stopTag, stopTitle, platformOrder, branch, route, dirTag);
						
						routeConfig.addStop(stopTag, stop);
					}
					
					sharedStops.put(stopTag, stop);
				}
				stopCursor.moveToNext();
			}
			Log.v("BostonBusMap", "getRoute ended successfully");
			
			return routeConfig;
		}
		finally
		{
			database.close();
			if (routeCursor != null)
			{
				routeCursor.close();
			}
			if (stopCursor != null)
			{
				stopCursor.close();
			}
		}
	}

	public synchronized ArrayList<String> routeInfoNeedsUpdating(String[] supportedRoutes) {
		HashSet<String> routesInDB = new HashSet<String>();
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			cursor = database.query(verboseRoutes, new String[]{routeKey}, null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				routesInDB.add(cursor.getString(0));
				
				cursor.moveToNext();
			}
		}
		finally
		{
			database.close();
			if (cursor != null)
			{
				cursor.close();
			}
		}
		
		ArrayList<String> routesThatNeedUpdating = new ArrayList<String>();
		
		for (String route : supportedRoutes)
		{
			if (routesInDB.contains(route) == false)
			{
				routesThatNeedUpdating.add(route);
			}
		}
		
		return routesThatNeedUpdating;
	}

	/**
	 * Populate directions from the database
	 * 
	 * NOTE: these data structures are assumed to be synchronized
	 * @param indexes
	 * @param names
	 * @param titles
	 */
	public synchronized void refreshDirections(HashMap<String, Integer> indexes,
			ArrayList<String> names, ArrayList<String> titles, ArrayList<String> routes) {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			cursor = database.query(directionsTable, new String[]{dirTagKey, dirNameKey, dirTitleKey, dirRouteKey},
					null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String dirTag = cursor.getString(0);
				String dirName = cursor.getString(1);
				String dirTitle = cursor.getString(2);
				String dirRoute = cursor.getString(3);
				
				indexes.put(dirTag, names.size());
				names.add(dirName);
				titles.add(dirTitle);
				routes.add(dirRoute);
				
				cursor.moveToNext();
			}
		}
		finally
		{
			database.close();
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}

	public synchronized void writeDirections(boolean wipe, HashMap<String, Integer> indexes,
			ArrayList<String> names, ArrayList<String> titles, ArrayList<String> routes) {
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();
			if (wipe)
			{
				database.delete(directionsTable, null, null);
			}

			for (String dirTag : indexes.keySet())
			{
				Integer i = indexes.get(dirTag);
				if (i >= names.size() || i >= titles.size() || i >= routes.size())
				{
					//should be a rare case hopefully
					continue;
				}
				String name = names.get(i);
				String title = titles.get(i);
				String route = routes.get(i);

				ContentValues values = new ContentValues();
				values.put(dirNameKey, name);
				values.put(dirRouteKey, route);
				values.put(dirTagKey, dirTag);
				values.put(dirTitleKey, title);

				if (wipe)
				{
					database.insert(directionsTable, null, values);
				}
				else
				{
					database.replace(directionsTable, null, values);
				}
			}
			database.setTransactionSuccessful();
			database.endTransaction();
		}
		finally
		{
			database.close();
		}
	}

	public synchronized void saveFavorites(HashSet<String> favoriteStops, HashMap<String, StopLocation> sharedStops) {
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();

			database.delete(verboseFavorites, null, null);

			for (String stopTag : favoriteStops)
			{
				StopLocation stopLocation = sharedStops.get(stopTag);
				
				if (stopLocation != null)
				{
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopTag);
					database.replace(verboseFavorites, null, values);
				}
			}

			database.setTransactionSuccessful();
			database.endTransaction();

		}
		finally
		{
			database.close();
		}
	}

	public synchronized Cursor getCursorForRoutes() {
		SQLiteDatabase database = getReadableDatabase();
		
		return database.query(verboseRoutes, new String[]{routeKey}, null, null, null, null, null);
	}

	public synchronized Cursor getCursorForSearch(String search) {
		String[] columns = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_TEXT_2};
		MatrixCursor ret = new MatrixCursor(columns);
		
		SQLiteDatabase database = getReadableDatabase();
		try
		{
			addSearchRoutes(database, search, ret);
			addSearchStops(database, search, ret);
		}
		finally
		{
			database.close();
		}
		
		
		return ret;
	}

	private void addSearchRoutes(SQLiteDatabase database, String search, MatrixCursor ret)
	{
		if (search == null)
		{
			return;
		}
		
		Cursor cursor = null;
		try
		{
			cursor = database.query(verboseRoutes, new String[]{routeTitleKey, routeKey}, routeTitleKey + " LIKE ?",
					new String[]{"%" + search + "%"}, null, null, routeTitleKey);
			if (cursor.moveToFirst() == false)
			{
				return;
			}

			int count = 0;
			while (!cursor.isAfterLast())
			{
				String routeTitle = cursor.getString(0);
				String routeKey = cursor.getString(1);

				ret.addRow(new Object[]{count, routeTitle, "route " + routeKey, "Route"});

				cursor.moveToNext();
				count++;
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
	
	private void addSearchStops(SQLiteDatabase database, String search, MatrixCursor ret)
	{
		if (search == null)
		{
			return;
		}
		
		Cursor cursor = null;
		try
		{
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			
			String tables = verboseStops +
			" JOIN " + stopsRoutesMap + " AS sm1 ON (" + verboseStops + "." + stopTagKey + " = sm1." + stopTagKey + ")" +
			" JOIN " + verboseRoutes + " AS r1 ON (sm1." + routeKey + " = r1." + routeKey + ")";

			
			builder.setTables(tables);
			
			
			String thisStopTitleKey = verboseStops + "." + stopTitleKey;
			String[] projectionIn = new String[] {thisStopTitleKey, verboseStops + "." + stopTagKey, "r1." + routeTitleKey};
			String select = thisStopTitleKey + " LIKE ?";
			String[] selectArray = new String[]{"%" + search + "%"};
			
			cursor = builder.query(database, projectionIn, select, selectArray, null, null, thisStopTitleKey);

			if (cursor.moveToFirst() == false)
			{
				return;
			}

			int count = 0;
			String prevStopTag = null;
			String prevStopTitle = null;
			StringBuilder routes = new StringBuilder();
			int routeCount = 0;
			while (!cursor.isAfterLast())
			{
				String stopTitle = cursor.getString(0);
				String stopTag = cursor.getString(1);
				String routeTitle = cursor.getString(2);

				if (prevStopTag == null)
				{
					// do nothing, first row
					prevStopTag = stopTag;
					prevStopTitle = stopTitle;
					routeCount++;
					routes.append(routeTitle);
				}
				else if (!prevStopTag.equals(stopTag))
				{
					// change in row. write out this row
					String routeString = routeCount == 0 ? "Stop" 
							: routeCount == 1 ? ("Stop on route " + routes.toString())
									: ("Stop on routes " + routes);
					ret.addRow(new Object[]{count, prevStopTitle, "stop " + prevStopTag, routeString});
					prevStopTag = stopTag;
					prevStopTitle = stopTitle;
					routeCount = 1;
					routes.setLength(0);
					routes.append(routeTitle);
				}
				else
				{
					// just add a new route
					routes.append(", ");
					routes.append(routeTitle);
					routeCount++;
				}
				

				cursor.moveToNext();
				count++;
			}
			
			if (prevStopTag != null)
			{
				// at least one row
				String routeString = routeCount == 0 ? "Stop" 
						: routeCount == 1 ? ("Stop on route " + routes.toString())
								: ("Stop on routes " + routes);
				ret.addRow(new Object[]{count, prevStopTitle, "stop " + prevStopTag, routeString});
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
	public synchronized ArrayList<StopLocation> getClosestStops(double currentLat, double currentLon, TransitSystem transitSystem, HashMap<String, StopLocation> sharedStops, int limit)
	{
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			// what we should scale longitude by for 1 unit longitude to roughly equal 1 unit latitude
			double lonFactor = Math.cos(currentLat * Geometry.degreesToRadians);
			
			
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			
			String tables = verboseStops;

			builder.setTables(tables);
			
			final String distanceKey = "distance";
			String[] projectionIn = new String[] {stopTagKey, distanceKey};
			HashMap<String, String> projectionMap = new HashMap<String, String>();
			projectionMap.put(stopTagKey, stopTagKey);

			String latDiff = "(" + latitudeKey + " - " + currentLat + ")";
			String lonDiff = "((" + longitudeKey + " - " + currentLon + ")*" + lonFactor + ")";
			projectionMap.put("distance", latDiff + "*" + latDiff + " + " + lonDiff + "*" + lonDiff + " AS " + distanceKey);
			builder.setProjectionMap(projectionMap);
			cursor = builder.query(database, projectionIn, null, null, null, null, distanceKey, new Integer(limit).toString());
			
			if (cursor.moveToFirst() == false)
			{
				return new ArrayList<StopLocation>();
			}
			
			ArrayList<String> stopTags = new ArrayList<String>();
			while (!cursor.isAfterLast())
			{
				String id = cursor.getString(0);
				stopTags.add(id);
				
				cursor.moveToNext();
			}
			
			getStops(stopTags, transitSystem, sharedStops);
			
			ArrayList<StopLocation> ret = new ArrayList<StopLocation>();
			for (String stopTag : stopTags)
			{
				ret.add(sharedStops.get(stopTag));
			}
			
			return ret;
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
			database.close();
		}
	}
	
	public synchronized Cursor getCursorForDirection(String dirTag) {
		SQLiteDatabase database = getReadableDatabase();
		
		return database.query(directionsTable, new String[]{dirTagKey, dirTitleKey, dirRouteKey}, dirTagKey + "=?", 
				new String[]{dirTag}, null, null, null);
		
	}

	public synchronized Cursor getCursorForDirections() {
		SQLiteDatabase database = getReadableDatabase();
		
		return database.query(directionsTable, new String[]{dirTagKey, dirTitleKey, dirRouteKey}, null, 
				null, null, null, null);
	}
	
	public synchronized void upgradeIfNecessary() {
		//trigger an upgrade so future calls of getReadableDatabase won't complain that you can't upgrade a read only db
		getWritableDatabase();
		
	}

	/**
	 * Is stopTag a stop? Returns the number of routes it's on
	 * @param stopTag
	 * @return
	 */
	public ArrayList<String> isStop(String stopTag)
	{
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(stopsRoutesMap);
			String[] projectionIn = new String[] {routeKey};
			
			cursor = builder.query(database, projectionIn, stopTagKey + " = ?", new String[] {stopTag}, null, null, null);
			
			ArrayList<String> routes = new ArrayList<String>(3);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String route = cursor.getString(0);
				routes.add(route);
				
				cursor.moveToNext();
			}
			
			return routes;
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
			database.close();
		}
	}
	
	/**
	 * Read stops from the database and return a mapping of the stop tag to the stop object
	 * @param stopTag
	 * @param transitSystem
	 * @return
	 */
	public void getStops(List<String> stopTags, TransitSystem transitSystem, HashMap<String, StopLocation> outputMapping) {
		if (stopTags == null || stopTags.size() == 0)
		{
			return;
		}
		
		SQLiteDatabase database = getReadableDatabase();
		Cursor stopCursor = null;
		try
		{
			//TODO: we should have a factory somewhere to abstract details away regarding subway vs bus

			//get stop with name stopTag, joining with the subway table
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			String tables = verboseStops + " JOIN " + stopsRoutesMap + " ON (" + verboseStops + "." + stopTagKey + " = " +
			stopsRoutesMap + "." + stopTagKey + ") LEFT OUTER JOIN " +
			subwaySpecificTable + " ON (" + verboseStops + "." + stopTagKey + " = " + 
			subwaySpecificTable + "." + stopTagKey + ")";


			builder.setTables(tables);

			String[] projectionIn = new String[] {verboseStops + "." + stopTagKey, latitudeKey, longitudeKey, 
					stopTitleKey, platformOrderKey, branchKey, stopsRoutesMap + "." + routeKey, stopsRoutesMap + "." + dirTagKey};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;
			if (stopTags.size() == 1)
			{
				String stopTag = stopTags.get(0);
				
				select = new StringBuilder(verboseStops + "." + stopTagKey + "=?");
				selectArray = new String[]{stopTag};

				Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, verboseStops + "." + stopTagKey + "=\"" + stopTagKey + "\"",
						null, null, null, null));
			}
			else
			{
				select = new StringBuilder(verboseStops + "." + stopTagKey + " IN (");
				
				for (int i = 0; i < stopTags.size(); i++)
				{
					String stopTag = stopTags.get(i);
					select.append('\'').append(stopTag);
					if (i != stopTags.size() - 1)
					{
						select.append("', ");
					}
					else
					{
						select.append("')");
					}
				}
				selectArray = null;
				
				Log.v("BostonBusMap", select.toString());
			}

			stopCursor = builder.query(database, projectionIn, select.toString(), selectArray, null, null, null);

			stopCursor.moveToFirst();
			
			//iterate through the stops in the database and create new ones if necessary
			//stops will be repeated if they are on multiple routes. If so, just skip to the bottom and add the route and dirTag
			while (stopCursor.isAfterLast() == false)
			{
				String stopTag = stopCursor.getString(0);
				
				String route = stopCursor.getString(6);
				String dirTag = stopCursor.getString(7);

				StopLocation stop = outputMapping.get(stopTag);
				if (stop == null)
				{
					float lat = stopCursor.getFloat(1);
					float lon = stopCursor.getFloat(2);
					String title = stopCursor.getString(3);

					int platformOrder = 0;
					String branch = null;
					if (stopCursor.isNull(4) == false)
					{
						platformOrder = stopCursor.getInt(4);
						branch = stopCursor.getString(5);
					}

					stop = transitSystem.createStop(lat, lon, stopTag, title, platformOrder, branch, route, dirTag);
					outputMapping.put(stopTag, stop);
				}
				else
				{
					stop.addRouteAndDirTag(route, dirTag);
				}
				
				stopCursor.moveToNext();
			}
		}
		finally
		{
			if (stopCursor != null)
			{
				stopCursor.close();
			}
			database.close();
		}
	}
}
