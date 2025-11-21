package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class RootDeserializer implements JsonDeserializer<Root> {
    @Override
    public Root deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }

        JsonElement dataArray = json.getAsJsonObject().get("data");
        List<Resource> dataResources = Lists.newArrayList();
        for (JsonElement dataObject : dataArray.getAsJsonArray()) {
            Resource resource = context.deserialize(dataObject, Resource.class);
            dataResources.add(resource);
        }

        JsonElement includedArray = json.getAsJsonObject().get("included");
        List<Resource> includedResources = Lists.newArrayList();
        if (includedArray != null) {
            for (JsonElement includedObject : includedArray.getAsJsonArray()) {
                Resource resource = context.deserialize(includedObject, Resource.class);
                includedResources.add(resource);
            }
        }

        return new Root(dataResources, includedResources);
    }
}
