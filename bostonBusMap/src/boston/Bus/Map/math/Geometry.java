package boston.Bus.Map.math;

import android.util.FloatMath;

/**
 * All non-specific math stuff used to calculate angles and distances should go in here
 * @author schneg
 *
 */
public class Geometry {
	
	/**
	 * Used in calculating the distance between coordinates
	 */
	private static final float radiusOfEarthInKilo = 6371.2f;
	private static final float kilometersPerMile = 1.609344f;
	
	private static final float radiusOfEarthInMiles = radiusOfEarthInKilo / kilometersPerMile;

	public static final float degreesToRadians = (float)(java.lang.Math.PI / 180.0f);

	public static final float radiansToDegrees = (float)(180.0f / java.lang.Math.PI);

	private static final float InvPITimes180 = (1.0f / (float)Math.PI) * 180f;
	
	/**
	 * Returns a compare distance, or a distance which is distorted for efficiency sake but comparisons are always correct 
	 * 
	 * @param lat1 latitude in radians
	 * @param lat2 longitude in radians
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	public static float computeCompareDistance(float lat1, float lon1, float lat2, float lon2)
	{
		//great circle distance
		//double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		float dist = (((1.0f - FloatMath.cos(lat1 - lat2)) * 0.5f) + FloatMath.cos(lat1) * FloatMath.cos(lat2) * 
				((1.0f - FloatMath.cos(lon1 - lon2)) * 0.5f)); 
		return dist * radiusOfEarthInMiles;
	}
	
	public static int getDegreesFromSlope(double y, double x)
	{
		double thetaInRadians = Math.atan2(y, x);
		
		int degrees = mathRadiansToDegrees(thetaInRadians);
		return degrees;
	}
	
	/**
	 * Convert from radians starting east and going counterclockwise, to a degree direction starting north and going clockwise
	 * 
	 * @param thetaBackup direction in radians, where east is 0 and going counterclockwise
	 * @return a descriptive String showing the direction (for example: E (90 deg))
	 */
	public static int mathRadiansToDegrees(double thetaAsRadians)
	{
		//NOTE: degrees will be 0 == north, going clockwise
		int degrees = (int)(thetaAsRadians * InvPITimes180);
		if (degrees < 0)
		{
			degrees += 360;
		}
		
		//convert to usual compass orientation
		degrees = -degrees + 90;
		if (degrees < 0)
		{
			degrees += 360;
		}
		
		return degrees;
	}

	
}
