package boston.Bus.Map.data;

public class Point {
	public final double lat;
	public final double lon;
	private final int id;
	
	public Point(int id, double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
}
