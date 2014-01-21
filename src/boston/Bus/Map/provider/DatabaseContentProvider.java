package boston.Bus.Map.provider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import java.util.zip.GZIPInputStream;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import boston.Bus.Map.data.Alarm;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.IntersectionLocation.Builder;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TimeBounds;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.database.Schema.Stopmapping;
import boston.Bus.Map.main.Preferences;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.IBox;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;
import android.R;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseContentProvider extends ContentProvider {
	private static final UriMatcher uriMatcher;
	public static final String AUTHORITY = "com.bostonbusmap.databaseprovider";

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

	private static final String STOPS_AND_ROUTES_WITH_DISTANCE_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.stop_and_route_with_distance";
	public static final Uri STOPS_AND_ROUTES_WITH_DISTANCE_URI = Uri.parse("content://" + AUTHORITY + "/stops_and_routes_with_distance");
	private static final int STOPS_AND_ROUTES_WITH_DISTANCE = 17;

	private static final String ROUTES_AND_BOUNDS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap/route_and_bound";
	public static final Uri ROUTES_AND_BOUNDS_URI = Uri.parse("content://" + AUTHORITY + "/routes_and_bounds");
	private static final int ROUTES_AND_BOUNDS = 18;
	
	private final static String DATABASE_VERSION_KEY = "DB_VERSION";
	
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

	public final static int FIRST_COPYING_DB = 37;
	public final static int ADDING_BOUNDS = 38;
	public final static int ADDING_BOUNDS_1 = 39;
	public final static int ADDING_BOUNDS_2 = 42;
	public final static int FIX_LOCATIONS = 43;
	public final static int NEW_SUBWAY = 44;
	public final static int NEW_CR = 45;
	public final static int NEW_CR_2 = 46;
	public final static int HUBWAY_1 = 50;
	public final static int HUBWAY_2 = 51;
	public final static int HUBWAY_3 = 52;
	public final static int HUBWAY_4 = 53;
	public final static int HUBWAY_5 = 54;
	public final static int HUBWAY_6 = 55;
	public final static int CURRENT_DB_VERSION = HUBWAY_6;

	public static final int ALWAYS_POPULATE = 3;
	public static final int POPULATE_IF_UPGRADE = 2;
	public static final int MAYBE = 1;

	/**
	 * Handles the database which stores route information
	 * 
	 * @author schneg
	 *
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper
	{
		private static DatabaseHelper instance;
		private Context context;

		/**
		 * Don't call this, use getInstance instead
		 * @param context
		 */
		private DatabaseHelper(Context context) {
			super(context, Schema.dbName, null, CURRENT_DB_VERSION);

			this.context = context;
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
		public SQLiteDatabase getReadableDatabase() {
			return getWritableDatabase();
		}
		
		@Override
		public SQLiteDatabase getWritableDatabase() {
			SQLiteDatabase db = null;
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			int version = preferences.getInt(DATABASE_VERSION_KEY, -1);
			if (version < CURRENT_DB_VERSION) {
				try {
					copyDatabase();
					preferences.edit().putInt(DATABASE_VERSION_KEY, CURRENT_DB_VERSION).commit();
				} catch (IOException e) {
					LogUtil.e(e);
					return null;
				}
				
				db = super.getWritableDatabase();
			}
			else
			{
				db = super.getWritableDatabase();
			}
			
			return db;
		}
		
		private void copyDatabase() throws IOException {
			InputStream in = null;
			GZIPInputStream stream = null;
			OutputStream outputStream = null;
			try
			{
				in = context.getResources().openRawResource(boston.Bus.Map.R.raw.databasegz);

				stream = new GZIPInputStream(in); 

				SQLiteDatabase tempDatabase = null;
				try
				{
					tempDatabase = super.getWritableDatabase();
				}
				catch (Exception e) {
					// ignore 
				}
				finally
				{
					if (tempDatabase != null) {
						tempDatabase.close();
					}
				}
				// overwrite database with prepopulated database
				outputStream = new FileOutputStream(context.getDatabasePath(Schema.dbName));
				ByteStreams.copy(stream, outputStream);
			}
			finally
			{
				try
				{
					if (in != null) {
						in.close();
					}
					if (stream != null) {
						stream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				}
				catch (IOException e) {
					LogUtil.e(e);
				}
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}


	public static class DatabaseAgent {
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
				Schema.Routes.executeInsertHelper(routeInsertHelper, route, 
						routeConfig.getColor(), routeConfig.getOppositeColor(),
						pathsToBlob(routeConfig.getPaths()), routeConfig.getListOrder(),
						routeConfig.getTransitSourceId(), routeTitle);
			}
			finally
			{
				routeInsertHelper.close();
			}
			//add all stops associated with the route, if they don't already exist

			InsertHelper stopsToInsert = new InsertHelper(database, Schema.Stops.table);
			InsertHelper stopRoutesToInsert = new InsertHelper(database, Schema.Stopmapping.table);
			try
			{
				for (StopLocation stop : routeConfig.getStops())
				{
					String stopTag = stop.getStopTag();

					if (sharedStops.contains(stopTag) == false)
					{
						sharedStops.add(stopTag);

						Schema.Stops.executeInsertHelper(stopsToInsert, stopTag, stop.getLatitudeAsDegrees(), stop.getLongitudeAsDegrees(), stop.getTitle());
					}

					//show that there's a relationship between the stop and this route
					Schema.Stopmapping.executeInsertHelper(stopRoutesToInsert, route, stopTag);
				}
			}
			finally
			{
				stopsToInsert.close();
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

			resolver.bulkInsert(FavoritesContentProvider.FAVORITES_URI, allValues.toArray(new ContentValues[0]));
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

		public static RouteConfig getRoute(ContentResolver resolver, String routeToUpdate, 
				ConcurrentMap<String, StopLocation> sharedStops,
				TransitSystem transitSystem) throws IOException {

			//get the route-specific information, like the path outline and the color
			RouteConfig.Builder routeConfigBuilder = null;
			{
				Cursor cursor = null;
				try
				{
					String[] projectionIn = new String[]{Schema.Routes.colorColumn,
							Schema.Routes.oppositecolorColumn, Schema.Routes.pathblobColumn, 
							Schema.Routes.routetitleColumn, Schema.Routes.listorderColumn, 
							Schema.Routes.agencyidColumn, 
							Schema.Bounds.weekdaysColumn, Schema.Bounds.startColumn,
							Schema.Bounds.stopColumn};
					cursor = resolver.query(ROUTES_AND_BOUNDS_URI, projectionIn,
							Schema.Routes.routeColumnOnTable + "=?",
							new String[]{routeToUpdate}, null);
					if (cursor.getCount() == 0)
					{
						return null;
					}

					cursor.moveToFirst();

					while (cursor.isAfterLast() == false) {
						if (routeConfigBuilder == null) {
							TransitSource source = transitSystem.getTransitSource(routeToUpdate);

							int color = cursor.getInt(0);
							int oppositeColor = cursor.getInt(1);
							byte[] pathsBlob = cursor.getBlob(2);
							String routeTitle = cursor.getString(3);
							int listorder = cursor.getInt(4);
							int transitSourceId = cursor.getInt(5);

							Box pathsBlobBox = new Box(pathsBlob, CURRENT_DB_VERSION);

							routeConfigBuilder = new RouteConfig.Builder(routeToUpdate, routeTitle, 
									color, oppositeColor, source, listorder, transitSourceId, pathsBlobBox);
						}
						if (!cursor.isNull(6)) {
							int weekdays = cursor.getInt(6);
							int start = cursor.getInt(7);
							int stop = cursor.getInt(8);
							routeConfigBuilder.addTimeBound(weekdays, start, stop);
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

			{
				// get all stops, joining in stops again to get every route for every stop
				String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
						Schema.Stops.titleColumn, "sm2." + Schema.Stopmapping.routeColumn};
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
						String route = cursor.getString(4);

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

								stop = transitSystem.createStop(latitude, longitude, stopTag, stopTitle, route);

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
					boolean dirUseAsUI = Schema.fromInteger(cursor.getInt(4));

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

		public static Cursor getCursorForSearch(ContentResolver resolver, String search) {
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

			String thisStopTitleKey = Schema.Stops.titleColumnOnTable;
			String[] projectionIn = new String[] {thisStopTitleKey, Schema.Stops.tagColumnOnTable, "r1." + Schema.Routes.routetitleColumn};
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
		
		public static Collection<StopLocation> getClosestStopsAndFilterRoutes(ContentResolver resolver, 
				double currentLat, double currentLon, TransitSystem transitSystem, 
				ConcurrentMap<String, StopLocation> sharedStops, int limit, Set<String> routes) {
			return getClosestStops(resolver, currentLat, currentLon, transitSystem,
					sharedStops, limit, routes, true);
		}
		public static Collection<StopLocation> getClosestStops(ContentResolver resolver, 
				double currentLat, double currentLon, TransitSystem transitSystem, 
				ConcurrentMap<String, StopLocation> sharedStops, int limit) {
			Set<String> emptySet = Collections.emptySet();
			return getClosestStops(resolver, currentLat, currentLon, transitSystem,
					sharedStops, limit, emptySet, false);
			
		}
		private static Collection<StopLocation> getClosestStops(ContentResolver resolver, 
				double currentLat, double currentLon, TransitSystem transitSystem, 
				ConcurrentMap<String, StopLocation> sharedStops, int limit, Set<String> routes,
				boolean filterRoutes)
		{
			// what we should scale longitude by for 1 unit longitude to roughly equal 1 unit latitude

			int currentLatAsInt = (int)(currentLat * Constants.E6);
			int currentLonAsInt = (int)(currentLon * Constants.E6);
			Uri uri;
			String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, distanceKey};
			if (filterRoutes == false) {
				uri = STOPS_WITH_DISTANCE_URI;
			}
			else
			{
				uri = STOPS_AND_ROUTES_WITH_DISTANCE_URI;
			}
			uri = appendUris(uri, currentLatAsInt, currentLonAsInt, limit);
			
			Cursor cursor = null;
			try
			{
				String select;
				if (filterRoutes == false) {
					select = null;
				}
				else
				{
					StringBuilder selectBuilder = new StringBuilder();
					selectBuilder.append(Schema.Routes.routeColumn).append(" IN (").append(StringUtil.quotedJoin(routes)).append(")");
					select = selectBuilder.toString();
				}
				cursor = resolver.query(uri, projectionIn, select, null, distanceKey);
				if (cursor.moveToFirst() == false)
				{
					return Collections.emptyList();
				}

				ImmutableList.Builder<String> stopTagsBuilder = ImmutableList.builder();
				List<String> stopTagsInAll = Lists.newArrayList();
				while (!cursor.isAfterLast())
				{
					String id = cursor.getString(0);
					if (sharedStops.containsKey(id) == false) {
						stopTagsBuilder.add(id);
					}
					stopTagsInAll.add(id);

					cursor.moveToNext();
				}
				ImmutableList<String> stopTags = stopTagsBuilder.build();
				getStops(resolver, stopTags, transitSystem, sharedStops);

				ImmutableList.Builder<StopLocation> builder = ImmutableList.builder();
				for (String stopTag : stopTagsInAll)
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
			String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
					Schema.Stops.titleColumn, Schema.Stopmapping.routeColumnOnTable};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;

			select = new StringBuilder(Schema.Stops.tagColumnOnTable + "=? OR " + Schema.Stops.titleColumnOnTable + "=?");
			selectArray = new String[]{tagQuery, titleQuery};

			Cursor stopCursor = null;
			try
			{
				stopCursor = resolver.query(STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

				stopCursor.moveToFirst();

				if (stopCursor.isAfterLast() == false)
				{
					String stopTag = stopCursor.getString(0);

					String route = stopCursor.getString(4);

					float lat = stopCursor.getFloat(1);
					float lon = stopCursor.getFloat(2);
					String title = stopCursor.getString(3);

					StopLocation stop = transitSystem.createStop(lat, lon, stopTag, title, route);
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
			String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn, 
					Schema.Stops.titleColumn, Schema.Stopmapping.routeColumnOnTable};

			//if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
			StringBuilder select;
			String[] selectArray;
			if (stopTags.size() == 1)
			{
				String stopTag = stopTags.get(0);

				select = new StringBuilder(Schema.Stops.tagColumnOnTable + "=?");
				selectArray = new String[]{stopTag};

				//Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, verboseStops + "." + stopTagKey + "=\"" + stopTagKey + "\"",
				//		null, null, null, null));
			}
			else
			{
				select = new StringBuilder(Schema.Stops.tagColumnOnTable + " IN (");

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

					String route = stopCursor.getString(4);

					StopLocation stop = outputMapping.get(stopTag);
					if (stop == null)
					{
						float lat = stopCursor.getFloat(1);
						float lon = stopCursor.getFloat(2);
						String title = stopCursor.getString(3);

						stop = transitSystem.createStop(lat, lon, stopTag, title, route);
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

		public static List<Alarm> getAlarms(ContentResolver resolver) {
			List<Alarm> ret = Lists.newArrayList();
			Cursor cursor = null;
			try
			{
				String[] keys = new String[] {
					Schema.Alarms.alarm_timeColumn,
						Schema.Alarms.scheduled_timeColumn,
						Schema.Alarms.routeColumn,
						Schema.Alarms.stopColumn,
						Schema.Alarms.dirTagColumn,
						Schema.Alarms.minutes_beforeColumn
				};
				cursor = resolver.query(FavoritesContentProvider.ALARMS_URI, keys, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false) {
					long alarmTime = cursor.getLong(0);
					long scheduledTime = cursor.getLong(1);
					String route = cursor.getString(2);
					String stop = cursor.getString(3);
					String direction = cursor.getString(4);
					int minutes = cursor.getInt(5);

					ret.add(new Alarm(alarmTime, scheduledTime, stop, route, direction, minutes));

					cursor.moveToNext();
				}
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
			return ret;
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
			ArrayList<String> ret = Lists.newArrayList();
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

				Collection<StopLocation> stops = getClosestStops(resolver, 
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

		public static RouteTitles getRouteTitles(ContentResolver resolver) {
			
			Cursor cursor = resolver.query(ROUTES_URI, new String[]{
					Schema.Routes.routeColumn, Schema.Routes.routetitleColumn, Schema.Routes.agencyidColumn},
					null, null, Schema.Routes.listorderColumn);
			try
			{
				cursor.moveToFirst();
				
				ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
				ImmutableMap.Builder<String, Integer> agencyIdMap = ImmutableMap.builder();
				while (cursor.isAfterLast() == false) {
					String route = cursor.getString(0);
					String routetitle = cursor.getString(1);
					int agencyid = cursor.getInt(2);
					
					agencyIdMap.put(route, agencyid);
					builder.put(route, routetitle);
					cursor.moveToNext();
				}
				
				return new RouteTitles(builder.build(), agencyIdMap.build());
			}
			finally
			{
				if (cursor != null) {
					cursor.close();
				}
			}
		}


		public static void removeIntersection(ContentResolver contentResolver,
				String name) {
			int result = contentResolver.delete(FavoritesContentProvider.LOCATIONS_URI, Schema.Locations.nameColumn + "= ?", new String[]{name});
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

		public static void addAlarm(ContentResolver resolver,
				Alarm alarm) {
			ContentValues values = new ContentValues();
			values.put(Schema.Alarms.alarm_timeColumn, alarm.getAlarmTime());
			values.put(Schema.Alarms.scheduled_timeColumn, alarm.getScheduledTime());
			values.put(Schema.Alarms.stopColumn, alarm.getStop());
			values.put(Schema.Alarms.routeColumn, alarm.getRouteTitle());
			values.put(Schema.Alarms.dirTagColumn, alarm.getDirectionTitle());
			values.put(Schema.Alarms.minutes_beforeColumn, alarm.getMinutesBefore());

			resolver.insert(FavoritesContentProvider.ALARMS_URI, values);
		}

		public static void removeAlarm(ContentResolver resolver, Alarm alarm) {
			// TODO: use alarm id
			resolver.delete(FavoritesContentProvider.ALARMS_URI, Schema.Alarms.scheduled_timeColumn + " = ?",
					new String[] {String.valueOf(alarm.getScheduledTime())});
		}

		public static void updateAlarm(ContentResolver resolver, Alarm newAlarm) {
			ContentValues values = new ContentValues();
			values.put(Schema.Alarms.alarm_timeColumn, newAlarm.getAlarmTime());
			resolver.update(FavoritesContentProvider.ALARMS_URI, values,
					Schema.Alarms.scheduled_timeColumn + " = ?",
					new String[]{String.valueOf(newAlarm.getScheduledTime())});
		}
	}


	private DatabaseHelper helper;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
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
		uriMatcher.addURI(AUTHORITY, "stops_and_routes_with_distance/*/*/#", STOPS_AND_ROUTES_WITH_DISTANCE);
		uriMatcher.addURI(AUTHORITY, "routes_and_bounds", ROUTES_AND_BOUNDS);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
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
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
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
		case STOPS_AND_ROUTES_WITH_DISTANCE:
			return STOPS_AND_ROUTES_WITH_DISTANCE_TYPE;
		case ROUTES_AND_BOUNDS:
			return ROUTES_AND_BOUNDS_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case STOPS:
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
			
		case STOPS:
		{
			long rowId = db.replace(Schema.Stops.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return STOPS_URI;
			}
		}
		break;
		case ROUTES:
		{
			long rowId = db.replace(Schema.Routes.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return ROUTES_URI;
			}
		}
		break;
		case STOPS_ROUTES:
		{
			long rowId = db.replace(Schema.Stopmapping.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return STOPS_ROUTES_URI;
			}
		}
		break;
		case DIRECTIONS:
		{
			long rowId = db.replace(Schema.Directions.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return DIRECTIONS_URI;
			}
		}
		break;
		case DIRECTIONS_STOPS:
		{
			long rowId = db.replace(Schema.DirectionsStops.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return DIRECTIONS_STOPS_URI;
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
		case STOPS:
			builder.setTables(Schema.Stops.table);
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
						" JOIN " + Schema.Stopmapping.table + " AS sm1 ON (" + Schema.Stops.tagColumnOnTable + " = sm1." + Schema.Stopmapping.tagColumn + ")" +
						" JOIN " + Schema.Stopmapping.table + " AS sm2 ON (" + Schema.Stops.tagColumnOnTable + " = sm2." + Schema.Stopmapping.tagColumn + ")"
						);
			break;
		case DIRECTIONS:
			builder.setTables(Schema.Directions.table);
			break;
		case DIRECTIONS_STOPS:
			builder.setTables(Schema.DirectionsStops.table);
			break;
		case STOPS_LOOKUP_2:
			builder.setTables(Schema.Stops.table +
					" JOIN " + Schema.Stopmapping.table + " AS sm1 ON (" + Schema.Stops.tagColumnOnTable + " = sm1." + Schema.Stopmapping.tagColumn + ")" +
					" JOIN " + Schema.Routes.table + " AS r1 ON (sm1." + Schema.Stopmapping.routeColumn + " = r1." + Schema.Routes.routeColumn + ")");
			
			break;
		case STOPS_LOOKUP_3:
			builder.setTables(Schema.Stops.table + " JOIN " + Schema.Stopmapping.table + " ON (" + Schema.Stops.tagColumnOnTable + " = " +
					Schema.Stopmapping.tagColumnOnTable + ") ");
			break;
		case STOPS_WITH_DISTANCE:
		{
			List<String> pathSegments = uri.getPathSegments();
			double currentLat = Integer.parseInt(pathSegments.get(1)) * Constants.InvE6;
			double currentLon = Integer.parseInt(pathSegments.get(2)) * Constants.InvE6;
			limit = pathSegments.get(3);
			builder.setTables(Schema.Stops.table);

			HashMap<String, String> projectionMap = new HashMap<String, String>();
			projectionMap.put(Schema.Stops.tagColumnOnTable, Schema.Stops.tagColumnOnTable);

			double lonFactor = Math.cos(currentLat * Geometry.degreesToRadians);
			String latDiff = "(" + Schema.Stops.latColumn + " - " + currentLat + ")";
			String lonDiff = "((" + Schema.Stops.lonColumn + " - " + currentLon + ")*" + lonFactor + ")";
			projectionMap.put(distanceKey, latDiff + "*" + latDiff + " + " + lonDiff + "*" + lonDiff + " AS " + distanceKey);
			builder.setProjectionMap(projectionMap);
		}
			break;
		case STOPS_AND_ROUTES_WITH_DISTANCE:
		{
			List<String> pathSegments = uri.getPathSegments();
			double currentLat = Integer.parseInt(pathSegments.get(1)) * Constants.InvE6;
			double currentLon = Integer.parseInt(pathSegments.get(2)) * Constants.InvE6;
			limit = pathSegments.get(3);
			builder.setTables(Schema.Stops.table + " JOIN " + Schema.Stopmapping.table +
					" ON " + Schema.Stops.tagColumnOnTable + " = " + Schema.Stopmapping.tagColumnOnTable);

			HashMap<String, String> projectionMap = new HashMap<String, String>();
			projectionMap.put(Schema.Stops.tagColumnOnTable, Schema.Stops.tagColumnOnTable);

			double lonFactor = Math.cos(currentLat * Geometry.degreesToRadians);
			String latDiff = "(" + Schema.Stops.latColumn + " - " + currentLat + ")";
			String lonDiff = "((" + Schema.Stops.lonColumn + " - " + currentLon + ")*" + lonFactor + ")";
			projectionMap.put(distanceKey, latDiff + "*" + latDiff + " + " + lonDiff + "*" + lonDiff + " AS " + distanceKey);
			builder.setProjectionMap(projectionMap);
		}
			break;
		case ROUTES_AND_BOUNDS:
			builder.setTables(Schema.Routes.table + " LEFT OUTER JOIN " + Schema.Bounds.table + " ON " + Schema.Routes.routeColumnOnTable + " = " + Schema.Bounds.routeColumnOnTable);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		//Log.v("BostonBusMap", builder.buildQuery(projection, selection, null, null, null, sortOrder, limit));
		
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
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
}
