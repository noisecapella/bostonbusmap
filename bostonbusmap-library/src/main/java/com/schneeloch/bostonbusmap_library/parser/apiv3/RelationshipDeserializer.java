package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by schneg on 1/7/18.
 */

public class RelationshipDeserializer implements JsonDeserializer<Relationship> {
    @Override
    public Relationship deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Relationship relationship = new Relationship();
        if (json.isJsonNull()) {
            return relationship;
        }

        JsonElement data = json.getAsJsonObject().get("data");
        if (data.isJsonNull()) {
            return relationship;
        }
        relationship.id = data.getAsJsonObject().get("id").getAsString();
        return relationship;
    }
}
