package boston.Bus.Map;

import java.util.List;

import android.widget.TextView;

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
	private final List<Overlay> overlays;
	
	public CurrentState(TextView textView, MapView mapView,
			Locations busLocations, double lastUpdateTime) {
		if (textView == null)
		{
			textViewStatus = "";
		}
		else
		{
			textViewStatus = textView.getText();
		}
		if (mapView == null)
		{
			overlays = null;
		}
		else
		{
			overlays = mapView.getOverlays();
		}
		this.busLocations = busLocations;
		this.lastUpdateTime = lastUpdateTime;
	}

	public double getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public Locations getBusLocations()
	{
		return busLocations;
	}
	
	public void restoreWidgets(TextView textView, MapView mapView)
	{
		if (textView != null && textViewStatus.length() != 0)
		{
			textView.setText(textViewStatus);
		}
		
		if (mapView != null && overlays != null)
		{
			for (Overlay overlay : overlays)
			{
				mapView.getOverlays().add(overlay);
			}
		}
	}
}
