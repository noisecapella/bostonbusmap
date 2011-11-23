package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	public CommuterRailStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, Drawable busStop, Drawable busStopUpdated, String tag,
			String title, int platformOrder, String branch) {
		super(latitudeAsDegrees, longitudeAsDegrees, busStop, busStopUpdated, tag, title,
				platformOrder, branch);
	}

	@Override
	public boolean isBeta() {
		return true;
	}
}
