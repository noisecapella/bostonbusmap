package boston.Bus.Map.transit;

import boston.Bus.Map.main.Main;

public class TransitSystem {
	private static final double bostonLatitude = 42.3583333;
	private static final double bostonLongitude = -71.0602778;
	
	private static final String website = "http://www.terribleinformation.org/george/bostonbusmap";
	
	
	public static double getCenterLat() {
		return bostonLatitude;
	}

	public static double getCenterLon() {
		return bostonLongitude;
	}

	public static int getCenterLatAsInt()
	{
		return (int)(bostonLatitude * Main.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(bostonLongitude * Main.E6);
	}

	public static String getWebSite() {
		return website;
	}
	
}
