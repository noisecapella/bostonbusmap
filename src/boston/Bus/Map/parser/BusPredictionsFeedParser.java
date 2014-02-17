package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Maps;


import android.util.Xml.Encoding;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;

public class BusPredictionsFeedParser extends DefaultHandler
{
	private static final String stopTagKey = "stopTag";
	private static final String minutesKey = "minutes";
	private static final String epochTimeKey = "epochTime";
	private static final String vehicleKey = "vehicle";
	private static final String affectedByLayoverKey = "affectedByLayover";
	private static final String dirTagKey = "dirTag";
	private static final String predictionKey = "prediction";
	private static final String directionKey = "direction";
	private static final String predictionsKey = "predictions";
	private static final String routeTagKey = "routeTag";
	private static final String delayedKey = "delayed";
	private static final String titleKey = "title";
	private static final String blockKey = "block";
	
	private final RoutePool stopMapping;
	private StopLocation currentLocation;
	private RouteConfig currentRoute;
	private final Directions directions;
	private String directionTitle;

	private final long currentTimeMillis;

	private final Map<String, Integer> tagCache = Maps.newHashMap();
	
	public BusPredictionsFeedParser(RoutePool stopMapping, Directions directions) {
		this.stopMapping = stopMapping;
		this.directions = directions;

		this.currentTimeMillis = System.currentTimeMillis();
	}

	public void runParse(InputStream data) throws ParserConfigurationException, SAXException, IOException
	{
		android.util.Xml.parse(data, Encoding.UTF_8, this);
		data.close();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equals(predictionsKey))
		{
			String currentRouteTag = attributes.getValue(routeTagKey);
			try
			{
				currentRoute = stopMapping.get(currentRouteTag);
			}
			catch (IOException e)
			{
				LogUtil.e(e);
				currentRoute = null;
			}
			
			currentLocation = null;
			if (currentRoute != null)
			{
				String stopTag = attributes.getValue(stopTagKey);
				currentLocation = currentRoute.getStop(stopTag);
				
				if (currentLocation != null)
				{
					currentLocation.clearPredictions(currentRoute);
				}
			}
		}
		else if (localName.equals(directionKey)) {
			clearAttributes(attributes);

			directionTitle = getAttribute(titleKey, attributes);
		}
		else if (localName.equals(predictionKey))
		{
			clearAttributes(attributes);
			
			if (currentLocation != null && currentRoute != null)
			{
				int minutes = Integer.parseInt(getAttribute(minutesKey, attributes));

				long epochTime = Long.parseLong(getAttribute(epochTimeKey, attributes));

				String vehicleId = getAttribute(vehicleKey, attributes);
				
				boolean affectedByLayover = Boolean.parseBoolean(getAttribute(affectedByLayoverKey, attributes));
				
				boolean isDelayed = Boolean.parseBoolean(getAttribute(delayedKey, attributes));

				
				
				String dirTag = getAttribute(dirTagKey, attributes);
				String block = getAttribute(blockKey, attributes);

				String directionSnippet = directions.getTitleAndName(dirTag);
				if (StringUtil.isEmpty(directionSnippet)) {
					directionSnippet = directionTitle;
				}
				long arrivalTimeMillis = currentTimeMillis + minutes * 60 * 1000;

				TimePrediction prediction = new TimePrediction(arrivalTimeMillis, vehicleId,
                        directionSnippet, currentRoute.getRouteName(),
                        currentRoute.getRouteTitle(), affectedByLayover, isDelayed,
                        TimePrediction.NULL_LATENESS, block);
				currentLocation.addPrediction(prediction);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals(directionKey)) {
			directionTitle = null;
		}
	}

	private String getAttribute(String key, Attributes attributes)
	{
		return XmlParserHelper.getAttribute(key, attributes, tagCache);
	}

	private void clearAttributes(Attributes attributes)
	{
		XmlParserHelper.clearAttributes(attributes, tagCache);
	}


}
