package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class TransitDrawables {
	private final Drawable stop;
	private final Drawable stopUpdated;
	
	public TransitDrawables(Drawable stop, Drawable stopUpdated, Drawable vehicle,
			Drawable arrow) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
	}

	public Drawable getStop() {
		return stop;
	}

	public Drawable getStopUpdated() {
		return stopUpdated;
	}
}
