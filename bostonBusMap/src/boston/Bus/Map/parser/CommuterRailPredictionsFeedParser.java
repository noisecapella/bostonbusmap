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
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.transit.SubwayTransitSource;
import boston.Bus.Map.transit.TransitSystem;

public class CommuterRailPredictionsFeedParser
{
	private final RouteConfig routeConfig;
	private final Directions directions;
	private final Drawable rail;
	private final Drawable railArrow;

	public static final String fakeData = "{"+
	"    \"Messages\": ["+
	"        ["+
	"            {"+
	"                \"Key\": \"Time Stamp\","+
	"                \"Value\": \"/Date(1306205146386-0400)/\""+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Stop Id\","+
	"                \"Value\": \"Nantasket Junction\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Id\","+
	"                \"Value\": \"1507\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Trip Id\","+
	"                \"Value\": 93"+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Trip Id\","+
	"                \"Value\": \"093\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Destination GTFS Id\","+
	"                \"Value\": \"Greenbush\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Latitude\","+
	"                \"Value\": \"42.24497\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Longitude\","+
	"                \"Value\": \"-70.8688\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Scheduled Arrival Time\","+
	"                \"Value\": \"5/23/2011 10:37:00 PM\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Train Lateness in Seconds\","+
	"                \"Value\": \"\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Event Flag Name\","+
	"                \"Value\": \"Approaching\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Heading\","+
	"                \"Value\": 5"+
	"            },"+
	"            {"+
	"                \"Key\": \"Speed\","+
	"                \"Value\": 90"+
	"            }"+
	"        ],"+
	"        ["+
	"            {"+
	"                \"Key\": \"Time Stamp\","+
	"                \"Value\": \"/Date(1306205146386-0400)/\""+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Stop Id\","+
	"                \"Value\": \"Cohasset\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Id\","+
	"                \"Value\": \"1507\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Trip Id\","+
	"                \"Value\": 93"+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Trip Id\","+
	"                \"Value\": \"093\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Destination GTFS Id\","+
	"                \"Value\": \"Greenbush\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Latitude\","+
	"                \"Value\": \"42.24497\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Longitude\","+
	"                \"Value\": \"-70.8688\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Scheduled Arrival Time\","+
	"                \"Value\": \"5/23/2011 10:40:00 PM\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Train Lateness in Seconds\","+
	"                \"Value\": \"540\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Event Flag Name\","+
	"                \"Value\": \"Notification\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Heading\","+
	"                \"Value\": 5"+
	"            },"+
	"            {"+
	"                \"Key\": \"Speed\","+
	"                \"Value\": 90"+
	"            }"+
	"        ],"+
	"        ["+
	"            {"+
	"                \"Key\": \"Time Stamp\","+
	"                \"Value\": \"/Date(1306205146386-0400)/\""+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Stop Id\","+
	"                \"Value\": \"North Scituate\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Id\","+
	"                \"Value\": \"1507\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Trip Id\","+
	"                \"Value\": 93"+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Trip Id\","+
	"                \"Value\": \"093\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Destination GTFS Id\","+
	"                \"Value\": \"Greenbush\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Latitude\","+
	"                \"Value\": \"42.24497\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Longitude\","+
	"                \"Value\": \"-70.8688\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Scheduled Arrival Time\","+
	"                \"Value\": \"5/23/2011 10:48:00 PM\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Train Lateness in Seconds\","+
	"                \"Value\": \"540\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Event Flag Name\","+
	"                \"Value\": \"Notification\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Heading\","+
	"                \"Value\": 5"+
	"            },"+
	"            {"+
	"                \"Key\": \"Speed\","+
	"                \"Value\": 90"+
	"            }"+
	"        ],"+
	"        ["+
	"            {"+
	"                \"Key\": \"Time Stamp\","+
	"                \"Value\": \"/Date(1306205146386-0400)/\""+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Stop Id\","+
	"                \"Value\": \"Greenbush\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Id\","+
	"                \"Value\": \"1507\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Trip Id\","+
	"                \"Value\": 93"+
	"            },"+
	"            {"+
	"                \"Key\": \"GTFS Trip Id\","+
	"                \"Value\": \"093\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Destination GTFS Id\","+
	"                \"Value\": \"Greenbush\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Latitude\","+
	"                \"Value\": \"42.24497\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Vehicle Longitude\","+
	"                \"Value\": \"-70.8688\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Scheduled Arrival Time\","+
	"                \"Value\": \"5/23/2011 10:58:00 PM\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Train Lateness in Seconds\","+
	"                \"Value\": \"540\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Event Flag Name\","+
	"                \"Value\": \"Pre\""+
	"            },"+
	"            {"+
	"                \"Key\": \"Heading\","+
	"                \"Value\": 0"+
	"            },"+
	"            {"+
	"                \"Key\": \"Speed\","+
	"                \"Value\": 0"+
	"            }"+
	"        ]"+
	"    ],"+
	"    \"UpdateDate\": \"/Date(1306205147293-0400)/\""+
	"}";;
	
