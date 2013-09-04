package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

import boston.Bus.Map.database.Schema;
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
	public Drawable getDrawable(TransitSystem transitSystem) {
		// stops all look the same, and they can support multiple transit sources
		// so we'll just use the default transit source's drawables
		TransitDrawables drawables = transitSystem.getTransitSource("Hubway").getDrawables();
		return recentlyUpdated ? drawables.getStopUpdated() : drawables.getStop();
	}

	@Override
	public boolean supportsBusPredictionsAllMode() {
		return true;
	}
}
