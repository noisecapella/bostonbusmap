package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

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

	public AlertsFuture(final IDatabaseAgent databaseAgent, final IAlertsParser parser) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try
				{
					IAlerts alerts = parser.obtainAlerts(databaseAgent);
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
