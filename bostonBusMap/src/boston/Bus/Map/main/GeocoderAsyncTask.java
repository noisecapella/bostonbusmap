package boston.Bus.Map.main;

import java.io.IOException;
import java.util.List;

import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Constants;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ContextMenu;
import android.widget.Toast;

/**
 * Translate an address or other string into a lat/lon
 * @author schneg
 *
 */
public class GeocoderAsyncTask extends AsyncTask<Object, String, List<Address>> {
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
	protected List<Address> doInBackground(Object... params) {
		Geocoder geocoder = new Geocoder(context);
		try
		{
			List<Address> addresses = geocoder.getFromLocationName(query, 5, TransitSystem.lowerLeftLat,
						TransitSystem.lowerLeftLon, TransitSystem.upperRightLat, TransitSystem.upperRightLon);
			if (addresses.size() < 1)
			{
				publishProgress("No results found for \"" + query + "\"");
			}
			else
			{
				return addresses;
			}
		}
		catch (IOException e)
		{
			//this is not important enough to risk crashing the phone
			Log.e("BostonBusMap", e.getMessage());
			
			publishProgress("An unknown error occurred while searching for \"" + query + "\"");
		}
		catch (IllegalStateException e)
		{
			//this is not important enough to risk crashing the phone
			Log.e("BostonBusMap", e.getMessage());
			
			publishProgress("An unknown error occurred while searching for \"" + query + "\"");
		}
		return null;
	}

	@Override
	protected void onPostExecute(final List<Address> addresses) {
		if (addresses != null)
		{
			if (addresses.size() == 1)
			{
				//just go to the place
				Address address = addresses.get(0);
				Toast.makeText(context, "Found one result: " + makeResultText(address), Toast.LENGTH_LONG).show();
				zoomToAddress(address);
			}
			else
			{
				final String[] items = new String[addresses.size()];
				for (int i = 0; i < items.length; i++)
				{
					items[i] = makeResultText(addresses.get(i));
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Pick an address");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Address address = addresses.get(item);
						zoomToAddress(address);
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	private void zoomToAddress(Address address)
	{
		double lat = address.getLatitude();
		double lon = address.getLongitude();
		GeoPoint point = new GeoPoint((int)(lat * Constants.E6), (int)(lon * Constants.E6));
		mapView.getController().animateTo(point);
	}
	
	private String makeResultText(Address address) {
		String ret = address.getAddressLine(0);
		if (ret == null)
		{
			return "(no description)";
		}
		
		String city = address.getLocality();
		if (city != null)
		{
			ret += ", " + city;
		}
		
		return ret;
	}
}
