package boston.Bus.Map.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import boston.Bus.Map.data.PredictionStopLocationPair;

/**
 * Created by schneg on 8/17/14.
 */
public class MbtaPredictionsFeedParser {
    private final static String KEY = "wX9NwuHnZU2ToO7GmGR9uw";

    public void runParse(InputStreamReader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader, 2048);

        JsonElement root = new JsonParser().parse(bufferedReader);
        List<PredictionStopLocationPair> pairs = parseTree(root.getAsJsonObject());


    }

    private void parseTree(JSONObject object) {
        // parse http://realtime.mbta.com/developer/api/v2/vehiclesbyroute?api_key=wX9NwuHnZU2ToO7GmGR9uw&route=01
        // then
    }
}
