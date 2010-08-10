package boston.Bus.Map.data;

public class LocationConstants {
	/**
	 * Used in calculating the distance between coordinates
	 */
	private static final double radiusOfEarthInKilo = 6371.2;
	private static final double kilometersPerMile = 1.609344;
	
	private static final double radiusOfEarthInMiles = radiusOfEarthInKilo / kilometersPerMile;
	

	
	/**
	 * @param lat1 latitude in radians
	 * @param lat2 longitude in radians
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	public static double computeCompareDistance(double lat1, double lon1, double lat2, double lon2)
	{
		//great circle distance
		//double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		return (((1 - Math.cos((lat1 - lat2))) / 2) + Math.cos(lat1) * Math.cos(lat2) * 
				((1 - Math.cos((lon1 - lon2))) / 2)) * radiusOfEarthInMiles;
	}
	

}
