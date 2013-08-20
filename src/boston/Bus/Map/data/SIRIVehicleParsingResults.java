package boston.Bus.Map.data;

import java.util.List;

import com.google.common.collect.Lists;

public class SIRIVehicleParsingResults {
	public final List<PredictionStopLocationPair> pairs;
	public final IAlerts alerts;
	public final List<BusLocation> busLocations;
	
	public SIRIVehicleParsingResults(List<PredictionStopLocationPair> pairs, IAlerts alerts,
			List<BusLocation> busLocations) {
		this.pairs = pairs;
		this.alerts = alerts;
		this.busLocations = busLocations;
	}
}
