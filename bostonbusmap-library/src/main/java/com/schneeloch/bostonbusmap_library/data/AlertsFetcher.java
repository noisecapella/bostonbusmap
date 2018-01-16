package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.parser.MbtaAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

import java.util.Collection;

/**
 * Created by schneg on 1/15/18.
 */

public class AlertsFetcher implements IAlertsFetcher {
    public static final IAlerts EMPTY = new EmptyAlerts();

    /**
     * Gets alerts data over the internet and provides it when asked. If asked before
     * data is available, an empty set of alerts is returned
     * @author schneg
     *
     */
    private static class AlertsFuture implements IAlertsFuture
    {

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

        @Override
        public long getCreationTime() {
            return creationTime;
        }

        @Override
        public  IAlerts getAlerts() {
            synchronized (lock) {
                return alerts;
            }
        }
    }

    @Override
    public IAlertsFuture fetchAlerts(IDatabaseAgent databaseAgent, IAlertsParser parser, Runnable runnable) {
        return new AlertsFuture(databaseAgent, parser, runnable);
    }
}
