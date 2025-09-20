package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 1/7/18.
 */

public class Resource {
    @SerializedName("type")
    public String type;
    @SerializedName("id")
    public String id;
    @SerializedName("relationships")
    public Relationships relationships;
    @SerializedName("predictionAttributes")
    public PredictionAttributes predictionAttributes;
    @SerializedName("vehicleAttributes")
    public VehicleAttributes vehicleAttributes;
    @SerializedName("tripAttributes")
    public TripAttributes tripAttributes;
}
