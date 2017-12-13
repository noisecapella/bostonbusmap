package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;

import com.schneeloch.bostonbusmap_library.database.Schema;

public class SubwayTrainLocation extends BusLocation {

	public SubwayTrainLocation(float latitude, float longitude, String id,
                               long lastFeedUpdateInMillis, Optional<Integer> heading,
                               String routeName, String headsign) {
		super(latitude, longitude, id, lastFeedUpdateInMillis,
				heading, routeName, headsign);
	}
	
	@Override
	protected String getBusNumberMessage() {
		return "Train number: " + busId + "<br />\n";
	}

	public Schema.Routes.SourceId getVehicleSourceId() {
		return Schema.Routes.SourceId.Subway;
	}
}
