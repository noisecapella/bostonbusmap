package boston.Bus.Map.data;

/**
 * Convenience class to group these two items together
 * @author schneg
 *
 */
public class PredictionStopLocationPair {
	public final IPrediction prediction;
	public final StopLocation stopLocation;
	
	public PredictionStopLocationPair(IPrediction prediction, StopLocation stopLocation) {
		this.prediction = prediction;
		this.stopLocation = stopLocation;
	}

}
