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
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.database.InMemoryAgent;
import boston.Bus.Map.util.LogUtil;

public class BusPredictionsFeedParser extends DefaultHandler
{
	private static final String stopTagKey = "stopTag";
	private static final String minutesKey = "minutes";
	private static final String epochTimeKey = "epochTime";
	private static final String vehicleKey = "vehicle";
	private static final String affectedByLayoverKey = "affectedByLayover";
	private static final String dirTagKey = "dirTag";
	private static final String predictionKey = "prediction";
	private static final String predictionsKey = "predictions";
	private static final String routeTagKey = "routeTag";
	private static final String delayedKey = "delayed";
	
	private final Directions directions;
	private String currentRouteTag;
	private String currentStopTag;
	
	private final Map<String, Integer> tagCache = Maps.newHashMap();
	
	public BusPredictionsFeedParser(Directions directions) {
		this.directions = directions;
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
			currentRouteTag = attributes.getValue(routeTagKey);
			
			currentStopTag = attributes.getValue(stopTagKey);
			InMemoryAgent.clearPredictions(currentStopTag, currentRouteTag);
		}
		else if (localName.equals(predictionKey))
		{
			clearAttributes(attributes);
			
			if (currentStopTag != null && currentRouteTag != null)
			{
				int minutes = Integer.parseInt(getAttribute(minutesKey, attributes));

				long epochTime = Long.parseLong(getAttribute(epochTimeKey, attributes));

				String vehicleId = getAttribute(vehicleKey, attributes);
				
				boolean affectedByLayover = Boolean.parseBoolean(getAttribute(affectedByLayoverKey, attributes));
				
				boolean isDelayed = Boolean.parseBoolean(getAttribute(delayedKey, attributes));

				
				
				String dirTag = getAttribute(dirTagKey, attributes);

				InMemoryAgent.addPrediction(currentStopTag, minutes, epochTime, vehicleId, dirTag, currentRouteTag, directions, affectedByLayover,
						isDelayed, Prediction.NULL_LATENESS);
			}
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
