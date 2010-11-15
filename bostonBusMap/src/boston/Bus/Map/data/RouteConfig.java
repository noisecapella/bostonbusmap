package boston.Bus.Map.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RouteConfig
{

	private final HashMap<String, StopLocation> stops;
	private final ArrayList<Path> paths;
	private final String route;
	
	private final int color;
	private final int oppositeColor;
	
	private final TransitSource transitSource;
	
	public RouteConfig(String route, int color, int oppositeColor, TransitSource transitAgency) throws IOException
	{
		this(route, color, oppositeColor, transitAgency, null);
	}
	
	public void addStop(String tag, StopLocation stopLocation) {
		stops.put(tag, stopLocation);
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

	public int getColor()
	{
		return color;
	}

	public void serializePath(Box dest) throws IOException
	{
		dest.writePathsList(paths);
	}
	
	public RouteConfig(String route, int color, int oppositeColor, TransitSource transitAgency, Box serializedPath)
			throws IOException {
		this.route = route;
		stops = new HashMap<String, StopLocation>();
		
		this.color = color;
		this.oppositeColor = oppositeColor;
		this.transitSource = transitAgency;

		if (serializedPath != null)
		{
			paths = serializedPath.readPathsList();
		}
		else
		{
			paths = new ArrayList<Path>();
		}
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
}
