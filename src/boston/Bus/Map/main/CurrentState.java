package boston.Bus.Map.main;


import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.RouteOverlay;

import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

/**
 * Stores state when MainActivity pauses temporarily
 * @author schneg
 *
 */
public class CurrentState {
	private final long lastUpdateTime;
	private final int updateConstantlyInterval;
	private int selectedRouteIndex;
	private int selectedBusPredictions;
	private final boolean progressState;
	private final boolean locationEnabled;
	
	private final UpdateArguments updateArguments;
	
	public CurrentState(UpdateArguments updateArguments, long lastUpdateTime, int updateConstantlyInterval,
			int selectedRouteIndex, int selectedBusPredictions,
			boolean progressState, boolean locationEnabled) 
	{
		this.updateArguments = updateArguments.cloneMe();
		
		this.lastUpdateTime = lastUpdateTime;
		this.updateConstantlyInterval = updateConstantlyInterval;
		this.selectedRouteIndex = selectedRouteIndex;
		this.selectedBusPredictions = selectedBusPredictions;
		this.progressState = progressState;
		this.locationEnabled = locationEnabled;
	}

	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public void restoreWidgets()
	{
	}

	public boolean getProgressState()
	{
		return progressState;
	}
	
	public int getUpdateConstantlyInterval() {
		return updateConstantlyInterval;
	}

	/**
	 * It's probably unnecessary to clone a new object for this
	 * @param context
	 * @param mapView
	 * @return
	 */
	public BusOverlay cloneBusOverlay(Main context, MapView mapView, MyHashMap<String, String> routeKeysToTitles)
	{
		BusOverlay ret = new BusOverlay(updateArguments.getBusOverlay(), context, mapView, routeKeysToTitles);
		
		return ret;
	}

	public int getSelectedRouteIndex()
	{
		return selectedRouteIndex;
	}
	
	public int getSelectedBusPredictions() {
		return selectedBusPredictions;
	}

	public UpdateArguments getUpdateArguments() {
		return updateArguments;
	}

	/**
	 * It's probably unnecessary to clone here 
	 * @param projection
	 * @return
	 */
	public RouteOverlay cloneRouteOverlay(Projection projection) {
		RouteOverlay ret = new RouteOverlay(updateArguments.getRouteOverlay(), projection);
		
		return ret;
	}

	public boolean getLocationEnabled() {
		return locationEnabled;
	}
	
}
