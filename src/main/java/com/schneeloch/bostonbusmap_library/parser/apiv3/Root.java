package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by schneg on 12/15/17.
 */

public class Root {
    @SerializedName("data")
    public List<Resource> data;
    @SerializedName("included")
    public List<Resource> included;

    public Root(List<Resource> data, List<Resource> included) {
        this.data = data;
        this.included = included;
    }
}
