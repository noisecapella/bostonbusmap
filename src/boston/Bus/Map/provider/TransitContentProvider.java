package boston.Bus.Map.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.schneeloch.suffixarray.ObjectWithString;
import com.schneeloch.suffixarray.SuffixArray;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashSet;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopLocationGroup;
import boston.Bus.Map.data.prepopulated.CommuterRailPrepopulatedData;
import boston.Bus.Map.data.prepopulated.NextbusPrepopulatedData;
import boston.Bus.Map.data.prepopulated.SubwayPrepopulatedData;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class TransitContentProvider extends SearchRecentSuggestionsProvider {

	private UriMatcher matcher;

	public static final String AUTHORITY = "com.bostonbusmap.transitprovider";
	public static final int MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;
	
	
	private static final int SUGGESTIONS_CODE = 5;
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public TransitContentProvider()
	{
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_CODE);
		setupSuggestions(AUTHORITY, MODE);
	}
	
	@Override
	public boolean onCreate() {
		boolean create = super.onCreate();
		return create;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
		int code = matcher.match(uri);
		switch (code)
		{
		case SUGGESTIONS_CODE:
			if (selectionArgs == null || selectionArgs.length == 0 || selectionArgs[0].trim().length() == 0)
			{
				return super.query(uri, projection, selection, selectionArgs, sortOrder);
			}
			else
			{
				return makeCursorForSearch(selectionArgs[0]);
			}
		default:
			return super.query(uri, projection, selection, selectionArgs, sortOrder);
		}
	}

	private Cursor makeCursorForSearch(String search) {
		String[] columns = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_TEXT_2};
		MatrixCursor ret = new MatrixCursor(columns);

		try {
			addSearchRoutes(search, ret);
			addSearchStops(search, ret);
		} catch (IOException e) {
			LogUtil.e(e);
		}


		return ret;
	}

	private void addSearchRoutes(String search, MatrixCursor ret) throws IOException {
		int count = 0;
		SuffixArray<RouteConfig> routeSuffixArray = RoutePool.getRouteSuffixArray();
		if (routeSuffixArray != null) {
			for (RouteConfig route : routeSuffixArray.search(search)) {
				ret.addRow(new Object[] {count, route.getRouteTitle(), "route " + route.getRouteName(), "Route"});
				count++;
			}
		}
	}

	private void addSearchStops(String search, MatrixCursor ret) throws IOException {
		SuffixArray<StopLocation> stopSuffixArray = RoutePool.getStopSuffixArray();
		MyHashSet<StopLocationGroup> set = new MyHashSet<StopLocationGroup>();
		ArrayList<StopLocationGroup> ordered = new ArrayList<StopLocationGroup>();

		if (stopSuffixArray != null) {
			for (StopLocation stop : stopSuffixArray.search(search)) {
				if (set.contains(stop) == false) { 
					set.add(stop);
					ordered.add(stop);
				}
				if (set.size() >= 30) {
					break;
				}
			}
			
			int count = 0;
			for (StopLocationGroup group : ordered) {
				List<String> routes = group.getAllRoutes();
				String allRoutes;
				if (routes.size() == 1) {
					allRoutes = "Stop on route " + routes.get(0);
				}
				else
				{
					allRoutes = "Stop on routes " + StringUtil.join(routes, ", ");
				}
				
				ret.addRow(new Object[]{count, group.getFirstTitle(), "stop " + group.getFirstStopTag(), allRoutes});
				count++;
			}
		}
	}
}
