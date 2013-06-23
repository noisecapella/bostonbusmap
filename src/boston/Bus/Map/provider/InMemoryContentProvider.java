package boston.Bus.Map.provider;

import boston.Bus.Map.database.Schema;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class InMemoryContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.bostonbusmap.inmemoryprovider";
	private static final String PREDICTIONS_TYPE = "vnd.android.cursor.dir/vnd.bostonbusmap.prediction";
	public static final Uri PREDICTIONS_URI = Uri.parse("content://" + AUTHORITY + "/predictions");
	private static final int PREDICTIONS = 1;

	private final UriMatcher uriMatcher;
	
	private InMemoryHelper helper;
	public InMemoryContentProvider() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "predictions", PREDICTIONS);
	}
	
	private static class InMemoryHelper extends SQLiteOpenHelper {
		public InMemoryHelper(Context context) {
			super(context, null, null, DatabaseContentProvider.CURRENT_DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Schema.Predictions.createSql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(Schema.Predictions.dropSql);
			onCreate(db);
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case PREDICTIONS:
			count = db.delete(Schema.Predictions.table, selection, selectionArgs);
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
		case PREDICTIONS:
			return PREDICTIONS_TYPE;

		}
		throw new IllegalArgumentException("Unknown uri: " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int count;
		switch (uriMatcher.match(uri)) {
		case PREDICTIONS:
			break;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		
		SQLiteDatabase db = helper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case PREDICTIONS:
		{
			long rowId = db.replace(Schema.Predictions.table, null, values);
			if (rowId >= 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return PREDICTIONS_URI;
			}
		}
		break;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		helper = new InMemoryHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case PREDICTIONS:
			builder.setTables(Schema.Predictions.table);
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
		case PREDICTIONS:
			count = db.update(Schema.Predictions.table, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
