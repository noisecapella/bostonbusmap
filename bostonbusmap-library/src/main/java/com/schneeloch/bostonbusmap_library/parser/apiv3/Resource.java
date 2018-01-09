package com.schneeloch.bostonbusmap_library.parser.apiv3;

/**
 * Created by schneg on 1/7/18.
 */

public class Resource {
    public String type;
    public String id;
    public Relationships relationships;
    public PredictionAttributes predictionAttributes;
    public VehicleAttributes vehicleAttributes;
    public TripAttributes tripAttributes;
}
