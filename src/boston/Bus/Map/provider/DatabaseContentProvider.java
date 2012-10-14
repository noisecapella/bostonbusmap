package boston.Bus.Map.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.database.Schema.Stopmapping;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.IBox;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseContentProvider extends ContentProvider {
	private static final UriMatcher uriMatcher;
	public static final String AUTHORITY = "com.bostonbusmap.databaseprovider";

	private static final String FAVORITES_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.favorite";
	public static final Uri FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/favorites");
	private static final int FAVORITES = 1;

	private static final String STOPS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop";
	public static final Uri STOPS_URI = Uri.parse("content://" + AUTHORITY + "/stops");
	private static final int STOPS = 2;

	private static final String ROUTES_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.route";
	public static final Uri ROUTES_URI = Uri.parse("content://" + AUTHORITY + "/routes");
	private static final int ROUTES = 3;

	private static final String STOPS_ROUTES_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_route";
	public static final Uri STOPS_ROUTES_URI = Uri.parse("content://" + AUTHORITY + "/stops_routes");
	private static final int STOPS_ROUTES = 4;

	private static final String STOPS_STOPS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_stop";
	public static final Uri STOPS_STOPS_URI = Uri.parse("content://" + AUTHORITY + "/stops_stops");
	private static final int STOPS_STOPS = 5;

	private static final String STOPS_LOOKUP_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_lookup";
	public static final Uri STOPS_LOOKUP_URI = Uri.parse("content://" + AUTHORITY + "/stops_lookup");
	private static final int STOPS_LOOKUP = 6;

	private static final String DIRECTIONS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.direction";
	public static final Uri DIRECTIONS_URI = Uri.parse("content://" + AUTHORITY + "/directions");
	private static final int DIRECTIONS = 7;

	private static final String DIRECTIONS_STOPS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.direction_stop";
	public static final Uri DIRECTIONS_STOPS_URI = Uri.parse("content://" + AUTHORITY + "/directions_stops");
	private static final int DIRECTIONS_STOPS = 8;

	private static final String STOPS_LOOKUP_2_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_lookup_2";
	public static final Uri STOPS_LOOKUP_2_URI = Uri.parse("content://" + AUTHORITY + "/stops_lookup_2");
	private static final int STOPS_LOOKUP_2 = 9;

	private static final String STOPS_LOOKUP_3_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_lookup_3";
	public static final Uri STOPS_LOOKUP_3_URI = Uri.parse("content://" + AUTHORITY + "/stops_lookup_3");
	private static final int STOPS_LOOKUP_3 = 10;

	private static final String STOPS_WITH_DISTANCE_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_with_distance";
	public static final Uri STOPS_WITH_DISTANCE_URI = Uri.parse("content://" + AUTHORITY + "/stops_with_distance");
	private static final int STOPS_WITH_DISTANCE = 11;

	private static final String FAVORITES_WITH_SAME_LOCATION_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.favorite_with_same_location";
	public static final Uri FAVORITES_WITH_SAME_LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/favorites_with_same_location");
	private static final int FAVORITES_WITH_SAME_LOCATION = 12;

	private static final String SUBWAY_STOPS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.subway_stop";
	public static final Uri SUBWAY_STOPS_URI = Uri.parse("content://" + AUTHORITY + "/subway_stops");
	private static final int SUBWAY_STOPS = 13;

	private final static String stopsRoutesMapIndexTag = "IDX_stopmapping";
	private final static String stopsRoutesMapIndexRoute = "IDX_routemapping";
	private final static String directionsStopsMapIndexStop = "IDX_directionsstop_stop";
	private final static String directionsStopsMapIndexDirTag = "IDX_directionsstop_dirtag";

	private static final String distanceKey = "distance";

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
	public final static int VERBOSE_DBV2_3 = 27;
	public final static int VERBOSE_DBV2_4 = 28;

	public final static int WITH_STOPS_FOR_DIR = 36;

	public final static int CURRENT_DB_VERSION = WITH_STOPS_FOR_DIR;

	public static final int ALWAYS_POPULATE = 3;
	public static final int POPULATE_IF_UPGRADE = 2;
	public static final int MAYBE = 1;

	public static final int INT_TRUE = 1;
	public static final int INT_FALSE = 0;

	/**
	 * Handles the database which stores route information
	 * 
	 * @author schneg
	 *
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper
	{
		private static DatabaseHelper instance;

		/**
		 * Don't call this, use getInstance instead
		 * @param context
		 */
		private DatabaseHelper(Context context) {
			super(context, Schema.dbName, null, CURRENT_DB_VERSION);

		}
		
		/**
		 * Note that synchronized refers to the class variable, not 'this'
		 * @param context
		 * @return
		 */
		public static DatabaseHelper getInstance(Context context) {
			synchronized (DatabaseHelper.class) {
				if (instance == null) {
					instance = new DatabaseHelper(context);
				}
				return instance; 
			}
		}
		

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING PRIMARY KEY, " + blobKey + " BLOB)");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + routePoolTable + " (" + routeKey + " STRING PRIMARY KEY)");*/
			db.execSQL(Schema.Favorites.createSql);

			db.execSQL(Schema.Directions.createSql);

			db.execSQL(Schema.Routes.createSql);

			db.execSQL(Schema.Stops.createSql);

			db.execSQL(Schema.Stopmapping.createSql);

			db.execSQL(Schema.Subway.createSql);

			db.execSQL(Schema.DirectionsStops.createSql);

			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexRoute + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.routeColumn + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexTag + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.tagColumn + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexStop + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.tagColumn + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexDirTag + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.dirTagColumn + ")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.v("BostonBusMap", "upgrading database from " + oldVersion + " to " + newVersion);
			HashSet<String> favorites = null;

			try
			{
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

				if (oldVersion < CURRENT_DB_VERSION)
				{
					db.execSQL("DROP TABLE IF EXISTS " + Schema.Directions.table);
					db.execSQL("DROP TABLE IF EXISTS " + Schema.DirectionsStops.table);
					db.execSQL("DROP TABLE IF EXISTS " + Schema.Stops.table);
					db.execSQL("DROP TABLE IF EXISTS " + Schema.Routes.table);
					db.execSQL("DROP TABLE IF EXISTS " + Schema.Stopmapping.table);
					db.execSQL("DROP TABLE IF EXISTS " + Schema.Subway.table);
				}

				//if it's verboseFavorites, we want to save it since it's user specified data

				onCreate(db);

				/*if (favorites != null)
			{
				writeVerboseFavorites(db, favorites);
			}*/

				db.setTransactionSuccessful();
			}
			finally
			{
				db.endTransaction();
			}
		}
		
		@Override
		public SQLiteDatabase getReadableDatabase() {
			return super.getReadableDatabase();
		}
		
		@Override
		public SQLiteDatabase getWritableDatabase() {
			return super.getWritableDatabase();
		}
	}


	public static class DatabaseAgent {
		/**
		 * Fill the given HashSet with all stop tags that are favorites
		 * @param favorites
		 */
		public static void populateFavorites(ContentResolver contentResolver, 
				CopyOnWriteArraySet<String> favorites, boolean lookForOtherStopsAtSameLocation)
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
					builder.setDistinct(true);

					cursor = contentResolver.query(FAVORITES_WITH_SAME_LOCATION_URI, new String[]{"s2." + Schema.Favorites.tagColumn}, null, null, null);
				}
				else
				{
					cursor = contentResolver.query(FAVORITES_URI, new String[]{Schema.Favorites.tagColumn},
							null, null, null);
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
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		public static void saveMapping(Context context, 
				Map<String, RouteConfig> mapping,
				HashSet<String> sharedStops, UpdateAsyncTask task)
						throws IOException, RemoteException, OperationApplicationException
		{
			int total = mapping.keySet().size();
			task.publish(new ProgressMessage(ProgressMessage.SET_MAX, total));

			int count = 0;
			for (String route : mapping.keySet())
			{
				RouteConfig routeConfig = mapping.get(route);
				if (routeConfig != null)
				{
					String routeTitle = routeConfig.getRouteTitle();
					saveMappingKernel(context, route, routeTitle, routeConfig, sharedStops);
				}

				count++;
				task.publish(count);
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
		private static void saveMappingKernel(Context context, 
				String route, String routeTitle, RouteConfig routeConfig,
				HashSet<String> sharedStops) throws IOException
		{
			DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			InsertHelper routeInsertHelper = new InsertHelper(database, Schema.Routes.table);
			try
			{
				Schema.Routes.executeInsertHelper(routeInsertHelper, route, routeConfig.getColor(), routeConfig.getOppositeColor(), pathsToBlob(routeConfig.getPaths()), routeTitle);
			}
			finally
			{
				routeInsertHelper.close();
			}
			//add all stops associated with the route, if they don't already exist

			InsertHelper stopsToInsert = new InsertHelper(database, Schema.Stops.table);
			InsertHelper subwayStopsToInsert = new InsertHelper(database, Schema.Subway.table);
			InsertHelper stopRoutesToInsert = new InsertHelper(database, Schema.Stopmapping.table);
			try
			{
				for (StopLocation stop : routeConfig.getStops())
				{
					/*"CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
				latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING, " +
				branchKey + " STRING, " + platformOrderKey + " SHORT)"*/
					String stopTag = stop.getStopTag();

					if (sharedStops.contains(stopTag) == false)
					{
						sharedStops.add(stopTag);

						Schema.Stops.executeInsertHelper(stopsToInsert, stopTag, stop.getLatitudeAsDegrees(), stop.getLongitudeAsDegrees(), stop.getTitle());

						if (stop instanceof SubwayStopLocation)
						{
							SubwayStopLocation subwayStop = (SubwayStopLocation)stop;
							Schema.Subway.executeInsertHelper(subwayStopsToInsert, stopTag, subwayStop.getPlatformOrder(), subwayStop.getBranch());
						}
					}

					//show that there's a relationship between the stop and this route
					Schema.Stopmapping.executeInsertHelper(stopRoutesToInsert, route, stopTag, "");
				}
			}
			finally
			{
				stopsToInsert.close();
				subwayStopsToInsert.close();
				stopRoutesToInsert.close();
			}
		}

		public static ImmutableList<String> getAllStopTagsAtLocation(ContentResolver resolver, 
				String stopTag)
		{
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(STOPS_STOPS_URI, 
						new String[]{"s2." + Schema.Stops.tagColumn}, "s1." + Schema.Stops.tagColumn + " = ? AND s1." + Schema.Stops.latColumn + " = s2." + Schema.Stops.latColumn +
						" AND s1." + Schema.Stops.lonColumn + " = s2." + Schema.Stops.lonColumn + "", new String[]{stopTag}, null);

				ImmutableList.Builder<String> ret = ImmutableList.builder();
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					String tag = cursor.getString(0);
					ret.add(tag);

					cursor.moveToNext();
				}

				return ret.build();
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
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

			resolver.bulkInsert(FAVORITES_URI, allValues.toArray(new ContentValues[0]));
		}


		public static void saveFavorite(ContentResolver resolver, 
				String stopTag, Collection<String> stopTags, boolean isFavorite) throws RemoteException {
			if (isFavorite)
			{
				storeFavorite(resolver, stopTags);
			}
			else
			{
				//delete all stops at location
				resolver.delete(FAVORITES_URI, Schema.Favorites.table + "." + Schema.Favorites.tagColumn + 
						" IN (SELECT s2." + Schema.Stops.tagColumn + " FROM " + Schema.Stops.table + " as s1, " + Schema.Stops.table + " as s2 WHERE " +
						"s1." + Schema.Stops.latColumn + " = s2." + Schema.Stops.latColumn + " AND s1." + Schema.Stops.lonColumn +
						" = s2." + Schema.Stops.lonColumn + " AND s1." + Schema.Stops.tagColumn + " = ?)", new String[]{stopTag});
			}
		}

		public static RouteConfig getRoute(ContentResolver resolver, String routeToUpdate, 
				ConcurrentMap<String, StopLocation> sharedStops,
				TransitSystem transitSystem) throws IOException {

			//get the route-specific information, like the path outline and the color
			RouteConfig.Builder routeConfigBuilder;
			{
				Cursor cursor = null;
				try
				{
					cursor = resolver.query(ROUTES_URI, new String[]{Schema.Routes.colorColumn, Schema.Routes.oppositecolorColumn, Schema.Routes.pathblobColumn, Schema.Routes.routetitleColumn}, Schema.Routes.routeColumn + "=?",
							new String[]{routeToUpdate}, null);
					if (cursor.getCount() == 0)
					{
						return null;
					}

					cursor.moveToFirst();

					TransitSource source = transitSystem.getTransitSource(routeToUpdate);

					int color = cursor.getInt(0);
					int oppositeColor = cursor.getInt(1);
					byte[] pathsBlob = cursor.getBlob(2);
					String routeTitle = cursor.getString(3);
					Box pathsBlobBox = new Box(pathsBlob, CURRENT_DB_VERSION);

					routeConfigBuilder = new RouteConfig.Builder(routeToUpdate, routeTitle, color, oppositeColor, source, pathsBlobBox);
				}
				finally
				{
					if (cursor != null) {
						cursor.close();
					}
				}
			}

			{
				//get all stops, joining in the subway stops, making sure that the stop references the route we're on

				/* select stops.tag, lat, lon, title, platformorder, branch, stopmapping1.dirTag, stopmapping2.route 
				 * from stops inner join stopmapping as stopmapping1 on (stops.tag = stopmapping1.tag) 
				 * inner join stopmapping as stopmapping2 on (stops.tag = stopmapping2.tag)
				 * left outer join subway on (stops.tag = subway.tag) 
				 * where stopmapping1.route=71;*/ 
				String[] projectionIn = new String[] {Schema.Stops.table + "." + Schema.Stops.tagColumn, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
						Schema.Stops.titleColumn, Schema.Subway.platformorderColumn, Schema.Subway.branchColumn, "sm2." + Schema.Stopmapping.dirTagColumn, "sm2." + Schema.Stopmapping.routeColumn};
				String select = "sm1." + Schema.Stopmapping.routeColumn + "=?";
				String[] selectArray = new String[]{routeToUpdate};

				Cursor cursor = null;
				try
				{
					cursor = resolver.query(STOPS_LOOKUP_URI, projectionIn, select, selectArray, null);

					cursor.moveToFirst();
					while (cursor.isAfterLast() == false)
					{
						String stopTag = cursor.getString(0);
						String route = cursor.getString(7);

						//we need to ensure this stop is in the sharedstops and the route
						StopLocation stop = sharedStops.get(stopTag);
						if (stop != null)
						{
							//make sure it exists in the route too
							if (routeConfigBuilder.containsStop(stopTag) == false)
							{
								routeConfigBuilder.addStop(stopTag, stop);
							}
							stop.addRoute(route);
						}
						else
						{
							stop = routeConfigBuilder.getStop(stopTag);

							if (stop == null)
							{
								float latitude = cursor.getFloat(1);
								float longitude = cursor.getFloat(2);
								String stopTitle = cursor.getString(3);
								String branch = cursor.getString(5);

								int platformOrder = cursor.getInt(4);

								stop = transitSystem.createStop(latitude, longitude, stopTag, stopTitle, platformOrder, branch, route);

								routeConfigBuilder.addStop(stopTag, stop);
							}

							sharedStops.put(stopTag, stop);
						}
						cursor.moveToNext();
					}
				}
				finally
				{
					if (cursor != null) {
						cursor.close();
					}
				}
			}

			return routeConfigBuilder.build();
		}

		public static ImmutableList<String> routeInfoNeedsUpdating(ContentResolver resolver, 
				RouteTitles supportedRoutes) {
			Set<String> routesInDB = Sets.newHashSet();
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(ROUTES_URI, new String[]{Schema.Routes.routeColumn}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					routesInDB.add(cursor.getString(0));

					cursor.moveToNext();
				}

				ImmutableList.Builder<String> routesThatNeedUpdating = ImmutableList.builder();

				for (String route : supportedRoutes.routeTags())
				{
					if (routesInDB.contains(route) == false)
					{
						routesThatNeedUpdating.add(route);
					}
				}

				return routesThatNeedUpdating.build();
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		/**
		 * Populate directions from the database
		 * 
		 * NOTE: these data structures are assumed to be synchronized
		 * @param indexes
		 * @param names
		 * @param titles
		 */
		public static void refreshDirections(ContentResolver resolver, ConcurrentHashMap<String, Direction> directions) {
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(DIRECTIONS_URI, new String[]{Schema.Directions.dirTagColumn, Schema.Directions.dirNameKeyColumn, Schema.Directions.dirTitleKeyColumn, Schema.Directions.dirRouteKeyColumn, Schema.Directions.useAsUIColumn},
						null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					String dirTag = cursor.getString(0);
					String dirName = cursor.getString(1);
					String dirTitle = cursor.getString(2);
					String dirRoute = cursor.getString(3);
					boolean dirUseAsUI = cursor.getInt(4) == INT_TRUE;

					Direction direction = new Direction(dirName, dirTitle, dirRoute, dirUseAsUI);
					directions.put(dirTag, direction);

					cursor.moveToNext();
				}
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		public static void writeDirections(Context context, ConcurrentHashMap<String, Direction> directions) throws RemoteException, OperationApplicationException {
			DatabaseHelper helper = DatabaseHelper.getInstance(context);

			SQLiteDatabase database = helper.getWritableDatabase();
			InsertHelper directionHelper = new InsertHelper(database, Schema.Directions.table);
			InsertHelper directionStopHelper = new InsertHelper(database, Schema.DirectionsStops.table);
			try
			{
				database.beginTransaction();
				for (String dirTag : directions.keySet())
				{
					Direction direction = directions.get(dirTag);
					String name = direction.getName();
					String title = direction.getTitle();
					String route = direction.getRoute();
					boolean useAsUI = direction.isUseForUI();

					Schema.Directions.executeInsertHelper(directionHelper, dirTag, name, title, route, Schema.toInteger(useAsUI));

					for (String stopTag : direction.getStopTags()) {
						Schema.DirectionsStops.executeInsertHelper(directionStopHelper, dirTag, stopTag);
					}

				}
				database.setTransactionSuccessful();
			}
			finally
			{
				if (directionHelper != null) {
					directionHelper.close();
				}
				if (directionStopHelper != null) {
					directionStopHelper.close();
				}
				database.endTransaction();
			}
		}


		public static void saveFavorites(ContentResolver resolver, Set<String> favoriteStops, Map<String, StopLocation> sharedStops) throws RemoteException, OperationApplicationException {
			resolver.delete(FAVORITES_URI, null, null);
			
			List<ContentValues> favoritesToWrite = Lists.newArrayList();
			for (String stopTag : favoriteStops)
			{
				StopLocation stopLocation = sharedStops.get(stopTag);

				if (stopLocation != null)
				{
					ContentValues values = new ContentValues();
					values.put(Schema.Favorites.tagColumn, stopLocation.getStopTag());
					resolver.insert(FAVORITES_URI, values);
				}
			}
		}

		public static Cursor getCursorForSearch(ContentResolver resolver, String search, int mode) {
			String[] columns = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_TEXT_2};
			MatrixCursor ret = new MatrixCursor(columns);

			addSearchRoutes(resolver, search, ret);
			addSearchStops(resolver, search, ret);


			return ret;
		}

		private static void addSearchRoutes(ContentResolver resolver, String search, MatrixCursor ret)
		{
			if (search == null)
			{
				return;
			}

			Cursor cursor = null;
			try
			{
				cursor = resolver.query(ROUTES_URI, new String[]{Schema.Routes.routetitleColumn, Schema.Routes.routeColumn}, Schema.Routes.routetitleColumn + " LIKE ?",
						new String[]{"%" + search + "%"}, Schema.Routes.routetitleColumn);
				if (cursor.moveToFirst() == false)
				{
					return;
				}

				while (!cursor.isAfterLast())
				{
					String routeTitle = cursor.getString(0);
					String routeKey = cursor.getString(1);

					ret.addRow(new Object[]{ret.getCount(), routeTitle, "route " + routeKey, "Route"});

					cursor.moveToNext();
				}
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		private static void addSearchStops(ContentResolver resolver, String search, MatrixCursor ret)
		{
			if (search == null)
			{
				return;
			}

			String thisStopTitleKey = Schema.Stops.table + "." + Schema.Stops.titleColumn;
			String[] projectionIn = new String[] {thisStopTitleKey, Schema.Stops.table + "." + Schema.Stops.tagColumn, "r1." + Schema.Routes.routetitleColumn};
			String select = thisStopTitleKey + " LIKE ?";
			String[] selectArray = new String[]{"%" + search + "%"};

			Cursor cursor = null;
			try
			{
				cursor = resolver.query(STOPS_LOOKUP_2_URI, 
						projectionIn, select, selectArray, null);

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
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		
		private static Uri appendUris(Uri uri, int... x) {
			for (int id : x) {
				uri = ContentUris.withAppendedId(uri, id);
			}
			return uri;
		}
		
		public static Collection<StopLocation> getClosestStops(ContentResolver resolver, 
				double currentLat, double currentLon, TransitSystem transitSystem, 
				ConcurrentMap<String, StopLocation> sharedStops, int limit)
		{
			// what we should scale longitude by for 1 unit longitude to roughly equal 1 unit latitude

			String[] projectionIn = new String[] {Schema.Stops.tagColumn, distanceKey};
			int currentLatAsInt = (int)(currentLat * Constants.E6);
			int currentLonAsInt = (int)(currentLon * Constants.E6);
			Uri uri = appendUris(STOPS_WITH_DISTANCE_URI, currentLatAsInt, currentLonAsInt, limit);

			Cursor cursor = null;
			try
			{
				cursor = resolver.query(uri, projectionIn, null, null, distanceKey);
				if (cursor.moveToFirst() == false)
				{
					return Collections.emptyList();
				}

				ImmutableList.Builder<String> stopTagsBuilder = ImmutableList.builder();
				while (!cursor.isAfterLast())
				{
					String id = cursor.getString(0);
					stopTagsBuilder.add(id);

					cursor.moveToNext();
				}
				ImmutableList<String> stopTags = stopTagsBuilder.build();
				getStops(resolver, stopTags, transitSystem, sharedStops);

				ImmutableList.Builder<StopLocation> builder = ImmutableList.builder();
				for (String stopTag : stopTags)
				{
					builder.add(sharedStops.get(stopTag));
				}

				return builder.build();
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		public static StopLocation getStopByTagOrTitle(ContentResolver resolver, 
				String tagQuery, String titleQuery, TransitSystem transitSystem)
		{
			//TODO: we should have a factory somewhere to abstract details away regarding subway vs bus

			//get stop with name stopTag, joining with the subway table
			String[] projectionIn = new String[] {Schema.Stops.table + "." + Schema.Stops.tagColumn, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
					Schema.Stops.titleColumn, Schema.Subway.platformorderColumn, Schema.Subway.branchColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.routeColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.dirTagColumn};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;

			select = new StringBuilder(Schema.Stops.table + "." + Schema.Stops.tagColumn + "=? OR " + Schema.Stops.table + "." + Schema.Stops.titleColumn + "=?");
			selectArray = new String[]{tagQuery, titleQuery};

			Cursor stopCursor = null;
			try
			{
				stopCursor = resolver.query(STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

				stopCursor.moveToFirst();

				if (stopCursor.isAfterLast() == false)
				{
					String stopTag = stopCursor.getString(0);

					String route = stopCursor.getString(6);

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

					StopLocation stop = transitSystem.createStop(lat, lon, stopTag, title, platformOrder, branch, route);
					return stop;
				}
				else
				{
					return null;
				}
			}
			finally
			{
				if (stopCursor != null) {
					stopCursor.close();
				}
			}
		}

		/**
		 * Read stops from the database and return a mapping of the stop tag to the stop object
		 * @param stopTag
		 * @param transitSystem
		 * @return
		 */
		public static void getStops(ContentResolver resolver, ImmutableList<String> stopTags, 
				TransitSystem transitSystem, ConcurrentMap<String, StopLocation> outputMapping) {
			if (stopTags == null || stopTags.size() == 0)
			{
				return;
			}

			//TODO: we should have a factory somewhere to abstract details away regarding subway vs bus

			//get stop with name stopTag, joining with the subway table
			String[] projectionIn = new String[] {Schema.Stops.table + "." + Schema.Stops.tagColumn, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
					Schema.Stops.titleColumn, Schema.Subway.platformorderColumn, Schema.Subway.branchColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.routeColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.dirTagColumn};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;
			if (stopTags.size() == 1)
			{
				String stopTag = stopTags.get(0);

				select = new StringBuilder(Schema.Stops.table + "." + Schema.Stops.tagColumn + "=?");
				selectArray = new String[]{stopTag};

				//Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, verboseStops + "." + stopTagKey + "=\"" + stopTagKey + "\"",
				//		null, null, null, null));
			}
			else
			{
				select = new StringBuilder(Schema.Stops.table + "." + Schema.Stops.tagColumn + " IN (");

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

				//Log.v("BostonBusMap", select.toString());
			}

			Cursor stopCursor = null;
			try
			{
				stopCursor = resolver.query(STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

				stopCursor.moveToFirst();

				//iterate through the stops in the database and create new ones if necessary
				//stops will be repeated if they are on multiple routes. If so, just skip to the bottom and add the route and dirTag
				while (stopCursor.isAfterLast() == false)
				{
					String stopTag = stopCursor.getString(0);

					String route = stopCursor.getString(6);

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

						stop = transitSystem.createStop(lat, lon, stopTag, title, platformOrder, branch, route);
						outputMapping.putIfAbsent(stopTag, stop);
					}
					else
					{
						stop.addRoute(route);
					}

					stopCursor.moveToNext();
				}
			}
			finally
			{
				if (stopCursor != null) {
					stopCursor.close();
				}
			}
		}

		public static ArrayList<StopLocation> getStopsByDirtag(ContentResolver resolver, 
				String dirTag, TransitSystem transitSystem) {
			ArrayList<StopLocation> ret = new ArrayList<StopLocation>();
			String[] projectionIn = new String[] {Schema.Stops.table + "." + Schema.Stops.tagColumn, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
					Schema.Stops.titleColumn, Schema.Subway.platformorderColumn, Schema.Subway.branchColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.routeColumn, Schema.Stopmapping.table + "." + Schema.Stopmapping.dirTagColumn};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;

			select = new StringBuilder(Schema.Stopmapping.table + "." + Schema.Stopmapping.dirTagColumn + "=?");
			selectArray = new String[]{dirTag};

			Cursor stopCursor = null;
			try
			{
				stopCursor = resolver.query(STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

				stopCursor.moveToFirst();
				while (stopCursor.isAfterLast() == false)
				{
					String stopTag = stopCursor.getString(0);

					String route = stopCursor.getString(6);

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

					StopLocation stop = transitSystem.createStop(lat, lon, stopTag, title, platformOrder, branch, route);
					ret.add(stop);
					stopCursor.moveToNext();
				}
				return ret;
			}
			finally
			{
				if (stopCursor != null) {
					stopCursor.close();
				}
			}
		}

		public static ArrayList<String> getDirectionTagsForStop(ContentResolver resolver, 
				String stopTag) {
			ArrayList<String> ret = new ArrayList<String>();
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(DIRECTIONS_STOPS_URI, new String[] {Schema.DirectionsStops.dirTagColumn},
						Schema.DirectionsStops.tagColumn + " = ?", new String[] {stopTag}, null);

				cursor.moveToFirst();
				while (cursor.isAfterLast() == false) {
					String dirTag = cursor.getString(0);
					ret.add(dirTag);
					cursor.moveToNext();
				}
				return ret;
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		public static List<String> getStopTagsForDirTag(ContentResolver resolver,
				String dirTag) {
			ArrayList<String> ret = new ArrayList<String>();
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(DIRECTIONS_STOPS_URI, new String[] {Schema.DirectionsStops.tagColumn},
						Schema.DirectionsStops.dirTagColumn + " = ?", new String[] {dirTag}, null);

				cursor.moveToFirst();
				while (cursor.isAfterLast() == false) {
					String stopTag = cursor.getString(0);
					ret.add(stopTag);
					cursor.moveToNext();
				}
				return ret;
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		public static byte[] pathsToBlob(
				Path[] currentPaths) throws IOException {
			byte[] pathsBlob = null;
			if (currentPaths != null) {
				IBox serializedPath = new Box(null, CURRENT_DB_VERSION);

				serializedPath.writePathsList(currentPaths);

				pathsBlob = serializedPath.getBlob();
			}
			return pathsBlob;
		}

	}


	private DatabaseHelper helper;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "favorites", FAVORITES);
		uriMatcher.addURI(AUTHORITY, "stops", STOPS);
		uriMatcher.addURI(AUTHORITY, "routes", ROUTES);
		uriMatcher.addURI(AUTHORITY, "stops_routes", STOPS_ROUTES);
		uriMatcher.addURI(AUTHORITY, "stops_stops", STOPS_STOPS);
		uriMatcher.addURI(AUTHORITY, "stops_lookup", STOPS_LOOKUP);
		uriMatcher.addURI(AUTHORITY, "directions", DIRECTIONS);
		uriMatcher.addURI(AUTHORITY, "directions_stops", DIRECTIONS_STOPS);
		uriMatcher.addURI(AUTHORITY, "stops_lookup_2", STOPS_LOOKUP_2);
		uriMatcher.addURI(AUTHORITY, "stops_lookup_3", STOPS_LOOKUP_3);
		uriMatcher.addURI(AUTHORITY, "stops_with_distance/*/*/#", STOPS_WITH_DISTANCE);
		uriMatcher.addURI(AUTHORITY, "favorites_with_same_location", FAVORITES_WITH_SAME_LOCATION);
		uriMatcher.addURI(AUTHORITY, "subway_stops", SUBWAY_STOPS);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			count = db.delete(Schema.Favorites.table, selection, selectionArgs);
			break;
		case STOPS:
			count = db.delete(Schema.Stops.table, selection, selectionArgs);
			break;
		case ROUTES:
			count = db.delete(Schema.Routes.table, selection, selectionArgs);
			break;
		case STOPS_ROUTES:
			count = db.delete(Schema.Stopmapping.table, selection, selectionArgs);
			break;
		case DIRECTIONS:
			count = db.delete(Schema.Directions.table, selection, selectionArgs);
			break;
		case DIRECTIONS_STOPS:
			count = db.delete(Schema.DirectionsStops.table, selection, selectionArgs);
			break;
		case SUBWAY_STOPS:
			count = db.delete(Schema.Subway.table, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			return FAVORITES_TYPE;
		case STOPS:
			return STOPS_TYPE;
		case ROUTES:
			return ROUTES_TYPE;
		case STOPS_ROUTES:
			return STOPS_ROUTES_TYPE;
		case STOPS_STOPS:
			return STOPS_STOPS_TYPE;
		case STOPS_LOOKUP:
			return STOPS_LOOKUP_TYPE;
		case DIRECTIONS:
			return DIRECTIONS_TYPE;
		case DIRECTIONS_STOPS:
			return DIRECTIONS_STOPS_TYPE;
		case STOPS_LOOKUP_2:
			return STOPS_LOOKUP_2_TYPE;
		case STOPS_LOOKUP_3:
			return STOPS_LOOKUP_3_TYPE;
		case STOPS_WITH_DISTANCE:
			return STOPS_WITH_DISTANCE_TYPE;
		case FAVORITES_WITH_SAME_LOCATION:
			return FAVORITES_WITH_SAME_LOCATION_TYPE;
		case SUBWAY_STOPS:
			return SUBWAY_STOPS_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case FAVORITES:
		case STOPS:
		case SUBWAY_STOPS:
		case ROUTES:
		case STOPS_ROUTES:
		case DIRECTIONS:
		case DIRECTIONS_STOPS:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = helper.getWritableDatabase();
		switch (match) {
		case FAVORITES:
		{
			long rowId = db.replace(Schema.Favorites.table, null, values);
			if (rowId >= 0) {
				return FAVORITES_URI;
			}
		}
		break;
			
		case STOPS:
		{
			long rowId = db.replace(Schema.Stops.table, null, values);
			if (rowId >= 0) {
				return STOPS_URI;
			}
		}
		break;
		case ROUTES:
		{
			long rowId = db.replace(Schema.Routes.table, null, values);
			if (rowId >= 0) {
				return ROUTES_URI;
			}
		}
		break;
		case STOPS_ROUTES:
		{
			long rowId = db.replace(Schema.Stopmapping.table, null, values);
			if (rowId >= 0) {
				return STOPS_ROUTES_URI;
			}
		}
		break;
		case DIRECTIONS:
		{
			long rowId = db.replace(Schema.Directions.table, null, values);
			if (rowId >= 0) {
				return DIRECTIONS_URI;
			}
		}
		break;
		case DIRECTIONS_STOPS:
		{
			long rowId = db.replace(Schema.DirectionsStops.table, null, values);
			if (rowId >= 0) {
				return DIRECTIONS_STOPS_URI;
			}
		}
		break;
		case SUBWAY_STOPS:
		{
			long rowId = db.replace(Schema.Subway.table, null, values);
			if (rowId >= 0) {
				return SUBWAY_STOPS_URI;
			}
		}
		break;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		helper = DatabaseHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		String limit = null;

		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			builder.setTables(Schema.Favorites.table);
			break;
		case FAVORITES_WITH_SAME_LOCATION:
			builder.setTables(Schema.Favorites.table + " JOIN " + Schema.Stops.table + " as s1 ON " + Schema.Favorites.table + "." + Schema.Favorites.tagColumn +
						" = s1." + Schema.Stops.tagColumn + " JOIN " + Schema.Stops.table + " as s2 ON s1." +  Schema.Stops.latColumn + 
						" = s2." + Schema.Stops.latColumn + " AND s1." + Schema.Stops.lonColumn + " = s2." + Schema.Stops.lonColumn + "");
			builder.setDistinct(true);
			break;
		case STOPS:
			builder.setTables(Schema.Stops.table);
			break;
		case SUBWAY_STOPS:
			builder.setTables(Schema.Subway.table);
			break;
		case ROUTES:
			builder.setTables(Schema.Routes.table);
			break;
		case STOPS_ROUTES:
			builder.setTables(Schema.Stopmapping.table);
			break;
		case STOPS_STOPS:
			builder.setTables(Schema.Stops.table + " as s1, " + Schema.Stops.table + " as s2");
			break;
		case STOPS_LOOKUP:
			builder.setTables(Schema.Stops.table +
						" JOIN " + Schema.Stopmapping.table + " AS sm1 ON (" + Schema.Stops.table + "." + Schema.Stopmapping.tagColumn + " = sm1." + Schema.Stopmapping.tagColumn + ")" +
						" JOIN " + Schema.Stopmapping.table + " AS sm2 ON (" + Schema.Stops.table + "." + Schema.Stopmapping.tagColumn + " = sm2." + Schema.Stopmapping.tagColumn + ")" +
						" LEFT OUTER JOIN " + Schema.Subway.table + " ON (" + Schema.Stops.table + "." + Schema.Stopmapping.tagColumn + " = " + 
						Schema.Subway.table + "." + Schema.Subway.tagColumn + ")");
			break;
		case DIRECTIONS:
			builder.setTables(Schema.Directions.table);
			break;
		case DIRECTIONS_STOPS:
			builder.setTables(Schema.DirectionsStops.table);
			break;
		case STOPS_LOOKUP_2:
			builder.setTables(Schema.Stops.table +
					" JOIN " + Schema.Stopmapping.table + " AS sm1 ON (" + Schema.Stops.table + "." + Schema.Stops.tagColumn + " = sm1." + Schema.Stopmapping.tagColumn + ")" +
					" JOIN " + Schema.Routes.table + " AS r1 ON (sm1." + Schema.Stopmapping.routeColumn + " = r1." + Schema.Routes.routeColumn + ")");
			
			break;
		case STOPS_LOOKUP_3:
			builder.setTables(Schema.Stops.table + " JOIN " + Schema.Stopmapping.table + " ON (" + Schema.Stops.table + "." + Schema.Stops.tagColumn + " = " +
					Schema.Stopmapping.table + "." + Schema.Stopmapping.tagColumn + ") LEFT OUTER JOIN " +
					Schema.Subway.table + " ON (" + Schema.Stops.table + "." + Schema.Stops.tagColumn + " = " + 
					Schema.Subway.table + "." + Schema.Subway.tagColumn + ")");
			break;
		case STOPS_WITH_DISTANCE:
		{
			builder.setTables(Schema.Stops.table);

			List<String> pathSegments = uri.getPathSegments();
			double currentLat = Integer.parseInt(pathSegments.get(1)) * Constants.InvE6;
			double currentLon = Integer.parseInt(pathSegments.get(2)) * Constants.InvE6;
			limit = pathSegments.get(3);
			HashMap<String, String> projectionMap = new HashMap<String, String>();
			projectionMap.put(Schema.Stops.tagColumn, Schema.Stops.tagColumn);

			double lonFactor = Math.cos(currentLat * Geometry.degreesToRadians);
			String latDiff = "(" + Schema.Stops.latColumn + " - " + currentLat + ")";
			String lonDiff = "((" + Schema.Stops.lonColumn + " - " + currentLon + ")*" + lonFactor + ")";
			projectionMap.put(distanceKey, latDiff + "*" + latDiff + " + " + lonDiff + "*" + lonDiff + " AS " + distanceKey);
			builder.setProjectionMap(projectionMap);
		}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			count = db.update(Schema.Favorites.table, values, selection, selectionArgs);
			break;
		case STOPS:
			count = db.update(Schema.Stops.table, values, selection, selectionArgs);
			break;
		case ROUTES:
			count = db.update(Schema.Routes.table, values, selection, selectionArgs);
			break;
		case STOPS_ROUTES:
			count = db.update(Schema.Stopmapping.table, values, selection, selectionArgs);
			break;
		case DIRECTIONS:
			count = db.update(Schema.Directions.table, values, selection, selectionArgs);
			break;
		case DIRECTIONS_STOPS:
			count = db.update(Schema.DirectionsStops.table, values, selection, selectionArgs);
			break;
		case SUBWAY_STOPS:
			count = db.update(Schema.Subway.table, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
}
