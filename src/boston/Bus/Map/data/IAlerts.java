package boston.Bus.Map.data;

import java.util.Collection;

import com.google.common.collect.ImmutableCollection;

public interface IAlerts {

	public abstract ImmutableCollection<Alert> getAlertsByCommuterRailTripId(
			String tripId, String routeId);

	public abstract ImmutableCollection<Alert> getAlertsByRoute(
			String routeName, int routeType);

	public abstract ImmutableCollection<Alert> getAlertsByRouteSetAndStop(
			Collection<String> routes, String tag, int routeType);

}