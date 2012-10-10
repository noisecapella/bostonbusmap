package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import skylight1.opengl.files.QuickParseUtil;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.database.Schema;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;
import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseHelper;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.FeedException;


import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
	
	private static final ContentValues[] nullContentValues = new ContentValues[0];
	
	private boolean inRoute;
	private boolean inDirection;
	private boolean inStop;
	private boolean inPath;
	
	private final List<Path> currentPaths = Lists.newArrayList();
	private String currentRouteTag;
	private String currentRouteTitle;
	private int currentRouteColor;
	private int currentRouteOppositeColor;
	
	private final List<Float> currentPathPoints = Lists.newArrayList();
	private final TransitSource transitSource;
	private String currentDirTag;
	
	private final InsertHelper stopInsertHelper;
	private final InsertHelper routeInsertHelper;
	private final InsertHelper stopMappingInsertHelper;
	private final InsertHelper directionsInsertHelper;
	private final InsertHelper directionsStopsInsertHelper;
	
	private final Context context;
	
	/**
	 * You must call cleanup() when you are done with this object!
	 * @param context
	 * @param transitSource
	 */
	public RouteConfigFeedParser(Context context, NextBusTransitSource transitSource)
	{
		this.transitSource = transitSource;
		this.context = context;
		
		SQLiteDatabase database = DatabaseHelper.getInstance(context).getWritableDatabase();
		stopInsertHelper = new InsertHelper(database, Schema.Stops.table);
		routeInsertHelper = new InsertHelper(database, Schema.Routes.table);
		stopMappingInsertHelper = new InsertHelper(database, Schema.Stopmapping.table);
		directionsInsertHelper = new InsertHelper(database, Schema.Directions.table);
		directionsStopsInsertHelper = new InsertHelper(database, Schema.DirectionsStops.table);
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
					
					Schema.Stops.executeInsertHelper(stopInsertHelper, tag, latitudeAsDegrees, longitudeAsDegrees, title);
					Schema.Stopmapping.executeInsertHelper(stopMappingInsertHelper,
							currentRouteTag, tag, attributes.getValue(dirTagKey));
				}
				else
				{
					Schema.DirectionsStops.executeInsertHelper(directionsStopsInsertHelper, currentDirTag, tag);
				}
			}
		}
		else if (directionKey.equals(localName))
		{
			inDirection = true;
			
			if (inRoute)
			{
				String tag = attributes.getValue(tagKey);
				currentDirTag = tag;
				String title = attributes.getValue(titleKey);
				String name = attributes.getValue(nameKey);
				boolean useForUI = Boolean.getBoolean(attributes.getValue(useForUIKey));

				Schema.Directions.executeInsertHelper(directionsInsertHelper, tag, name, title, currentRouteTag, Schema.toInteger(useForUI));
			}
		}
		else if (routeKey.equals(localName))
		{
			inRoute = true;
			
			currentRouteTag = attributes.getValue(tagKey);
			currentRouteColor = parseColor(attributes.getValue(colorKey));
			currentRouteOppositeColor = parseColor(attributes.getValue(oppositeColorKey));
			RouteTitles routeKeysToTitles = transitSource.getRouteKeysToTitles();
			currentRouteTitle = routeKeysToTitles.getTitle(currentRouteTag);
		}
		else if (pathKey.equals(localName))
		{
			inPath = true;
			
			currentPathPoints.clear();
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
				Path[] pathblob = currentPaths != null ? currentPaths.toArray(new Path[0]) : null;
				byte[] blob = DatabaseAgent.pathsToBlob(pathblob);
				Schema.Routes.executeInsertHelper(routeInsertHelper, currentRouteTag, currentRouteColor, currentRouteOppositeColor, blob, currentRouteTitle);

				currentRouteTag = null;
				currentRouteTitle = null;
				currentRouteColor = 0;
				currentRouteOppositeColor = 0;
				currentPaths.clear();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if (pathKey.equals(localName))
		{
			inPath = false;
			
			if (currentRouteTag != null)
			{
				Path path = new Path(currentPathPoints);
				currentPaths.add(path);
			}
		}
		
	}
	
	public void cleanup() {
		stopInsertHelper.close();
		routeInsertHelper.close();
		stopMappingInsertHelper.close();
		directionsInsertHelper.close();
		directionsStopsInsertHelper.close();
	}
}
