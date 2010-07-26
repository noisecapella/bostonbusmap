package boston.Bus.Map.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.Box;
import junit.framework.TestCase;


public class TestSerialization extends TestCase {
	public void testString() throws IOException
	{
		String x = null;
		Box outputBox = new Box(null);
		outputBox.writeString(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		String string2 = inputBox.readString();
		
		assertEquals(x, string2);
	}
	public void testString2() throws IOException
	{
		String x = "A quick brown fox";
		Box outputBox = new Box(null);
		outputBox.writeString(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		String string2 = inputBox.readString();
		
		assertEquals(x, string2);
	}
	public void testLong() throws IOException
	{
		long x = -4557498050202912686l;
		Box outputBox = new Box(null);
		outputBox.writeLong(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		long string2 = inputBox.readLong();
		
		assertEquals(x, string2);
	}
	public void testInt() throws IOException
	{
		int x = -8455;
		Box outputBox = new Box(null);
		outputBox.writeInt(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		int string2 = inputBox.readInt();
		
		assertEquals(x, string2);
	}
	
	public void testDouble() throws IOException
	{
		double x = -8455.34;
		Box outputBox = new Box(null);
		outputBox.writeDouble(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		double string2 = inputBox.readDouble();
		
		assertEquals(x, string2);
	}
	
	public void testFloat() throws IOException
	{
		float x = -8455.88f;
		Box outputBox = new Box(null);
		outputBox.writeFloat(x);
		
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		float string2 = inputBox.readFloat();
		
		assertEquals(x, string2);
	}
	
	private void assertValid(Box outputBox) throws IOException
	{
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		RouteConfig routeConfig2 = new RouteConfig(inputBox, null);
		
		Box outputBox2 = new Box(null);
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
		RouteConfig routeConfig = new RouteConfig("x");
		
		routeConfig.addPath(1, 3, 4);
		routeConfig.addStop(5, new StopLocation(44.0, 55.0, null, 5, "xy", "ture", routeConfig));
		routeConfig.addDirection("XYZSD", "akosod", "asodkosd");
		
		Box outputBox = new Box(null);
		
		routeConfig.serialize(outputBox);

		assertValid(outputBox);
	}
	public void testBasic2() throws IOException
	{
		RouteConfig routeConfig = new RouteConfig("x");
		
		routeConfig.addPath(1, 3, 4);
		routeConfig.addStop(5, new StopLocation(44.0, 55.0, null, 5, "xy", "ture", routeConfig));
		//routeConfig.addStop(6, new StopLocation(47.0, 56.0, null, 5, "x", "tue", routeConfig));
		//routeConfig.addDirection("XYZSD", "akosod", "asodkosd");
		
		Box outputBox = new Box(null);
		
		routeConfig.serialize(outputBox);

		assertValid(outputBox);
	}
	
	public void testStringMap() throws IOException
	{
		HashMap<String, String> mapping = new HashMap<String, String>();
		
		mapping.put("Apple", "cranberry");
		mapping.put("avocado", "jellyfish");
		
		mapping.put("sea cucumber", null);
		
		Box outputBox = new Box(null);
		
		outputBox.writeStringMap(mapping);
		HashMap<String, String> newMapping = new HashMap<String, String>();
		
		Box inputBox = new Box(outputBox.getBlob());
		inputBox.readStringMap(newMapping);
		
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
	
	public void testStopLocation() throws IOException
	{
		StopLocation stopLocation = new StopLocation(44.6, -45.6, null, 3, "stop", "in", null);
		
		Box outputBox = new Box(null);
		
		stopLocation.serialize(outputBox);
		
		assertValidStopLocation(outputBox);
	}
	
	public void testPath() throws IOException
	{
		Path stopLocation = new Path(3);
		
		Box outputBox = new Box(null);
		
		stopLocation.serialize(outputBox);
		
		assertValidPath(outputBox);
	}
	
	public void testPrediction() throws IOException
	{
		Prediction prediction = new Prediction(34, -3948394855l, 94, "out");
		
		Box outputBox = new Box(null);
		
		prediction.serialize(outputBox);
		
		assertValidPrediction(outputBox);
	}
	
	private void assertValidStopLocation(Box outputBox) throws IOException
	{
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		StopLocation routeConfig2 = new StopLocation(inputBox, null, null);
		
		Box outputBox2 = new Box(null);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
	private void assertValidPrediction(Box outputBox) throws IOException
	{
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		Prediction routeConfig2 = new Prediction(inputBox);
		
		Box outputBox2 = new Box(null);
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
		byte[] blob = outputBox.getBlob();
		
		Box inputBox = new Box(blob);
		
		Path routeConfig2 = new Path(inputBox);
		
		Box outputBox2 = new Box(null);
		routeConfig2.serialize(outputBox2);
		
		byte[] blob2 = outputBox2.getBlob();
		
		assertEquals(blob.length, blob2.length);
		
		for (int i = 0; i < blob.length; i++)
		{
			assertEquals(blob[i], blob2[i]);
		}
	}
	
}
