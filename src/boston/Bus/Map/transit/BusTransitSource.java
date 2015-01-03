package boston.Bus.Map.transit;

import boston.Bus.Map.data.ITransitDrawables;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitSourceTitles;

public class BusTransitSource extends NextBusTransitSource {

	public BusTransitSource(TransitSystem transitSystem, ITransitDrawables drawables, TransitSourceTitles routeTitles, RouteTitles allRouteTitles)
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
