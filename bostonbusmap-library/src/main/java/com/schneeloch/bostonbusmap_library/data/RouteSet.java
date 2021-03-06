package com.schneeloch.bostonbusmap_library.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.schneeloch.bostonbusmap_library.annotations.KeepSorted;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A thread safe sorted set of route tags (strings, basically)
 * @author schneg
 *
 */
public class RouteSet {
	@KeepSorted
	private List<String> routes;
	private boolean immutable;
	
	public RouteSet() {
		routes = Lists.newArrayList();
	}
	
	public synchronized ImmutableList<String> getRoutes() {
		if (!immutable) {
            Collections.sort(routes);
			routes = ImmutableList.copyOf(routes);
			immutable = true;
		}
		return (ImmutableList<String>)routes;
	}

	public synchronized void addRoute(String route) {
		if (routes.contains(route)) {
			return;
		}
		if (immutable) {
			routes = Lists.newArrayList(routes);
			immutable = false;
		}
		routes.add(route);
	}

	public synchronized String getFirstRoute() {
		return Iterables.getFirst(getRoutes(), "");
	}

	public synchronized boolean hasRoute(String route) {
        return routes.contains(route);
	}
	

}
