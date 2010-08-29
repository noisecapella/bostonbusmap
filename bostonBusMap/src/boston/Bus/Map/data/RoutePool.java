package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import boston.Bus.Map.database.DatabaseHelper;

public class RoutePool {
	private final DatabaseHelper helper;
	
	
	private final LinkedList<String> priorities = new LinkedList<String>();
	private final HashMap<String, RouteConfig> pool = new HashMap<String, RouteConfig>();
	private final HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
	
	private static final int MAX_ROUTES = 50;
	
	public RoutePool(DatabaseHelper helper) {
		this.helper = helper;
	}
	
	/**
	 * In the future, this may be necessary to implement. Currently all route data is shipped with the app
	 * 
	 * @param route
	 * @return
	 */
	public boolean isMissingRouteInfo(String route) {
		return false;
	}

	public RouteConfig get(String routeToUpdate) throws IOException {
		RouteConfig routeConfig = pool.get(routeToUpdate);
		if (routeConfig != null)
		{
			return routeConfig;
		}
		else
		{
			synchronized (helper)
			{
				routeConfig = helper.getRoute(routeToUpdate, sharedStops);
				if (routeConfig == null)
				{
					return null;
				}
				else
				{
					if (priorities.size() >= MAX_ROUTES)
					{
						removeARoute();
					}

					addARoute(routeToUpdate, routeConfig);

					return routeConfig;
				}
			}
		}
	}

	private void addARoute(String routeToUpdate, RouteConfig routeConfig) {
		priorities.add(routeToUpdate);
		pool.put(routeToUpdate, routeConfig);
	}

	private void removeARoute() {
		String firstRoute = priorities.removeFirst();
		RouteConfig routeConfig = pool.get(firstRoute);
		pool.remove(firstRoute);
		
		//TODO: can this be done faster?
		
		//remove all stops from sharedStops,
		//unless that stop is owned by another route also which is currently in the pool
		for (StopLocation stopLocation : routeConfig.getStops())
		{
			boolean keepStop = false;
			for (String route : stopLocation.getRoutes())
			{
				if (pool.containsKey(route))
				{
					//keep this stop
					keepStop = true;
					break;
				}
			}
			
			if (keepStop == false)
			{
				sharedStops.remove(stopLocation.getStopTag());
			}
		}
	}

	public void writeToDatabase(HashMap<String, RouteConfig> map, boolean wipe) throws IOException {
		helper.saveMapping(map, wipe);
		
	}

}
