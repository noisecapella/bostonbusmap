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
	
	public SubwayStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, TransitDrawables drawables, String tag,
			String title, int platformOrder, String branch)
	{
		super(latitudeAsDegrees, longitudeAsDegrees, drawables, tag, title);
		
		this.platformOrder = platformOrder;
		this.branch = branch;
	}
	

	public int getPlatformOrder() {
		return platformOrder;
	}

	public String getBranch() {
		return branch;
	}
}
