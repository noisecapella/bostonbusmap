package boston.Bus.Map.data;

import com.schneeloch.mta.R;

import android.content.SharedPreferences;
import com.google.common.collect.ImmutableMap;


/**
 * Maintains selection state. 
 * @author schneg
 *
 */
public class Selection {
	public static enum Mode {
		VEHICLE_LOCATIONS_ALL(1, "vehicle_locations_all", R.drawable.bus_all, R.string.all_buses),
		BUS_PREDICTIONS_ONE(2, "vehicle_predictions_one", R.drawable.busstop, R.string.stops_and_predictions_on_one_route),
		VEHICLE_LOCATIONS_ONE(3, "vehicle_locations_one", R.drawable.bus_one, R.string.vehicles_on_one_route),
		BUS_PREDICTIONS_ALL(4, "vehicle_predictions_all", R.drawable.busstop_all, R.string.stops_and_predictions_on_all_routes),
		BUS_PREDICTIONS_STAR(5, "vehicle_predictions_star", R.drawable.busstop_star, R.string.favorite_stops);

		public final int modeInt;
		public final String modeString;
		public final int iconResource;
		public final int textResource;

		Mode(int modeInt, String modeString, int iconResource, int textResource) {
			this.modeInt = modeInt;
			this.modeString = modeString;
			this.iconResource = iconResource;
			this.textResource = textResource;
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
