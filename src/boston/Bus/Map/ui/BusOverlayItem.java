package boston.Bus.Map.ui;

import android.graphics.drawable.Drawable;
import com.schneeloch.bostonbusmap_library.data.Alert;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;

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
