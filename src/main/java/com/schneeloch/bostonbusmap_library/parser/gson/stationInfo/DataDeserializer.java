package com.schneeloch.bostonbusmap_library.parser.gson.stationInfo;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class DataDeserializer implements JsonDeserializer<Data> {

    @Override
    public Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        JsonElement stationsArray = json.getAsJsonObject().get("stations");
        List<InfoStation> stations = Lists.newArrayList();
        for (JsonElement stationObject : stationsArray.getAsJsonArray()) {
            InfoStation station = context.deserialize(stationObject, InfoStation.class);
            stations.add(station);
        }
        return new Data(stations);
    }
}
