package com.schneeloch.bostonbusmap_library.parser.gson.stationStatus;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 7/29/16.
 */
public class StatusStation {
    @SerializedName("capacity")
    public int capacity;
    @SerializedName("station_id")
    public String station_id;
    @SerializedName("num_bikes_available")
    public int num_bikes_available;
    @SerializedName("num_bikes_disabled")
    public int num_bikes_disabled;
    @SerializedName("num_docks_available")
    public int num_docks_available;
    @SerializedName("num_docks_disabled")
    public int num_docks_disabled;
    @SerializedName("is_installed")
    public int is_installed;
    @SerializedName("is_renting")
    public int is_renting;
    @SerializedName("is_returning")
    public int is_returning;
}
