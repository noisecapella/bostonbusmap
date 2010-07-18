package boston.Bus.Map.main;

import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.TextView;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.ui.BusOverlay;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Stores state when MainActivity pauses temporarily
 * @author schneg
 *
 */
public class CurrentState {
	private final CharSequence textViewStatus;
	private final double lastUpdateTime;
	private final Locations busLocations;
	private final boolean updateConstantly;
	private int currentRoutesSupportedIndex;
	private final BusOverlay busOverlay;
	
	public CurrentState(TextView textView,
			Locations busLocations, double lastUpdateTime, boolean updateConstantly,
			int currentRoutesSupportedIndex, BusOverlay busOverlay) {
		if (textView == null)
		{
			textViewStatus = "";
		}
		else
		{
			textViewStatus = textView.getText();
		}
		this.busLocations = busLocations;
		this.lastUpdateTime = lastUpdateTime;
		this.updateConstantly = updateConstantly;
		this.currentRoutesSupportedIndex = currentRoutesSupportedIndex;
		this.busOverlay = busOverlay;
	}

	public double getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public Locations getBusLocations()
	{
		return busLocations;
	}
	
	public void restoreWidgets(TextView textView)
	{
		if (textView != null && textViewStatus.length() != 0)
		{
			textView.setText(textViewStatus);
		}
	}

	public boolean getUpdateConstantly() {
		return updateConstantly;
	}

	public int getCurrentRoutesSupportedIndex() {
		return currentRoutesSupportedIndex;
	}

	public BusOverlay getBusOverlay() {
		return busOverlay;
	}

	public BusOverlay cloneBusOverlay(Context context, MapView mapView) {
		BusOverlay ret = new BusOverlay(busOverlay, context, mapView);
		
		
		
		return ret;
	}
}
