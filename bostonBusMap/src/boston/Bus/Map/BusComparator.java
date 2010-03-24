package boston.Bus.Map;

import java.util.Comparator;


public class BusComparator implements Comparator<BusLocation>
{
	/**
	 * Center latitude in radians
	 */
	private final double centerLatitude;
	/**
	 * Center longitude in radians
	 */
	private final double centerLongitude;
	
	public BusComparator(double centerLatitude, double centerLongitude)
	{
		this.centerLatitude = centerLatitude * (Math.PI / 180.0);
		this.centerLongitude = centerLongitude * (Math.PI / 180.0);
	}
	
	/**
	 * Sort by distance to the center
	 */
	@Override
	public int compare(BusLocation arg0, BusLocation arg1)
	{
		final double centerLatitude = this.centerLatitude;
		final double centerLongitude = this.centerLongitude;
		
		double dist = arg0.distanceFrom(centerLatitude, centerLongitude);
		double otherDist = arg1.distanceFrom(centerLatitude, centerLongitude);
		
		return Double.compare(dist, otherDist);
	}
}
