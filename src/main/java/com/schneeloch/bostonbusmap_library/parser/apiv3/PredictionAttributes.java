package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 12/15/17.
 */

public class PredictionAttributes {
    @SerializedName("departure_time")
    public Timestamp departure_time;
    @SerializedName("arrival_time")
    public Timestamp arrival_time;
    @SerializedName("track")
    public String track;
    @SerializedName("status")
    public String status;
    @SerializedName("direction_id")
    public int direction_id;
}
