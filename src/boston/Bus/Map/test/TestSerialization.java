package boston.Bus.Map.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.util.Box;
import junit.framework.TestCase;


public class TestSerialization extends TestCase {
	public void testString() throws IOException
	{
		String x = null;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		
		try
		{
			outputBox.writeString(x);
			assertTrue(false);
		
			byte[] blob = outputBox.getBlob();

			Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);

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
		String x = "A quick brown fox";
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		outputBox.writeString(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		String string2 = inputBox.readString();
		
		assertEquals(x, string2);
	}
	public void testLong() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		long x = -4557498050202912686l;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		outputBox.writeLong(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		long string2 = inputBox.readLong();
		
		assertEquals(x, string2);
	}
	public void testInt() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		int x = -8455;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		outputBox.writeInt(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		int string2 = inputBox.readInt();
		
		assertEquals(x, string2);
	}
	
	public void testDouble() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		double x = -8455.34;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		outputBox.writeDouble(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		double string2 = inputBox.readDouble();
		
		assertEquals(x, string2);
	}
	
	public void testFloat() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		float x = -8455.88f;
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		outputBox.writeFloat(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		float string2 = inputBox.readFloat();
		
		assertEquals(x, string2);
	}
	
	
	public void testStringMap() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		HashMap<String, String> mapping = new HashMap<String, String>();
		
		mapping.put("Apple", "cranberry");
		mapping.put("avocado", "jellyfish");
		
		mapping.put("sea cucumber", null);

		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);

		try
		{
			outputBox.writeStringMap(mapping);
			assertTrue(false);

			Box inputBox = new Box(outputBox.getBlob(), DatabaseHelper.CURRENT_DB_VERSION);
			MyHashMap<String, String> newMapping = inputBox.readStringMap();

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
	
	public void testPath() throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		ArrayList<Float> floats = new ArrayList<Float>();
		floats.add(2.3f);
		floats.add(-42.3f);
		floats.add(-502.3f);
		
		
		Path stopLocation = new Path(floats);
		
		Box outputBox = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		
		stopLocation.serialize(outputBox);
		
		assertValidPath(outputBox);
	}

	

	private void assertValidPath(Box outputBox) throws IOException
	{
		HashMap<String, StopLocation> sharedStops = new HashMap<String, StopLocation>();
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob, DatabaseHelper.CURRENT_DB_VERSION);
		
		Path routeConfig2 = new Path(inputBox);
		
		Box outputBox2 = new Box(null, DatabaseHelper.CURRENT_DB_VERSION);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
}
