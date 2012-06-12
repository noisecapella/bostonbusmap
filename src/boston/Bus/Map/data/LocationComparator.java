package boston.Bus.Map.data;

import java.util.Comparator;

import boston.Bus.Map.math.Geometry;

public class LocationComparator implements Comparator<LocationGroup> {
	private final double centerLatitudeAsRadians; 
	private final double centerLongitudeAsRadians;


	public LocationComparator(double centerLatitudeAsDegrees, double centerLongitudeAsDegrees)
	{
		centerLatitudeAsRadians = centerLatitudeAsDegrees * Geometry.degreesToRadians;
		centerLongitudeAsRadians = centerLongitudeAsDegrees * Geometry.degreesToRadians;
	}

	@Override
	public int compare(LocationGroup a, LocationGroup b)
	{
		if (a.getLatitudeAsDegrees() == b.getLatitudeAsDegrees() &&
				a.getLongitudeAsDegrees() == b.getLongitudeAsDegrees())
		{
			return 0;
		}
		
		double dist = a.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);
		double otherDist = b.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);

		return Double.compare(dist, otherDist);
	}

}
