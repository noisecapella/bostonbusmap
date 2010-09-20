package boston.Bus.Map.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import boston.Bus.Map.transit.TransitSource;
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
	
	private final String color;
	private final String oppositeColor;
	
	private final TransitSource transitSource;
	
	public RouteConfig(String route, String color, String oppositeColor, TransitSource transitAgency)
	{
		this.route = route;
		stops = new HashMap<String, StopLocation>();
		paths = new ArrayList<Path>();
		
		this.color = color;
		this.oppositeColor = oppositeColor;
		this.transitSource = transitAgency;
	}
	
	
	
	public void addStop(String id, StopLocation stopLocation) {
		stops.put(id, stopLocation);
	}
	
	public StopLocation getStop(String tag)
	{
		return stops.get(tag);
	}

		
	
	public Collection<StopLocation> getStops() {
		return stops.values();
	}

	public HashMap<String, StopLocation> getStopMapping()
	{
		return stops;
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

	public String getColor()
	{
		return color;
	}
	

	@Override
	public void serialize(Box dest) throws IOException {
		
		dest.writeString(route);
		dest.writeStringUnique(color);
		dest.writeString(oppositeColor);
		dest.writeStopsMap(stops);
		dest.writePathsList(paths);
	}

	public RouteConfig(Box source, Drawable busStop, HashMap<String, String> routeKeysToTitles,
			TransitSource transitAgency) throws IOException {
		route = source.readString();
		color = source.readStringUnique();
		oppositeColor = source.readString();
		stops = source.readStopsMap(this, busStop, routeKeysToTitles);
		paths = source.readPathsList();
		
		this.transitSource = transitAgency;
		
	}



	public TransitSource getTransitSource() {
		return transitSource;
	}
	
	public boolean hasPaths()
	{
		return transitSource.hasPaths();
	}
}
