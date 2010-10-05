package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.R.string;
import android.graphics.drawable.Drawable;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
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
	

	
	private final HashMap<String, TransitSource> transitSourceMap = new HashMap<String, TransitSource>();
	private final ArrayList<TransitSource> transitSources = new ArrayList<TransitSource>();
	
	/**
	 * Be careful with this; this stays around forever since it's static
	 */
	private TransitSource defaultTransitSource;
	
	public void setDefaultTransitSource(Drawable busStop, Drawable bus, Drawable arrow, Drawable rail)
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
			
			TransitSource transitSource = transitSourceMap.get(routeToUpdate);
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
		if (transitSources.size() > 1)
		{
			ArrayList<String> ret = new ArrayList<String>();

			for (TransitSource source : transitSources)
			{
				if (source.getRoutes() != null)
				{
					for (String route : source.getRoutes())
					{
						ret.add(route);
					}
				}
			}
			
			return ret.toArray(new String[0]);
		}
		else
		{
			String[] routes = defaultTransitSource.getRoutes();
			return routes;
		}
	}

	public HashMap<String, String> getRouteKeysToTitles() {
		if (transitSources.size() <= 1)
		{
			return defaultTransitSource.getRouteKeysToTitles();
		}
		else
		{
			HashMap<String, String> ret = new HashMap<String, String>();
			
			for (TransitSource source : transitSources)
			{
				ret.putAll(source.getRouteKeysToTitles());
			}
			
			return ret;
		}
	}

	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude, HashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool,
			Directions directions, Locations locations) throws IOException, ParserConfigurationException, SAXException {
		for (TransitSource source : transitSources)
		{
			source.refreshData(routeConfig, selectedBusPredictions, maxStops, centerLatitude,
					centerLongitude, busMapping, selectedRoute, routePool, directions, locations);
		}
	}
}
