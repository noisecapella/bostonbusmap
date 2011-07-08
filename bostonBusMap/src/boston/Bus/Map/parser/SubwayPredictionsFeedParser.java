package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSystem;

public class SubwayPredictionsFeedParser 
{
	private final String currentRoute;
	private final RoutePool routePool;
	private final Directions directions;
	private final Drawable rail;
	private final Drawable railArrow;
	
	private final ConcurrentHashMap<Integer, BusLocation> busMapping;
	private final HashMap<String, String> routeKeysToTitles;
	
	private static final int ROUTE_INDEX = 0;
	private static final int TRIP_ID_INDEX = 1;
	private static final int STOP_TAG_INDEX = 2;
	private static final int ARRIVAL_STATUS_INDEX = 3;
	private static final int TIME_INDEX = 4;
	private static final int TIME_DIFF_INDEX = 5;
	private static final int REVENUE_INDEX = 6;
	private static final int BRANCH_INDEX = 7;
	
	public SubwayPredictionsFeedParser(String route, RoutePool routePool, Directions directions, Drawable bus, Drawable railArrow, 
			ConcurrentHashMap<Integer, BusLocation> busMapping, HashMap<String, String> routeKeysToTitles)
	{
		this.currentRoute = route;
		this.routePool = routePool;
		this.directions = directions;
		this.rail = bus;
		this.railArrow = railArrow;
		this.busMapping = busMapping;
		this.routeKeysToTitles = routeKeysToTitles;
		
		format = new SimpleDateFormat("M/d/y K:m:s");
		format.setTimeZone(TransitSystem.getTimeZone());
	}
	
	private void clearPredictions(String route) throws IOException
	{
		if (route != null)
		{
			RouteConfig routeConfig = routePool.get(route);
			if (routeConfig != null)
			{
				for (StopLocation stopLocation : routeConfig.getStops())
				{
					stopLocation.clearPredictions(routeConfig);
				}
			}
		}
		else
		{
			for (String subwayRoute : SubwayTransitSource.getAllSubwayRoutes())
			{
				RouteConfig routeConfig = routePool.get(subwayRoute);
				if (routeConfig != null)
				{
					for (StopLocation stopLocation : routeConfig.getStops())
					{
						stopLocation.clearPredictions(routeConfig);
					}
				}
			}
		}
	}
	
	public void runParse(InputStream data) throws IOException
	{
		String string = streamToString(data);

		
		//store everything here, then write out all out at once
		ArrayList<Prediction> predictions = new ArrayList<Prediction>(); 
		ArrayList<StopLocation> stopLocations = new ArrayList<StopLocation>(); 
		
		//start off with all the buses to be removed, and if they're still around remove them from toRemove
		HashSet<Integer> toRemove = new HashSet<Integer>();
		for (Integer id : busMapping.keySet())
		{
			BusLocation busLocation = busMapping.get(id);
			if (busLocation.isDisappearAfterRefresh() && currentRoute.equals(busLocation.getRouteId()))
			{
				toRemove.add(id);
			}
		}
		
		String route = null;
		try
		{
			//TODO: there's a bug here where it doesn't interpret time after midnight correctly
			String[] array = string.split("\n");
			for (int i = 0; i < array.length; i++)
			{
				String line = array[i].trim();
				String[] lineArray = line.split(",");
				
				
				
				route = lineArray[ROUTE_INDEX].trim();
				RouteConfig routeConfig = routePool.get(route);

				if (routeConfig == null)
				{
					//bogus JSON maybe?
					continue;
				}
				
				String stopKey = lineArray[STOP_TAG_INDEX].trim();
				
				//this is a subway route so all StopLocations should be SubwayStopLocations 
				SubwayStopLocation stopLocation = (SubwayStopLocation)routeConfig.getStop(stopKey);

				if (stopLocation == null)
				{
					continue;
				}
				
				String timeString = lineArray[TIME_INDEX].trim();
				Date date = parseTime(timeString);
				long lastFeedUpdateTime = date.getTime();
				int offset = TransitSystem.getTimeZone().getOffset(lastFeedUpdateTime);
				lastFeedUpdateTime += offset;
				
				long currentMillis = TransitSystem.currentTimeMillis();
				long diff = lastFeedUpdateTime - currentMillis;
				int minutes = (int)(diff / 1000 / 60);
				
				if (diff < 0 && minutes == 0)
				{
					//just to make sure we don't count this
					minutes = -1;
				}
				
				String stopDirection = stopKey.charAt(4) + "B";
				String branch = lineArray[BRANCH_INDEX].trim();
				String direction = route + stopDirection + branch;
				int vehicleId = 0;

				predictions.add(new Prediction(minutes, vehicleId, directions.getTitleAndName(direction),
						routeConfig.getRouteName(), false, false, Prediction.NULL_LATENESS));
				stopLocations.add(stopLocation);

				String informationType = lineArray[ARRIVAL_STATUS_INDEX].trim();
				if ("Arrived".equals(informationType))
				{
					StopLocation nextStop = getNextStop(routeConfig, stopLocation, direction);

					final int arrowTopDiff = rail.getIntrinsicHeight() / 5;

					//first, see if there's a subway car which pretty much matches an old BusLocation
					BusLocation busLocation = null;
					int id = 0;
					try
					{
						String tripId = lineArray[TRIP_ID_INDEX].trim();
						id = Integer.parseInt(tripId);
					}
					catch (NumberFormatException e)
					{
						Log.e("BostonBusMap", e.getMessage());
						id = -1;
					}

					String routeTitle = routeKeysToTitles.get(route);
					if (routeTitle == null)
					{
						routeTitle = route;
					}

					busLocation = new BusLocation(stopLocation.getLatitudeAsDegrees(), stopLocation.getLongitudeAsDegrees(),
							id, lastFeedUpdateTime, currentMillis, null, true, direction, null, rail, 
							railArrow, route, directions, routeTitle, true, false, arrowTopDiff);
					busMapping.put(id, busLocation);

					toRemove.remove(id);


					//set arrow to point to correct direction

					if (nextStop != null)
					{
						busLocation.movedTo(nextStop.getLatitudeAsDegrees(), nextStop.getLongitudeAsDegrees());
					}
				}
			}

		}
		catch (ParseException e)
		{
			Log.e("BostonBusMap", e.getMessage());
		}
		catch (ClassCastException e)
		{
			//probably updating the wrong url?
			Log.e("BostonBusMap", e.getMessage());
		}
		
		clearPredictions(route);

		for (int i = 0; i < stopLocations.size(); i++)
		{
			StopLocation stopLocation = stopLocations.get(i);
			stopLocation.addPrediction(predictions.get(i));
		}
		
		for (Integer id : toRemove)
		{
			busMapping.remove(id);
		}
	}

