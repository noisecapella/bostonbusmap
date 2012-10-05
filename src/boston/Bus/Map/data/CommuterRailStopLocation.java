package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	protected CommuterRailStopLocation(CommuterRailBuilder build) {
		super(build);
	}

	public static class CommuterRailBuilder extends SubwayBuilder {
		public CommuterRailBuilder(float latitudeAsDegrees,
				float longitudeAsDegrees, TransitDrawables drawables, String tag,
				String title, int platformOrder, String branch) {
			super (latitudeAsDegrees, longitudeAsDegrees, drawables, tag,
					title, platformOrder, branch);
		}
	}
	
	@Override
	public boolean isBeta() {
		return true;
	}
}
