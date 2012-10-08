package boston.Bus.Map.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
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

	private final static String dbName = "bostonBusMap";

	private final static String verboseRoutes = "routes";
	private final static String verboseStops = "stops";
	private final static String stopsRoutesMap = "stopmapping";
	private final static String stopsRoutesMapIndexTag = "IDX_stopmapping";
	private final static String stopsRoutesMapIndexRoute = "IDX_routemapping";
	private final static String directionsStopsMapIndexStop = "IDX_directionsstop_stop";
	private final static String directionsStopsMapIndexDirTag = "IDX_directionsstop_dirtag";


	private final static String subwaySpecificTable = "subway";


	private final static String directionsTable = "directions";
	private final static String directionsStopsTable = "directionsStops";
	private final static String stopsTable = "stops";
	private final static String routesTable = "routes";
	private final static String pathsTable = "paths";
	private final static String blobsTable = "blobs";

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
	private static final String dirUseAsUIKey = "useAsUI";
	
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
	private static class DatabaseHelper extends SQLiteOpenHelper
	{


		public DatabaseHelper(Context context) {
			super(context, dbName, null, CURRENT_DB_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			/*db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING PRIMARY KEY, " + blobKey + " BLOB)");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + routePoolTable + " (" + routeKey + " STRING PRIMARY KEY)");*/
			db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseFavorites + " (" + stopTagKey + " STRING PRIMARY KEY)");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + dirTagKey + " STRING PRIMARY KEY, " + 
					dirNameKey + " STRING, " + dirTitleKey + " STRING, " + dirRouteKey + " STRING, " + 
					dirUseAsUIKey + " INTEGER)");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseRoutes + " (" + routeKey + " STRING PRIMARY KEY, " + colorKey + 
					" INTEGER, " + oppositeColorKey + " INTEGER, " + pathsBlobKey + " BLOB, " + routeTitleKey + " STRING)");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
					latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING)");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + stopsRoutesMap + " (" + routeKey + " STRING, " + stopTagKey + " STRING, " +
					dirTagKey + " STRING, PRIMARY KEY (" + routeKey + ", " + stopTagKey + "))");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + subwaySpecificTable + " (" + stopTagKey + " STRING PRIMARY KEY, " +
					platformOrderKey + " INTEGER, " + 
					branchKey + " STRING)");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsStopsTable +
					"(" + dirTagKey + " STRING, " + stopTagKey + " STRING)");

			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexRoute + " ON " + stopsRoutesMap + " (" + routeKey + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexTag + " ON " + stopsRoutesMap + " (" + stopTagKey + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexStop + " ON " + directionsStopsTable + " (" + stopTagKey + ")");
			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexDirTag + " ON " + directionsStopsTable + " (" + dirTagKey + ")");
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

			if (oldVersion < CURRENT_DB_VERSION)
			{
				db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
				db.execSQL("DROP TABLE IF EXISTS " + directionsStopsTable);
				db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
				db.execSQL("DROP TABLE IF EXISTS " + routesTable);
				db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
				db.execSQL("DROP TABLE IF EXISTS " + blobsTable);
				db.execSQL("DROP TABLE IF EXISTS " + verboseRoutes);
				db.execSQL("DROP TABLE IF EXISTS " + verboseStops);
				db.execSQL("DROP TABLE IF EXISTS " + stopsRoutesMap);
			}

			//if it's verboseFavorites, we want to save it since it's user specified data

			onCreate(db);

			/*if (favorites != null)
			{
				writeVerboseFavorites(db, favorites);
			}*/

			db.setTransactionSuccessful();
			db.endTransaction();

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

					cursor = contentResolver.query(FAVORITES_WITH_SAME_LOCATION_URI, new String[]{"s2." + stopTagKey}, null, null, null);
				}
				else
				{
					cursor = contentResolver.query(FAVORITES_URI, new String[]{stopTagKey},
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

		public static void saveMapping(ContentResolver contentResolver, 
				Map<String, RouteConfig> mapping,
				boolean wipe, HashSet<String> sharedStops, UpdateAsyncTask task)
						throws IOException, RemoteException, OperationApplicationException
		{
			ArrayList<ContentProviderOperation> operations = 
					new ArrayList<ContentProviderOperation>();
			if (wipe)
			{
				//database.delete(stopsTable, null, null);
				//database.delete(directionsTable, null, null);
				//database.delete(pathsTable, null, null);
				//database.delete(blobsTable, null, null);
				operations.add(ContentProviderOperation.newDelete(STOPS_URI).build());
				operations.add(ContentProviderOperation.newDelete(ROUTES_URI).build());
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
					saveMappingKernel(operations, route, routeTitle, routeConfig, sharedStops);
				}

				count++;
				task.publish(count);
			}

			contentResolver.applyBatch(AUTHORITY, operations);
		}

		/**
		 * 
		 * @param database
		 * @param route
		 * @param routeConfig
		 * @param useInsert insert all rows, don't replace them. I assume this is faster since there's no lookup involved
		 * @throws IOException 
		 */
		private static void saveMappingKernel(ArrayList<ContentProviderOperation> operations, 
				String route, String routeTitle, RouteConfig routeConfig,
				HashSet<String> sharedStops) throws IOException
		{
			operations.add(makeRoute(route, routeTitle, routeConfig.getColor(), routeConfig.getOppositeColor(), routeConfig.getPaths()));

			//add all stops associated with the route, if they don't already exist

			for (StopLocation stop : routeConfig.getStops())
			{
				/*"CREATE TABLE IF NOT EXISTS " + verboseStops + " (" + stopTagKey + " STRING PRIMARY KEY, " + 
				latitudeKey + " FLOAT, " + longitudeKey + " FLOAT, " + stopTitleKey + " STRING, " +
				branchKey + " STRING, " + platformOrderKey + " SHORT)"*/
				String stopTag = stop.getStopTag();

				if (sharedStops.contains(stopTag) == false)
				{

					sharedStops.add(stopTag);

					operations.add(makeStop(stopTag, stop.getLatitudeAsDegrees(), stop.getLongitudeAsDegrees(), stop.getTitle()));

					if (stop instanceof SubwayStopLocation)
					{
						SubwayStopLocation subwayStop = (SubwayStopLocation)stop;
						operations.add(makeSubwayStop(stopTag, subwayStop.getPlatformOrder(), subwayStop.getBranch()));
					}
				}

				//show that there's a relationship between the stop and this route
				operations.add(makeStopRoute(stopTag, route, ""));
			}
		}

		public static ImmutableList<String> getAllStopTagsAtLocation(ContentResolver resolver, 
				String stopTag)
		{
			Cursor cursor = null;
			try
			{
				cursor = resolver.query(STOPS_STOPS_URI, 
						new String[]{"s2." + stopTagKey}, "s1." + stopTagKey + " = ? AND s1." + latitudeKey + " = s2." + latitudeKey +
						" AND s1." + longitudeKey + " = s2." + longitudeKey + "", new String[]{stopTag}, null);

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

		private static void storeFavorite(ContentResolver resolver, Collection<String> stopTags) throws RemoteException, OperationApplicationException
		{
			if (stopTags == null || stopTags.size() == 0)
			{
				return;
			}

			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			for (String tag : stopTags)
			{
				ContentValues values = new ContentValues();
				values.put(stopTagKey, tag);

				operations.add(ContentProviderOperation.newInsert(FAVORITES_URI).withValues(values).build());
			}
			
			resolver.applyBatch(AUTHORITY, operations);
		}


		public static void saveFavorite(ContentResolver resolver, 
				String stopTag, Collection<String> stopTags, boolean isFavorite) throws RemoteException, OperationApplicationException {
			if (isFavorite)
			{
				storeFavorite(resolver, stopTags);
			}
			else
			{
				//delete all stops at location
				resolver.delete(FAVORITES_URI, verboseFavorites + "." + stopTagKey + 
						" IN (SELECT s2." + stopTagKey + " FROM " + verboseStops + " as s1, " + verboseStops + " as s2 WHERE " +
						"s1." + latitudeKey + " = s2." + latitudeKey + " AND s1." + longitudeKey +
						" = s2." + longitudeKey + " AND s1." + stopTagKey + " = ?)", new String[]{stopTag});
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
					cursor = resolver.query(ROUTES_URI, new String[]{colorKey, oppositeColorKey, pathsBlobKey, routeTitleKey}, routeKey + "=?",
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
				String[] projectionIn = new String[] {verboseStops + "." + stopTagKey, latitudeKey, longitudeKey, 
						stopTitleKey, platformOrderKey, branchKey, "sm2." + dirTagKey, "sm2." + routeKey};
				String select = "sm1." + routeKey + "=?";
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
				cursor = resolver.query(ROUTES_URI, new String[]{routeKey}, null, null, null);
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
				cursor = resolver.query(DIRECTIONS_URI, new String[]{dirTagKey, dirNameKey, dirTitleKey, dirRouteKey, dirUseAsUIKey},
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

		public static void writeDirections(ContentResolver resolver, boolean wipe, ConcurrentHashMap<String, Direction> directions) throws RemoteException, OperationApplicationException {
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			if (wipe)
			{
				operations.add(ContentProviderOperation.newDelete(DIRECTIONS_URI).build());
				operations.add(ContentProviderOperation.newDelete(DIRECTIONS_STOPS_URI).build());
			}

			for (String dirTag : directions.keySet())
			{
				Direction direction = directions.get(dirTag);
				String name = direction.getName();
				String title = direction.getTitle();
				String route = direction.getRoute();
				boolean useAsUI = direction.isUseForUI();

				operations.add(makeDirection(dirTag, name, title, route, useAsUI));

				for (String stopTag : direction.getStopTags()) {
					operations.add(makeStopDirection(stopTag, dirTag));
				}

			}
			resolver.applyBatch(AUTHORITY, operations);
		}

		public static void saveFavorites(ContentResolver resolver, Set<String> favoriteStops, Map<String, StopLocation> sharedStops) throws RemoteException, OperationApplicationException {
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			operations.add(ContentProviderOperation.newDelete(FAVORITES_URI).build());

			for (String stopTag : favoriteStops)
			{
				StopLocation stopLocation = sharedStops.get(stopTag);

				if (stopLocation != null)
				{
					operations.add(ContentProviderOperation.newInsert(FAVORITES_URI).withValue(stopTagKey, stopTag).build());
				}
			}
			
			resolver.applyBatch(AUTHORITY, operations);
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
				cursor = resolver.query(ROUTES_URI, new String[]{routeTitleKey, routeKey}, routeTitleKey + " LIKE ?",
						new String[]{"%" + search + "%"}, routeTitleKey);
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

			String thisStopTitleKey = verboseStops + "." + stopTitleKey;
			String[] projectionIn = new String[] {thisStopTitleKey, verboseStops + "." + stopTagKey, "r1." + routeTitleKey};
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

			String[] projectionIn = new String[] {stopTagKey, distanceKey};
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

				ArrayList<String> stopTags = new ArrayList<String>();
				while (!cursor.isAfterLast())
				{
					String id = cursor.getString(0);
					stopTags.add(id);

					cursor.moveToNext();
				}

				getStops(resolver, ImmutableList.copyOf(stopTags), transitSystem, sharedStops);

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
			String[] projectionIn = new String[] {verboseStops + "." + stopTagKey, latitudeKey, longitudeKey, 
					stopTitleKey, platformOrderKey, branchKey, stopsRoutesMap + "." + routeKey, stopsRoutesMap + "." + dirTagKey};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;

			select = new StringBuilder(verboseStops + "." + stopTagKey + "=? OR " + verboseStops + "." + stopTitleKey + "=?");
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

				//Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, verboseStops + "." + stopTagKey + "=\"" + stopTagKey + "\"",
				//		null, null, null, null));
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
			String[] projectionIn = new String[] {verboseStops + "." + stopTagKey, latitudeKey, longitudeKey, 
					stopTitleKey, platformOrderKey, branchKey, stopsRoutesMap + "." + routeKey, stopsRoutesMap + "." + dirTagKey};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;

			select = new StringBuilder(verboseStops + "." + dirTagKey + "=?");
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
				cursor = resolver.query(DIRECTIONS_STOPS_URI, new String[] {dirTagKey},
						stopTagKey + " = ?", new String[] {stopTag}, null);

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
				cursor = resolver.query(DIRECTIONS_STOPS_URI, new String[] {stopTagKey},
						dirTagKey + " = ?", new String[] {dirTag}, null);

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

		public static ContentProviderOperation makeStop(
				String tag,
				float latitudeAsDegrees, float longitudeAsDegrees, String title) {
			return ContentProviderOperation.newInsert(STOPS_URI).withValue(stopTagKey, tag)
					.withValue(latitudeKey, latitudeAsDegrees)
					.withValue(longitudeKey, longitudeAsDegrees)
					.withValue(stopTitleKey, title).build();
		}

		public static ContentProviderOperation makeRoute(
				String tag,
				String routeTitle, int color, int oppositeColor,
				Path[] currentPaths) throws IOException {
			byte[] pathsBlob = null;
			if (currentPaths != null) {
				IBox serializedPath = new Box(null, CURRENT_DB_VERSION);

				serializedPath.writePathsList(currentPaths);

				pathsBlob = serializedPath.getBlob();
			}
	
			
			return ContentProviderOperation.newInsert(ROUTES_URI).withValue(routeKey, tag)
					.withValue(routeTitleKey, routeTitle)
					.withValue(colorKey, color)
					.withValue(oppositeColorKey, oppositeColor)
					.withValue(pathsBlobKey, pathsBlob).build();
		}

		public static ContentProviderOperation makeDirection(
				String tag, String name,
				String title, String routeName, boolean useForUI) {
			return ContentProviderOperation.newInsert(DIRECTIONS_URI)
					.withValue(dirTagKey, tag)
					.withValue(dirNameKey, name)
					.withValue(dirTitleKey, title)
					.withValue(dirRouteKey, routeName)
					.withValue(dirUseAsUIKey, useForUI).build();
		}

		public static ContentProviderOperation makeStopRoute(String tag,
				String routeName, String dirTag) {
			return ContentProviderOperation.newInsert(STOPS_ROUTES_URI)
					.withValue(stopTagKey, tag)
					.withValue(routeKey, routeName)
					.withValue(dirTagKey, dirTag).build();
		}

		private static ContentProviderOperation makeSubwayStop(String stopTag,
				int platformOrder, String branch) {
			ContentValues values = new ContentValues();
			values.put(stopTagKey, stopTag);
			values.put(platformOrderKey, platformOrder);
			values.put(branchKey, branch);

			return ContentProviderOperation.newInsert(SUBWAY_STOPS_URI).withValues(values).build();
		}

		public static ContentProviderOperation makeStopDirection(String stopTag,
				String dirTag) {
			return ContentProviderOperation.newInsert(DIRECTIONS_STOPS_URI)
					.withValue(stopTagKey, stopTag)
					.withValue(dirTagKey, dirTag).build();
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
			count = db.delete(verboseFavorites, selection, selectionArgs);
			break;
		case STOPS:
			count = db.delete(verboseStops, selection, selectionArgs);
			break;
		case ROUTES:
			count = db.delete(verboseRoutes, selection, selectionArgs);
			break;
		case STOPS_ROUTES:
			count = db.delete(stopsRoutesMap, selection, selectionArgs);
			break;
		case DIRECTIONS:
			count = db.delete(directionsTable, selection, selectionArgs);
			break;
		case DIRECTIONS_STOPS:
			count = db.delete(directionsStopsTable, selection, selectionArgs);
			break;
		case SUBWAY_STOPS:
			count = db.delete(subwaySpecificTable, selection, selectionArgs);
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
			long rowId = db.replace(verboseFavorites, null, values);
			if (rowId >= 0) {
				return FAVORITES_URI;
			}
		}
		break;
			
		case STOPS:
		{
			long rowId = db.replace(verboseStops, null, values);
			if (rowId >= 0) {
				return STOPS_URI;
			}
		}
		break;
		case ROUTES:
		{
			long rowId = db.replace(verboseRoutes, null, values);
			if (rowId >= 0) {
				return ROUTES_URI;
			}
		}
		break;
		case STOPS_ROUTES:
		{
			long rowId = db.replace(stopsRoutesMap, null, values);
			if (rowId >= 0) {
				return STOPS_ROUTES_URI;
			}
		}
		break;
		case DIRECTIONS:
		{
			long rowId = db.replace(directionsTable, null, values);
			if (rowId >= 0) {
				return DIRECTIONS_URI;
			}
		}
		break;
		case DIRECTIONS_STOPS:
		{
			long rowId = db.replace(directionsStopsTable, null, values);
			if (rowId >= 0) {
				return DIRECTIONS_STOPS_URI;
			}
		}
		break;
		case SUBWAY_STOPS:
		{
			long rowId = db.replace(subwaySpecificTable, null, values);
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
		helper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		String limit = null;

		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			builder.setTables(verboseFavorites);
			break;
		case FAVORITES_WITH_SAME_LOCATION:
			builder.setTables(verboseFavorites + " JOIN stops as s1 ON " + verboseFavorites + "." + stopTagKey +
						" = s1." + stopTagKey + " JOIN stops as s2 ON s1." +  latitudeKey + 
						" = s2." + latitudeKey + " AND s1." + longitudeKey + " = s2." + longitudeKey + "");
			builder.setDistinct(true);
			break;
		case STOPS:
			builder.setTables(verboseStops);
			break;
		case SUBWAY_STOPS:
			builder.setTables(subwaySpecificTable);
			break;
		case ROUTES:
			builder.setTables(verboseRoutes);
			break;
		case STOPS_ROUTES:
			builder.setTables(stopsRoutesMap);
			break;
		case STOPS_STOPS:
			builder.setTables(verboseStops + " as s1, " + verboseStops + " as s2");
			break;
		case STOPS_LOOKUP:
			builder.setTables(verboseStops +
						" JOIN " + stopsRoutesMap + " AS sm1 ON (" + verboseStops + "." + stopTagKey + " = sm1." + stopTagKey + ")" +
						" JOIN " + stopsRoutesMap + " AS sm2 ON (" + verboseStops + "." + stopTagKey + " = sm2." + stopTagKey + ")" +
						" LEFT OUTER JOIN " + subwaySpecificTable + " ON (" + verboseStops + "." + stopTagKey + " = " + 
						subwaySpecificTable + "." + stopTagKey + ")");
			break;
		case DIRECTIONS:
			builder.setTables(directionsTable);
			break;
		case DIRECTIONS_STOPS:
			builder.setTables(directionsStopsTable);
			break;
		case STOPS_LOOKUP_2:
			builder.setTables(verboseStops +
					" JOIN " + stopsRoutesMap + " AS sm1 ON (" + verboseStops + "." + stopTagKey + " = sm1." + stopTagKey + ")" +
					" JOIN " + verboseRoutes + " AS r1 ON (sm1." + routeKey + " = r1." + routeKey + ")");
			
			break;
		case STOPS_LOOKUP_3:
			builder.setTables(verboseStops + " JOIN " + stopsRoutesMap + " ON (" + verboseStops + "." + stopTagKey + " = " +
					stopsRoutesMap + "." + stopTagKey + ") LEFT OUTER JOIN " +
					subwaySpecificTable + " ON (" + verboseStops + "." + stopTagKey + " = " + 
					subwaySpecificTable + "." + stopTagKey + ")");
			break;
		case STOPS_WITH_DISTANCE:
		{
			builder.setTables(verboseStops);

			List<String> pathSegments = uri.getPathSegments();
			double currentLat = Integer.parseInt(pathSegments.get(1)) * Constants.InvE6;
			double currentLon = Integer.parseInt(pathSegments.get(2)) * Constants.InvE6;
			limit = pathSegments.get(3);
			HashMap<String, String> projectionMap = new HashMap<String, String>();
			projectionMap.put(stopTagKey, stopTagKey);

			double lonFactor = Math.cos(currentLat * Geometry.degreesToRadians);
			String latDiff = "(" + latitudeKey + " - " + currentLat + ")";
			String lonDiff = "((" + longitudeKey + " - " + currentLon + ")*" + lonFactor + ")";
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
			count = db.update(verboseFavorites, values, selection, selectionArgs);
			break;
		case STOPS:
			count = db.update(verboseStops, values, selection, selectionArgs);
			break;
		case ROUTES:
			count = db.update(verboseRoutes, values, selection, selectionArgs);
			break;
		case STOPS_ROUTES:
			count = db.update(stopsRoutesMap, values, selection, selectionArgs);
			break;
		case DIRECTIONS:
			count = db.update(directionsTable, values, selection, selectionArgs);
			break;
		case DIRECTIONS_STOPS:
			count = db.update(directionsStopsTable, values, selection, selectionArgs);
			break;
		case SUBWAY_STOPS:
			count = db.update(subwaySpecificTable, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		final SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		try
		{
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		}
		finally
		{
			db.endTransaction();
		}
	}

}
