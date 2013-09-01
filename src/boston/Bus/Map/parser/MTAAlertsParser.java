package boston.Bus.Map.parser;

import android.content.Context;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.SIRIVehicleParsingResults;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.DownloadHelper;

/**
 * Created by schneg on 8/31/13.
 */
public class MTAAlertsParser implements IAlertsParser
{
	private final TransitSystem transitSystem;
	private final Directions directions;
	private final RoutePool routePool;
	private final ConcurrentHashMap<String, BusLocation> busMapping;

	public MTAAlertsParser(TransitSystem transitSystem, Directions directions,
						   RoutePool routePool, ConcurrentHashMap<String, BusLocation> busMapping) {

		this.transitSystem = transitSystem;
		this.directions = directions;
		this.routePool = routePool;
		this.busMapping = busMapping;
	}

	@Override
	public IAlerts obtainAlerts(Context context) throws IOException {
		String alertsUrl = TransitSystem.ALERTS_URL;
		DownloadHelper downloadHelper = new DownloadHelper(alertsUrl);
		downloadHelper.connect();
		InputStream data = downloadHelper.getResponseData();

		TransitSource defaultTransitSource = transitSystem.getDefaultTransitSource();
		TransitSourceTitles routeTitles = defaultTransitSource.getRouteTitles();

		SIRIVehicleLocationsFeedParser parser = new SIRIVehicleLocationsFeedParser(
				null, directions, routeTitles, routePool);
		SIRIVehicleParsingResults results = parser.runParse(new InputStreamReader(data), busMapping);

		data.close();

		return results.alerts;
	}
}
