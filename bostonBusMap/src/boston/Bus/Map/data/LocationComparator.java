package boston.Bus.Map.data;

import java.util.Comparator;

import boston.Bus.Map.math.Geometry;

public class LocationComparator implements Comparator<boston.Bus.Map.data.Location> {
	private final float centerLatitudeAsRadians; 
	private final float centerLongitudeAsRadians;


	public LocationComparator(float centerLatitude, float centerLongitude)
	{
		centerLatitudeAsRadians = centerLatitude * Geometry.degreesToRadians;
		centerLongitudeAsRadians = centerLongitude * Geometry.degreesToRadians;
	}

	public int compare(Location a, Location b)
	{
		double dist = a.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);
		double otherDist = b.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);

		int comparison = java.lang.Double.compare(dist, otherDist);
		if (comparison == 0)
		{
			//two different stops or buses at same location
			//if it's equal, the TreeSet may just remove one, assuming equality
			//so we need something else to compare to show that it's not exactly the same stop
			return new Integer(a.getId()).compareTo(b.getId());
		}
		else
		{
			return comparison;
		}
	}

}
