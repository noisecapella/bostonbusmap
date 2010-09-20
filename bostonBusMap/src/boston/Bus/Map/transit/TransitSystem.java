package boston.Bus.Map.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.util.Constants;
/**
 * Any transit-system specific stuff should go here, if possible
 * @author schneg
 *
 */
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
		return (int)(bostonLatitude * Constants.E6);
	}
	
	public static int getCenterLonAsInt()
	{
		return (int)(bostonLongitude * Constants.E6);
	}

	public static String getWebSite() {
		return website;
	}
	

	
	private static final String subwayPredictionsDataUrl = "http://developer.mbta.com/Data/";
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
			defaultTransitSource = new MBTABusTransitSource(busStop, bus, arrow);
			SubwayTransitSource subwayTransitSource = new SubwayTransitSource(busStop, bus, arrow);
			transitSources.put(SubwayTransitSource.RedLine, subwayTransitSource);
			transitSources.put(SubwayTransitSource.OrangeLine, subwayTransitSource);
			transitSources.put(SubwayTransitSource.BlueLine, subwayTransitSource);
		}
	}
	
/*		if (isSubway(route) && route != null)
		{
			return subwayPredictionsDataUrl + route + ".json";
		}*/
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
