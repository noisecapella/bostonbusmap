package boston.Bus.Map;

import java.util.Comparator;

import boston.Bus.Map.BusLocations.BusLocation;

public class BusComparator implements Comparator<BusLocation>
{
	private final double centerLatitude;
	private final double centerLongitude;
	
	public BusComparator(double centerLatitude, double centerLongitude)
	{
		this.centerLatitude = centerLatitude;
		this.centerLongitude = centerLongitude;
	}
	
	/**
	 * Sort by distance to the center
	 */
	@Override
	public int compare(BusLocation arg0, BusLocation arg1)
	{
		double dist = arg0.distanceFrom(centerLatitude * (Math.PI / 180.0), centerLongitude * (Math.PI / 180.0));
		double otherDist = arg1.distanceFrom(centerLatitude * (Math.PI / 180.0), centerLongitude * (Math.PI / 180.0));
		
		return Double.compare(dist, otherDist);
	}
}
