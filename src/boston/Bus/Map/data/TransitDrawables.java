package boston.Bus.Map.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.LocationType;
import com.schneeloch.bostonbusmap_library.database.Schema;

import boston.Bus.Map.ui.BusDrawablesLookup;

/**
 * Drawables for a particular TransitSource
 * @author schneg
 *
 */
public class TransitDrawables implements ITransitDrawables {
	private final Context context;
	private final Drawable intersection;
	private final Drawable arrow;
	private final int arrowTopDiff;
	private final Drawable stop;
	private final Drawable stopUpdated;
	private final Drawable vehicle;

    public static ITransitDrawables busDrawables;
    public static ITransitDrawables commuterRailDrawables;
    public static ITransitDrawables hubwayDrawables;
    public static ITransitDrawables subwayDrawables;

	private final SparseArray<Drawable> vehicles = new SparseArray<Drawable>();
	
	private static final int[][] validStates = new int[][] {
		new int[]{android.R.attr.state_focused}, new int[0]
	};
	
	public TransitDrawables(Context context, Drawable stop, Drawable stopUpdated, Drawable vehicle,
			Drawable arrow, int arrowTop, Drawable intersection) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
		this.vehicle = vehicle;
		this.arrow = arrow;
		this.arrowTopDiff = arrowTop;
		this.intersection = intersection;
		this.context = context;
	}

    @Override
    public Object getBitmapDescriptor(Location location, boolean isSelected) {
        LocationType locationType = location.getLocationType();
        boolean isUpdated = location.isUpdated();
        if (locationType == LocationType.Intersection) {
            return intersection;
        }
        else if (locationType == LocationType.Stop) {
            if (isUpdated) {
                return stopUpdated;
            }
            else {
                return stop;
            }
        }
        else if (locationType == LocationType.Vehicle) {
            boolean isRail = true;
            if (location.getTransitSourceType() == Schema.Routes.SourceId.Bus) {
                isRail = false;
            }
            return context.getResources().getDrawable(BusDrawablesLookup.getIdFromAngle(location.getHeading(), false, isRail));
        }
        else {
            throw new RuntimeException("Unexpected location type");
        }
    }
}
