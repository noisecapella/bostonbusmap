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

import boston.Bus.Map.data.Path;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.LocationGroup;
import boston.Bus.Map.data.MultipleStopLocations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopLocationGroup;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
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
	public final static int VERBOSE_DBV2_2 = 26;
	public final static int NO_DB = 100;
	public final static int CURRENT_DB_VERSION = NO_DB;
	
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
						
		/*db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + dirTagKey + " STRING PRIMARY KEY, " + 
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
		*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("BostonBusMap", "upgrading database from " + oldVersion + " to " + newVersion);
		HashSet<String> favorites = null;

		db.beginTransaction();
		/*if (oldVersion > STOP_LOCATIONS_STORE_ROUTE_STRINGS && oldVersion < VERBOSE_DB)
		{
			favorites = readOldFavorites(db);
		}
		else if (oldVersion >= VERBOSE_DB)
		{
			favorites = new HashSet<String>();
			populateFavorites(favorites, false, db);
		}*/
		
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

		//if it's verboseFavorites, we want to save it since it's user specified data

		onCreate(db);

		/*if (favorites != null)
		{
			writeVerboseFavorites(db, favorites);
		}*/

		db.setTransactionSuccessful();
		db.endTransaction();
		
	}

	
	public synchronized void populateFavorites(HashSet<String> favorites)
	{
		SQLiteDatabase database = null;
		try
		{
			database = getReadableDatabase();
			populateFavorites(favorites, database);
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
	public synchronized void populateFavorites(HashSet<String> favorites, 
			SQLiteDatabase database)
	{
		Cursor cursor = null;

		try
		{
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(verboseFavorites);

			cursor = builder.query(database, new String[]{stopTagKey}, 
					null, null, null, null, null);
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
	
	private void storeFavorite(LocationGroup group)
	{
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();
			if (group instanceof MultipleStopLocations) {
				MultipleStopLocations stopLocations = (MultipleStopLocations)group;
				for (StopLocation stopLocation : stopLocations.getStops())
				{
					ContentValues values = new ContentValues();
					values.put(stopTagKey, stopLocation.getStopTag());
					
					database.replace(verboseFavorites, null, values);
				}
				
			}
			else if (group instanceof StopLocation)
			{
				StopLocation stop = (StopLocation)group;
				ContentValues values = new ContentValues();
				values.put(stopTagKey, stop.getStopTag());
				
				database.replace(verboseFavorites, null, values);
			}
			else
			{
				throw new RuntimeException("BusLocation appeared in storeFavorite");
			}
			database.setTransactionSuccessful();
			database.endTransaction();
		}
		finally
		{
			database.close();
		}
	}
	
	
	public synchronized void saveFavorite(LocationGroup group, boolean isFavorite) {
		if (isFavorite)
		{
			storeFavorite(group);
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
				String whereClause = verboseFavorites + "." + stopTagKey + 
						" = ?";
				if (group instanceof MultipleStopLocations) {
					MultipleStopLocations stopLocations = (MultipleStopLocations)group;
					for (StopLocation stop : stopLocations.getStops()) {
						String stopTag = stop.getStopTag();
						database.delete(verboseFavorites, whereClause, new String[]{stopTag});
					}
				}
				else if (group instanceof StopLocation)
				{
					StopLocation stop = (StopLocation)group;
					String stopTag = stop.getStopTag();
					database.delete(verboseFavorites, whereClause, new String[]{stopTag});
				}
				else
				{
					throw new RuntimeException("BusLocation appeared in DatabaseHelper.saveFavorite");
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



	public synchronized void saveFavorites(HashSet<StopLocationGroup> favoriteStops) {
		SQLiteDatabase database = getWritableDatabase();
		try
		{
			database.beginTransaction();

			database.delete(verboseFavorites, null, null);

			HashSet<String> stopTags = new HashSet<String>();
			for (StopLocationGroup locationGroup : favoriteStops)
			{
				StopLocationGroup stopLocationGroup = (StopLocationGroup)locationGroup;
				for (StopLocation stop : stopLocationGroup.getStops()) {
					String stopTag = stop.getStopTag();
					stopTags.add(stopTag);
				}
			}
			
			for (String stopTag : stopTags) {
				ContentValues values = new ContentValues();
				values.put(stopTagKey, stopTag);
				database.insert(verboseFavorites, null, values);
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
