package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.parser.AlertParser;
import boston.Bus.Map.util.DownloadHelper;

public class BusTransitSource extends NextBusTransitSource {

	public BusTransitSource(TransitSystem transitSystem, TransitDrawables drawables, TransitSourceTitles routeTitles, RouteTitles allRouteTitles)
	{
		super(transitSystem, drawables, "mbta", routeTitles, allRouteTitles);
	}
	

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		//TODO: don't hard code this
		if ("sl1".equals(lowercaseQuery) || 
				"sl2".equals(lowercaseQuery) ||
				"sl".equals(lowercaseQuery) ||
				"sl4".equals(lowercaseQuery) ||
				"sl5".equals(lowercaseQuery))
		{
			lowercaseQuery = "silverline" + lowercaseQuery;
		}
		else if (lowercaseQuery.startsWith("silver") && lowercaseQuery.contains("line") == false)
		{
			//ugh, what a hack
			lowercaseQuery = lowercaseQuery.substring(0, 6) + "line" + lowercaseQuery.substring(6);
		}
		
		return super.searchForRoute(indexingQuery, lowercaseQuery);
	}
}
