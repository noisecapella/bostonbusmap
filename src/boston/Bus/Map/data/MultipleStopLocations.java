package boston.Bus.Map.data;

import java.util.ArrayList;

public class MultipleStopLocations implements LocationGroup {
	private final ArrayList<StopLocation> stops = new ArrayList<StopLocation>(1);
	public MultipleStopLocations(StopLocation stop1, StopLocation stop2) {
		stops.add(stop1);
		stops.add(stop2);
	}

	public void addStop(StopLocation stop) {
		stops.add(stop);
	}
	
	public ArrayList<StopLocation> getStops() {
		return stops;
	}

	@Override
	public float getLatitudeAsDegrees() {
		return stops.get(0).getLatitudeAsDegrees();
	}

	@Override
	public float getLongitudeAsDegrees() {
		return stops.get(0).getLongitudeAsDegrees();
	}
}
