package boston.Bus.Map.data;

import boston.Bus.Map.database.Schema;

import com.google.common.collect.ImmutableSet;

import android.graphics.drawable.Drawable;

public class CommuterRailStopLocation extends SubwayStopLocation {

	protected CommuterRailStopLocation(CommuterRailBuilder build) {
		super(build);
	}

	public static class CommuterRailBuilder extends SubwayBuilder {
		public CommuterRailBuilder(float latitudeAsDegrees,
				float longitudeAsDegrees, String tag,
				String title, int platformOrder, String branch) {
			super (latitudeAsDegrees, longitudeAsDegrees, tag,
					title, platformOrder, branch);
		}
		
		@Override
		public CommuterRailStopLocation build() {
			return new CommuterRailStopLocation(this);
		}
	}
	
	@Override
	public int getTransitSourceType() {
		return Schema.Routes.enumagencyidCommuterRail;
	}
}
