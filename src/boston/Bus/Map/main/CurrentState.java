package boston.Bus.Map.main;


import android.content.Context;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.OverlayGroup;
import boston.Bus.Map.ui.RouteOverlay;

import com.google.common.collect.ImmutableMap;

import org.osmdroid.views.MapView;

/**
 * Stores state when MainActivity pauses temporarily
 * @author schneg
 *
 */
public class CurrentState {
	private final long lastUpdateTime;
	private final int updateConstantlyInterval;
	private final boolean progressState;
	private final boolean locationEnabled;
	
	private final UpdateArguments updateArguments;
	
	public CurrentState(UpdateArguments updateArguments, long lastUpdateTime, int updateConstantlyInterval,
			boolean progressState, boolean locationEnabled) 
	{
		this.updateArguments = updateArguments.cloneMe();
		
		this.lastUpdateTime = lastUpdateTime;
		this.updateConstantlyInterval = updateConstantlyInterval;
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

	public UpdateArguments getUpdateArguments() {
		return updateArguments;
	}

	public boolean getLocationEnabled() {
		return locationEnabled;
	}

	/**
	 * Might be unnecessary to clone, but there shouldn't be a big performance penalty
	 * @param context
	 * @param mapView
	 * @param dropdownRouteKeysToTitles
	 * @return
	 */
	public OverlayGroup cloneOverlays(Main context, MapView mapView,
			RouteTitles dropdownRouteKeysToTitles) {
		return updateArguments.getOverlayGroup().cloneOverlays(context, mapView, dropdownRouteKeysToTitles);
	}
	
}
