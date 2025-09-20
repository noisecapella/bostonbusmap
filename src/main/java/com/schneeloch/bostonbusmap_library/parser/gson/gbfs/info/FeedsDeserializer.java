package com.schneeloch.bostonbusmap_library.parser.gson.gbfs.info;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class FeedsDeserializer implements JsonDeserializer<Feeds> {

    @Override
    public Feeds deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }

        JsonElement feedsArray = json.getAsJsonObject().get("feeds");
        List<Feed> feeds = Lists.newArrayList();
        for (JsonElement feedObject : feedsArray.getAsJsonArray()) {
            Feed feed = context.deserialize(feedObject, Feed.class);
            feeds.add(feed);
        }
        return new Feeds(feeds);
    }
}
