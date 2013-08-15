package boston.Bus.Map.data;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import boston.Bus.Map.database.Schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import android.graphics.drawable.Drawable;

public class SubwayStopLocation extends StopLocation {

	
	protected SubwayStopLocation(SubwayBuilder builder)
	{
		super(builder);
	}
	
	public static class SubwayBuilder extends Builder {
		public SubwayBuilder(float latitudeAsDegrees,
				float longitudeAsDegrees, String tag,
				String title) {
			super(latitudeAsDegrees, longitudeAsDegrees, tag, title);
		}
		
		@Override
		public SubwayStopLocation build() {
			return new SubwayStopLocation(this);
		}
	}

	@Override
	public int getTransitSourceType() {
		return Schema.Routes.enumagencyidSubway;
	}
}
