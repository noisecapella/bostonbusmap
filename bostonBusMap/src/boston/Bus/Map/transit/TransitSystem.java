package boston.Bus.Map.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;

import boston.Bus.Map.util.Constants;
/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
public class TransitSystem {
	private static final double annArborLatitude = 42.277;
	private static final double annArborLongitude = -83.74;
	
	private static final String website = "http://www.terribleinformation.org/george/umichbusmap";
	
	
	public static double getCenterLat() {
		return annArborLatitude;
	}

	public static double getCenterLon() {
		return annArborLongitude;
	}

	public static int getCenterLatAsInt()
	{
		return (int)(annArborLatitude * Constants.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(annArborLongitude * Constants.E6);
	}

	public static String getWebSite() {
		return website;
	}
	

	
	private final static HashMap<String, TransitSource> transitSources = new HashMap<String, TransitSource>();  
	private static TransitSource defaultTransitSource;
	
	public static void addTransitSource(String route, TransitSource source)
	{
		transitSources.put(route, source);
	}
	
	public static void setDefaultTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		if (defaultTransitSource == null)
		{
			defaultTransitSource = new UMichTransitSource(busStop);
		}
	}
	
	public static TransitSource getTransitSource(String routeToUpdate) {
		if (null == routeToUpdate)
		{
			return defaultTransitSource;
		}
		else
		{
			
			TransitSource transitSource = transitSources.get(routeToUpdate);
			if (transitSource == null)
			{
				return defaultTransitSource;
			}
			else
			{
				return transitSource;
			}
		}
	}
}
