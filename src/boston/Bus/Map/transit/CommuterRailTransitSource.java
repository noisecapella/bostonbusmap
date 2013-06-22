package boston.Bus.Map.transit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.Log;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CommuterRailStopLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.parser.CommuterRailPredictionsFeedParser;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.DownloadHelper;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.SearchHelper;

public class CommuterRailTransitSource implements TransitSource {
	private final TransitDrawables drawables;
	private final TransitSourceTitles routeTitles;
	private final TransitSystem transitSystem;
	
	public static final int COLOR = 0x940088;
	
	private final ImmutableMap<String, String> routesToUrls;
	
	public CommuterRailTransitSource(TransitDrawables drawables,
			TransitSourceTitles routeTitles,
			TransitSystem transitSystem)
	{
		this.drawables = drawables;
		this.routeTitles = routeTitles;
		this.transitSystem = transitSystem;
		
		final String predictionsUrlSuffix = ".json";
		final String dataUrlPrefix = "http://developer.mbta.com/lib/RTCR/RailLine_";
		
		ImmutableMap.Builder<String, String> urlBuilder = ImmutableMap.builder();
		urlBuilder.put("CR-Greenbush", dataUrlPrefix + 1 + predictionsUrlSuffix);
		urlBuilder.put("CR-Kingston", dataUrlPrefix + 2 + predictionsUrlSuffix);
		urlBuilder.put("CR-Middleborough", dataUrlPrefix + 3 + predictionsUrlSuffix);
		urlBuilder.put("CR-Fairmount", dataUrlPrefix + 4 + predictionsUrlSuffix);
		urlBuilder.put("CR-Providence", dataUrlPrefix + 5 + predictionsUrlSuffix);
		urlBuilder.put("CR-Franklin", dataUrlPrefix + 6 + predictionsUrlSuffix);
		urlBuilder.put("CR-Needham", dataUrlPrefix + 7 + predictionsUrlSuffix);
		urlBuilder.put("CR-Worcester", dataUrlPrefix + 8 + predictionsUrlSuffix);
		urlBuilder.put("CR-Fitchburg", dataUrlPrefix + 9 + predictionsUrlSuffix);
		urlBuilder.put("CR-Lowell", dataUrlPrefix + 10 + predictionsUrlSuffix);
		urlBuilder.put("CR-Haverhill", dataUrlPrefix + 11 + predictionsUrlSuffix);
		urlBuilder.put("CR-Newburyport", dataUrlPrefix + 12 + predictionsUrlSuffix);
		routesToUrls = urlBuilder.build();
	}

	@Override
	public void refreshData(RouteConfig routeConfig,
			Selection selection, int maxStops, double centerLatitude,
			double centerLongitude,
			ConcurrentHashMap<String, BusLocation> busMapping,
			RoutePool routePool, Directions directions,
			Locations locationsObj) throws IOException,
			ParserConfigurationException, SAXException
	{
		int selectedBusPredictions = selection.getMode();
		if (selectedBusPredictions == Selection.VEHICLE_LOCATIONS_ALL)
		{
			//for now I'm only refreshing data for buses if this is checked
			return;
		}
		
		
		List<RefreshData> outputData = Lists.newArrayList();
		switch (selectedBusPredictions)
		{
		case  Selection.BUS_PREDICTIONS_ONE:
		case Selection.VEHICLE_LOCATIONS_ONE:
		{

			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);

			//ok, do predictions now
			getPredictionsUrl(locations, maxStops, routeConfig.getRouteName(), outputData, selectedBusPredictions);
			break;
		}
		case Selection.BUS_PREDICTIONS_ALL:
		case Selection.VEHICLE_LOCATIONS_ALL:
		case Selection.BUS_PREDICTIONS_STAR:
		{
			List<Location> locations = locationsObj.getLocations(maxStops, centerLatitude, centerLongitude, false, selection);
			
			getPredictionsUrl(locations, maxStops, null, outputData, selectedBusPredictions);

		}
		break;

		}

