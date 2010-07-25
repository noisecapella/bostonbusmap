package boston.Bus.Map.database;

import java.util.ArrayList;
import java.util.HashMap;

import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.Point;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.os.Debug;
import android.util.Log;

/**
 * Handles the database which stores route information
 * 
 * @author schneg
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String dbName = "bostonBusMap";
	 
	private final static String directionsTable = "directions";
	private final static String stopsTable = "stops";
	private final static String routesTable = "routes";
	private final static String pathsTable = "paths";
	
	private final static String routeKey = "route";
	private final static String stopIdKey = "stopId";
	private final static String tagKey = "tag";
	private final static String latitudeKey = "lat";
	private final static String longitudeKey = "lon";
	private final static String titleKey = "title";
	private final static String dirtagKey = "dirtag";
	private final static String nameKey = "name";
	private final static String pathIdKey = "pathid";

	private final static int tagIndex = 1;
	private final static int nameIndex = 2;
	private final static int titleIndex = 3;
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, 4);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + routeKey + " STRING, " + tagKey + " STRING, "
				+ nameKey + " STRING, " + titleKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + stopsTable + " (" + routeKey + " STRING, " +
				stopIdKey + " INTEGER PRIMARY KEY, " + latitudeKey + " FLOAT, " + longitudeKey +
				" FLOAT, " + titleKey + " STRING, " + dirtagKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + routesTable + " (" + routeKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + pathsTable + " (" + pathIdKey + " INTEGER, "
				 + routeKey + " STRING, "
				+ latitudeKey + " FLOAT, " + longitudeKey + " FLOAT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
		db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
		db.execSQL("DROP TABLE IF EXISTS " + routesTable);
		db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
		
		onCreate(db);
	}

	public String[] getRoutes()
	{
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		ArrayList<String> routes = new ArrayList<String>();
		try
		{
			cursor = database.query(routesTable, new String[] {routeKey}, null, null, null, null, null);
			cursor.moveToFirst();
			
			while (cursor.isAfterLast() == false)
			{
				String route = cursor.getString(0);
				routes.add(route);
				
				cursor.moveToNext();
			}
		}
		finally
		{
			database.close();
			if (cursor != null)
			{
				cursor.close();
			}
		}
	
		return routes.toArray(new String[0]);
	}
	
	public void populateMap(HashMap<String, RouteConfig> map, Drawable busStop, String[] routes) {

		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			//Debug.startMethodTracing("database");
			for (String route : routes)
			{
				RouteConfig routeConfig = new RouteConfig(route);

				
				
				cursor = database.query(directionsTable, new String[] {routeKey, tagKey, nameKey, titleKey},
						routeKey + "=?", new String[]{route}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					String tag = cursor.getString(tagIndex);
					String name = cursor.getString(nameIndex);
					String title = cursor.getString(titleIndex);
					routeConfig.addDirection(tag, title, name);

					cursor.moveToNext();
				}
				cursor.close();
				
				cursor = database.query(stopsTable, new String[] {routeKey, stopIdKey, latitudeKey, longitudeKey, titleKey, dirtagKey}, 
						routeKey + "=?", new String[]{route}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					int stopId = cursor.getInt(1);
					float lat = cursor.getFloat(2);
					float lon = cursor.getFloat(3);
					String title = cursor.getString(4);
					String dirtag = cursor.getString(5);

					StopLocation location = new StopLocation(lat, lon, busStop, stopId, title, dirtag, routeConfig);

					routeConfig.addStop(stopId, location);
					cursor.moveToNext();
				}
				cursor.close();
				
				cursor = database.query(pathsTable, new String[] {pathIdKey, routeKey, latitudeKey, longitudeKey},
						routeKey + "=?", new String[]{route}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					int pathId = cursor.getInt(0);
					float lat = cursor.getFloat(2);
					float lon = cursor.getFloat(3);
					
					routeConfig.addPath(pathId, lat, lon);
					cursor.moveToNext();
				}
				cursor.close();
				
				
				map.put(route, routeConfig);
			}
			//Debug.stopMethodTracing();
		}
		finally
		{
			database.close();
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}

	public void saveRoutes(String[] routes)
	{
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
		
			try
			{
				database.beginTransaction();
				
				database.delete(routesTable, null, null);
				
				ContentValues values = new ContentValues();
				for (String route : routes)
				{
					values.put(routeKey, route);
					database.insert(routesTable, null, values);
				}
				
				database.setTransactionSuccessful();
				database.endTransaction();
			}
			finally
			{
				database.close();
			}
		}
		
	}
	
	public void saveMapping(HashMap<String, RouteConfig> mapping, boolean wipe)
	{
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
			try
			{
				database.beginTransaction();
			
				if (wipe)
				{
					database.delete(stopsTable, null, null);
					database.delete(directionsTable, null, null);
					database.delete(pathsTable, null, null);
				}
				
				for (String route : mapping.keySet())
				{
					saveMappingKernel(database, route, mapping.get(route), wipe);
				}
				
				database.setTransactionSuccessful();
				database.endTransaction();
				
			}
			finally
			{
				database.close();
			}
		}
		
	}
	
	/**
	 * 
	 * @param database
	 * @param route
	 * @param routeConfig
	 * @param useInsert insert all rows, don't replace them. I assume this is faster since there's no lookup involved
	 */
	private void saveMappingKernel(SQLiteDatabase database, String route, RouteConfig routeConfig, boolean useInsert)
	{
		for (StopLocation location : routeConfig.getStops())
		{
			ContentValues values = new ContentValues();
			int stopId = location.getStopNumber();
			values.put(routeKey, route);
			values.put(stopIdKey, stopId);
			values.put(latitudeKey, (float)location.getLatitudeAsDegrees());
			values.put(longitudeKey, (float)location.getLongitudeAsDegrees());
			values.put(titleKey, location.getTitle());
			values.put(dirtagKey, location.getDirtag());

			if (useInsert)
			{
				database.insert(stopsTable, null, values);
			}
			else
			{
				database.replace(stopsTable, null, values);
			}
		}

		for (String dirtag : routeConfig.getDirtags())
		{
			ContentValues values = new ContentValues();
			values.put(routeKey, route);
			values.put(tagKey, dirtag);
			values.put(nameKey, routeConfig.getDirectionName(dirtag));
			values.put(titleKey, routeConfig.getDirectionTitle(dirtag));
			
			if (useInsert)
			{
				database.insert(directionsTable, null, values);
			}
			else
			{
				database.replace(directionsTable, null, values);
			}
		}

		for (Integer pathId : routeConfig.getPaths().keySet())
		{
			Path path = routeConfig.getPaths().get(pathId);
			for (Point point : path.getPoints())
			{
				ContentValues values = new ContentValues();
				values.put(pathIdKey, pathId);
				values.put(routeKey, route);
				values.put(latitudeKey, point.lat);
				values.put(longitudeKey, point.lon);
				
				if (useInsert)
				{
					database.insert(pathsTable, null, values);
				}
				else
				{
					database.replace(pathsTable, null, values);
				}
			}
		}
	}


}
