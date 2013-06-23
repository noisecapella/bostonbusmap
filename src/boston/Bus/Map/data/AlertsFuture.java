package boston.Bus.Map.data;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import boston.Bus.Map.annotations.KeepSorted;
import boston.Bus.Map.parser.IAlertsParser;
import boston.Bus.Map.parser.MbtaAlertsParser;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;

/**
 * Gets alerts data over the internet and provides it when asked. If asked before
 * data is available, an empty set of alerts is returned
 * @author schneg
 *
 */
public class AlertsFuture
{
	public static final IAlerts EMPTY = new EmptyAlerts();
	
	@IsGuardedBy("this")
	private IAlerts alerts = EMPTY;
	
	private final Object lock = new Object();

	public AlertsFuture(final Context context, final IAlertsParser parser) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try
				{
					IAlerts alerts = parser.obtainAlerts(context);
					synchronized (lock) {
						AlertsFuture.this.alerts = alerts;
					}
				}
				catch (IOException e) {
					LogUtil.e(e);
				}
			}
		};
		thread.start();
	}
	
	public  IAlerts getAlerts() {
		synchronized (lock) {
			return alerts;
		}
	}
	
	private static class EmptyAlerts implements IAlerts {

		@Override
		public ImmutableCollection<Alert> getAlertsByCommuterRailTripId(
				String tripId, String routeId) {
			return ImmutableList.of();
		}

		@Override
		public ImmutableCollection<Alert> getAlertsByRoute(String routeName,
				int routeType) {
			return ImmutableList.of();
		}

		@Override
		public ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
				Collection<String> routes, String tag, int routeType) {
			return ImmutableList.of();
		}
		
	}
}
