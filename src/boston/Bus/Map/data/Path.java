package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;

public class Path implements CanBeSerialized
{
	private final float[] points;
	
	/**
	 * points is a series of lat lon pairs. Therefore points must have an even number of elements
	 * @param points
	 */
	public Path(ArrayList<Float> points)
	{
		this.points = new float[points.size()];
		for (int i = 0; i < points.size(); i++)
		{
			this.points[i] = points.get(i);
		}
	}

	@Override
	public void serialize(Box dest) throws IOException {
		dest.writeInt(points.length);
		for (float f : points)
		{
			dest.writeFloat(f);
		}
	}
	
	public Path(Box source) throws IOException {
		int size = source.readInt();
		points = new float[size];
		for (int i = 0; i < size; i++)
		{
			points[i] = source.readFloat();
		}
	}

	/**
	 * get the latitude of a point
	 * @param i
	 * @return
	 */
	public float getPointLat(int i) {
		return points[i * 2];
	}
	/**
	 * get the longitude of a point
	 * @param i
	 * @return
	 */
	public float getPointLon(int i) {
		return points[i*2 + 1];
	}

	/**
	 * get the number of points
	 * @return
	 */
	public int getPointsSize() {
		//divide by half
		return points.length >> 1;
	}
}
