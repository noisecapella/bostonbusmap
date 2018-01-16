package com.schneeloch.bostonbusmap_library.data;

import com.schneeloch.bostonbusmap_library.parser.IAlertsParser;
import com.schneeloch.bostonbusmap_library.parser.MbtaAlertsParser;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;

/**
 * Created by schneg on 1/15/18.
 */

public interface IAlertsFetcher {
    IAlertsFuture fetchAlerts(IDatabaseAgent databaseAgent, IAlertsParser parser, Runnable runnable);
}
