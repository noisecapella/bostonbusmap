package boston.Bus.Map.data;

import java.util.Collection;

import boston.Bus.Map.database.Schema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;

public class Alerts implements IAlerts {
	private final ImmutableMultimap<String, Alert> alertsByRoute;
	private final ImmutableMultimap<String, Alert> alertsByStop;
	private final ImmutableMultimap<Integer, Alert> alertsByRouteType;
	private final ImmutableMultimap<String, Alert> alertsByCommuterRailTripId;
	private final ImmutableList<Alert> systemWideAlerts;
	
	private Alerts(ImmutableMultimap<String, Alert> alertsByRoute, 
			ImmutableMultimap<String, Alert> alertsByStop,
			ImmutableMultimap<Integer, Alert> alertsByRouteType,
			ImmutableMultimap<String, Alert> alertsByCommuterRailTripId,
			ImmutableList<Alert> systemWideAlerts) {
		this.alertsByRoute = alertsByRoute;
		this.alertsByStop = alertsByStop;
		this.alertsByRouteType = alertsByRouteType;
		this.alertsByCommuterRailTripId = alertsByCommuterRailTripId;
		this.systemWideAlerts = systemWideAlerts;
	}
	
	public static class Builder {
		private final ImmutableMultimap.Builder<String, Alert> alertsByRoute;
		private final ImmutableMultimap.Builder<String, Alert> alertsByStop;
		private final ImmutableMultimap.Builder<Integer, Alert> alertsByRouteType;
		private final ImmutableMultimap.Builder<String, Alert> alertsByCommuterRailTripId;
		private final ImmutableList.Builder<Alert> systemWideAlerts;
		
		public Builder() {
			this.alertsByRoute = ImmutableMultimap.builder();
			this.alertsByStop = ImmutableMultimap.builder();
			this.alertsByRouteType = ImmutableMultimap.builder();
			this.alertsByCommuterRailTripId = ImmutableMultimap.builder();
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

		public void addAlertForCommuterRailTrip(String commuterRailTripId,
				Alert alert) {
			alertsByCommuterRailTripId.put(commuterRailTripId, alert);
		}

		public IAlerts build() {
			return new Alerts(alertsByRoute.build(),
					alertsByStop.build(),
					alertsByRouteType.build(),
					alertsByCommuterRailTripId.build(),
					systemWideAlerts.build());
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	/* (non-Javadoc)
	 * @see boston.Bus.Map.data.IAlerts#getAlertsByCommuterRailTripId(java.lang.String, java.lang.String)
	 */
	@Override
	public ImmutableCollection<Alert> getAlertsByCommuterRailTripId(String tripId,
			String routeId) {
		ImmutableCollection.Builder<Alert> ret = ImmutableList.builder();
		ret.addAll(systemWideAlerts);
		ret.addAll(alertsByCommuterRailTripId.get(tripId));
		ret.addAll(alertsByRouteType.get(Schema.Routes.enumagencyidCommuterRail));
		ret.addAll(alertsByRoute.get(routeId));
		return ret.build();
	}
	
	/* (non-Javadoc)
	 * @see boston.Bus.Map.data.IAlerts#getAlertsByRoute(java.lang.String, int)
	 */
	@Override
	public ImmutableCollection<Alert> getAlertsByRoute(String routeName,
			int routeType) {
		ImmutableCollection.Builder<Alert> ret = ImmutableList.builder();
		ret.addAll(systemWideAlerts);
		ret.addAll(alertsByRouteType.get(routeType));
		ret.addAll(alertsByRoute.get(routeName));
		return ret.build();
	}

	/* (non-Javadoc)
	 * @see boston.Bus.Map.data.IAlerts#getAlertsByRouteSetAndStop(java.util.Collection, java.lang.String, int)
	 */
	@Override
	public ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
			Collection<String> routes, String tag, int routeType) {
		ImmutableCollection.Builder<Alert> ret = ImmutableList.builder();
		ret.addAll(systemWideAlerts);
		ImmutableCollection<Alert> routeTypeAlerts = alertsByRouteType.get(routeType);
		ret.addAll(routeTypeAlerts);
		for (String route : routes) {
			ImmutableCollection<Alert> routeAlerts = alertsByRoute.get(route);
			ret.addAll(routeAlerts);
		}
		ret.addAll(alertsByStop.get(tag));
		return ret.build();
	}
}
