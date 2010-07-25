package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Path {
	private final ArrayList<Point> points = new ArrayList<Point>();
	private final int id;
	
	public Path(int id)
	{
		this.id = id;
	}
	
	public void addPoint(double lat, double lon)
	{
		points.add(new Point(lat, lon));
	}
	
	public int getId()
	{
		return id;
	}

	public void condense() {
		if (points.size() > 2)
		{
			Point first = points.get(0);
			Point last = points.get(points.size() - 1);
			points.clear();
			points.add(first);
			points.add(last);
		}
		
	}

	public ArrayList<Point> getPoints() {
		return points;
	}
}
