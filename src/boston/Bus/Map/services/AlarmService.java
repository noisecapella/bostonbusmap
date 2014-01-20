package boston.Bus.Map.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.common.collect.Lists;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import boston.Bus.Map.data.Alarm;
import boston.Bus.Map.data.Alerts;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IPrediction;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Predictions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopPredictionView;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.receivers.AlarmReceiver;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;

public class AlarmService extends IntentService {
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public AlarmService() {
		super("alarm");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try
		{
			ContentResolver resolver = getContentResolver();
			List<Alarm> alarms = DatabaseContentProvider.DatabaseAgent.getAlarms(resolver);

			boolean checkAgain = false;

			long nowSeconds = System.currentTimeMillis() / 1000;
			for (Alarm alarm : alarms) {
				if (alarm.getAlarmTime() < nowSeconds - (10 * 60)) {
					// TODO: error in this case
					AlarmReceiver.triggerNotification(getApplicationContext(), "Error: still waiting for vehicle at stop " + alarm.getStop());
					DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
				}
				else if (alarm.getAlarmTime() < nowSeconds + (10 * 60)) {
					if (checkStops(alarm)) {
						triggerAlarm(alarm);
						DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
					}
					else
					{
						checkAgain = true;
					}
				}
				else
				{
					AlarmReceiver.triggerNotification(getApplicationContext(), "Error: too far into the future");
					DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
				}
			}

			if (checkAgain) {
				AlarmReceiver.scheduleAlarm(getApplicationContext(), 30);
			}

		}
		catch (Exception e) {
			LogUtil.e(e);
			AlarmReceiver.triggerNotification(getApplicationContext(), "Error: " + e.getMessage());
		}
		finally
		{
			WakefulBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	protected boolean checkStops(Alarm alarm) throws IOException, ParserConfigurationException, SAXException {
		Context context = getApplicationContext();
		TransitSystem transitSystem = new TransitSystem();
		transitSystem.setDefaultTransitSource(null, null, null, null, context);

		RouteTitles routeTitles = transitSystem.getRouteKeysToTitles();

		String routeTitle = alarm.getRouteTitle();
		String route = routeTitles.getKey(routeTitle);
		String stop = alarm.getStop();

		String directionTitle = alarm.getDirectionTitle();


		TransitSource source = transitSystem.getTransitSource(route);
		Selection selection = new Selection(Selection.BUS_PREDICTIONS_ONE, route);
		RoutePool routePool = new RoutePool(context, transitSystem);
		RouteConfig routeConfig = routePool.get(route);
		Directions directions = new Directions(context);
		Locations locations = new Locations(context, transitSystem, selection);
		StopLocation stopLocation = routeConfig.getStop(stop);

		source.refreshData(routeConfig, selection, 1, stopLocation.getLatitudeAsDegrees(),
				stopLocation.getLongitudeAsDegrees(), null, routePool, directions, locations);

		stopLocation.makeSnippetAndTitle(routeConfig, transitSystem.getRouteKeysToTitles(), locations, context);
		Predictions predictions = stopLocation.getPredictions();
		StopPredictionView stopPredictionView = (StopPredictionView)predictions.getPredictionView();
		List<IPrediction> predictionList = Arrays.asList(stopPredictionView.getPredictions());

		for (IPrediction prediction : predictionList) {
			TimePrediction timePrediction = (TimePrediction)prediction;
			if (directionTitle != null && directionTitle.equals(timePrediction.getDirectionTitle())) {
				int minutes = timePrediction.getMinutes();
				if (minutes <= alarm.getMinutesBefore() || minutes <= 2) {
					return true;
				}
				else
				{
					return false;
				}
			}
		}

		throw new RuntimeException("No predictions found");
	}

	protected void triggerAlarm(Alarm alarm) {
		AlarmReceiver.triggerNotification(getApplicationContext(), "Arrival for route " + alarm.getRouteTitle() + ", stop " + alarm.getStop());
	}
}
