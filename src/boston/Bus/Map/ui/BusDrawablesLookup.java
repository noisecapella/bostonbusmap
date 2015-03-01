package boston.Bus.Map.ui;

import com.schneeloch.latransit.R;

/**
 * Created by schneg on 1/13/15.
 */
public class BusDrawablesLookup {
    public static int getIdFromAngle(int angle, boolean isSelected, boolean isRail) {
        angle += (360 / BusDrawables.bus_selectedLookup.length) / 2;
        angle = angle % 360;

        if (!isRail) {
            if (angle < 0 || angle >= 360) {
                return R.drawable.bus_statelist_0;
            }
            return BusDrawables.bus_selectedLookup[angle / 8];
        }
        else {
            if (angle < 0 || angle >= 360) {
                return R.drawable.rail_statelist_0;
            }
            return BusDrawables.rail_selectedLookup[angle / 8];
        }
    }

}
