package boston.Bus.Map.data;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import boston.Bus.Map.ui.BusDrawable;

import com.google.common.collect.Maps;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;

/**
 * Drawables for a particular TransitSource
 * @author schneg
 *
 */
public class TransitDrawables {
	private final Drawable intersection;
	private final Drawable arrow;
	private final int arrowTop;
	private final Drawable stop;
	private final Drawable stopUpdated;
	private final Drawable vehicle;
	
	private final SparseArray<Drawable> vehicles = new SparseArray<Drawable>();
	
	public TransitDrawables(Drawable stop, Drawable stopUpdated, Drawable vehicle,
			Drawable arrow, int arrowTop, Drawable intersection) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
		this.vehicle = vehicle;
		this.arrow = arrow;
		this.arrowTop = arrowTop;
		this.intersection = intersection;
	}

	public Drawable getVehicle(int heading) {
		Drawable drawable = vehicles.get(heading);
		if (drawable == null) {
			drawable = new BusDrawable(vehicle, heading, arrow, arrowTop);
			vehicles.put(heading, drawable);
		}
		return drawable;
	}

	public Drawable getStop() {
		return stop;
	}

	public Drawable getStopUpdated() {
		return stopUpdated;
	}

	public Drawable getIntersection() {
		return intersection;
	}
}
