package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import android.text.format.Time;
import android.util.Log;
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

	public SubwayPredictionsFeedParser(RoutePool routePool, Directions directions)
	{
		this.routePool = routePool;
		this.directions = directions;
	}
	
	private void clearPredictions() throws IOException
	{
		String[] routes = SubwayTransitSource.getAllSubwayRoutes();
		
		for (String route : routes)
		{
			RouteConfig routeConfig = routePool.get(route);
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
	}
	
	public void runParse(InputStream data) throws IOException
	{
		clearPredictions();

		String string = streamToString(data);

		JSONTokener tokener = new JSONTokener(string);
		try
		{
			JSONArray array = (JSONArray)tokener.nextValue();


			SimpleDateFormat format = new SimpleDateFormat("M/d/y K:m:s a");

			//TODO: there's a bug here where it doesn't interpret time after midnight correctly
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject object = (JSONObject)array.get(i);

				String route = object.getString("Line");
				RouteConfig routeConfig = routePool.get(route);

				String stopKey = object.getString("PlatformKey");
				StopLocation stopLocation = routeConfig.getStop(stopKey);

				if (stopLocation == null)
				{
					continue;
				}

				Date date = format.parse(object.getString("Time"));
				long epochTime = date.getTime();
				long diff = epochTime - (long)System.currentTimeMillis();
				int minutes = (int)(diff / 1000 / 60);
				
				if (diff < 0 && minutes == 0)
				{
					//just to make sure we don't count this
					minutes = -1;
				}
				
				String stopDirection = stopKey.charAt(4) + "B";
				String direction = route + stopDirection + object.getString("Route");
				int vehicleId = 0;


				stopLocation.addPrediction(minutes, epochTime, vehicleId, direction, routeConfig, directions);
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
}
