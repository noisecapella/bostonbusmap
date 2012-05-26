package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class TransitDrawables {
	private final Drawable vehicle;
	private final Drawable arrow;
	private final Drawable stop;
	private final Drawable stopUpdated;
	
	public TransitDrawables(Drawable stop, Drawable stopUpdated, Drawable vehicle,
			Drawable arrow) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
		this.vehicle = vehicle;
		this.arrow = arrow;
	}

	public Drawable getVehicle() {
		return vehicle;
	}

	public Drawable getArrow() {
		return arrow;
	}

	public Drawable getStop() {
		return stop;
	}

	public Drawable getStopUpdated() {
		return stopUpdated;
	}
}
