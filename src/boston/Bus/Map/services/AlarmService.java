package boston.Bus.Map.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IPrediction;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Predictions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TimePrediction;
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
			Context context = getApplicationContext();
			TransitSystem transitSystem = new TransitSystem();
			transitSystem.setDefaultTransitSource(null, null, null, null, context);

			String route = intent.getStringExtra(AlarmReceiver.ROUTE);
			String stop = intent.getStringExtra(AlarmReceiver.STOP);

			TransitSource source = transitSystem.getTransitSource(route);
			Selection selection = new Selection(Selection.BUS_PREDICTIONS_ONE, route);
			RoutePool routePool = new RoutePool(context, transitSystem);
			RouteConfig routeConfig = routePool.get(route);
			Directions directions = new Directions(context);
			Locations locations = new Locations(context, transitSystem, selection);

			source.refreshData(routeConfig, selection, 1, 0, 0, null, routePool, directions, locations);

			StopLocation stopLocation = routeConfig.getStop(stop);
			Predictions predictions = stopLocation.getPredictions();
			List<IPrediction> predictionList = predictions.getPredictions();

			// TODO: throw better exceptions here if empty list or not TimePrediction
			TimePrediction timePrediction = (TimePrediction)predictionList.get(0);
			int minutes = timePrediction.getMinutes();
			if (minutes <= 1) {
				AlarmReceiver.triggerNotification(getApplicationContext(), "Arrival for route " + route + ", stop " + stopLocation.getTitle());
			}
			else
			{
				LogUtil.i("Minutes is " + minutes + ", setting alarm again");
				AlarmReceiver.setAlarm(context, route, stop, 15);
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
}
