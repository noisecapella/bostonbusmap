package com.schneeloch.bostonbusmap_library.parser.gson.stationStatus;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by schneg on 7/29/16.
 */
public class Data {
    @SerializedName("stations")
    public List<StatusStation> stations;

    public Data(List<StatusStation> stations) {
        this.stations = stations;
    }
}
