package boston.Bus.Map.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import boston.Bus.Map.annotations.KeepSorted;

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
	
	public synchronized Collection<String> getRoutes() {
		if (!immutable) {
			routes = ImmutableList.copyOf(routes);
			immutable = true;
		}
		return routes;
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
		Collections.sort(routes);
	}

	public synchronized String getFirstRoute() {
		return Iterables.getFirst(routes, "");
	}

	public synchronized boolean hasRoute(String route) {
		int insertionPoint = Collections.binarySearch(routes, route);
		return insertionPoint >= 0;
	}
	

}
