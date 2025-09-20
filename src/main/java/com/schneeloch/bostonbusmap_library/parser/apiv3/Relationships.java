package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 12/15/17.
 */

public class Relationships {
    @SerializedName("stop")
    public Relationship stop;
    @SerializedName("route")
    public Relationship route;
    @SerializedName("vehicle")
    public Relationship vehicle;
    @SerializedName("trip")
    public Relationship trip;
}
