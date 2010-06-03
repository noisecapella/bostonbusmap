package boston.Bus.Map;

import java.util.Comparator;


public class LocationComparator implements Comparator<Location>
{
	/**
	 * Center latitude in radians
	 */
	private final double centerLatitude;
	/**
	 * Center longitude in radians
	 */
	private final double centerLongitude;
	
	public LocationComparator(double centerLatitude, double centerLongitude)
	{
		this.centerLatitude = centerLatitude * degreesToRadians;
		this.centerLongitude = centerLongitude * degreesToRadians;
	}
	
	/**
	 * Sort by distance to the center
	 */
	@Override
	public int compare(Location arg0, Location arg1)
	{
		double dist = arg0.distanceFrom(centerLatitude, centerLongitude);
		double otherDist = arg1.distanceFrom(centerLatitude, centerLongitude);
		
		return Double.compare(dist, otherDist);
	}
	
	/**
	 * Used in calculating the distance between coordinates
	 */
	private static final double radiusOfEarthInKilo = 6371.2;
	private static final double kilometersPerMile = 1.609344;
	
	public static final double degreesToRadians = Math.PI / 180.0;
	

	
	/**
	 * @param lat1 latitude in radians
	 * @param lat2 longitude in radians
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	public static double computeDistance(double lat1, double lon1, double lat2, double lon2)
	{
		//great circle distance
		double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		dist *= radiusOfEarthInKilo;
		dist /= kilometersPerMile;
		
		return dist;
	}
	

}
