package com.schneeloch.bostonbusmap_library.data;

import java.util.Comparator;

import com.schneeloch.bostonbusmap_library.math.Geometry;

public class LocationComparator implements Comparator<com.schneeloch.bostonbusmap_library.data.Location> {
	private final double centerLatitudeAsRadians; 
	private final double centerLongitudeAsRadians;


	public LocationComparator(double centerLatitudeAsDegrees, double centerLongitudeAsDegrees)
	{
		centerLatitudeAsRadians = centerLatitudeAsDegrees * Geometry.degreesToRadians;
		centerLongitudeAsRadians = centerLongitudeAsDegrees * Geometry.degreesToRadians;
	}

	public int compare(Location a, Location b)
	{
		double dist = a.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);
		double otherDist = b.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians);

		return java.lang.Double.compare(dist, otherDist);
	}

}
