package boston.Bus.Map.data;

import android.content.SharedPreferences;
import boston.Bus.Map.R;

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
	public static final int BUS_PREDICTIONS_INTERSECT = 6;

	public static final int[] modesSupported = new int[]{
		VEHICLE_LOCATIONS_ALL, VEHICLE_LOCATIONS_ONE, BUS_PREDICTIONS_ALL, 
		BUS_PREDICTIONS_ONE, BUS_PREDICTIONS_STAR, BUS_PREDICTIONS_INTERSECT
	};
	
	public static final int[] modeIconsSupported = new int[]{
		R.drawable.bus_all, R.drawable.bus_one, R.drawable.busstop_all, R.drawable.busstop_one, 
		R.drawable.busstop_star, R.drawable.busstop_intersect
		
	};
	
	public static final int[] modeTextSupported = new int[]{
		R.string.all_buses, R.string.vehicles_on_one_route, 
		R.string.stops_and_predictions_on_all_routes,
		R.string.stops_and_predictions_on_one_route, R.string.favorite_stops,
		R.string.intersection_stops
		
	};
	
	private final int mode;
	private final String route;
	private final String intersection;
	
	public Selection(int mode, String route, String intersection) {
		this.mode = mode;
		this.route = route;
		this.intersection = intersection;
	}
	
	public int getMode() {
		return mode;
	}
	
	public String getRoute() {
		return route;
	}
	
	public String getIntersection() {
		return intersection;
	}

	public Selection withDifferentRoute(String newRoute) {
		return new Selection(mode, newRoute, intersection);
	}
	
	public Selection withDifferentMode(int newMode) {
		return new Selection(newMode, route, intersection);
	}
	
	public Selection withDifferentModeAndRoute(int newMode, String newRoute) {
		return new Selection(newMode, newRoute, intersection);
	}
	
	public Selection withDifferentIntersection(String newIntersection) {
		return new Selection(mode, route, newIntersection);
	}

}