	private final ConcurrentHashMap<Integer, BusLocation> busMapping;

	public CommuterRailPredictionsFeedParser(RouteConfig routeConfig, Directions directions, Drawable bus, Drawable railArrow, 
			ConcurrentHashMap<Integer, BusLocation> busMapping)
	{
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.rail = bus;
		this.railArrow = railArrow;
		this.busMapping = busMapping;

		format = new SimpleDateFormat("M/d/y K:m:s");
		format.setTimeZone(TransitSystem.getTimeZone());
	}

	private void clearPredictions(String route) throws IOException
	{
		if (routeConfig != null)
		{
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
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
		HashSet<Integer> toRemove = new HashSet<Integer>();
		for (Integer id : busMapping.keySet())
		{
			BusLocation busLocation = busMapping.get(id);
			if (busLocation.isDisappearAfterRefresh())
			{
				toRemove.add(id);
			}
		}

		String route = null;
		try
		{
			Object jsonObj = tokener.nextValue();
			JSONObject root = (JSONObject)jsonObj;
			JSONArray array = root.getJSONArray("Messages");

			//TODO: there's a bug here where it doesn't interpret time after midnight correctly
			for (int i = 0; i < array.length(); i++)
			{
				JSONArray properties = (JSONArray)array.get(i);

				HashMap<String, Object> propertyMap = new HashMap<String, Object>();
				for (int j = 0; j < properties.length(); j++)
				{
					JSONObject keyValue = (JSONObject)properties.get(j);
					String key = keyValue.getString("Key");
					Object value = keyValue.get("Value");
					propertyMap.put(key, value);
				}
				
				
				String stopKey = (String)propertyMap.get("GTFS Stop Id");

				StopLocation stopLocation = (StopLocation)routeConfig.getStop("CRK-" + stopKey);

				if (stopLocation == null)
				{
					continue;
				}

				String time = (String)propertyMap.get("Scheduled Arrival Time");
				Date date = parseTime(time);
				long epochTime = date.getTime();
				epochTime += TransitSystem.getTimeZone().getOffset(epochTime);

				long currentMillis = TransitSystem.currentTimeMillis();
				long diff = epochTime - currentMillis;
				int minutes = (int)(diff / 1000 / 60);

				if (diff < 0 && minutes == 0)
				{
					//just to make sure we don't count this
					minutes = -1;
				}

				String stopDirection = (String)propertyMap.get("Destination GTFS Id");
				String direction = stopDirection;
				directions.add(direction, direction, "", routeConfig.getRouteName());
				int vehicleId = 0;

				int lateness = Prediction.NULL_LATENESS;
				Object latenessObj = propertyMap.get("Train Lateness in Seconds");
				if (latenessObj != null && latenessObj instanceof String)
				{
					String latenessStr = (String)latenessObj;
					try
					{
						lateness = Integer.parseInt(latenessStr);
					}
					catch (NumberFormatException e)
					{
						//oh well
					}
					
				}
				 
				
				predictions.add(new Prediction(minutes, vehicleId, directions.getTitleAndName(direction),
						routeConfig.getRouteName(), false, false, lateness));
				stopLocations.add(stopLocation);

				float lat = 0;
				float lon = 0;
				String latString = (String)propertyMap.get("Vehicle Latitude");
				String lonString = (String)propertyMap.get("Vehicle Longitude");
				
				try
				{
					lat = Float.parseFloat(latString);
					lon = Float.parseFloat(lonString);
				}
				catch (NumberFormatException e)
				{
					//oh well
				}
				
				String informationType = (String)propertyMap.get("Event Flag Name");
				if (lat != 0 && lon != 0)
				{
					int seconds = (int)-(diff / 1000);

					//NOTE: I'm not sure if I want to keep subway cars around for a long time, but there's no good way of knowing
					//when they're not around

					//if (seconds < 300)
					if (true)
					{
						//StopLocation nextStop = getNextStop(routeConfig, stopLocation, direction);

						final int arrowTopDiff = 9;

						//first, see if there's a subway car which pretty much matches an old BusLocation
						BusLocation busLocation = null;
						int id = 0;
						try
						{
							
							id = Integer.parseInt((String)propertyMap.get("Trip Id"));
						}
						catch (NumberFormatException e)
						{
							Log.e("BostonBusMap", e.getMessage());
							id = -1;
						}

						busLocation = new BusLocation(lat, lon,
								id, seconds, currentMillis, null, true, direction, null, rail, 
								railArrow, route, directions, route + " at " + stopLocation.getTitle(), true, false, arrowTopDiff);
						busMapping.put(id, busLocation);

						toRemove.remove(id);


						//set arrow to point to correct direction

						/*if (nextStop != null)
						{
							//Log.v("BostonBusMap", "at " + stopLocation.getTitle() + " moving to " + nextStop.getTitle());
							busLocation.movedTo(nextStop.getLatitudeAsDegrees(), nextStop.getLongitudeAsDegrees());
						}
						else
						{
							//Log.v("BostonBusMap", "at " + stopLocation.getTitle() + ", nothing to move to");
						}*/
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
