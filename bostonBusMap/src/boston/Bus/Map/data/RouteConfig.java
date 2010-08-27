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

	private final HashMap<String, StopLocation> stops;
	private final ArrayList<String> dirTags;
	private final ArrayList<String> directionTitles;
	private final ArrayList<String> directionNames;
	private final ArrayList<Path> paths;
	private final String route;
	
	public RouteConfig(String route)
	{
		this.route = route;
		stops = new HashMap<String, StopLocation>();
		directionTitles = new ArrayList<String>();
		directionNames = new ArrayList<String>();
		dirTags = new ArrayList<String>();
		paths = new ArrayList<Path>();
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
		int pos = dirTags.indexOf(dirTag);
		if (pos != -1)
		{
			return directionTitles.get(pos);
		}
		else
		{
			return "";
		}
	}

	/*public String getDirectionName(String dirTag)
	{
		int pos = dirTags.indexOf(dirTag);
		if (pos != -1)
		{
			return directionNames.get(pos);
		}
		else
		{
			return "";
		}
	}*/

	
	
	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	public void addDirection(String tag, String title, String name) {
		dirTags.add(tag);
		directionTitles.add(title);
		directionNames.add(name);
	}



	public String getRouteName() {
		return route;
	}



	public Collection<String> getDirtags() {
		return directionTitles;
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
		dest.writeStringKeyValue(dirTags, directionTitles);
		dest.writeStringKeyValue(dirTags, directionNames);
		dest.writeStopsMap(stops);
		dest.writePathsList(paths);
		
		//NOTE: optimization, since we don't use directionNames currently
		directionNames.clear();
	}

	public RouteConfig(Box source, Drawable busStop) throws IOException {
		route = source.readString();
		Object[] objs = source.readStringKeyValue();
		dirTags = (ArrayList<String>)objs[0];
		directionTitles = (ArrayList<String>)objs[1];
		
		objs = source.readStringKeyValue();
		directionNames = (ArrayList<String>)objs[1];
		
		stops = source.readStopsMap(this, busStop);
		paths = source.readPathsList();
		
		//NOTE: optimization, since we don't use directionNames currently
		directionNames.clear();
	}
}
