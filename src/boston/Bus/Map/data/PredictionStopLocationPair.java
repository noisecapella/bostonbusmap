package boston.Bus.Map.data;

/**
 * Created by schneg on 8/31/13.
 */
public class PredictionStopLocationPair {
    public final IPrediction prediction;
    public final StopLocation stopLocation;

    public PredictionStopLocationPair(IPrediction prediction, StopLocation stopLocation) {
        this.prediction = prediction;
        this.stopLocation = stopLocation;
    }
}
