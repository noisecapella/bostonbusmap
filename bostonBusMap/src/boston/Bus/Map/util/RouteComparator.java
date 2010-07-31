package boston.Bus.Map.util;

import java.util.Collection;
import java.util.Comparator;

import boston.Bus.Map.data.RouteConfig;

public class RouteComparator implements Comparator<RouteConfig> {

	@Override
	public int compare(RouteConfig object1, RouteConfig object2) {
		return object1.getRouteName().compareTo(object2.getRouteName());
	}
	
}
