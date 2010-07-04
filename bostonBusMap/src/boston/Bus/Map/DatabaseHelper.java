package boston.Bus.Map;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String dbName = "bostonBusMap";
	 

	public DatabaseHelper(Context context) {
		super(context, dbName, null, 3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS directions (route STRING, tag STRING, name STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS stops (route STRING, stopId INTEGER PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING, dirtag STRING)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS directions");
		db.execSQL("DROP TABLE IF EXISTS stops");
		onCreate(db);
	}

	public void populateMap(HashMap<String, RouteConfig> map, Drawable busStop, String[] routes) {

		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			for (String route : routes)
			{
				RouteConfig routeConfig = new RouteConfig(route);

				
				
				cursor = database.query("directions", new String[] {"route", "tag", "name"},
						"route=?", new String[]{route}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					String tag = cursor.getString(1);
					String name = cursor.getString(2);
					routeConfig.addDirection(tag, name);

					cursor.moveToNext();
				}
				cursor.close();
				
				cursor = database.query("stops", new String[] {"route", "stopId", "lat", "lon", "title", "dirtag"}, 
						"route=?", new String[]{route}, null, null, null);
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
				
				map.put(route, routeConfig);
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
	}

	public void saveMapping(String route, RouteConfig routeConfig) {
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
			try
			{

				database.beginTransaction();

				for (StopLocation location : routeConfig.getStops())
				{
					ContentValues values = new ContentValues();
					int stopId = location.getStopNumber();
					values.put("route", route);
					values.put("stopId", stopId);
					values.put("lat", (float)location.getLatitudeAsDegrees());
					values.put("lon", (float)location.getLongitudeAsDegrees());
					values.put("title", location.getTitle());
					values.put("dirtag", location.getDirtag());

					long numUpdated = database.replace("stops", null, values);
					if (numUpdated != 0)
					{
						//Log.i("NUMUPDATED", numUpdated + " ");
					}
					else
					{
						//Log.i("NUMUPDATED", numUpdated + " ");
					}
				}

				for (String dirtag : routeConfig.getDirtags())
				{
					ContentValues values = new ContentValues();
					values.put("route", route);
					values.put("tag", dirtag);
					values.put("name", routeConfig.getDirection(dirtag));
					//values.put("title", title);

					database.replace("directions", null, values);
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
}
