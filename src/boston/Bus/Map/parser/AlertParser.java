package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import android.util.Xml.Encoding;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;

/**
 * Parses the MBTA alerts RSS feed for information
 * @author schneg
 *
 */
public class AlertParser extends DefaultHandler
{
	private Date currentDate;
	private final SimpleDateFormat format;
	
	private static final int NO_STATE = 0;
	private static final int IN_LINK = 2;
	private static final int IN_DESCRIPTION = 3;
	private static final int IN_METADATA = 4;
	private static final int IN_PUBDATE = 5;
	private static final int IN_TITLE = 6;
	
	private boolean beforeFirstItem = true;
	private int currentState;
	private final StringBuilder currentTitle = new StringBuilder();
	private final StringBuilder currentDescription = new StringBuilder();
	private final StringBuilder currentDelay = new StringBuilder();
	private final ImmutableSet.Builder<Alert> alerts = ImmutableSet.builder();
	
	public AlertParser()
	{
		// Fri, 17 Jun 2011 02:30:29 GMT
		format = new SimpleDateFormat("E, d MMM yyyy KK:mm:ss z");
	}
	public void runParse(Reader data) throws IOException, SAXException
	{
		android.util.Xml.parse(data, this);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if ("item".equals(localName))
		{
			beforeFirstItem = false;
			currentState = NO_STATE;
		}
		else if (beforeFirstItem == false)
		{
			if ("title".equals(localName))
			{
				currentState = IN_TITLE;
			}
			else if ("description".equals(localName))
			{
				currentState = IN_DESCRIPTION;
			}
			else if ("metadata".equals(localName))
			{
				currentState = IN_METADATA;

				String value = attributes.getValue("delayTime");
				if (value != null)
				{
					currentDelay.append(value);
				}
			}
			else if ("pubDate".equals(localName))
			{
				currentState = IN_PUBDATE;
			}
			else
			{
				currentState = NO_STATE;
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException
	{
		switch (currentState)
		{
		case IN_TITLE:
			currentTitle.append(new String(ch, start, length));
			break;
		case IN_DESCRIPTION:
			currentDescription.append(new String(ch, start, length));
			break;
		case IN_PUBDATE:
			String currentDateString = new String(ch, start, length);
			try {
				currentDate = format.parse(currentDateString);
			} catch (ParseException e) {
				LogUtil.e(e);
			}
			break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException
	{
		if ("item".equals(localName))
		{
			Alert alert = new Alert(currentDate, currentTitle, currentDescription, currentDelay);
			alerts.add(alert);
			currentTitle.setLength(0);
			currentDescription.setLength(0);
			currentDelay.setLength(0);
		}
	}
	
	public ImmutableSet<Alert> getAlerts()
	{
		return alerts.build();
	}
}
