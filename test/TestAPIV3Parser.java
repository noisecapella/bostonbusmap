import android.content.ContentResolver;
import android.text.Html;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import boston.Bus.Map.provider.DatabaseAgent;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.test.TestingDownloader;
import com.schneeloch.bostonbusmap_library.test.TestingTransitDrawables;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.MbtaV3TransitSource;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

import boston.Bus.Map.BuildConfig;

import static junit.framework.Assert.assertEquals;

/**
 * Created by schneg on 12/17/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants=BuildConfig.class)
public class TestAPIV3Parser {

    @Test
    public void testPredictions() throws Exception {
        LogUtil.print = true;
        Now.useFakeTime = true;
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-07T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        ITransitSystem transitSystem = new TransitSystem(new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2043,2050,2044,2049,8816,8297,8817,8815,8296,88171,2042,2048,2051,2046,8178,8818,1452,2047,2052,1432,900,8179,8295,8284,8177,8180,1451,1433,2040,8819,8298,2054,1450,1434,8294,8181,8820,8176,8175,8182,1449,1435,17711,989,902,7711,2106,2107,2128,2127,2056,2126,2108,14481,2104,2129,2038,2125,8174,8339,8183,8293,1900,77110,77051,988,8173,2057,2110,2103,1448,2124,2130,8292,1436&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream
        )));


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RoutePool routePool = locations.getRoutePool();
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // watertown square
        double lat = 42.3709;
        double lon = -71.1828;
        ImmutableList<ImmutableList<Location>> groups = locations.getLocations(
                75,
                lat,
                lon,
                true,
                selection
        );

        VehicleLocations busMapping = new VehicleLocations();
        Directions directions = locations.getDirections();
        RouteConfig routeConfig = null;
        transitSystem.refreshData(routeConfig, selection, 75, lat, lon, busMapping, routePool, directions, locations);
        // watertown square
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("8178");
        StopLocation stop = group.get("8178");

        // prepare
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("\nRoute 71, Vehicle y0603\n" +
                "Watertown Square\n" +
                "Arriving in 60 min\n" +
                "\n" +
                "Route 71\n" +
                "Harvard\n" +
                "Arriving in 66 min\n" +
                "\n" +
                "Route 71\n" +
                "Watertown Square\n" +
                "Arriving in 78 min", Html.fromHtml(stop.getPredictionView().getSnippet()).toString());
    }
}
