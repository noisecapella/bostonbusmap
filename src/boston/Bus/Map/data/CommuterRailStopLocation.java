package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	public CommuterRailStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, TransitDrawables drawables, String tag,
			String title, int platformOrder, String branch) {
		super(latitudeAsDegrees, longitudeAsDegrees, drawables, tag, title,
				platformOrder, branch);
	}

	@Override
	public boolean isBeta() {
		return true;
	}
}
