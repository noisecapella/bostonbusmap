package boston.Bus.Map.data;

import java.util.Comparator;

public class LocationComparator implements Comparator<boston.Bus.Map.data.Location> {
	private static final double degreesToRadians = java.lang.Math.PI / 180.0;


	private final double centerLatitudeAsRadians; 
	private final double centerLongitudeAsRadians;


	public LocationComparator(double centerLatitude, double centerLongitude)
	{
		centerLatitudeAsRadians = centerLatitude * degreesToRadians;
		centerLongitudeAsRadians = centerLongitude * degreesToRadians;
	}

	public int compare(Location a, Location b)
	{
		double dist = a.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);
		double otherDist = b.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);

		return java.lang.Double.compare(dist, otherDist);
	}

}
