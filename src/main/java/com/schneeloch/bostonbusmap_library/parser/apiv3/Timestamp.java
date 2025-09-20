package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by schneg on 12/16/17.
 */

public class Timestamp {
    @SerializedName("millis")
    public final long millis;

    public Timestamp(long millis) {
        this.millis = millis;
    }
}
