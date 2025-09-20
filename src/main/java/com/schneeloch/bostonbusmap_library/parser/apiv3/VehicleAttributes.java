package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;
import com.schneeloch.bostonbusmap_library.parser.apiv3.Timestamp;

/**
 * Created by schneg on 12/16/17.
 */

public class VehicleAttributes {
    @SerializedName("longitude")
    public float longitude;
    @SerializedName("latitude")
    public float latitude;
    @SerializedName("last_updated")
    public Timestamp last_updated;
    @SerializedName("label")
    public String label;
    @SerializedName("bearing")
    public float bearing;
    @SerializedName("headsign")
    public String headsign;
    @SerializedName("direction_id")
    public int direction_id;
    @SerializedName("current_status")
    public String current_status;
}
