package boston.Bus.Map.data;

import java.util.Map;

import boston.Bus.Map.transit.TransitSystem;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;



public class AlertsMapping {
	public static final String alertUrlPrefix = "http://talerts.com/rssfeed/alertsrss.aspx?";

	private final ImmutableMap<String, Integer> routeDescriptionToAlertKey;
	
	public AlertsMapping(ImmutableMap<String, Integer> map)
	{
		routeDescriptionToAlertKey = map;
	}

	public static String getUrlForAllRoutes() {
		return alertUrlPrefix + TransitSystem.allRoutesAlertNumber;
	}
	
	public String getUrlForRoute(String routeName) {
		return alertUrlPrefix + routeDescriptionToAlertKey.get(routeName);
	}

	public boolean hasRoute(String routeName) {
		return routeDescriptionToAlertKey.containsKey(routeName);
	}

	
}
