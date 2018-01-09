package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

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

    private final long creationTime;

	public AlertsFuture(final IDatabaseAgent databaseAgent, final IAlertsParser parser, final Runnable runnable) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try
				{
					IAlerts alerts = parser.obtainAlerts(databaseAgent);
					synchronized (lock) {
						AlertsFuture.this.alerts = alerts;
					}
                    if (runnable != null) {
                        runnable.run();
                    }
				}
				catch (Throwable e) {
					LogUtil.e(e);
				}
			}
		};
		thread.start();

        creationTime = Now.getMillis();
	}

    public long getCreationTime() {
        return creationTime;
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
				Schema.Routes.SourceId routeType) {
			return ImmutableList.of();
		}

		@Override
		public ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
				Collection<String> routes, String tag, ImmutableSet<Schema.Routes.SourceId> routeTypes) {
			return ImmutableList.of();
		}
		
	}
}
