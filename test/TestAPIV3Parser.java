import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.text.Html;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.data.Alert;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlertsFetcher;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.LocationType;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import boston.Bus.Map.provider.DatabaseAgent;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.test.TestingAlertsFetcher;
import com.schneeloch.bostonbusmap_library.test.TestingDownloader;
import com.schneeloch.bostonbusmap_library.test.TestingTransitDrawables;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.MbtaV3TransitSource;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.IDownloader;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import boston.Bus.Map.BuildConfig;

import static junit.framework.Assert.assertEquals;

/**
 * Created by schneg on 12/17/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants=BuildConfig.class)
public class TestAPIV3Parser {
    @Before
    public void setUp() {
        LogUtil.print = true;
        Now.useFakeTime = true;
    }


    @Test
    public void testPredictionsWatertown() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-07T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2043,2050,2044,2049,8816,8297,8817,8815,8296,88171,2042,2048,2051,2046,8178&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // watertown square
        double lat = 42.3709;
        double lon = -71.1828;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        // watertown square
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("8178");
        StopLocation stop = group.get("8178");

        // prepare
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("\nRoute 71, Vehicle 0603\n" +
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

        StringBuilder alertsText = new StringBuilder();
        for (Alert alert : stop.getPredictions().getPredictionView().getAlerts()) {
            alertsText.append(alert.getTitle() + "\n" + alert.getDescription() + "\n");
        }
        assertEquals(
                alertsText.toString(),
                "Route 71\n" +
                        "Minor Route 71 delay\n" +
                        "\n" +
                        "Route 71 experiencing minor delays due to traffic.\n"
        );
    }

    @Test
    public void testPredictionsWorcester() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-15T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions_worcester.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=7742,81684,81685,7741,7743,81683,7744,81682,81686,8189,81681,7740,8285,8169,81687,8188,7739,9492,8168,82851,7646,7629,7627,7647,17645,7630,7648,7626,8167,Newtonville,7645,7738,7631,81688,7649,76251,7580,7482,78513,8166,82853,8170,78512,8187,7802,78073,7752,7853,78071,77379,7852,7808,7806,7854,81651,7632,7644,78571,7625,7855,78531,7807,7650,8171,8186,7805,7856,7804,8192,8165,77378,77521,7624,78511,7654&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // albemarle field, newton
        double lat = 42.3580;
        double lon = -71.2155;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        // watertown square
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("8178");
        StopLocation stop = group.get("8178");

        // prepare
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("No information received yet for this stop", Html.fromHtml(stop.getPredictionView().getSnippet()).toString());

        stop = locations.getAllStopsAtStop("Newtonville").get("Newtonville");
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("\nRoute Framingham/Worcester Line, Vehicle 1827\n" +
                "Worcester\n" +
                "Arriving in 574 min\n" +
                "\n" +
                "Route Framingham/Worcester Line\n" +
                "Worcester\n" +
                "Arriving in 632 min", Html.fromHtml(stop.getPredictionView().getSnippet()).toString());
    }

    @Test
    public void testPredictionsProvidence() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-15T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions_providence.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2043,2050,2044,2049,8816,8297,8817,8815,8296,88171,2042,2048,2051,2046,8178,8818,1452,2047,2052,1432,900,8179,8295,8284,8177,8180,1451,1433,2040,8819,8298,2054,1450,1434,8294,8181,8820,8176,8175,8182,1449,1435,17711,989,902,7711,2106,2107,2128,2127,2056,2126,2108,14481,2104,2129,2038,2125,8174,8339,8183,8293,1900,77110,77051,988,8173,2057,2110,2103,1448,2124,2130,8292,1436&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // providence
        double lat = 41.8240;
        double lon = -71.4128;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("Providence");
        StopLocation stop = group.get("Providence");

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

    @Test
    public void testPredictionsSouthStation() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-15T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions_south_station.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2043,2050,2044,2049,8816,8297,8817,8815,8296,88171,2042,2048,2051,2046,8178,8818,1452,2047,2052,1432,900,8179,8295,8284,8177,8180,1451,1433,2040,8819,8298,2054,1450,1434,8294,8181,8820,8176,8175,8182,1449,1435,17711,989,902,7711,2106,2107,2128,2127,2056,2126,2108,14481,2104,2129,2038,2125,8174,8339,8183,8293,1900,77110,77051,988,8173,2057,2110,2103,1448,2124,2130,8292,1436&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // south station
        double lat = 42.352271;
        double lon = -71.055242;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("South Station");
        assertEquals(group.size(), 1);
        StopLocation stop = group.get("South Station");
        assertEquals(stop.getParent().isPresent(), false);

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

    @Test
    public void testPredictionsEmpty() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-07T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions_empty.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2043,2050,2044,2049,8816,8297,8817,8815,8296,88171,2042,2048,2051,2046,8178&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


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

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        // watertown square
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("8178");
        StopLocation stop = group.get("8178");

        // prepare
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("No predictions for this stop", Html.fromHtml(stop.getPredictionView().getSnippet()).toString());
    }

    @Test
    public void testVehicles() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-09T04:34:02+00:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/vehicles.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/vehicles?api_key=109fafba79a848e792e8e7c584f6d1f1&include=trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.VEHICLE_LOCATIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // watertown square
        double lat = 42.3709;
        double lon = -71.1828;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        VehicleLocations busMapping = locations.getVehicleLocations();

        assertEquals(busMapping.values().size(), 410);
        BusLocation commuter = busMapping.get(new VehicleLocations.Key(
                Schema.Routes.SourceId.CommuterRail, "CR-Haverhill", "1627"
        ));
        BusLocation bus = busMapping.get(new VehicleLocations.Key(
                Schema.Routes.SourceId.Bus, "87", "y1447"
        ));
        BusLocation subway = busMapping.get(new VehicleLocations.Key(
                Schema.Routes.SourceId.Subway, "751", "y1275"
        ));

        commuter.makeSnippetAndTitle(routeConfig, routeTitles, locations);
        bus.makeSnippetAndTitle(routeConfig, routeTitles, locations);
        subway.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals(commuter.busId, "1627");
        assertEquals(commuter.needsUpdating(), true);
        assertEquals(Html.fromHtml(commuter.getPredictionView().getSnippet()).toString(),
                "Bus number: 1627\n" +
                        "Last update: -572630 seconds ago\n" +
                        "Heading: 16 deg (N)"
        );
        assertEquals(commuter.getVehicleSourceId(), Schema.Routes.SourceId.CommuterRail);
        assertEquals(commuter.getLongitudeAsDegrees(), 33);
        assertEquals(commuter.getLatitudeAsDegrees(), 44);
        assertEquals(commuter.getRoutes().toArray(), new String[] {});
        assertEquals(commuter.getRouteId(), "");
        assertEquals(commuter.getBusNumber(), "");
        assertEquals(commuter.getParent().isPresent(), false);
        assertEquals(commuter.getHeading(), 33);
        assertEquals(commuter.getLocationType(), LocationType.Vehicle);

        assertEquals(bus.busId, "1627");
        assertEquals(bus.needsUpdating(), true);
        assertEquals(Html.fromHtml(bus.getPredictionView().getSnippet()).toString(),
                "Bus number: 1627\n" +
                        "Last update: -572630 seconds ago\n" +
                        "Heading: 16 deg (N)"
        );
        assertEquals(bus.getVehicleSourceId(), Schema.Routes.SourceId.Bus);
        assertEquals(bus.getLongitudeAsDegrees(), 33);
        assertEquals(bus.getLatitudeAsDegrees(), 44);
        assertEquals(bus.getRoutes().toArray(), new String[] {});
        assertEquals(bus.getRouteId(), "");
        assertEquals(bus.getBusNumber(), "");
        assertEquals(bus.getParent().isPresent(), false);
        assertEquals(bus.getHeading(), 33);
        assertEquals(bus.getLocationType(), LocationType.Vehicle);


        assertEquals(subway.busId, "1627");
        assertEquals(subway.needsUpdating(), true);
        assertEquals(Html.fromHtml(subway.getPredictionView().getSnippet()).toString(),
                "Bus number: 1627\n" +
                        "Last update: -572630 seconds ago\n" +
                        "Heading: 16 deg (N)"
        );
        assertEquals(subway.getVehicleSourceId(), Schema.Routes.SourceId.Subway);
        assertEquals(subway.getLongitudeAsDegrees(), 33);
        assertEquals(subway.getLatitudeAsDegrees(), 44);
        assertEquals(subway.getRoutes().toArray(), new String[] {});
        assertEquals(subway.getRouteId(), "");
        assertEquals(subway.getBusNumber(), "");
        assertEquals(subway.getParent().isPresent(), false);
        assertEquals(subway.getHeading(), 33);
        assertEquals(subway.getLocationType(), LocationType.Vehicle);
    }


    @Test
    public void testVehiclesUnionSquare() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-15T04:34:02+00:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/vehicles_union_sq.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/vehicles?api_key=109fafba79a848e792e8e7c584f6d1f1&include=trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.VEHICLE_LOCATIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);

        // watertown square
        double lat = 42.3709;
        double lon = -71.1828;

        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        VehicleLocations busMapping = locations.getVehicleLocations();

        assertEquals(busMapping.values().size(), 387);
        BusLocation busLocation = busMapping.get(new VehicleLocations.Key(Schema.Routes.SourceId.Bus, "71", "3"));
        assertEquals(busLocation.busId, "");
    }

    @Test
    public void testKendall() throws Exception {
        Now.fakeTimeMillis = new SimpleDateFormat(Constants.ISO8601, Locale.getDefault()).parse("2018-01-16T11:23:43-05:00").getTime();

        ContentResolver resolver = RuntimeEnvironment.application.getContentResolver();
        IDatabaseAgent databaseAgent = new DatabaseAgent(resolver);

        InputStream apiv3Stream = new FileInputStream(new File("./test/resources/predictions_kendall.json"));
        InputStream hubwayInfoStream = new FileInputStream(new File("./test/resources/station_information.json"));
        InputStream hubwayStatusStream = new FileInputStream(new File("./test/resources/station_status.json"));
        InputStream alertsStream = new FileInputStream(new File("./test/resources/alerts.pb"));
        IDownloader downloader = new TestingDownloader(ImmutableMap.of(
                "https://api-v3.mbta.com/predictions?api_key=109fafba79a848e792e8e7c584f6d1f1&filter[stop]=2231,70071,70072,9070071,2228,24485,24486,2518,2521,21773,97,75,2522,11771,96&include=vehicle,trip", apiv3Stream,
                "https://gbfs.thehubway.com/gbfs/en/station_information.json", hubwayInfoStream,
                "https://gbfs.thehubway.com/gbfs/en/station_status.json", hubwayStatusStream,
                "http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb", alertsStream
        ));
        ITransitSystem transitSystem = new TransitSystem(downloader, new TestingAlertsFetcher());


        ITransitDrawables drawables = new TestingTransitDrawables();
        transitSystem.setDefaultTransitSource(
                drawables, drawables, drawables, drawables, databaseAgent
        );
        Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ALL, "71");
        Locations locations = new Locations(databaseAgent, transitSystem, selection);
        RouteTitles routeTitles = databaseAgent.getRouteTitles();

        // east cambridge/inman square
        double lat = 42.362425;
        double lon = -71.086255;

        RouteConfig routeConfig = null;
        locations.refresh(databaseAgent, selection, lat, lon, true, null);
        // watertown square
        ConcurrentMap<String, StopLocation> group = locations.getAllStopsAtStop("2231");
        StopLocation stop = group.get("2231");

        // prepare
        stop.makeSnippetAndTitle(routeConfig, routeTitles, locations);

        assertEquals("", Html.fromHtml(stop.getPredictionView().getSnippet()).toString());

    }
}

