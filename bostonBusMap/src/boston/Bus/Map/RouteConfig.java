package boston.Bus.Map;

import java.util.Collection;
import java.util.HashMap;

public class RouteConfig {

	private final HashMap<Integer, StopLocation> stops = new HashMap<Integer, StopLocation>();
	private final HashMap<String, String> directions = new HashMap<String, String>();
	
	private final String route;
	
	public RouteConfig(String route)
	{
		this.route = route;
	}
	
	
	
	public void addStop(int id, StopLocation stopLocation) {
		stops.put(id, stopLocation);
	}
	
	public StopLocation getStop(int id)
	{
		return stops.get(id);
	}

	
	public String getDirection(String dirTag)
	{
		if (directions.containsKey(dirTag))
		{
			return directions.get(dirTag);
		}
		else
		{
			return "";
		}
	}

	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	public void addDirection(String tag, String name) {
		directions.put(tag, name);
		
	}



	public String getRouteName() {
		return route;
	}



	public Collection<String> getDirtags() {
		return directions.keySet();
	}
			
}
