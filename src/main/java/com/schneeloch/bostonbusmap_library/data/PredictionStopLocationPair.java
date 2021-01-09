package com.schneeloch.bostonbusmap_library.data;

/**
 * Created by schneg on 8/31/13.
 */
public class PredictionStopLocationPair {
    public final IPrediction prediction;
    public final StopLocation stopLocation;

    public PredictionStopLocationPair(IPrediction prediction, StopLocation stopLocation) {
        if (prediction == null) {
            throw new IllegalArgumentException();
        }
        if (stopLocation == null) {
            throw new IllegalArgumentException();
        }
        this.prediction = prediction;
        this.stopLocation = stopLocation;
    }
}
