package boston.Bus.Map.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import boston.Bus.Map.data.Path;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.Box;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.os.Debug;
import android.os.Parcel;
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
	private final static String blobsTable = "blobs";
	
	
	private final static String routeKey = "route";
	private final static String stopIdKey = "stopId";
	private final static String tagKey = "tag";
	private final static String latitudeKey = "lat";
	private final static String longitudeKey = "lon";
	private final static String titleKey = "title";
	private final static String dirtagKey = "dirtag";
	private final static String nameKey = "name";
	private final static String pathIdKey = "pathid";
	private final static String blobKey = "blob";

	private final static int tagIndex = 1;
	private final static int nameIndex = 2;
	private final static int titleIndex = 3;
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, 5);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*db.execSQL("CREATE TABLE IF NOT EXISTS " + directionsTable + " (" + routeKey + " STRING, " + tagKey + " STRING, "
				+ nameKey + " STRING, " + titleKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + stopsTable + " (" + routeKey + " STRING, " +
				stopIdKey + " INTEGER PRIMARY KEY, " + latitudeKey + " FLOAT, " + longitudeKey +
				" FLOAT, " + titleKey + " STRING, " + dirtagKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + routesTable + " (" + routeKey + " STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + pathsTable + " (" + pathIdKey + " INTEGER, "
				 + routeKey + " STRING, "
				+ latitudeKey + " FLOAT, " + longitudeKey + " FLOAT)");*/
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + blobsTable + " (" + routeKey + " STRING, " + blobKey + " BLOB)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + directionsTable);
		db.execSQL("DROP TABLE IF EXISTS " + stopsTable);
		db.execSQL("DROP TABLE IF EXISTS " + routesTable);
		db.execSQL("DROP TABLE IF EXISTS " + pathsTable);
		db.execSQL("DROP TABLE IF EXISTS " + blobsTable);
		
		onCreate(db);
	}

	public void populateMap(HashMap<String, RouteConfig> map, Drawable busStop, String[] routes) throws IOException {

		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = null;
		try
		{
			for (String route : routes)
			{
				cursor = database.query(blobsTable, new String[] {routeKey, blobKey},
						routeKey + "=?", new String[]{route}, null, null, null);
				cursor.moveToFirst();
				while (cursor.isAfterLast() == false)
				{
					byte[] blob = cursor.getBlob(1);
					
					RouteConfig routeConfig = new RouteConfig(new Box(blob), busStop);


					map.put(route, routeConfig);
				}
				cursor.close();
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

	public void saveMapping(HashMap<String, RouteConfig> mapping, boolean wipe) throws IOException
	{
		SQLiteDatabase database = getWritableDatabase();
		synchronized (database) {
			try
			{
				database.beginTransaction();
			
				if (wipe)
				{
					//database.delete(stopsTable, null, null);
					//database.delete(directionsTable, null, null);
					//database.delete(pathsTable, null, null);
					database.delete(blobsTable, null, null);
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
	 * @throws IOException 
	 */
	private void saveMappingKernel(SQLiteDatabase database, String route, RouteConfig routeConfig, boolean useInsert) throws IOException
	{
		Box box = new Box(null);
		
		routeConfig.serialize(box);
		
		byte[] blob = box.getBlob();
		
		ContentValues values = new ContentValues();
		values.put(routeKey, route);
		values.put(blobKey, blob);
			
			
		if (useInsert)
		{
			database.insert(blobsTable, null, values);
		}
		else
		{
			database.replace(blobsTable, null, values);
		}

	}


}
