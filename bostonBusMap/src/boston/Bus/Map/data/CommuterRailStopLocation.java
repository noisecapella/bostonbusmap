package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends StopLocation {
	private final int latenessInSeconds;
	
	public CommuterRailStopLocation(float lat,
			float lon, Drawable busStop, String tag, String title, int latenessInSeconds)
	{
		super(lat, lon, busStop, tag, title);
		
		this.latenessInSeconds = latenessInSeconds;
	}

	
}
