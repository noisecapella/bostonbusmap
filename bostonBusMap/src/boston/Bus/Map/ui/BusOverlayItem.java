package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.HashMap;

import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BusOverlayItem extends OverlayItem
{
	private Location location;
	private final ArrayList<Alert> alerts;
		
	public BusOverlayItem(GeoPoint point, String title, String snippet, ArrayList<Alert> alerts)
	{
		super(point, title, snippet);
		this.alerts = alerts;
	}

	public void setCurrentLocation(Location location)
	{
		this.location = location;
	}

	public Location getCurrentLocation()
	{
		return location;
	}

	public ArrayList<Alert> getAlerts()
	{
		return alerts;
	}
	
}
