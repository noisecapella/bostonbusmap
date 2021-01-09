package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.database.Schema;

import java.util.Collection;

/**
 * Created by schneg on 1/15/18.
 */

public class EmptyAlerts implements IAlerts {

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
