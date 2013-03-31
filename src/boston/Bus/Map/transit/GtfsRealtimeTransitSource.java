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
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.util.DownloadHelper;

public class GtfsRealtimeTransitSource implements TransitSource {

	@Override
	public void populateStops(Context context, RoutePool routeMapping,
			String routeToUpdate, Directions directions, UpdateAsyncTask task,
			boolean silent) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException, RemoteException,
			OperationApplicationException {
		// pass
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException, RemoteException,
			OperationApplicationException {
		// TODO Auto-generated method stub

	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransitDrawables getDrawables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle, int platformOrder, String branch,
			String route) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public int getTransitSourceId() {
		return 5;
	}

	@Override
	public boolean requiresSubwayTable() {
		return false;
	}

}
