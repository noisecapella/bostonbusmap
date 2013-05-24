package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;

public class GtfsRealtimeTransitSource implements TransitSource {
	private final TransitSystem transitSystem;
	private final TransitDrawables drawables;

	private final TransitSourceTitles routeTitles;
	private final RouteTitles allRouteTitles;

	public GtfsRealtimeTransitSource(TransitSystem transitSystem, 
			TransitDrawables drawables, TransitSourceTitles routeTitles,
			RouteTitles allRouteTitles) {
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.allRouteTitles = allRouteTitles;
		
	}
	
	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		DownloadHelper helper;
		int mode = selection.getMode();
		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_ONE:
		case Selection.BUS_PREDICTIONS_STAR:
		{
			helper = new DownloadHelper("http://developer.mbta.com/lib/gtrtfs/Passages.pb");
			
			
			break;
		}
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ONE:
		{
			helper = new DownloadHelper("http://developer.mbta.com/lib/gtrtfs/Vehicles.pb");
			break;
		}
		default:
		{
			throw new RuntimeException("Unexpected mode");
		}
		}
		
		
		helper.connect();
		InputStream stream = helper.getResponseData();
		
		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_ONE:
		case Selection.BUS_PREDICTIONS_STAR:
			TripUpdate update = TripUpdate.parseFrom(stream);
			
			break;
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ONE:
			VehiclePosition position = VehiclePosition.parseFrom(stream);
			break;
		}
	}

	@Override
	public boolean hasPaths() {
		return true;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, transitSystem.getRouteKeysToTitles());
	}

	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public StopLocation createStop(float lat, float lon,
			String stopTag, String title, int platformOrder, String branch,
			String route) {
		StopLocation stop = new StopLocation.Builder(lat, lon, stopTag, title).build();
		stop.addRoute(route);
		return stop;
		
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public int getLoadOrder() {
		return 1;
	}

	@Override
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidBus;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

}
