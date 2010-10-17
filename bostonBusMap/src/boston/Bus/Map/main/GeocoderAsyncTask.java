package boston.Bus.Map.main;

import java.io.IOException;
import java.util.List;

import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Constants;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Translate an address or other string into a lat/lon
 * @author schneg
 *
 */
public class GeocoderAsyncTask extends AsyncTask<Object, String, GeoPoint> {
	private final Context context;
	private final MapView mapView;
	private final String query;
	
	private final double currentLat;
	private final double currentLon;
	
	public GeocoderAsyncTask(Context context, MapView mapView, String query) {
		this.context = context;
		this.mapView = mapView;
		this.query = query;
		
		GeoPoint geoPoint = mapView.getMapCenter();
		currentLat = geoPoint.getLatitudeE6() / (float)Constants.E6;
		currentLon = geoPoint.getLongitudeE6() / (float)Constants.E6;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected GeoPoint doInBackground(Object... params) {
		Geocoder geocoder = new Geocoder(context);
		try
		{
			List<Address> addresses = geocoder.getFromLocationName(query, 1, TransitSystem.lowerLeftLat,
						TransitSystem.lowerLeftLon, TransitSystem.upperRightLat, TransitSystem.upperRightLon);
			if (addresses.size() < 1)
			{
				publishProgress("No results found for \"" + query + "\"");
			}
			else
			{
				Address address = addresses.get(0);
				double lon = address.getLongitude();
				double lat = address.getLatitude();
				
				String name = address.getAddressLine(0);
				if (name == null)
				{
					name = "null";
				}
				publishProgress("Found a result: " + name);
				
				GeoPoint point = new GeoPoint((int)lat * Constants.E6, (int)lon * Constants.E6);
				return point;
			}
		}
		catch (IOException e)
		{
			//this is not important enough to risk crashing the phone
			Log.e("BostonBusMap", e.getMessage());
			
			publishProgress("An unknown error occurred while searching for \"" + query + "\"");
		}
		return null;
	}

	@Override
	protected void onPostExecute(GeoPoint point) {
		if (point != null)
		{
			mapView.getController().animateTo(point);
		}
	}
}
