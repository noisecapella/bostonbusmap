package boston.Bus.Map.provider;

import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.TransitSystem;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TransitContentProvider extends SearchRecentSuggestionsProvider {

	private TransitSystem transit;
	private UriMatcher matcher;
	private DatabaseHelper helper;

	public static final String AUTHORITY = "com.bostonbusmap.transitprovider";
	public static final int MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;
	
	
	private static final int ROUTES_CODE = 1;
	private static final int DIRECTIONS_CODE = 3;
	private static final int DIRECTION_ID_CODE = 4;
	private static final int SUGGESTIONS_CODE = 5;
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public TransitContentProvider()
	{
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		/*matcher.addURI(AUTHORITY, "routes", ROUTES_CODE);
		matcher.addURI(AUTHORITY, "directions", DIRECTIONS_CODE);
		matcher.addURI(AUTHORITY, "directions/#", DIRECTION_ID_CODE);*/
		
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_CODE);
		setupSuggestions(AUTHORITY, MODE);
	}
	
	@Override
	public boolean onCreate() {
		boolean create = super.onCreate();
		helper = new DatabaseHelper(this.getContext());
		return create;
	}
	
	/*
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		//ignore
		return 0;
	}
*/
	@Override
	public String getType(Uri uri) {
		int code = matcher.match(uri);
		switch (code)
		{
		case ROUTES_CODE:
			return "vnd.android.cursor.dir/vnd.bostonbusmap.route";
		case DIRECTION_ID_CODE:
			return "vnd.android.cursor.item/vnd.bostonbusmap.direction";
		case DIRECTIONS_CODE:
			return "vnd.android.cursor.dir/vnd.bostonbusmap.direction";
		default:
			//throw new IllegalArgumentException("Unsupported URI: " + uri);
			return super.getType(uri);
		}
	}
/*
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
*/
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
		int code = matcher.match(uri);
		switch (code)
		{
		case ROUTES_CODE:
			 return helper.getCursorForRoutes();
		case DIRECTIONS_CODE:
			return helper.getCursorForDirections();
		case DIRECTION_ID_CODE:
			String dirTag = uri.getPathSegments().get(1);
			return helper.getCursorForDirection(dirTag);
		case SUGGESTIONS_CODE:
			return helper.getCursorForRoutes(selectionArgs != null && selectionArgs.length >= 1 ? selectionArgs[0] : null);
		default:
			return super.query(uri, projection, selection, selectionArgs, sortOrder);
		}
	}
/*
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}*/
/*
	@Override
	public boolean onCreate() {
		helper = new DatabaseHelper(getContext());
		return true;
	}*/
}
