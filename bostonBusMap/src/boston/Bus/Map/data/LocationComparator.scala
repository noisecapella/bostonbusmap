package boston.Bus.Map.data

import java.util.Comparator

class LocationComparator(centerLatitude:Double, centerLongitude:Double) extends Comparator[Location]
{
	private val degreesToRadians:Double = java.lang.Math.PI / 180.0;
	
	private val centerLatitudeAsRadians = centerLatitude * degreesToRadians
	private val centerLongitudeAsRadians = centerLongitude * degreesToRadians
	
	override def compare(a:Location, b:Location):Int =
	{
		val dist = a.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians)
		val otherDist = b.distanceFrom(centerLatitudeAsRadians, centerLongitudeAsRadians)
		
		return java.lang.Double.compare(dist, otherDist)
	}
}