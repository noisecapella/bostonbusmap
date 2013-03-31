package boston.Bus.Map.provider;

import boston.Bus.Map.database.Schema;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FavoritesContentProvider extends ContentProvider
{
	public static final String AUTHORITY = "com.bostonbusmap.torontofavoritesprovider";

	private static final String FAVORITES_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.favorite";
	public static final Uri FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/favorites");
	private static final int FAVORITES = 1;

	private static final String LOCATIONS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.location";
	public static final Uri LOCATIONS_URI = Uri.parse("content://" + AUTHORITY + "/locations");
	private static final int LOCATIONS = 14;
	
	private final UriMatcher uriMatcher;

	private FavoritesDatabaseHelper helper;
	
	public FavoritesContentProvider() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "favorites", FAVORITES);
		uriMatcher.addURI(AUTHORITY, "locations", LOCATIONS);
	}
	
	private static class FavoritesDatabaseHelper extends SQLiteOpenHelper
	{

		public FavoritesDatabaseHelper(Context context) {
			super(context, Schema.oldDb, null, DatabaseContentProvider.CURRENT_DB_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Schema.Favorites.createSql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (newVersion < DatabaseContentProvider.CURRENT_DB_VERSION) {
				db.execSQL(Schema.Stops.dropSql);
				db.execSQL(Schema.Stopmapping.dropSql);
				db.execSQL(Schema.Routes.dropSql);
				db.execSQL(Schema.Subway.dropSql);
				db.execSQL(Schema.Directions.dropSql);
			}
			
			onCreate(db);
			
		}
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			count = db.delete(Schema.Favorites.table, selection, selectionArgs);
			break;
		case LOCATIONS:
			count = db.delete(Schema.Locations.table, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
			return FAVORITES_TYPE;
		case LOCATIONS:
			return LOCATIONS_TYPE;

		}
		throw new IllegalArgumentException("Unknown uri: " + uri);
	}
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int count;
		switch (uriMatcher.match(uri)) {
		case FAVORITES:
		case LOCATIONS:
			break;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		
		SQLiteDatabase db = helper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case LOCATIONS:
		{
			long rowId = db.replace(Schema.Locations.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return LOCATIONS_URI;
			}
		}
		break;
		case FAVORITES:
		{
			long rowId = db.replace(Schema.Favorites.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return FAVORITES_URI;
			}
		}
		break;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	@Override
	public boolean onCreate() {
		helper = new FavoritesDatabaseHelper(getContext());
		return true;
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case LOCATIONS:
			builder.setTables(Schema.Locations.table);
			break;
		case FAVORITES:
			builder.setTables(Schema.Favorites.table);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);
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
		case LOCATIONS:
			count = db.update(Schema.Locations.table, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}


}
