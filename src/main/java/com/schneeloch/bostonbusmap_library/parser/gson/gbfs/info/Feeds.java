package com.schneeloch.bostonbusmap_library.parser.gson.gbfs.info;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Feeds {
    public Feeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    @SerializedName("feeds")
    public List<Feed> feeds;
}
