package boston.Bus.Map.util;

import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.latransit.R;

/**
 * Created by schneg on 1/11/15.
 */
public class SelectionResources {
    public static int getDrawable(Selection.Mode mode) {
        if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL) {
            return R.drawable.bus_all;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
            return R.drawable.busstop;
        }
        else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ONE) {
            return R.drawable.bus_one;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_ALL) {
            return R.drawable.busstop_all;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
            return R.drawable.busstop_star;
        }
        else {
            throw new RuntimeException("Unknown mode");
        }
    }
    public static int getText(Selection.Mode mode) {
        if (mode == Selection.Mode.VEHICLE_LOCATIONS_ALL) {
            return R.string.all_buses;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_ONE) {
            return R.string.stops_and_predictions_on_one_route;
        }
        else if (mode == Selection.Mode.VEHICLE_LOCATIONS_ONE) {
            return R.string.vehicles_on_one_route;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_ALL) {
            return R.string.stops_and_predictions_on_all_routes;
        }
        else if (mode == Selection.Mode.BUS_PREDICTIONS_STAR) {
            return R.string.favorite_stops;
        }
        else {
            throw new RuntimeException("Unknown mode");
        }
    }
}
