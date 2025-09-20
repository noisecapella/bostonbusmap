package com.schneeloch.bostonbusmap_library.parser.gson.gbfs.info;

import com.google.gson.annotations.SerializedName;

public class Feed {
    @SerializedName("name")
    public String name;
    @SerializedName("url")
    public String url;
}
