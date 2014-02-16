package boston.Bus.Map.data;

import android.content.SharedPreferences;
import com.schneeloch.torontotransit.R;;

import com.google.common.collect.ImmutableMap;

/**
 * Maintains selection state. 
 * @author schneg
 *
 */
public class Selection {
	public static final int VEHICLE_LOCATIONS_ALL = 1;
	public static final int BUS_PREDICTIONS_ONE = 2;
	public static final int VEHICLE_LOCATIONS_ONE = 3;
	public static final int BUS_PREDICTIONS_ALL = 4;
	public static final int BUS_PREDICTIONS_STAR = 5;

	public static final String STRING_VEHICLE_LOCATIONS_ALL = "vehicle_locations_all";
	public static final String STRING_BUS_PREDICTIONS_ONE = "vehicle_predictions_one";
	public static final String STRING_VEHICLE_LOCATIONS_ONE = "vehicle_locations_one";
	public static final String STRING_BUS_PREDICTIONS_ALL = "vehicle_predictions_all";
	public static final String STRING_BUS_PREDICTIONS_STAR = "vehicle_predictions_star";

	public static final int[] modesSupported = new int[]{
		VEHICLE_LOCATIONS_ALL, VEHICLE_LOCATIONS_ONE, BUS_PREDICTIONS_ALL, 
		BUS_PREDICTIONS_ONE, BUS_PREDICTIONS_STAR
	};
	
	public static final int[] modeIconsSupported = new int[]{
		R.drawable.bus_all, R.drawable.bus_one, R.drawable.busstop_all, R.drawable.busstop, 
		R.drawable.busstop_star
		
	};
	
	public static final int[] modeTextSupported = new int[]{
		R.string.all_buses, R.string.vehicles_on_one_route, 
		R.string.stops_and_predictions_on_all_routes,
		R.string.stops_and_predictions_on_one_route, R.string.favorite_stops
		
	};

	public static final ImmutableMap<String, Integer> modeMap = ImmutableMap.of(
			STRING_VEHICLE_LOCATIONS_ALL, VEHICLE_LOCATIONS_ALL,
			STRING_VEHICLE_LOCATIONS_ONE, VEHICLE_LOCATIONS_ONE,
			STRING_BUS_PREDICTIONS_ALL, BUS_PREDICTIONS_ALL,
			STRING_BUS_PREDICTIONS_ONE, BUS_PREDICTIONS_ONE,
			STRING_BUS_PREDICTIONS_STAR, BUS_PREDICTIONS_STAR
	);

	private final int mode;
	private final String route;
	
	public Selection(int mode, String route) {
		this.mode = mode;
		this.route = route;
	}
	
	public int getMode() {
		return mode;
	}
	
	public String getRoute() {
		return route;
	}
	
	public Selection withDifferentRoute(String newRoute) {
		return new Selection(mode, newRoute);
	}
	
	public Selection withDifferentMode(int newMode) {
		return new Selection(newMode, route);
	}
	
	public Selection withDifferentModeAndRoute(int newMode, String newRoute) {
		return new Selection(newMode, newRoute);
	}
	
}
