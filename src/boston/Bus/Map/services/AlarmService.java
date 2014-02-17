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
			TransitSystem transitSystem = new TransitSystem();
			transitSystem.setDefaultTransitSource(null, null, null, null, getApplicationContext());
			RouteTitles routeTitles = transitSystem.getRouteKeysToTitles();

			ContentResolver resolver = getContentResolver();
			List<Alarm> alarms = DatabaseContentProvider.DatabaseAgent.getAlarms(resolver);

			boolean checkAgain = false;

			Integer checkAgainSeconds = null;

			long nowSeconds = System.currentTimeMillis() / 1000;
			for (Alarm alarm : alarms) {
				// TODO: we shouldn't be looking up the route key from the route title
				String route = routeTitles.getKey(alarm.getRouteTitle());
				try {
					int minutesBefore = alarm.getMinutesBefore();
					long adjustedAlarmTime = alarm.getAlarmTime() - (minutesBefore * 60);
					if (adjustedAlarmTime < nowSeconds - (10 * 60)) {
						// TODO: is this condition necessary?
						AlarmReceiver.triggerNotification(getApplicationContext(),
								"Error: still waiting for vehicle at stop " + alarm.getStopTitle(),
								route, alarm.getRouteTitle(), alarm.getStop(), alarm.getStopTitle());
						DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
					} else if (adjustedAlarmTime < nowSeconds + (10 * 60)) {
						TimePrediction prediction = checkStops(transitSystem, alarm);
						if (prediction != null) {
							if (prediction.getMinutes() < minutesBefore) {
								AlarmReceiver.triggerNotification(getApplicationContext(),
										"Arrival at " + alarm.getStopTitle(),
										route, alarm.getRouteTitle(), alarm.getStop(), alarm.getStopTitle());
								DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
							}
							else
							{
								Alarm newAlarm = alarm.withUpdatedTime(nowSeconds + (prediction.getMinutes() * 60));
								DatabaseContentProvider.DatabaseAgent.updateAlarm(resolver, newAlarm);
								checkAgain = true;

								if (checkAgainSeconds == null || (checkAgainSeconds > 30)) {
									checkAgainSeconds = 30;
								}
							}
						} else {
							AlarmReceiver.triggerNotification(getApplicationContext(), "Error: no predictions found",
									route, alarm.getRouteTitle(), alarm.getStop(), alarm.getStopTitle());
							DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
						}
					} else {
						// too far in future
						// adjustedAlarmTime >= nowSeconds + (10 * 60)

						int seconds = (int)(adjustedAlarmTime - (nowSeconds + (10*60)));
						checkAgain = true;
						if (checkAgainSeconds == null || (checkAgainSeconds > seconds)) {
							checkAgainSeconds = seconds;
						}
					}
				} catch (Exception e) {
					LogUtil.e(e);
					String stop = null;
					String routeTitle = null;
					String stopTitle = null;
					if (alarm != null) {
						stop = alarm.getStop();
						routeTitle = alarm.getRouteTitle();
						stopTitle = alarm.getStopTitle();
					}
					AlarmReceiver.triggerNotification(getApplicationContext(), "Error: " + e.getMessage(),
							route,
							routeTitle,
							stop,
							stopTitle);
					DatabaseContentProvider.DatabaseAgent.removeAlarm(resolver, alarm);
				}
			}

			if (checkAgain) {
				long seconds;
				if (checkAgainSeconds == null) {
					throw new RuntimeException("checkAgainSeconds not defined");
				}
				else
				{
					seconds = checkAgainSeconds;
				}
				AlarmReceiver.scheduleAlarm(getApplicationContext(), (int)seconds);
			}
			else
			{
				AlarmReceiver.cancelAllAlarms(getApplicationContext());
			}

		}
		catch (Exception e) {
			LogUtil.e(e);
			AlarmReceiver.triggerNotification(getApplicationContext(), "Error: " + e.getMessage(),
					null, null, null, null);
		}
		finally
		{
			WakefulBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	protected TimePrediction checkStops(TransitSystem transitSystem,
										Alarm alarm) throws IOException, ParserConfigurationException, SAXException {
		Context context = getApplicationContext();

		RouteTitles routeTitles = transitSystem.getRouteKeysToTitles();

		String routeTitle = alarm.getRouteTitle();
		String route = routeTitles.getKey(routeTitle);
		String stop = alarm.getStop();

		String directionTitle = alarm.getDirectionTitle();


		TransitSource source = transitSystem.getTransitSource(route);
		Selection selection = new Selection(Selection.Mode.BUS_PREDICTIONS_ONE, route);
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
				return timePrediction;
			}
		}

		return null;
	}
}
