package boston.Bus.Map.provider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.common.io.ByteStreams;

import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.math.Geometry;

import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;

public class DatabaseContentProvider extends ContentProvider {
	private static final UriMatcher uriMatcher;
	public static final String AUTHORITY = "com.bostonbusmap.torontodatabaseprovider";

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
	
	public static final String distanceKey = "distance";

	/**
	 * Handles the database which stores route information
	 * 
	 * @author schneg
	 *
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper
	{
		private Context context;

        /**
         * Get the application's version code and use it to keep track of database versions.
         * This means the database will be assumed to have changed on each update, which will cause this
         * database to be recreated (the favorites database should be left alone though)
         */
        public static int getVersionCode(Context context) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

		/**
		 * Don't call this, use getInstance instead
		 * @param context
		 */
		private DatabaseHelper(Context context) {
			super(context, Schema.dbName, null, getVersionCode(context));

			this.context = context;
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
			if (version != getVersionCode(context)) {
				try {
					copyDatabase();
					preferences.edit().putInt(DATABASE_VERSION_KEY, getVersionCode(context)).commit();
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
				in = context.getResources().openRawResource(com.schneeloch.torontotransit.R.raw.databasegz);

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
		helper = new DatabaseHelper(getContext());
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