		for (RefreshData outputRow : outputData)
		{
			String url = outputRow.url;
			DownloadHelper downloadHelper = new DownloadHelper(url);
			
			downloadHelper.connect();
			

			InputStream stream = downloadHelper.getResponseData();
			InputStreamReader data = new InputStreamReader(stream);
			//StringReader data = new StringReader(hardcodedData);

			//bus prediction

			String route = outputRow.route;
			RouteConfig railRouteConfig = routePool.get(route);
			CommuterRailPredictionsFeedParser parser = new CommuterRailPredictionsFeedParser(railRouteConfig, directions,
					busMapping, locationsObj.getRouteTitles());

			parser.runParse(data);
			data.close();
		}
	}

	private static class RefreshData {
		private final String url;
		private final String route;
		
		public RefreshData(String url, String route) {
			this.url = url;
			this.route = route;
		}
	}
	
	private void getPredictionsUrl(List<Location> locations, int maxStops,
			String routeName, List<RefreshData> outputData, int mode)
	{
		//http://developer.mbta.com/lib/RTCR/RailLine_1.csv
		
		//BUS_PREDICTIONS_ONE or VEHICLE_LOCATIONS_ONE
		if (routeName != null)
		{
			//we know we're updating only one route
			if (isCommuterRail(routeName))
			{
				
				String url = routesToUrls.get(routeName);
				
				outputData.add(new RefreshData(url, routeName));
				return;
			}
		}
		else
		{
			if (mode == Selection.BUS_PREDICTIONS_STAR)
			{
				//ok, let's look at the locations and see what we can get
				for (Location location : locations)
				{
					if (location instanceof StopLocation)
					{
						StopLocation stopLocation = (StopLocation)location;


						for (String route : stopLocation.getRoutes())
						{
							if (isCommuterRail(route) && containsRoute(route, outputData) == false)
							{
								String url = routesToUrls.get(route);
								outputData.add(new RefreshData(url, route));
							}
						}
					}
					else if (location instanceof BusLocation)
					{
						//bus location
						BusLocation busLocation = (BusLocation)location;
						String route = busLocation.getRouteId();

						if (isCommuterRail(route) && containsRoute(route, outputData) == false)
						{
							String url = routesToUrls.get(route);
							outputData.add(new RefreshData(url, route));
						}
					}
				}
			}
			else
			{
				//add all 12 of them

				for (String route : routesToUrls.keySet())
				{
					String url = routesToUrls.get(route);
					
					outputData.add(new RefreshData(url, route));
				}
			}
		}
	}

	private static boolean containsRoute(String route, List<RefreshData> outputData) {
		boolean containsRoute = false;
		for (RefreshData row : outputData) {
			if (row.route.equals(route)) {
				containsRoute = true;
				break;
			}
		}
		return containsRoute;
	}

	private boolean isCommuterRail(String routeName) {
		return routeTitles.hasRoute(routeName);
	}

	@Override
	public boolean hasPaths() {
		return false;
	}

	@Override
	public TransitDrawables getDrawables() {
		return drawables;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery)
	{
		//try splitting up the route keys along the diagonal and see if they match one piece of it
		for (String route : routeTitles.routeTags())
		{
			String title = routeTitles.getTitle(route);
			if (title.contains("/"))
			{
				String[] pieces = title.split("/");
				for (int i = 0; i < pieces.length; i++)
				{
					if (lowercaseQuery.equals(pieces[i].toLowerCase()))
					{
						return route;
					}
				}
			}
		}
		
		return SearchHelper.naiveSearch(indexingQuery, lowercaseQuery, routeTitles);
		
	}

	@Override
	public CommuterRailStopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle, int platformOrder, String branch,
			String route) {
		CommuterRailStopLocation stop = new CommuterRailStopLocation.CommuterRailBuilder(
				latitude, longitude, stopTag, stopTitle, platformOrder, branch).build();
		stop.addRoute(route);
		return stop;
	}

	@Override
	public int getLoadOrder() {
		return 3;
	}

	@Override
	public TransitSourceTitles getRouteTitles() {
		return routeTitles;
	}

	@Override
	public int getTransitSourceId() {
		return Schema.Routes.enumagencyidCommuterRail;
	}
	
	@Override
	public boolean requiresSubwayTable() {
		return true;
	}

	@Override
	public IAlerts getAlerts() {
		return transitSystem.getAlerts();
	}
	
	@Override
	public String getDescription() {
		return "Commuter Rail";
	}
}
