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
import boston.Bus.Map.util.Box;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
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
	
	public final static int CURRENT_DB_VERSION = ROUTE_POOL_DB_VERSION;
	
	private final Drawable busStop;

	public DatabaseHelper(Context context, Drawable busStop) {
		super(context, dbName, null, CURRENT_DB_VERSION);
		
		this.busStop = busStop;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING, " + blobKey + " BLOB)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + newFavoritesTable + " (" + newFavoritesTagKey + " STRING PRIMARY KEY, " +
				newFavoritesRouteKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + routePoolTable + " (" + routeKey + " STRING)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < NEW_ROUTES_DB_VERSION)
		{
			db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
			db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
			db.execSQL("DROP TABLE IF EXISTS " + routesTable);
			db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
			db.execSQL("DROP TABLE IF EXISTS " + blobsTable);

			HashSet<String> favorites = null;
			if (oldVersion == ADDED_FAVORITE_DB_VERSION)
			{
				favorites = readv6Favorites(db);
			}
			else if (oldVersion == NEW_ROUTES_DB_VERSION)
			{
				favorites = readv7Favorites(db);
			}

			db.execSQL("DROP TABLE IF EXISTS " + oldFavoritesTable);
			db.execSQL("DROP TABLE IF EXISTS " + newFavoritesTable);

			onCreate(db);

			if (favorites != null)
			{
				writeNewFavorites(db, favorites);
			}
		}
		else
		{
			onCreate(db);
		}
		
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
			database.close();
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
	
	
	private void reserialize(SQLiteDatabase database, int oldVersion, int newVersion) {
		HashMap<String, byte[]> routesToWrite = new HashMap<String, byte[]>();

		Cursor cursor = null;
		try
		{
			HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
			
			cursor = database.query(blobsTable, new String[] {routeKey, blobKey}, null, null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false)
			{
				String route = cursor.getString(0);
				byte[] blob = cursor.getBlob(1);
				
				Log.v("BostonBusMap", "converting over route " + route);
				RouteConfig routeConfig = new RouteConfig(new Box(blob, oldVersion, sharedStops), busStop);
				Box outputBox = new Box(null, newVersion, sharedStops);
				routeConfig.serialize(outputBox);
				
				routesToWrite.put(route, outputBox.getBlob());
				cursor.moveToNext();
			}
			cursor.close();
		}
		catch (IOException e)
		{
			Log.e("BostonBusMap", "Exception during serialization: " + e.getMessage());
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		
		//write out everything we just read in
		database.delete(blobsTable, null, null);

		for (String route : routesToWrite.keySet())
		{
			byte[] blob = routesToWrite.get(route);

			ContentValues values = new ContentValues();
			values.put(routeKey, route);
			values.put(blobKey, blob);

			database.insert(blobsTable, null, values);
		}
	}

	public void populateFavorites(HashMap<String, String> favorites)
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
	
	public void saveMapping(HashMap<String, RouteConfig> mapping, boolean wipe) throws IOException
	{
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
			try
			{
				database.beginTransaction();
			
				if (wipe)
				{
					//database.delete(stopsTable, null, null);
					//database.delete(directionsTable, null, null);
					//database.delete(pathsTable, null, null);
					database.delete(blobsTable, null, null);
				}
				
				HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
				
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
		Box box = new Box(null, CURRENT_DB_VERSION, sharedStops);
		
		routeConfig.serialize(box);
		
		byte[] blob = box.getBlob();
		
		ContentValues values = new ContentValues();
		values.put(routeKey, route);
		values.put(blobKey, blob);
			
			
		if (useInsert)
		{
			database.insert(blobsTable, null, values);
		}
		else
		{
			database.replace(blobsTable, null, values);
		}

	}

	public boolean checkFreeSpace() {
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

	public void saveFavorite(String stopTag, String routeTag, boolean isFavorite) {
		Log.v("BostonBusMap", "Saving favorite " + stopTag + " as " + isFavorite);
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
			try
			{
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
		}
	}

	public RouteConfig getRoute(String routeToUpdate, HashMap<String, StopLocation> sharedStops) throws IOException {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			cursor = database.query(blobsTable, new String[]{blobKey}, routeKey + "=?",
					new String[]{routeToUpdate}, null, null, null);
			if (cursor.getCount() == 0)
			{
				return null;
			}
			else
			{
				cursor.moveToFirst();
				byte[] data = cursor.getBlob(0);
				Box box = new Box(data, CURRENT_DB_VERSION, sharedStops);
				RouteConfig routeConfig = new RouteConfig(box, busStop);
				return routeConfig;
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
}
