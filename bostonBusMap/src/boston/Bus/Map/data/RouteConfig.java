package boston.Bus.Map.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class RouteConfig implements CanBeSerialized
{

	private final HashMap<String, StopLocation> stops;
	private final ArrayList<Path> paths;
	private final String route;
	
	public RouteConfig(String route)
	{
		this.route = route;
		stops = new HashMap<String, StopLocation>();
		paths = new ArrayList<Path>();
	}
	
	
	
	public void addStop(String id, StopLocation stopLocation) {
		stops.put(id, stopLocation);
	}
	
	public StopLocation getStop(String tag)
	{
		return stops.get(tag);
	}

	public HashMap<String, StopLocation> getStopMapping()
	{
		return stops;
	}
	
	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	

	public String getRouteName() {
		return route;
	}



	public void addPath(Path currentPath) {
		paths.add(currentPath);
		
	}

	public ArrayList<Path> getPaths() {
		return paths;
	}


	@Override
	public void serialize(Box dest) throws IOException {
		
		dest.writeString(route);
		dest.writeStopsMap(stops);
		dest.writePathsList(paths);
	}

	public RouteConfig(Box source, Drawable busStop, HashMap<String, String> routeKeysToTitles) throws IOException {
		route = source.readString();
		stops = source.readStopsMap(this, busStop, routeKeysToTitles);
		paths = source.readPathsList();
	}



	public boolean isSubway() {
		return TransitSystem.isSubway(route);
	}
}
