package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.main.UpdateAsyncTask;

public class UMichTransitSource implements TransitSource
{
	private final Drawable busStop;
	public UMichTransitSource(Drawable busStop)
	{
		this.busStop = busStop;
	}
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions,
			HashMap<String, String> routeKeysToTitles)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude, HashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj, HashMap<String, String> routeKeysToTitles)
			throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasPaths() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initializeAllRoutes(UpdateAsyncTask task, Context context,
			Directions directions, HashMap<String, String> routeKeysToTitles,
			RoutePool routeMapping) throws IOException,
			ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		
	}

}
