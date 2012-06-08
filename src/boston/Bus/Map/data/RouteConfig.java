package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.util.Box;

public class RouteConfig
{
	private final ArrayList<String> stopOrder;
	private final MyHashMap<String, StopLocation> stops;
	private Path[] paths;
	private final String route;
	private final String routeTitle;
	
	private final int color;
	private final int oppositeColor;
	
	private final TransitSource transitSource;
	
	public static final Path[] nullPaths = new Path[0];
	private ArrayList<Alert> alerts;
	
	private boolean obtainedAlerts;
	
	public RouteConfig(String route, String routeTitle, int color, int oppositeColor, TransitSource transitAgency) throws IOException
	{
		this(route, routeTitle, color, oppositeColor, transitAgency, null);
	}
	
	public void addStop(String tag, StopLocation stopLocation) {
		stops.put(tag, stopLocation);
		stopOrder.add(tag);
	}
	
	public StopLocation getStop(String tag)
	{
		return stops.get(tag);
	}

	public MyHashMap<String, StopLocation> getStopMapping()
	{
		return stops;
	}
	
	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	

	public String getRouteName() {
		return route;
	}
	
	public String getRouteTitle()
	{
		return routeTitle;
	}

	public Path[] getPaths() {
		return paths;
	}

	public int getColor()
	{
		return color;
	}

	public void serializePath(Box dest) throws IOException
	{
		dest.writePathsList(paths);
	}
	
	public RouteConfig(String route, String routeTitle, int color, int oppositeColor, TransitSource transitAgency, Box serializedPath)
			throws IOException {
		this.route = route;
		this.routeTitle = routeTitle;
		stops = new MyHashMap<String, StopLocation>();
		
		this.color = color;
		this.oppositeColor = oppositeColor;
		this.transitSource = transitAgency;

		if (serializedPath != null)
		{
			paths = serializedPath.readPathsList();
		}
		else
		{
			paths = nullPaths;
		}
		
		stopOrder = new ArrayList<String>();
	}



	public TransitSource getTransitSource() {
		return transitSource;
	}
	
	public boolean hasPaths()
	{
		return transitSource.hasPaths();
	}

	public int getOppositeColor() {
		return oppositeColor;
	}

	public void setPaths(Path[] paths)
	{
		this.paths = paths;
	}

	/**
	 * For efficiency's sake this should be called sparingly
	 * @param path
	 */
	public void addPaths(Path path)
	{
		Path[] paths = new Path[this.paths.length + 1];
		for (int i = 0; i < this.paths.length; i++)
		{
			paths[i] = this.paths[i];
		}
		paths[this.paths.length] = path;
		this.paths = paths;
	}

	public void setAlerts(ArrayList<Alert> alerts)
	{
		this.alerts = alerts;
		obtainedAlerts = true;
	}
	
	public ArrayList<Alert> getAlerts()
	{
		return alerts;
	}

	public boolean obtainedAlerts() {
		return obtainedAlerts;
	}
}
