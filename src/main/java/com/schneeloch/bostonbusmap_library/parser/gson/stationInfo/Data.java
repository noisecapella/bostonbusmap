package com.schneeloch.bostonbusmap_library.parser.gson.stationInfo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by schneg on 7/29/16.
 */
public class Data {
    @SerializedName("stations")
    public List<InfoStation> stations;

    public Data(List<InfoStation> stations) {
        this.stations = stations;
    }
}
