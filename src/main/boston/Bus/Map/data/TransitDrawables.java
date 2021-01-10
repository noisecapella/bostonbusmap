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
    private final int intersectionSelected;
    private final int stop;
    private final int stopSelected;
    private final int stopUpdated;
	private final int stopUpdatedSelected;

	public TransitDrawables(int intersection, int intersectionSelected, int stop, int stopSelected,
                            int stopUpdated, int stopUpdatedSelected) {
		this.stop = stop;
        this.stopSelected = stopSelected;
		this.stopUpdated = stopUpdated;
        this.stopUpdatedSelected = stopUpdatedSelected;
		this.intersection = intersection;
        this.intersectionSelected = intersectionSelected;
	}

    @Override
    public int getBitmapDescriptor(Location location, boolean isSelected) {
        LocationType locationType = location.getLocationType();
        boolean isUpdated = location.isUpdated();
        if (locationType == LocationType.Intersection) {
            if (isSelected) {
                return intersectionSelected;
            }
            else {
                return intersection;
            }
        }
        else if (locationType == LocationType.Stop) {
            if (isSelected) {
                if (isUpdated) {
                    return stopUpdatedSelected;
                }
                else {
                    return stopSelected;
                }
            }
            else {
                if (isUpdated) {
                    return stopUpdated;
                } else {
                    return stop;
                }
            }
        }
        else if (locationType == LocationType.Vehicle) {
            boolean isRail = true;
            if (location.getTransitSourceType() == Schema.Routes.SourceId.Bus) {
                isRail = false;
            }
            return BusDrawablesLookup.getIdFromAngle(location.getHeading(), isSelected, isRail);
        }
        else {
            throw new RuntimeException("Unexpected location type");
        }
    }
}
