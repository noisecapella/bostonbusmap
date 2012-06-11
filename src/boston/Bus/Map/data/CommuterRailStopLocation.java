package boston.Bus.Map.data;

import boston.Bus.Map.transit.TransitSource;
import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	public CommuterRailStopLocation(float latitudeAsDegrees,
			float longitudeAsDegrees, TransitSource transitSource, String tag,
			String title, int platformOrder, String branch) {
		super(latitudeAsDegrees, longitudeAsDegrees, transitSource, tag, title,
				platformOrder, branch);
	}

	@Override
	public boolean isBeta() {
		return true;
	}
}
