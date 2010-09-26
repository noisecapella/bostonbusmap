package boston.Bus.Map.transit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.string;
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
	

	
	private final HashMap<String, TransitSource> transitSources = new HashMap<String, TransitSource>();  
	/**
	 * Be careful with this; this stays around forever since it's static
	 */
	private TransitSource defaultTransitSource;
	
	public void addTransitSource(String route, TransitSource source)
	{
		transitSources.put(route, source);
	}
	
	public void setDefaultTransitSource(Drawable busStop, Drawable bus, Drawable arrow)
	{
		if (defaultTransitSource == null)
		{
			defaultTransitSource = new UMichTransitSource(busStop);
		}
	}
	
	public TransitSource getTransitSource(String routeToUpdate) {
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

	public String[] getRoutes() {
		String[] routes = defaultTransitSource.getRoutes();
		if (transitSources.size() != 0)
		{
			ArrayList<String> ret = new ArrayList<String>();
			for (String route : routes)
			{
				ret.add(route);
			}

			for (TransitSource source : transitSources.values())
			{
				for (String route : source.getRoutes())
				{
					ret.add(route);
				}
			}
			
			return ret.toArray(new String[0]);
		}
		else
		{
			return routes;
		}
	}

	public HashMap<String, String> getRouteKeysToTitles() {
		if (transitSources.size() == 0)
		{
			return defaultTransitSource.getRouteKeysToTitles();
		}
		else
		{
			HashMap<String, String> ret = new HashMap<String, String>();
			
			ret.putAll(defaultTransitSource.getRouteKeysToTitles());
			
			for (TransitSource source : transitSources.values())
			{
				ret.putAll(source.getRouteKeysToTitles());
			}
			
			return ret;
		}
	}
}
