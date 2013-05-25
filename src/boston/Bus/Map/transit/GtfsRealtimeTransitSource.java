package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import android.content.ContentResolver;
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
import boston.Bus.Map.data.TripInfo;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.SearchHelper;

public class GtfsRealtimeTransitSource implements TransitSource {
	private final TransitSystem transitSystem;
	private final TransitDrawables drawables;
	private final Context context;

	private final TransitSourceTitles routeTitles;
	private final RouteTitles allRouteTitles;

	public GtfsRealtimeTransitSource(TransitSystem transitSystem, 
			TransitDrawables drawables, TransitSourceTitles routeTitles,
			RouteTitles allRouteTitles, Context context) {
		this.transitSystem = transitSystem;
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.allRouteTitles = allRouteTitles;
		this.context = context;
	}
	
	@Override
	public void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
			throws IOException, ParserConfigurationException, SAXException {
		
		// first, get the right URL for the mode
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
		
		// download data from it
		helper.connect();
		InputStream stream = helper.getResponseData();
		FeedMessage message = FeedMessage.parseFrom(stream);
		
		// collect trip ids
		long lastUpdateInMillis = TransitSystem.currentTimeMillis();
		List<String> tripIdsInUpdate = Lists.newArrayList();
		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_ONE:
		case Selection.BUS_PREDICTIONS_STAR:
			for (FeedEntity entity : message.getEntityList()) {
				TripUpdate tripUpdate = entity.getTripUpdate();
				if (tripUpdate.hasTrip()) {
					String tripId = tripUpdate.getTrip().getTripId();
					tripIdsInUpdate.add(tripId);
				}
			}
			
			break;
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ONE:
			// TODO: preserve other transit modes here?
			busMapping.clear();
			for (FeedEntity entity : message.getEntityList()) {
				VehiclePosition vehicle = entity.getVehicle();
				if (vehicle.hasTrip()) {
					String tripId = vehicle.getTrip().getTripId();
					tripIdsInUpdate.add(tripId);
				}
				
			}
			break;
		}
		
		// now get information about these trips
		ContentResolver resolver = context.getContentResolver();
		Map<String, TripInfo> tripInfos = DatabaseAgent.getTripInfo(resolver,
				tripIdsInUpdate);
		
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long secondsSinceMidnight = (now - calendar.getTimeInMillis())/1000;
		
		switch (mode) {
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.BUS_PREDICTIONS_ONE:
		case Selection.BUS_PREDICTIONS_STAR:
			for (FeedEntity entity : message.getEntityList()) {
				TripUpdate tripUpdate = entity.getTripUpdate();

				Map<Integer, Integer> delayMap = Maps.newHashMap();
				for (StopTimeUpdate update : entity.getTripUpdate().getStopTimeUpdateList()) {
					// TODO: if update.hasArrival() is false, the prediction is unreliable
					// we should handle that case better
					if (update.hasArrival()) {
						delayMap.put(update.getStopSequence(),
								update.getArrival().getDelay());
					}
				}
				String tripId = tripUpdate.getTrip().getTripId();
				TripInfo tripInfo = tripInfos.get(tripId);
				if (tripInfo != null) {
					String routeId = tripInfo.getRouteId();
					// This might cause a huge memory grab, be aware
					RouteConfig route = locationsObj.getRoute(routeId);

					int delay = 0;
					int[] sequences = tripInfo.getSequences();
					String[] stopIds = tripInfo.getStopIds();
					int[] arrivalSeconds = tripInfo.getArrivalSeconds();
					String dirTag = tripInfo.getDirTag();
					for (int i = 0; i < sequences.length; i++) {
						int sequence = sequences[i];
						String stopId = stopIds[i];
						int arrivalSecond = arrivalSeconds[i];

						StopLocation stop = route.getStop(stopId);
						if (stop != null) {
							stop.clearPredictions(routeConfig);
							if (delayMap.containsKey(sequence)) {
								delay = delayMap.get(sequence);
							}

							int seconds = arrivalSecond + delay;
							if (seconds > 24*60*60) {
								// gtfs time wraps into tomorrow, ie 25:01:23 is around 1 am
								seconds -= 24*60*60;
							}
							long diffSeconds = seconds - secondsSinceMidnight;
							int minutes = (int)(diffSeconds / 60);
							stop.addPrediction(minutes, -1,
									"Unknown", dirTag, route,
									directions, false,
									false, 0);
						}
					}
				}
			}
			
			break;
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ONE:
			// TODO: preserve other transit modes here?
			busMapping.clear();
			for (FeedEntity entity : message.getEntityList()) {
				VehiclePosition vehicle = entity.getVehicle();
				Position position = vehicle.getPosition();
				float latitude = position.getLatitude();
				float longitude = position.getLongitude();
				String id = entity.getId();
				long lastFeedUpdateInMillis = vehicle.getTimestamp() * 1000;
				
				lastFeedUpdateInMillis += TransitSystem.getTimeZone().getOffset(lastFeedUpdateInMillis);
				
				if (vehicle.hasTrip()) {
					String tripId = vehicle.getTrip().getTripId();
					TripInfo tripInfo = tripInfos.get(tripId);
					String routeName = tripInfo.getRouteId();
					String routeTitle = routeTitles.getTitle(routeName);
					if (routeTitle == null) {
						routeTitle = routeName;
					}
					BusLocation busLocation = new BusLocation(latitude, longitude,
							id, lastFeedUpdateInMillis, lastUpdateInMillis,
							null, true, "TODODIR",
							routeName, directions, routeTitle);
					busMapping.put(id, busLocation);
				}
				else
				{
					String routeName = null;
					String routeTitle = null;
					BusLocation busLocation = new BusLocation(latitude, longitude,
							id, lastFeedUpdateInMillis, lastUpdateInMillis,
							null, false, null,
							routeName, directions, routeTitle);
					busMapping.put(id, busLocation);
				}
				
			}
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
