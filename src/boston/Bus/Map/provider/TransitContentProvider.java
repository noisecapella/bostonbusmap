package boston.Bus.Map.provider;

import java.io.IOException;

import com.schneeloch.suffixarray.ObjectWithString;
import com.schneeloch.suffixarray.SuffixArray;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.prepopulated.CommuterRailPrepopulatedData;
import boston.Bus.Map.data.prepopulated.NextbusPrepopulatedData;
import boston.Bus.Map.data.prepopulated.SubwayPrepopulatedData;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.FakeTransitSource;
import boston.Bus.Map.util.LogUtil;
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
	
	private SuffixArray routeSuffixArray;
	private SuffixArray stopSuffixArray;
	
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
		if (routeSuffixArray == null) {
			routeSuffixArray = new SuffixArray(true);
			FakeTransitSource fake = new FakeTransitSource();
			Directions directions = new Directions();
			for (RouteConfig route : new NextbusPrepopulatedData(fake).getAllRoutes(directions)) {
				routeSuffixArray.add(route);
			}
			for (RouteConfig route : new SubwayPrepopulatedData(fake).getAllRoutes(directions)) {
				routeSuffixArray.add(route);
			}
			for (RouteConfig route : new CommuterRailPrepopulatedData(fake).getAllRoutes(directions)) {
				routeSuffixArray.add(route);
			}
		}

		int count = 0;
		for (ObjectWithString objectWithString : routeSuffixArray.search(search)) {
			RouteConfig route = (RouteConfig)objectWithString;
			ret.addRow(new Object[] {count, route.getRouteTitle(), "route " + route.getRouteName(), "Route"});
			count++;
		}
	}

	private void addSearchStops(String search, MatrixCursor ret) throws IOException {
		if (stopSuffixArray == null) {
			stopSuffixArray = new SuffixArray(true);
			FakeTransitSource fake = new FakeTransitSource();
			Directions directions = new Directions();
			for (RouteConfig route : new NextbusPrepopulatedData(fake).getAllRoutes(directions)) {
				for (StopLocation stop : route.getStops()) {
					stopSuffixArray.add(stop);
				}
			}
			for (RouteConfig route : new SubwayPrepopulatedData(fake).getAllRoutes(directions)) {
				for (StopLocation stop : route.getStops()) {
					stopSuffixArray.add(stop);
				}
			}
			for (RouteConfig route : new CommuterRailPrepopulatedData(fake).getAllRoutes(directions)) {
				for (StopLocation stop : route.getStops()) {
					stopSuffixArray.add(stop);
				}
			}
		}
		
		int count = 0;
		for (ObjectWithString objectWithString : stopSuffixArray.search(search)) {
			StopLocation stop = (StopLocation)objectWithString;
			ret.addRow(new Object[]{count, stop.getTitle(), "stop " + stop.getStopTag(), "Stop on route " + stop.getFirstRoute()});
			count++;
		}
	}
}
