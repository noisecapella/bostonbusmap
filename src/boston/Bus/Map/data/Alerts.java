package boston.Bus.Map.data;

import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;

public class Alerts {
	private final ImmutableMultimap<String, Alert> alertsByRoute;
	private final ImmutableMultimap<String, Alert> alertsByStop;
	private final ImmutableMultimap<Integer, Alert> alertsByRouteType;
	private final ImmutableList<Alert> systemWideAlerts;
	
	private Alerts(ImmutableMultimap<String, Alert> alertsByRoute, 
			ImmutableMultimap<String, Alert> alertsByStop,
			ImmutableMultimap<Integer, Alert> alertsByRouteType,
			ImmutableList<Alert> systemWideAlerts) {
		this.alertsByRoute = alertsByRoute;
		this.alertsByStop = alertsByStop;
		this.alertsByRouteType = alertsByRouteType;
		this.systemWideAlerts = systemWideAlerts;
	}
	
	public static class Builder {
		private final ImmutableMultimap.Builder<String, Alert> alertsByRoute;
		private final ImmutableMultimap.Builder<String, Alert> alertsByStop;
		private final ImmutableMultimap.Builder<Integer, Alert> alertsByRouteType;
		private final ImmutableList.Builder<Alert> systemWideAlerts;
		
		public Builder() {
			this.alertsByRoute = ImmutableMultimap.builder();
			this.alertsByStop = ImmutableMultimap.builder();
			this.alertsByRouteType = ImmutableMultimap.builder();
			this.systemWideAlerts = ImmutableList.builder();
		}
		
		public void addAlertForRoute(String route, Alert alert) {
			this.alertsByRoute.put(route, alert);
		}
		
		public void addAlertForRouteType(int routeType, Alert alert) {
			this.alertsByRouteType.put(routeType, alert);
		}
		
		public void addAlertForStop(String stopId, Alert alert) {
			this.alertsByStop.put(stopId, alert);
		}
		
		public void addSystemWideAlert(Alert alert) {
			systemWideAlerts.add(alert);
		}

		public Alerts build() {
			return new Alerts(alertsByRoute.build(),
					alertsByStop.build(),
					alertsByRouteType.build(),
					systemWideAlerts.build());
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public ImmutableCollection<Alert> getAlertsByRouteAndStop(String route, String stopId) {
		// try not to create extra objects if feasible
		ImmutableCollection<Alert> routeAlerts = alertsByRoute.get(route);
		ImmutableCollection<Alert> stopAlerts = alertsByStop.get(stopId);
		
		int emptyCount = 0;
		if (routeAlerts.isEmpty() == false) {
			emptyCount += 1;
		}
		if (stopAlerts.isEmpty() == false) {
			emptyCount += 1;
		}
		if (systemWideAlerts.isEmpty() == false) {
			emptyCount += 1;
		}

		if (emptyCount >= 2) {
			ImmutableList.Builder<Alert> combined = ImmutableList.builder();
			combined.addAll(systemWideAlerts);
			combined.addAll(routeAlerts);
			combined.addAll(stopAlerts);
			return combined.build();
		}
		else if (emptyCount == 0) {
			return ImmutableList.of();
		}
		else if (routeAlerts.isEmpty() == false) {
			return routeAlerts;
		}
		else if (systemWideAlerts.isEmpty() == false) {
			return systemWideAlerts;
		}
		else {
			return stopAlerts;
		}
	}

	public ImmutableCollection<Alert> getAlertsByRoute(String routeName,
			int routeType) {
		ImmutableCollection.Builder<Alert> ret = ImmutableList.builder();
		ret.addAll(systemWideAlerts);
		ret.addAll(alertsByRouteType.get(routeType));
		ret.addAll(alertsByRoute.get(routeName));
		return ret.build();
	}

	public ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
			RouteSet routes, String tag, int routeType) {
		ImmutableCollection.Builder<Alert> ret = ImmutableList.builder();
		ret.addAll(systemWideAlerts);
		ImmutableCollection<Alert> routeTypeAlerts = alertsByRouteType.get(routeType);
		ret.addAll(routeTypeAlerts);
		for (String route : routes.getRoutes()) {
			ImmutableCollection<Alert> routeAlerts = alertsByRoute.get(route);
			ret.addAll(routeAlerts);
		}
		ret.addAll(alertsByStop.get(tag));
		return ret.build();
	}
}
