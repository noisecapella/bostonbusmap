package boston.Bus.Map.main;


import android.content.Context;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.UpdateArguments;

import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import com.google.common.collect.ImmutableMap;

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
}
