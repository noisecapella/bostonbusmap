package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableMap;

/**
 * Maintains selection state. 
 * @author schneg
 *
 */
public class Selection {
	public static enum Mode {
		VEHICLE_LOCATIONS_ALL(1, "vehicle_locations_all"),
		BUS_PREDICTIONS_ONE(2, "vehicle_predictions_one"),
		VEHICLE_LOCATIONS_ONE(3, "vehicle_locations_one"),
		BUS_PREDICTIONS_ALL(4, "vehicle_predictions_all"),
		BUS_PREDICTIONS_STAR(5, "vehicle_predictions_star");

		public final int modeInt;
		public final String modeString;

		Mode(int modeInt, String modeString) {
			this.modeInt = modeInt;
			this.modeString = modeString;
		}
	}

	public static final Mode[] modesSupported = new Mode[] {
			Mode.VEHICLE_LOCATIONS_ALL,
			Mode.VEHICLE_LOCATIONS_ONE,
			Mode.BUS_PREDICTIONS_ALL,
			Mode.BUS_PREDICTIONS_ONE,
			Mode.BUS_PREDICTIONS_STAR
	};

	private final Mode mode;
	private final String route;
	
	public Selection(Mode mode, String route) {
		this.mode = mode;
		this.route = route;
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public String getRoute() {
		return route;
	}
	
	public Selection withDifferentRoute(String newRoute) {
		return new Selection(mode, newRoute);
	}
	
	public Selection withDifferentMode(Mode newMode) {
		return new Selection(newMode, route);
	}
	
	public Selection withDifferentModeAndRoute(Mode newMode, String newRoute) {
		return new Selection(newMode, newRoute);
	}
	
}
