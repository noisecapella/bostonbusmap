package com.schneeloch.bostonbusmap_library.util;

import java.util.Comparator;

import com.schneeloch.bostonbusmap_library.data.RouteConfig;

public class RouteComparator implements Comparator<RouteConfig> {

	@Override
	public int compare(RouteConfig object1, RouteConfig object2) {
		return object1.getRouteName().compareTo(object2.getRouteName());
	}
	
}
