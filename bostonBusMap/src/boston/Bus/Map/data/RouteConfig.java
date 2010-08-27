package boston.Bus.Map.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class RouteConfig implements CanBeSerialized
{

	private final HashMap<String, StopLocation> stops = new HashMap<String, StopLocation>();
	private final HashMap<String, String> directionTitles = new HashMap<String, String>();
	private final HashMap<String, String> directionNames = new HashMap<String, String>();
	private final TreeMap<Integer, Path> paths = new TreeMap<Integer, Path>();
	private final String route;
	
	public RouteConfig(String route)
	{
		this.route = route;
	}
	
	
	
	public void addStop(String id, StopLocation stopLocation) {
		stops.put(id, stopLocation);
	}
	
	public StopLocation getStop(String tag)
	{
		return stops.get(tag);
	}

	
	public String getDirectionTitle(String dirTag)
	{
		if (directionTitles.containsKey(dirTag))
		{
			return directionTitles.get(dirTag);
		}
		else
		{
			return "";
		}
	}

	public String getDirectionName(String dirTag)
	{
		if (directionNames.containsKey(dirTag))
		{
			return directionNames.get(dirTag);
		}
		else
		{
			return "";
		}
	}

	
	
	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	public void addDirection(String tag, String title, String name) {
		directionTitles.put(tag, title);
		directionNames.put(tag, name);
	}



	public String getRouteName() {
		return route;
	}



	public Collection<String> getDirtags() {
		return directionTitles.keySet();
	}



	public void addPath(int id, Path currentPath) {
		paths.put(id, currentPath);
		
	}

	public SortedMap<Integer, Path> getPaths() {
		return paths;
	}


	@Override
	public void serialize(Box dest) throws IOException {
		
		dest.writeString(route);
		dest.writeStringMap(directionTitles);
		dest.writeStringMap(directionNames);
		dest.writeStopsMap(stops);
		dest.writePathsMap(paths);
		
	}

	public RouteConfig(Box source, Drawable busStop) throws IOException {
		route = source.readString();

		source.readStringMap(directionTitles);
		source.readStringMap(directionNames);
		source.readStopsMap(stops, this, busStop);
		source.readPathsMap(paths);
	}
}
