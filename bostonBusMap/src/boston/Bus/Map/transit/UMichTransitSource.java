package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import boston.Bus.Map.parser.UMichFeedParser;
import boston.Bus.Map.parser.UMichInitialFeedParser;
import boston.Bus.Map.util.DownloadHelper;

public class UMichTransitSource implements TransitSource
{
	private final Drawable busStop;
	public UMichTransitSource(Drawable busStop)
	{
		this.busStop = busStop;
	}
	
	private static final String dataUrl = "http://mbus.pts.umich.edu/shared/public_feed.xml";
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions,
			HashMap<String, String> routeKeysToTitles)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		//do nothing. we always have all the information we need
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			int selectedBusPredictions, int maxStops, float centerLatitude,
			float centerLongitude, HashMap<Integer, BusLocation> busMapping,
			String selectedRoute, RoutePool routePool, Directions directions,
			Locations locationsObj, HashMap<String, String> routeKeysToTitles)
			throws IOException, ParserConfigurationException, SAXException {
		UMichFeedParser parser = new UMichFeedParser(directions, routeKeysToTitles, busStop);
		
		URL url = new URL(dataUrl);
		InputStream data = url.openStream();
		parser.runParse(data);
		
		routePool.writeToDatabase(parser.getMapping(), true);
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
		UMichInitialFeedParser parser = new UMichInitialFeedParser(directions, routeKeysToTitles, busStop);
		URL url = new URL(dataUrl);
		InputStream data = url.openStream();
		parser.runParse(data);
		
		routeMapping.writeToDatabase(parser.getMapping(), true);
	}

}
