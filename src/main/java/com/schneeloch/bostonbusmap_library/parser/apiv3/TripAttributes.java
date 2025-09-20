package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 1/7/18.
 */

public class TripAttributes {
    @SerializedName("headsign")
    public String headsign;
    @SerializedName("direction_id")
    public int direction_id;
    @SerializedName("wheelchair_accessible")
    public int wheelchair_accessible;
    @SerializedName("block_id")
    public String block_id;
    @SerializedName("arrival_time")
    public Timestamp arrival_time;
    @SerializedName("departure_time")
    public Timestamp departure_time;
}
