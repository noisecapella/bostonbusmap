package com.schneeloch.bostonbusmap_library.data;

import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.database.Schema;

public interface IAlerts {

	public abstract ImmutableCollection<Alert> getAlertsByCommuterRailTripId(
			String tripId, String routeId);

	public abstract ImmutableCollection<Alert> getAlertsByRoute(
			String routeName, Schema.Routes.SourceId routeType);

	public abstract ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
			Collection<String> routes, String tag, ImmutableSet<Schema.Routes.SourceId> routeTypes);

}