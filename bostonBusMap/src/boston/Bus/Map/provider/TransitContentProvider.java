package boston.Bus.Map.provider;

import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.TransitSystem;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TransitContentProvider extends ContentProvider {

	private TransitSystem transit;
	private UriMatcher matcher;
	private DatabaseHelper helper;

	private static final String providerName = "com.bostonbusmap.transitprovider";
	
	private static final int ROUTES_CODE = 1;
	private static final int ROUTE_ID_CODE = 2;
	private static final int DIRECTIONS_CODE = 3;
	private static final int DIRECTION_ID_CODE = 4;
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + providerName);
	
	public TransitContentProvider()
	{
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(providerName, "routes", ROUTES_CODE);
		matcher.addURI(providerName, "routes/#", ROUTE_ID_CODE);
		matcher.addURI(providerName, "directions", DIRECTIONS_CODE);
		matcher.addURI(providerName, "directions/#", DIRECTION_ID_CODE);
		
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		//ignore
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		int code = matcher.match(uri);
		switch (code)
		{
		case ROUTE_ID_CODE:
			return "vnd.android.cursor.item/vnd.bostonbusmap.route";
		case ROUTES_CODE:
			return "vnd.android.cursor.dir/vnd.bostonbusmap.route";
		case DIRECTION_ID_CODE:
			return "vnd.android.cursor.item/vnd.bostonbusmap.direction";
		case DIRECTIONS_CODE:
			return "vnd.android.cursor.dir/vnd.bostonbusmap.direction";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		helper = new DatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		int code = matcher.match(uri);
		Cursor cursor;
		switch (code)
		{
		case ROUTES_CODE:
			 cursor = helper.getCursorForRoutes();
			 break;
		case ROUTE_ID_CODE:
			//path segment 1 should be the route name
			String routeName = uri.getPathSegments().get(1); 
			cursor = helper.getCursorForRoute(routeName);
			break;
		case DIRECTIONS_CODE:
			cursor = helper.getCursorForDirections();
			break;
		case DIRECTION_ID_CODE:
			String dirTag = uri.getPathSegments().get(1);
			cursor = helper.getCursorForDirection(dirTag);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
