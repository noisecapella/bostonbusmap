package boston.Bus.Map.main;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.TextView;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;

import boston.Bus.Map.ui.RouteOverlay;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

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
	private int selectedRouteIndex;
	private int selectedBusPredictions;
	private final BusOverlay busOverlay;
	private final RouteOverlay routeOverlay;
	private final LocationOverlay myLocationOverlay;
	private final UpdateAsyncTask majorHandler;
	
	public CurrentState(TextView textView,
			Locations busLocations, double lastUpdateTime, boolean updateConstantly,
			int selectedRouteIndex, int selectedBusPredictions, BusOverlay busOverlay, RouteOverlay routeOverlay,
			LocationOverlay myLocationOverlay,
			UpdateAsyncTask majorHandler) 
	{
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
		this.selectedRouteIndex = selectedRouteIndex;
		this.selectedBusPredictions = selectedBusPredictions;
		this.busOverlay = busOverlay;
		this.routeOverlay = routeOverlay;
		this.myLocationOverlay = myLocationOverlay;
		this.majorHandler = majorHandler;
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

	public BusOverlay getBusOverlay() {
		return busOverlay;
	}

	/**
	 * It's probably unnecessary to clone a new object for this
	 * @param context
	 * @param mapView
	 * @return
	 */
	public BusOverlay cloneBusOverlay(Main context, MapView mapView, HashMap<String, String> routeKeysToTitles) {
		BusOverlay ret = new BusOverlay(busOverlay, context, mapView, routeKeysToTitles);
		
		return ret;
	}

	public int getSelectedRouteIndex()
	{
		return selectedRouteIndex;
	}
	
	public int getSelectedBusPredictions() {
		return selectedBusPredictions;
	}

	public UpdateAsyncTask getMajorHandler() {
		return majorHandler;
	}

	/**
	 * It's probably unnecessary to clone here 
	 * @param projection
	 * @return
	 */
	public RouteOverlay cloneRouteOverlay(Projection projection) {
		RouteOverlay ret = new RouteOverlay(routeOverlay, projection);
		
		return ret;
	}
	
}
