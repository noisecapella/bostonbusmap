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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSystem;

public class SubwayPredictionsFeedParser 
{
	private final RoutePool routePool;
	private final Directions directions;
	private final Drawable rail;
	private final Drawable railArrow;
	
	private final HashMap<Integer, BusLocation> busMapping;
	
	public SubwayPredictionsFeedParser(RoutePool routePool, Directions directions, Drawable bus, Drawable railArrow, 
			HashMap<Integer, BusLocation> busMapping)
	{
		this.routePool = routePool;
		this.directions = directions;
		this.rail = bus;
		this.railArrow = railArrow;
		this.busMapping = busMapping;
	}
	
	private void clearPredictions(String route) throws IOException
	{
		if (route != null)
		{
			RouteConfig routeConfig = routePool.get(route);
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
		else
		{
			for (String subwayRoute : SubwayTransitSource.getAllSubwayRoutes())
			{
				RouteConfig routeConfig = routePool.get(subwayRoute);
				for (StopLocation stopLocation : routeConfig.getStops())
				{
					stopLocation.clearPredictions(routeConfig);
				}
			}
		}
	}
	
	public void runParse(InputStream data) throws IOException
	{
		String string = streamToString(data);

		JSONTokener tokener = new JSONTokener(string);
		
		//store everything here, then write out all out at once
		ArrayList<Prediction> predictions = new ArrayList<Prediction>(); 
		ArrayList<StopLocation> stopLocations = new ArrayList<StopLocation>(); 
		
		//start off with all the buses to be removed, and if they're still around remove them from toRemove
		HashSet<Integer> toRemove = new HashSet<Integer>(busMapping.keySet());
		
		String route = null;
		try
		{
			JSONArray array = (JSONArray)tokener.nextValue();

			//TODO: there's a bug here where it doesn't interpret time after midnight correctly
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject object = (JSONObject)array.get(i);

				route = object.getString("Line");
				RouteConfig routeConfig = routePool.get(route);

				if (routeConfig == null)
				{
					//bogus JSON maybe?
					continue;
				}
				
				String stopKey = object.getString("PlatformKey");
				StopLocation stopLocation = routeConfig.getStop(stopKey);

				if (stopLocation == null)
				{
					continue;
				}

				Date date = parseTime(object.getString("Time"));
				long epochTime = date.getTime();
				long currentMillis = System.currentTimeMillis();
				long diff = epochTime - currentMillis;
				int minutes = (int)(diff / 1000 / 60);
				
				if (diff < 0 && minutes == 0)
				{
					//just to make sure we don't count this
					minutes = -1;
				}
				
				String stopDirection = stopKey.charAt(4) + "B";
				String branch = object.getString("Route");
				String direction = route + stopDirection + branch;
				int vehicleId = 0;

				predictions.add(new Prediction(minutes, epochTime, vehicleId, directions.getTitleAndName(direction),
						routeConfig.getRouteName()));
				stopLocations.add(stopLocation);

				String informationType = object.getString("InformationType");
				if ("Arrived".equals(informationType))
				{
					int seconds = (int)-(diff / 1000);

					//NOTE: I'm not sure if I want to keep subway cars around for a long time, but there's no good way of knowing
					//when they're not around
					
					//if (seconds < 300)
					if (true)
					{
						StopLocation nextStop = getNextStop(routeConfig, stopLocation, direction);

						final int arrowTopDiff = 9;

						//first, see if there's a subway car which pretty much matches an old BusLocation
						BusLocation busLocation = null;
						int id = 0;
						for (BusLocation location : busMapping.values())
						{
							int secondsDiff = (int) ((currentMillis - location.lastUpdateInMillis) / 1000) - (seconds - location.seconds);
							if (location.getLatitudeAsDegrees() == stopLocation.getLatitudeAsDegrees() && 
									location.getLongitudeAsDegrees() == stopLocation.getLongitudeAsDegrees() &&
									Math.abs(secondsDiff) < 10)
							{
								//the last part is, does the number of seconds still pretty much match up, give or take 10 seconds?
								//if so, reuse the id so that highlighted buses remain highlighted after a refresh
								id = location.getId();
								break;
							}
						}
						
						if (id == 0)
						{
							id = (int)(Math.random() * 1234) + 1;
						}
						
						busLocation = new BusLocation(stopLocation.getLatitudeAsDegrees(), stopLocation.getLongitudeAsDegrees(),
								id, seconds, currentMillis, null, true, direction, null, rail, 
								railArrow, route, directions, route + " at " + stopLocation.getTitle(), true, false, arrowTopDiff);
						busMapping.put(id, busLocation);
						
						toRemove.remove(id);


						//set arrow to point to correct direction

						if (nextStop != null)
						{
							//Log.v("BostonBusMap", "at " + stopLocation.getTitle() + " moving to " + nextStop.getTitle());
							busLocation.movedTo(nextStop.getLatitudeAsDegrees(), nextStop.getLongitudeAsDegrees());
						}
						else
						{
							//Log.v("BostonBusMap", "at " + stopLocation.getTitle() + ", nothing to move to");
						}
					}
				}
			}

		}
		catch (JSONException e)
		{
			Log.e("BostonBusMap", e.getMessage());
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
	private StopLocation getNextStop(RouteConfig routeConfig, StopLocation stopLocation, String dirTag) {
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
				String toBranch = stop.getBranch();

				//Log.v("BostonBusMap", "from " + fromBranch + " to " + toBranch);
				if (stop.getPlatformOrder() == stopLocationPlatformOrder + 1 && fromBranch.equals(toBranch) &&
						stop.getStopTag().endsWith(dirSuffix))
				{
					return stop;
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

	private final SimpleDateFormat format = new SimpleDateFormat("M/d/y K:m:s");


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
