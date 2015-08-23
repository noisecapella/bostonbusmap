package boston.Bus.Map.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.common.collect.ImmutableMap;
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
	private final int intersection;
	private final int stop;
	private final int stopUpdated;

	public TransitDrawables(int stop, int stopUpdated,
                            int intersection) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
		this.intersection = intersection;
	}

    @Override
    public BitmapDescriptor getBitmapDescriptor(Location location, boolean isSelected) {
        LocationType locationType = location.getLocationType();
        boolean isUpdated = location.isUpdated();
        if (locationType == LocationType.Intersection) {
            return BitmapDescriptorFactory.fromResource(intersection);
        }
        else if (locationType == LocationType.Stop) {
            if (isUpdated) {
                return BitmapDescriptorFactory.fromResource(stopUpdated);
            }
            else {
                return BitmapDescriptorFactory.fromResource(stop);
            }
        }
        else if (locationType == LocationType.Vehicle) {
            boolean isRail = true;
            if (location.getTransitSourceType() == Schema.Routes.SourceId.Bus) {
                isRail = false;
            }
            return BitmapDescriptorFactory.fromResource(BusDrawablesLookup.getIdFromAngle(location.getHeading(), false, isRail));
        }
        else {
            throw new RuntimeException("Unexpected location type");
        }
    }
}
