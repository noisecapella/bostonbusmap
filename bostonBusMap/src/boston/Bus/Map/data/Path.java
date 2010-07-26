package boston.Bus.Map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;

import android.os.Parcel;
import android.os.Parcelable;

public class Path implements CanBeSerialized
{
	//private final ArrayList<Point> points = new ArrayList<Point>();
	public double firstLat;
	public double firstLon;
	public double lastLat;
	public double lastLon;
	private boolean isFirst = true;
	private final int id;
	
	public Path(int id)
	{
		this.id = id;
	}
	
	public void addPoint(double lat, double lon)
	{
		if (isFirst)
		{
			firstLat = lat;
			firstLon = lon;
			isFirst = false;
		}
		else
		{
			lastLat = lat;
			lastLon = lon;
		}
	}
	
	public int getId()
	{
		return id;
	}

	public void condense() {
		/*if (points.size() > 2)
		{
			Point first = points.get(0);
			Point last = points.get(points.size() - 1);
			points.clear();
			points.add(first);
			points.add(last);
		}*/
		
	}

	public double getFirstLat() {
		return firstLat;
	}
	public double getFirstLon() {
		return firstLon;
	}
	public double getLastLat() {
		return lastLat;
	}
	public double getLastLon() {
		return lastLon;
	}

	@Override
	public void serialize(Box dest) throws IOException {
		dest.writeInt(id);
		dest.writeDouble(firstLat);
		dest.writeDouble(firstLon);
		dest.writeDouble(lastLat);
		dest.writeDouble(lastLon);
	}
	
	public Path(Box source) throws IOException {
		id = source.readInt();

		double firstLat = source.readDouble();
		double firstLon = source.readDouble();
		double lastLat = source.readDouble();
		double lastLon = source.readDouble();

		addPoint(firstLat, firstLon);
		addPoint(lastLat, lastLon);
	}

	public double getPointLat(int i) {
		if (i == 0)
		{
			return firstLat;
		}
		else
		{
			return lastLat;
		}
	}
	public double getPointLon(int i) {
		if (i == 0)
		{
			return firstLon;
		}
		else
		{
			return lastLon;
		}
	}

	public int getPointsSize() {
		if (lastLon == 0)
		{
			if (firstLon == 0)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
		else
		{
			return 2;
		}
	}
}
