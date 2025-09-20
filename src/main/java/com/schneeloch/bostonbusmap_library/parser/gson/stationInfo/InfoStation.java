package com.schneeloch.bostonbusmap_library.parser.gson.stationInfo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 7/29/16.
 */
public class InfoStation {
    @SerializedName("station_id")
    public String station_id;
    @SerializedName("name")
    public String name;
    @SerializedName("short_name")
    public String short_name;
    @SerializedName("lat")
    public float lat;
    @SerializedName("lon")
    public float lon;
    @SerializedName("capacity")
    public int capacity;
}
