package boston.Bus.Map.data

object LocationConstants {
	/**
	 * Used in calculating the distance between coordinates
	 */
	private val radiusOfEarthInKilo = 6371.2
	private val kilometersPerMile = 1.609344
	
	private val radiusOfEarthInMiles = radiusOfEarthInKilo / kilometersPerMile
	

	
	/**
	 * @param lat1 latitude in radians
	 * @param lat2 longitude in radians
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	def computeCompareDistance(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double =
	{
		//great circle distance
		//double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		(((1 - math.cos((lat1 - lat2))) / 2) + math.cos(lat1) * math.cos(lat2) * ((1 - math.cos((lon1 - lon2))) / 2)) * radiusOfEarthInMiles
	}
	


}