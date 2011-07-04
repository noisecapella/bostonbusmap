package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class SubwayStopLocation extends StopLocation {

	
	/**
	 * The order of this stop compared to other stops. Optional, used only for subways
	 */
	private int platformOrder;
	
	/**
	 * What branch this subway is on. Optional, only used for subways
	 */
	private String branch;

	/**
	 * Mark that these predictions are experimental
	 */
	private boolean isBeta;
	
	public SubwayStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, Drawable busStop, String tag,
			String title, int platformOrder, String branch, boolean isBeta)
	{
		super(latitudeAsDegrees, longitudeAsDegrees, busStop, tag, title);
		
		this.platformOrder = platformOrder;
		this.branch = branch;
		this.isBeta = isBeta;

	}
	

	public int getPlatformOrder() {
		return platformOrder;
	}

	public String getBranch() {
		return branch;
	}

	@Override
	public boolean isBeta()
	{
		return isBeta;
	}
}
