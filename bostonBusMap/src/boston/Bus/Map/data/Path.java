package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Path {
	private final TreeMap<Integer, Point> points = new TreeMap<Integer, Point>();
	private final int id;
	
	public Path(int id)
	{
		this.id = id;
	}
	
	public void addPoint(int id, double lat, double lon)
	{
		points.put(id, new Point(id, lat, lon));
	}
	
	public int getId()
	{
		return id;
	}

	public TreeMap<Integer, Point> getPoints()
	{
		return points;
	}
}
