package boston.Bus.Map.transit;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.parser.RouteConfigFeedParser;
import boston.Bus.Map.util.DownloadHelper;

public class MBTABusTransitSource implements TransitSource
{
	private final Drawable busStop;
	
	public MBTABusTransitSource(Drawable busStop)
	{
		this.busStop = busStop;
	}
	
	
	@Override
	public void populateStops(RoutePool routeMapping, String routeToUpdate,
			RouteConfig oldRouteConfig, Directions directions, HashMap<String, String> routeKeysToTitles) 
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException 
	{
		final String urlString = TransitSystem.getRouteConfigUrl(routeToUpdate);

		DownloadHelper downloadHelper = new DownloadHelper(urlString);
		
		downloadHelper.connect();
		//just initialize the route and then end for this round
		
		RouteConfigFeedParser parser = new RouteConfigFeedParser(busStop, directions, routeKeysToTitles, oldRouteConfig);

		parser.runParse(downloadHelper.getResponseData()); 

		parser.writeToDatabase(routeMapping, false);
		
	}

}
