package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	public CommuterRailStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, Drawable busStop, String tag,
			String title, int platformOrder, String branch) {
		super(latitudeAsDegrees, longitudeAsDegrees, busStop, tag, title,
				platformOrder, branch);
	}

	@Override
	public boolean isBeta() {
		return true;
	}
}
