package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import skylight1.opengl.files.QuickParseUtil;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.DirectionWithStopTags;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.FeedException;


import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.Xml.Encoding;

public class RouteConfigFeedParser extends DefaultHandler
{
	private static final String routeKey = "route";
	private static final String directionKey = "direction";
	private static final String stopKey = "stop";

	private static final String stopIdKey = "stopId";
	private static final String tagKey = "tag";
	private static final String latitudeKey = "lat";
	private static final String longitudeKey = "lon";
	private static final String titleKey = "title";
	private static final String dirTagKey = "dirTag";
	private static final String nameKey = "name";
	private static final String pathKey = "path";
	private static final String pointKey = "point";
	private static final String latKey = "lat";
	private static final String lonKey = "lon";
	private static final String useForUIKey = "useForUI";
	
	private static final String colorKey = "color";
	private static final String oppositeColorKey = "oppositeColor";
	
	
	private boolean inRoute;
	private boolean inDirection;
	private boolean inStop;
	private boolean inPath;
	
	private ArrayList<Path> currentPaths;
	private RouteConfig currentRouteConfig;
	
	private ArrayList<Float> currentPathPoints;
	private final TransitSource transitSource;
	private String currentDirTag;
	
	private final ArrayList<ContentProviderOperation> currentOperations = new ArrayList<ContentProviderOperation>();
	
	private final Context context;
	
	public RouteConfigFeedParser(Context context, NextBusTransitSource transitSource)
	{
		this.transitSource = transitSource;
		this.context = context;
	}

	public void runParse(InputStream inputStream)  throws ParserConfigurationException, SAXException, IOException
	{
		android.util.Xml.parse(inputStream, Encoding.UTF_8, this);
	}
	
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (stopKey.equals(localName))
		{
			inStop = true;
			
			if (inRoute)
			{
				String tag = attributes.getValue(tagKey);

				if (inDirection == false)
				{
					float latitudeAsDegrees = QuickParseUtil.parseFloat(attributes.getValue(latitudeKey));
					float longitudeAsDegrees = QuickParseUtil.parseFloat(attributes.getValue(longitudeKey));

					String title = attributes.getValue(titleKey);

					currentOperations.add(DatabaseAgent.makeStop( 
							tag, latitudeAsDegrees, longitudeAsDegrees, title));
					currentOperations.add(DatabaseAgent.makeStopRoute(
							tag, currentRouteConfig.getRouteName(), attributes.getValue(dirTagKey)));
				}
				else
				{
					currentOperations.add(DatabaseAgent.makeStopDirection(
							tag, currentDirTag));
				}
			}
		}
		else if (directionKey.equals(localName))
		{
			inDirection = true;
			
			if (inRoute)
			{
				String tag = attributes.getValue(tagKey);
				String title = attributes.getValue(titleKey);
				String name = attributes.getValue(nameKey);
				boolean useForUI = Boolean.getBoolean(attributes.getValue(useForUIKey));

				currentOperations.add(DatabaseAgent.makeDirection(
						tag, name, title, currentRouteConfig.getRouteName(), useForUI));
			}
		}
		else if (routeKey.equals(localName))
		{
			inRoute = true;
			
			String currentRoute = attributes.getValue(tagKey);
			int color = parseColor(attributes.getValue(colorKey));
			int oppositeColor = parseColor(attributes.getValue(oppositeColorKey));
			try
			{
				MyHashMap<String, String> routeKeysToTitles = transitSource.getRouteKeysToTitles();
				String currentRouteTitle = routeKeysToTitles.get(currentRoute);
				currentRouteConfig = new RouteConfig(currentRoute, currentRouteTitle, color, 
						oppositeColor, transitSource);
				
			}
			catch (IOException e)
			{
				//this shouldn't happen...
				//this should be caught and reported where the caller originally called runParse
				throw new RuntimeException(e);
			}
		}
		else if (pathKey.equals(localName))
		{
			inPath = true;
			
			currentPathPoints = new ArrayList<Float>();
		}
		else if (pointKey.equals(localName))
		{
			float lat = QuickParseUtil.parseFloat(attributes.getValue(latKey));
			float lon = QuickParseUtil.parseFloat(attributes.getValue(lonKey));
			currentPathPoints.add(lat);
			currentPathPoints.add(lon);
		}
		else if (localName.equals("Error"))
		{
			//i hate checked exceptions
			throw new RuntimeException(new FeedException());
		}
		
		
	}
	
	private int parseColor(String value) {
		if (value == null)
		{
			return Color.BLUE;
		}
		try
		{
			String colorString = "#99" + Color.parseColor(value);
			int color = Color.parseColor(colorString);
			return color;
		}
		catch (IllegalArgumentException e)
		{
			//malformed color string?
			return Color.BLUE;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (stopKey.equals(localName))
		{
			inStop = false;
		}
		else if (directionKey.equals(localName))
		{
			inDirection = false;
		}
		else if (routeKey.equals(localName))
		{
			inRoute = false;
			
			try
			{
				currentOperations.add(DatabaseAgent.makeRoute(
						currentRouteConfig.getRouteName(), currentRouteConfig.getRouteTitle(),
						currentRouteConfig.getColor(), currentRouteConfig.getOppositeColor(), 
						currentPaths != null ? currentPaths.toArray(new Path[0]) : null));

				currentRouteConfig = null;
				currentPaths = null;
				
				writeToDatabase();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			} catch (OperationApplicationException e) {
				throw new RuntimeException(e);
			}
		}
		else if (pathKey.equals(localName))
		{
			inPath = false;
			
			if (currentRouteConfig != null)
			{
				Path path = new Path(currentPathPoints);
				currentPaths.add(path);
			}
		}
		
	}
	
	private void writeToDatabase() throws RemoteException, OperationApplicationException {
		context.getContentResolver().applyBatch(DatabaseContentProvider.AUTHORITY,
				currentOperations);
		currentOperations.clear();
	}
}
