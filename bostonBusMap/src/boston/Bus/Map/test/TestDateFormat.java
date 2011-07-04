package boston.Bus.Map.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.parser.SubwayPredictionsFeedParser;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDateFormat extends TestCase
{
	public void testBasic1() throws ParseException
	{
		
		String time = "10/2/2010 12:49:29 PM";
		
		SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser("Red", null, null, null, null, new ConcurrentHashMap<Integer, BusLocation>(), new HashMap<String, String>());
		
		Date date = parser.parseTime(time);
		
		GregorianCalendar calendar = new GregorianCalendar(2010, 9, 2, 12, 49, 29);
		Date expectedDate = calendar.getTime();
		Assert.assertEquals(expectedDate, date);
	}
	

	public void testBasic2() throws ParseException
	{
		
		String time = "10/2/2010 12:49:29 AM";
		
		SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser("Orange", null, null, null, null, new ConcurrentHashMap<Integer, BusLocation>(), new HashMap<String, String>());
		
		Date date = parser.parseTime(time);
		
		GregorianCalendar calendar = new GregorianCalendar(2010, 9, 2, 0, 49, 29);
		Date expectedDate = calendar.getTime();
		Assert.assertEquals(expectedDate, date);
	}
	public void testBasic3() throws ParseException
	{
		
		String time = "10/2/2010 1:49:29 PM";
		
		SubwayPredictionsFeedParser parser = new SubwayPredictionsFeedParser("Blue", null, null, null, null, new ConcurrentHashMap<Integer, BusLocation>(), new HashMap<String, String>());
		
		Date date = parser.parseTime(time);
		
		GregorianCalendar calendar = new GregorianCalendar(2010, 9, 2, 13, 49, 29);
		Date expectedDate = calendar.getTime();
		Assert.assertEquals(expectedDate, date);
	}
}
