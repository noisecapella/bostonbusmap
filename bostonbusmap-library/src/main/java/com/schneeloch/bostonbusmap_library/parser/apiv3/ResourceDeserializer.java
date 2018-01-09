package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by schneg on 1/7/18.
 */

public class ResourceDeserializer implements JsonDeserializer<Resource> {

    @Override
    public Resource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String type = object.get("type").getAsString();

        Resource resource = new Resource();
        resource.type = type;
        resource.id = object.get("id").getAsString();
        resource.relationships = context.deserialize(object.get("relationships"), Relationships.class);

        JsonElement attributesElement = object.get("attributes");
        if (attributesElement.isJsonNull()) {
            LogUtil.w("Unexpected null attributes for " + type);
        } else if (type.equals("prediction")) {
            resource.predictionAttributes = context.deserialize(attributesElement, PredictionAttributes.class);
        } else if (type.equals("trip")) {
            resource.tripAttributes = context.deserialize(attributesElement, TripAttributes.class);
        } else if (type.equals("vehicle")) {
            resource.vehicleAttributes = context.deserialize(attributesElement, VehicleAttributes.class);
        } else {
            LogUtil.w("Unexpected deserialization of resource for type " + type);
        }


        return resource;
    }
}
