package boston.Bus.Map.data;

import java.util.List;

import com.google.common.collect.Lists;

public class SIRIVehicleParsingResults {
	public final List<PredictionStopLocationPair> pairs;
	public final IAlerts alerts;
	
	public SIRIVehicleParsingResults(List<PredictionStopLocationPair> pairs, IAlerts alerts) {
		this.pairs = pairs;
		this.alerts = alerts;
	}
}
