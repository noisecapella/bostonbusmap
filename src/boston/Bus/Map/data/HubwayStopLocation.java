package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

import boston.Bus.Map.database.Schema;
import boston.Bus.Map.transit.ITransitSystem;
import boston.Bus.Map.transit.TransitSystem;

/**
 * Created by schneg on 9/1/13.
 */
public class HubwayStopLocation extends StopLocation {

	protected HubwayStopLocation(HubwayBuilder build) {
		super(build);
	}

	public static class HubwayBuilder extends Builder {
		public HubwayBuilder(float latitudeAsDegrees,
								   float longitudeAsDegrees, String tag,
								   String title) {
			super (latitudeAsDegrees, longitudeAsDegrees, tag,
					title);
		}

		@Override
		public HubwayStopLocation build() {
			return new HubwayStopLocation(this);
		}
	}

	@Override
	public int getTransitSourceType() {
		return Schema.Routes.enumagencyidHubway;
	}

	@Override
	public boolean supportsBusPredictionsAllMode() {
		return true;
	}
}
