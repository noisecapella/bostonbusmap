package com.schneeloch.bostonbusmap_library.data;

import com.schneeloch.bostonbusmap_library.database.Schema;

import com.google.common.collect.ImmutableSet;

import javax.xml.transform.Source;

public class CommuterRailStopLocation extends SubwayStopLocation {

	protected CommuterRailStopLocation(CommuterRailBuilder build) {
		super(build);
	}

	public static class CommuterRailBuilder extends SubwayBuilder {
		public CommuterRailBuilder(float latitudeAsDegrees,
				float longitudeAsDegrees, String tag,
				String title) {
			super (latitudeAsDegrees, longitudeAsDegrees, tag,
					title);
		}
		
		@Override
		public CommuterRailStopLocation build() {
			return new CommuterRailStopLocation(this);
		}
	}
	
	@Override
	public Schema.Routes.SourceId getTransitSourceType() {
		return Schema.Routes.SourceId.CommuterRail;
	}
}
