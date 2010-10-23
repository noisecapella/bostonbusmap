package boston.Bus.Map.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.MBTABusTransitSource;
import boston.Bus.Map.util.Box;
import junit.framework.TestCase;


public class TestSerialization extends TestCase {
	public void testString() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		String x = null;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		try
		{
			outputBox.writeString(x);
			assertTrue(false);
		
			byte[] blob = outputBox.getBlob();

			Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);

			String string2 = inputBox.readString();

			assertEquals(x, string2);
		}
		catch (NullPointerException e)
		{
			//good
		}
	}
	public void testString2() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		String x = "A quick brown fox";
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		outputBox.writeString(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		String string2 = inputBox.readString();
		
		assertEquals(x, string2);
	}
	public void testLong() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		long x = -4557498050202912686l;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		outputBox.writeLong(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		long string2 = inputBox.readLong();
		
		assertEquals(x, string2);
	}
	public void testInt() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		int x = -8455;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		outputBox.writeInt(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		int string2 = inputBox.readInt();
		
		assertEquals(x, string2);
	}
	
	public void testDouble() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		double x = -8455.34;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		outputBox.writeDouble(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		double string2 = inputBox.readDouble();
		
		assertEquals(x, string2);
	}
	
	public void testFloat() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		float x = -8455.88f;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		outputBox.writeFloat(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		float string2 = inputBox.readFloat();
		
		assertEquals(x, string2);
	}
	
	private void assertValid(Box outputBox) throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		RouteConfig routeConfig2 = new RouteConfig(inputBox, new MBTABusTransitSource(null, null, null));
		
		Box outputBox2 = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
	public void testBasic() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		RouteConfig routeConfig = new RouteConfig("x", "003344", "556677", new MBTABusTransitSource(null, null, null));
		
		routeConfig.addStop("5", new StopLocation(44.0f, 55.0f, null, "5", "xy", (short)9, null));
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		routeConfig.serialize(outputBox);

		assertValid(outputBox);
	}
	public void testBasic2() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		RouteConfig routeConfig = new RouteConfig("x", "123123", "ffeedd", new MBTABusTransitSource(null, null, null));
		
		routeConfig.addStop("5", new StopLocation(44.0f, 55.0f, null, "5", "xy", (short)9, null));
		//routeConfig.addStop(6, new StopLocation(47.0, 56.0, null, 5, "x", "tue", routeConfig));
		//routeConfig.addDirection("XYZSD", "akosod", "asodkosd");
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		routeConfig.serialize(outputBox);

		assertValid(outputBox);
	}
	
	public void testStringMap() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		HashMap<String, String> mapping = new HashMap<String, String>();
		
		mapping.put("Apple", "cranberry");
		mapping.put("avocado", "jellyfish");
		
		mapping.put("sea cucumber", null);

		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);

		try
		{
			outputBox.writeStringMap(mapping);
			assertTrue(false);

			HashMap<String, String> newMapping;

			Box inputBox = new Box(outputBox.getBlob(), DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
			newMapping = inputBox.readStringMap();

			assertEquals(newMapping.size(), mapping.size());

			SortedSet<String> list1 = new TreeSet<String>(mapping.keySet());
			SortedSet<String> list2 = new TreeSet<String>(newMapping.keySet());
			Iterator<String> iterator1 = list1.iterator();
			Iterator<String> iterator2 = list2.iterator();

			while (iterator1.hasNext())
			{
				String key = iterator1.next();
				String key2 = iterator2.next();

				assertEquals(key, key2);

				String value1 = mapping.get(key);
				String value2 = newMapping.get(key);

				assertEquals(value1, value2);
			}
		}
		catch (NullPointerException e)
		{
			//good
		}
	}
	
	public void testStopLocation() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		StopLocation stopLocation = new StopLocation(44.6f, -45.6f, null, "3", "stop", (short)9, null);
		
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		stopLocation.serialize(outputBox);
		
		assertValidStopLocation(outputBox);
	}
	
	public void testPath() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		ArrayList<Float> floats = new ArrayList<Float>();
		floats.add(2.3f);
		floats.add(-42.3f);
		floats.add(-502.3f);
		
		
		Path stopLocation = new Path(floats);
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		stopLocation.serialize(outputBox);
		
		assertValidPath(outputBox);
	}

	public void testRouteAndPath() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		ArrayList<Float> floats = new ArrayList<Float>();
		floats.add(2.3f);
		floats.add(-42.3f);
		floats.add(-502.3f);
		ArrayList<Float> floats2 = new ArrayList<Float>();
		floats2.add(2.43f);
		floats2.add(-42.53f);
		floats2.add(-502.63f);
		
		
		Path path = new Path(floats);
		
		RouteConfig routeConfig = new RouteConfig("6", "deadbe", "ef1234", new MBTABusTransitSource(null, null, null));
		routeConfig.addPath(path);
		routeConfig.addStop("xyz", new StopLocation(-3.4f, -6.5f, null, "s", "etwk", (short)9, null));
		routeConfig.addStop("yy", new StopLocation(-4f, 5f, null, "k", "xkfowe", (short)9, null));
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		
		routeConfig.serialize(outputBox);
		
		assertValid(outputBox);
	}
	
	
	

	private void assertValidStopLocation(Box outputBox) throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		StopLocation routeConfig2 = new StopLocation(inputBox, null);
		
		Box outputBox2 = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
	private void assertValidPath(Box outputBox) throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		
		Path routeConfig2 = new Path(inputBox);
		
		Box outputBox2 = new Box(null, DatabaseHelper.CURRENT_DB_VERSION, sharedStops);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
}
