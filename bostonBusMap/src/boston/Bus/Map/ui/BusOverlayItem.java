package boston.Bus.Map.ui;

import java.util.HashMap;

import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BusOverlayItem extends OverlayItem
{
	private Location location;
		
	public BusOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}

	public void setCurrentLocation(Location location) {
		this.location = location;
	}

	public Location getCurrentLocation()
	{
		return location;
	}
	
}
