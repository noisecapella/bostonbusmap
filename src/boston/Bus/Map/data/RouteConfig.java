package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import boston.Bus.Map.data.StopLocation.Builder;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.IBox;
import boston.Bus.Map.util.IBox;

public class RouteConfig
{

	private final ImmutableMap<String, StopLocation> stops;
	private Path[] paths;
	private final String route;
	private final String routeTitle;
	private final int listorder;
	private final int transitSourceId;
	
	private final int color;
	private final int oppositeColor;
	
	private final TransitSource transitSource;
	
	public static final Path[] nullPaths = new Path[0];
	private ImmutableSet<Alert> alerts = ImmutableSet.of();
	
	private boolean obtainedAlerts;

	private RouteConfig(Builder builder, ImmutableMap<String, StopLocation> stops) throws IOException {
		this.route = builder.route;
		this.routeTitle = builder.routeTitle;
		this.stops = stops;
		
		this.color = builder.color;
		this.oppositeColor = builder.oppositeColor;
		this.transitSource = builder.transitSource;

		if (builder.serializedPath.isEmpty() == false)
		{
			paths = builder.serializedPath.readPathsList();
		}
		else
		{
			paths = nullPaths;
		}
		
		this.listorder = builder.listorder;
		this.transitSourceId = builder.transitSourceId;
	}
	
	private RouteConfig(Builder builder)
			throws IOException {
		this(builder, ImmutableMap.copyOf(builder.stops));
	}

	public static class Builder {
		private final String route;
		private final String routeTitle;
		private final int color;
		private final int oppositeColor;
		private final TransitSource transitSource;
		private final IBox serializedPath;
		private final Map<String, StopLocation> stops = Maps.newHashMap();
		private final List<Path> paths = Lists.newArrayList();
		private final int listorder;
		private final int transitSourceId;
		
		public Builder(String route, String routeTitle, int color, int oppositeColor,
				TransitSource transitSource, int listorder, int transitSourceId) {
			this(route, routeTitle, color, oppositeColor,
					transitSource, listorder, transitSourceId, Box.emptyBox());
		}
		
		public Builder(String route, String routeTitle, int color, int oppositeColor,
				TransitSource transitSource, 
				int listorder, int transitSourceId, IBox serializedPath) {
			this.route = route;
			this.routeTitle = routeTitle;
			this.color = color;
			this.oppositeColor = oppositeColor;
			this.transitSource = transitSource;
			this.serializedPath = serializedPath;
			this.listorder = listorder;
			this.transitSourceId = transitSourceId;
		}
		
		public StopLocation getStop(String tag) {
			return stops.get(tag);
		}

		public void addStop(String stopTag, StopLocation stopLocation) {
			stops.put(stopTag, stopLocation);
		}

		public void addPaths(Path path) {
			paths.add(path);
		}

		public String getRouteName() {
			return route;
		}

		public String getRouteTitle() {
			return routeTitle;
		}

		public int getColor() {
			return color;
		}
		
		public int getOppositeColor() {
			return oppositeColor;
		}
		
		public RouteConfig build() throws IOException {
			return new RouteConfig(this);
		}

		public RouteConfig build(ImmutableMap<String, StopLocation> stops) throws IOException {
			return new RouteConfig(this, stops);
		}

		public boolean containsStop(String stopTag) {
			return stops.containsKey(stopTag);
		}
	}
	

	public StopLocation getStop(String tag)
	{
		return stops.get(tag);
	}

	public ImmutableMap<String, StopLocation> getStopMapping()
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

	public void serializePath(IBox dest) throws IOException
	{
		dest.writePathsList(paths);
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

	public void setAlerts(ImmutableSet<Alert> alerts)
	{
		this.alerts = alerts;
		obtainedAlerts = true;
	}
	
	public ImmutableSet<Alert> getAlerts()
	{
		return alerts;
	}

	public boolean obtainedAlerts() {
		return obtainedAlerts;
	}

	/**
	 * Find what stop has the same location as the given stop
	 * @param stop
	 * @return
	 */
	public String getCrossStopTag(StopLocation otherStop, List<String> stopTagsToChooseFrom) {
		float minDistance = 9999999;
		StopLocation candidate = null;
		for (String stopTag : stopTagsToChooseFrom) {
			StopLocation stop = stops.get(stopTag);
			float distance = Geometry.computeCompareDistanceFloat(stop.getLatitudeAsDegrees(), stop.getLongitudeAsDegrees(), otherStop.getLatitudeAsDegrees(), otherStop.getLongitudeAsDegrees());
			if (distance < minDistance) {
				candidate = stop;
				minDistance = distance;
			}
		}
		return candidate.getStopTag();
	}

	public int getTransitSourceId() {
		return transitSourceId;
	}
	
	public int getListOrder() {
		return listorder;
	}
}
