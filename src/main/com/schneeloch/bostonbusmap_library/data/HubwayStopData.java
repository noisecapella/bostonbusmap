package com.schneeloch.bostonbusmap_library.data;

/**
 * Created by georgeandroid on 11/7/15.
 */
public class HubwayStopData {

    public final String tag;
    public final String id;
    public final float latitude;
    public final float longitude;
    public final String name;
    public final String numberBikes;
    public final String numberEmptyDocks;
    public final boolean locked;
    public final boolean installed;

    public HubwayStopData(String tag, String id, float latitude, float longitude, String name, String numberBikes, String numberEmptyDocks, boolean locked, boolean installed) {
        this.tag = tag;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.numberBikes = numberBikes;
        this.numberEmptyDocks = numberEmptyDocks;
        this.locked = locked;
        this.installed = installed;
    }
}
