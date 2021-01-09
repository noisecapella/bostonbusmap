package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Optional;
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
				String title, Optional<String> parent) {
			super(latitudeAsDegrees, longitudeAsDegrees, tag, title, parent);
		}
		
		@Override
		public SubwayStopLocation build() {
			return new SubwayStopLocation(this);
		}
	}

	@Override
	public boolean supportsBusPredictionsAllMode() {
		return true;
	}
}
