package boston.Bus.Map.data;

import java.util.ArrayList;

public class MultipleStopLocations implements LocationGroup {
	private final ArrayList<StopLocation> stops = new ArrayList<StopLocation>(1);

	public void addStop(StopLocation stop) {
		stops.add(stop);
	}
	
	public ArrayList<StopLocation> getStops() {
		return stops;
	}
}
