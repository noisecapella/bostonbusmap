package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Objects;

/**
 * Created by george on 2/21/15.
 */
public class RouteStopPair {
    private final String route;
    private final String stopId;

    public RouteStopPair(String route, String stopId) {
        this.route = route;
        this.stopId = stopId;
    }

    public String getRoute() {
        return route;
    }

    public String getStopId() {
        return stopId;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RouteStopPair) {
            RouteStopPair other = (RouteStopPair)object;
            return Objects.equal(route, other.route) && Objects.equal(stopId, other.stopId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(route, stopId);
    }
}
