package com.schneeloch.bostonbusmap_library.test;

import com.schneeloch.bostonbusmap_library.data.IAlertsFetcher;
import com.schneeloch.bostonbusmap_library.data.IAlertsFuture;
import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;

import java.io.IOException;

/**
 * Created by schneg on 1/15/18.
 */

public class TestingAlertsFetcher implements IAlertsFetcher {

    @Override
    public IAlertsFuture fetchAlerts(IDatabaseAgent databaseAgent, IAlertsParser parser, Runnable runnable) {
        try {
            return new TestingAlertsFuture(databaseAgent, parser, runnable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
