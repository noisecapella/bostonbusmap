package com.schneeloch.bostonbusmap_library.data;

import com.schneeloch.bostonbusmap_library.database.Schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
	public Schema.Routes.SourceId getTransitSourceType() {
		return Schema.Routes.SourceId.Subway;
	}

	@Override
	public boolean supportsBusPredictionsAllMode() {
		return false;
	}
}
