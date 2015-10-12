package boston.Bus.Map.ui;

import com.schneeloch.latransit.R;

/**
 * Created by schneg on 1/13/15.
 */
public class BusDrawablesLookup {
    public static int getIdFromAngle(int angle, boolean isSelected, boolean isRail) {
        angle += (360 / BusDrawables.busSelectedLookup.length) / 2;
        angle = angle % 360;

        if (isSelected) {
            if (!isRail) {
                if (angle < 0 || angle >= 360) {
                    return R.drawable.bus_selected_0;
                }
                return BusDrawables.busSelectedLookup[angle / 8];
            } else {
                if (angle < 0 || angle >= 360) {
                    return R.drawable.rail_selected_0;
                }
                return BusDrawables.railSelectedLookup[angle / 8];
            }
        } else {
            if (!isRail) {
                if (angle < 0 || angle >= 360) {
                    return R.drawable.bus_0;
                }
                return BusDrawables.busLookup[angle / 8];
            } else {
                if (angle < 0 || angle >= 360) {
                    return R.drawable.rail_selected_0;
                }
                return BusDrawables.railLookup[angle / 8];
            }
        }
    }

}