	/**
	 * Get the next stop in the route, going in the same general direction, on the same branch
	 * @param routeConfig
	 * @param stopLocation
	 * @return
	 */
	private StopLocation getNextStop(RouteConfig routeConfig, SubwayStopLocation stopLocation, String dirTag) {
		String stoptag = stopLocation.getStopTag();
		String dirSuffix = stoptag.substring(stoptag.length() - 1);
		String fromBranch = stopLocation.getBranch();

		//first, check special cases, those that go to different branches
		//JFK is on Trunk, as well as anything north of it
		if (stoptag.equals("RSAVN") || stoptag.equals("RNQUN"))
		{
			return routeConfig.getStopMapping().get("RJFKN");
		}
		else if (stoptag.equals("RJFKS"))
		{
			if (dirTag.equals(SubwayRouteConfigFeedParser.RedSouthToAshmont))
			{
				return routeConfig.getStopMapping().get("RSAVS");
			}
			else
			{
				return routeConfig.getStopMapping().get("RNQUS");
			}
		}
		else
		{

			int stopLocationPlatformOrder = stopLocation.getPlatformOrder();

			for (StopLocation stop : routeConfig.getStops())
			{
				//this is a subway route so we can cast all stops on it
				SubwayStopLocation subwayStop = (SubwayStopLocation)stop;
				
				String toBranch = subwayStop.getBranch();

				//Log.v("BostonBusMap", "from " + fromBranch + " to " + toBranch);
				if (subwayStop.getPlatformOrder() == stopLocationPlatformOrder + 1 && fromBranch.equals(toBranch) &&
						subwayStop.getStopTag().endsWith(dirSuffix))
				{
					return subwayStop;
				}
			}

			return null;
		}
	}

	private String streamToString(InputStream data) throws IOException {
		//java is so annoying sometimes
		BufferedReader reader = new BufferedReader(new InputStreamReader(data));
		String line;
		StringBuilder ret = new StringBuilder();
		while ((line = reader.readLine()) != null)
		{
			ret.append(line).append('\n');
		}
		return ret.toString();
	}

	private final SimpleDateFormat format;


	public Date parseTime(String time) throws ParseException {
		Date date = format.parse(time);

		int hour = date.getHours();
		if (time.endsWith("PM") || time.endsWith("pm"))
		{
			if (hour == 12)
			{
				//do nothing
			}
			else
			{
				date.setHours(date.getHours() + 12);
			}
			
		}
		else
		{
			if (hour == 12)
			{
				date.setHours(0);
			}
		}
		return date;
	}

	public Map<? extends Integer, ? extends BusLocation> getBusMapping() {
		return busMapping;
	}
}
