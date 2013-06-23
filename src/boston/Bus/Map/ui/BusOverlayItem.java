package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.google.common.collect.ImmutableCollection;

public class BusOverlayItem extends OverlayItem
{
	private final Location location;
	private final ImmutableCollection<Alert> alerts;
	private boolean selected;
	private static final int[] zeroState = new int[]{};
	private static final int[] focusState = new int[]{android.R.attr.state_focused};
		
	public BusOverlayItem(GeoPoint point, String title, String snippet,
			ImmutableCollection<Alert> alerts, Location location)
	{
		super(point, title, snippet);
		this.alerts = alerts;
		this.location = location;
	}

	public Location getCurrentLocation()
	{
		return location;
	}

	public ImmutableCollection<Alert> getAlerts()
	{
		return alerts;
	}
	
	public void select(boolean value) {
		selected = value;
	}
	
	@Override
	public Drawable getMarker(int stateBitset) {
		Drawable drawable = super.getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK);
		if (drawable != null) {
			drawable.setState(selected ? focusState : zeroState);
		}
		return drawable;
	}
}
