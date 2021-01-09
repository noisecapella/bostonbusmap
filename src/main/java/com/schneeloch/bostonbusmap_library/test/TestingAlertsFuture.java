package com.schneeloch.bostonbusmap_library.test;

import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.IAlertsFetcher;
import com.schneeloch.bostonbusmap_library.data.IAlertsFuture;
import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.util.Now;

import java.io.IOException;

/**
 * Created by schneg on 1/15/18.
 */

public class TestingAlertsFuture implements IAlertsFuture {
    private final IAlerts alerts;
    private final long nowMillis;
    public TestingAlertsFuture(final IDatabaseAgent databaseAgent, final IAlertsParser parser, final Runnable runnable) throws IOException {
        nowMillis = Now.getMillis();

        alerts = parser.obtainAlerts(databaseAgent);
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public IAlerts getAlerts() {
        return alerts;
    }

    @Override
    public long getCreationTime() {
        return nowMillis;
    }
}
