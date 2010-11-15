package boston.Bus.Map.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import boston.Bus.Map.data.Path;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Box;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
	private final static String subwaySpecificTable = "subway";
	
	
	private final static String directionsTable = "directions";
	private final static String stopsTable = "stops";
	private final static String routesTable = "routes";
	private final static String pathsTable = "paths";
	private final static String blobsTable = "blobs";
	private final static String oldFavoritesTable = "favs";
	private final static String newFavoritesTable = "favs2";
	private final static String routePoolTable = "routepool";
	
	
	private final static String routeKey = "route";
	private final static String stopIdKey = "stopId";
	private final static String newFavoritesTagKey = "tag";
	private final static String latitudeKey = "lat";
	private final static String longitudeKey = "lon";
	private final static String titleKey = "title";
	private final static String dirtagKey = "dirtag";
	private final static String nameKey = "name";
	private final static String pathIdKey = "pathid";
	private final static String blobKey = "blob";
	private final static String oldFavoritesIdKey = "idkey";
	private final static String newFavoritesRouteKey = "route";

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
	
	private final static int tagIndex = 1;
	private final static int nameIndex = 2;
	private final static int titleIndex = 3;
	
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
	
	
	public final static int CURRENT_DB_VERSION = VERBOSE_DB;
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, CURRENT_DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		/*db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING PRIMARY KEY, " + blobKey + " BLOB)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + routePoolTable + " (" + routeKey + " STRING PRIMARY KEY)");*/
		db.execSQL("CREATE TABLE IF NOT EXISTS " + newFavoritesTable + " (" + newFavoritesTagKey + " STRING PRIMARY KEY, " +
				newFavoritesRouteKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + dirTagKey + " STRING PRIMARY KEY, " + 
				dirNameKey + " STRING, " + dirTitleKey + " STRING, " + dirRouteKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseRoutes + " (" + routeKey + " STRING PRIMARY KEY, " + colorKey + 
				" INTEGER, " + oppositeColorKey + " INTEGER, " + pathsBlobKey + " BLOB)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
				latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + stopsRoutesMap + " (" + routeKey + " STRING, " + stopTagKey + " STRING)" +
				dirTagKey + " STRING)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + subwaySpecificTable + " (" + stopTagKey + " STRING PRIMARY KEY, " +
				platformOrderKey + " INTEGER, " + 
				branchKey + " STRING)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("BostonBusMap", "upgrading database from " + oldVersion + " to " + newVersion);
		HashSet<String> favorites = null;
		if (oldVersion == NEW_ROUTES_DB_VERSION)
		{
			favorites = readv7Favorites(db);
		}

		db.beginTransaction();
		db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
		db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
		db.execSQL("DROP TABLE IF EXISTS " + routesTable);
		db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
		db.execSQL("DROP TABLE IF EXISTS " + blobsTable);
		db.execSQL("DROP TABLE IF EXISTS " + verboseRoutes);
		db.execSQL("DROP TABLE IF EXISTS " + verboseStops);
		db.execSQL("DROP TABLE IF EXISTS " + stopsRoutesMap);

		db.execSQL("DROP TABLE IF EXISTS " + oldFavoritesTable);
		if (oldVersion < ROUTE_POOL_DB_VERSION)
		{
			db.execSQL("DROP TABLE IF EXISTS " + newFavoritesTable);
		}
		else
		{
			//we want to save favorites between upgrades
		}

		onCreate(db);

		if (favorites != null)
		{
			writeNewFavorites(db, favorites);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		
	}

	private void writeNewFavorites(SQLiteDatabase db, HashSet<String> favorites) {
		for (String favorite : favorites) {
			ContentValues values = new ContentValues();
			values.put(newFavoritesTagKey, favorite);
			
			db.insert(newFavoritesTable, null, values);
		}
	}
	
	private HashSet<String> readv7Favorites(SQLiteDatabase database)
	{
		HashSet<String> favorites = new HashSet<String>();
		Cursor cursor = null;
		try
		{
			cursor = database.query(newFavoritesTable, new String[]{newFavoritesTagKey}, null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String key = cursor.getString(0);
				
				favorites.add(key);
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
		
		return favorites;
	}

	private HashSet<String> readv6Favorites(SQLiteDatabase database)
	{
		HashSet<String> favorites = new HashSet<String>();

		Cursor cursor = null;
		try
		{
			cursor = database.query(oldFavoritesTable, new String[] {oldFavoritesIdKey}, null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				int key = cursor.getInt(0);

				//the old favorites used the public locationtype id, which it shouldn't have
				key = key & 0xffff;
				favorites.add(new Integer(key).toString());

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

		return favorites;
	}
	
	
	public synchronized void populateFavorites(HashMap<String, String> favorites)
	{
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;

		try
		{
			cursor = database.query(newFavoritesTable, new String[]{newFavoritesTagKey, newFavoritesRouteKey},
					null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String favoriteStopKey = cursor.getString(0);
				String favoriteRouteKey = cursor.getString(1);
				favorites.put(favoriteStopKey, favoriteRouteKey);

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
	
	public synchronized void saveMapping(HashMap<String, RouteConfig> mapping, boolean wipe, HashMap<String, StopLocation> sharedStops) throws IOException
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

			for (String route : mapping.keySet())
			{
				RouteConfig routeConfig = mapping.get(route);
				if (routeConfig != null)
				{
					saveMappingKernel(database, route, routeConfig, wipe, sharedStops);
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
	
	/**
	 * 
	 * @param database
	 * @param route
	 * @param routeConfig
	 * @param useInsert insert all rows, don't replace them. I assume this is faster since there's no lookup involved
	 * @throws IOException 
	 */
	private void saveMappingKernel(SQLiteDatabase database, String route, RouteConfig routeConfig,
			boolean useInsert, HashMap<String, StopLocation> sharedStops) throws IOException
	{
		Box serializedPath = new Box(null, CURRENT_DB_VERSION);
		
		routeConfig.serializePath(serializedPath);
		
		byte[] serializedPathBlob = serializedPath.getBlob();
		
		{
			ContentValues values = new ContentValues();
			values.put(routeKey, route);
			values.put(pathsBlobKey, serializedPathBlob);
			values.put(colorKey, routeConfig.getColor());
			values.put(oppositeColorKey, routeConfig.getOppositeColor());

			if (useInsert)
			{
				database.insert(verboseRoutes, null, values);
			}
			else
			{
				database.replace(verboseRoutes, null, values);
			}
		}
		
		//add all stops associated with the route, if they don't already exist
		

		if (useInsert == false)
		{
			//if useInsert is false, it means we're replacing things which already exist.
			//for this table we need to clear it away
			database.delete(stopsRoutesMap, routeKey + "=?", new String[]{route});
		}
		
		
		for (StopLocation stop : routeConfig.getStops())
		{
			/*"CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
			latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING, " +
			branchKey + " STRING, " + platformOrderKey + " SHORT)"*/
			String stopTag = stop.getStopTag();
			
			if (sharedStops.containsKey(stopTag) == false)
			{
			
				sharedStops.put(stopTag, stop);

				{
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopTag);
					values.put(latitudeKey, stop.getLatitudeAsDegrees());
					values.put(longitudeKey, stop.getLongitudeAsDegrees());
					values.put(stopTitleKey, stop.getTitle());

					database.insert(verboseStops, null, values);
				}

				if (stop instanceof SubwayStopLocation)
				{
					SubwayStopLocation subwayStop = (SubwayStopLocation)stop;
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopTag);
					values.put(platformOrderKey, subwayStop.getPlatformOrder());
					values.put(branchKey, subwayStop.getBranch());

					database.insert(subwaySpecificTable, null, values);
				}
			}
			
			{
				//show that there's a relationship between the stop and this route
				ContentValues values = new ContentValues();
				values.put(routeKey, route);
				values.put(stopTagKey, stopTag);
				values.put(dirtagKey, stop.getDirTagForRoute(route));
				database.insert(stopsRoutesMap, null, values);
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
			return freeSpace >= 1024 * 1024 * 2;
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

	public synchronized boolean saveFavorite(String stopTag, String routeTag, boolean isFavorite) {
		Log.v("BostonBusMap", "Saving favorite " + stopTag + " as " + isFavorite);
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			if (database.isOpen() == false)
			{
				Log.e("BostonBusMap", "SERIOUS ERROR: database didn't save data properly");
				return false;
			}
			database.beginTransaction();

			if (isFavorite)
			{
				ContentValues values = new ContentValues();
				values.put(newFavoritesTagKey, stopTag);
				values.put(newFavoritesRouteKey, routeTag);
				database.replace(newFavoritesTable, null, values);
			}
			else
			{
				database.delete(newFavoritesTable, newFavoritesTagKey + "=?", new String[] {stopTag});					
			}

			database.setTransactionSuccessful();
			database.endTransaction();

		}
		finally
		{
			database.close();
		}
		return true;
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

			
			routeCursor = database.query(verboseRoutes, new String[]{colorKey, oppositeColorKey, pathsBlobKey}, routeKey + "=?",
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
			Box pathsBlobBox = new Box(pathsBlob, CURRENT_DB_VERSION);

			RouteConfig routeConfig = new RouteConfig(routeToUpdate, color, oppositeColor, source, pathsBlobBox);

			
			
			
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			stopCursor = builder.query(database, new String[] {stopTagKey, latitudeKey, longitudeKey, 
					stopTitleKey, platformOrderKey, branchKey},
					routeKey + "=?", new String[]{routeToUpdate}, null, null, null);
			
			stopCursor.moveToFirst();
			while (stopCursor.isAfterLast() == false)
			{
				String stopTag = stopCursor.getString(0);
				float latitude = stopCursor.getFloat(1);
				float longitude = stopCursor.getFloat(2);
				String stopTitle = stopCursor.getString(3);
				String branch = stopCursor.getString(5);
				int platformOrder = 0;
				
				StopLocation stop;
				Drawable busStop = source.getBusStopDrawable();
				if (stopCursor.isNull(4) == false)
				{
					platformOrder = stopCursor.getInt(4);
					
					stop = new SubwayStopLocation(latitude, longitude, busStop,
							stopTag, stopTitle, platformOrder, branch);
				}
				else
				{
					stop = new StopLocation(latitude, longitude, busStop, stopTag, stopTitle);
				}
				
				routeConfig.addStop(stopTag, stop);
				stopCursor.moveToNext();
			}
			
			
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

	public synchronized void saveFavorites(HashMap<String, String> favoriteStops) {
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();

			database.delete(newFavoritesTable, null, null);

			for (String stopTag : favoriteStops.keySet())
			{
				String routeTag = favoriteStops.get(stopTag);

				ContentValues values = new ContentValues();
				values.put(newFavoritesTagKey, stopTag);
				values.put(newFavoritesRouteKey, routeTag);
				database.insert(newFavoritesTable, null, values);
			}

			database.setTransactionSuccessful();
			database.endTransaction();

		}
		finally
		{
			database.close();
		}
	}

	public Cursor getCursorForRoutes() {
		throw new RuntimeException("Not Implemented");
	}

	public Cursor getCursorForRoute(String routeName) {
		throw new RuntimeException("Not Implemented");
	}

	public Cursor getCursorForDirections() {
		throw new RuntimeException("Not Implemented");
	}

	public Cursor getCursorForDirection(String dirTag) {
		throw new RuntimeException("Not Implemented");
	}
}
