package boston.Bus.Map.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.StopLocation;

public class Box {
	private final DataInputStream inputStream;
	private final DataOutputStream outputStream;
	private final ByteArrayOutputStream innerOutputStream;
	
	private static final byte IS_NULL = 1;
	private static final byte IS_NOT_NULL = 0;
	
	private final byte[] single = new byte[1];

	private final HashMap<String, Integer> sharedStringTable = new HashMap<String, Integer>();
	private final HashMap<Integer, String> sharedStringTableReverse = new HashMap<Integer, String>();
	
	/**
	 * The serialization version number
	 */
	private final int versionNumber;
	
	public Box(byte[] input, int versionNumber, HashMap<String, StopLocation> sharedStops)
	{
		this.versionNumber = versionNumber;
		if (input == null)
		{
			innerOutputStream = new ByteArrayOutputStream();
			outputStream = new DataOutputStream(innerOutputStream);
			inputStream = null;
		}
		else
		{
			outputStream = null;
			innerOutputStream = null;
			inputStream = new DataInputStream(new ByteArrayInputStream(input));
		}
		
		this.stopMap = sharedStops;
	}
	
	public void writeBytes(byte[] b) throws IOException
	{
		showProgress("writeBytes");
		if (b == null)
		{
			writeByte((byte) IS_NULL);
		}
		else
		{
			writeByte((byte) IS_NOT_NULL);
			writeInt(b.length);
			outputStream.write(b);
		}
	}
	
	public void writeInt(int i) throws IOException
	{
		showProgress("writeInt");
		outputStream.writeInt(i);
	}
	
	

	
	public int readInt() throws IOException
	{
		showProgress("readInt");
		return inputStream.readInt();
	}
	
	public byte[] readBytes() throws IOException
	{
		showProgress("readBytes");
		byte b = readByte();
		
		if (b == IS_NOT_NULL)
		{
			int len = readInt();
			byte[] ret = new byte[len];
			inputStream.read(ret, 0, len);
			return ret;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Writes a string to the stream
	 * @return
	 * @throws IOException
	 */
	public String readStringUnique() throws IOException
	{
		showProgress("readStringUnique");
		return inputStream.readUTF();
	}

	/**
	 * Reads a string from the stream
	 * @param s
	 * @throws IOException
	 */
	public void writeStringUnique(String s) throws IOException
	{
		showProgress("writeStringUnique");
		outputStream.writeUTF(s);
	}
	
	/**
	 * If it's a new string, reads a string from the stream, else it takes it from the hashtable
	 * @return
	 * @throws IOException
	 */
	public String readString() throws IOException
	{
		showProgress("readString");
		int index = inputStream.readInt();
		String s = sharedStringTableReverse.get(index);
		if (null == s)
		{
			//new string
			s = inputStream.readUTF();
			sharedStringTable.put(s, index);
			sharedStringTableReverse.put(index, s);
		}
		
		return s;
	}

	/**
	 * If it's a new string, it adds the string to the hashtable and writes its value to the stream,
	 * else it just writes its index
	 * @param s
	 * @throws IOException
	 */
	public void writeString(String s) throws IOException {
		showProgress("writeStringUnique");
		Integer index = sharedStringTable.get(s);
		if (null == index)
		{
			//new string
			int newIndex = sharedStringTable.size();
			sharedStringTable.put(s, newIndex);
			sharedStringTableReverse.put(newIndex, s);
			
			outputStream.writeInt(newIndex);
			outputStream.writeUTF(s);
		}
		else
		{
			//existing string
			outputStream.writeInt(index);
		}
	}
	
	public byte readByte() throws IOException
	{
		showProgress("readByte");
		inputStream.read(single, 0, 1);
		return single[0];
	}
	
	public void writeByte(byte b) throws IOException
	{
		showProgress("writeByte");
		single[0] = b;
		outputStream.write(single, 0, 1);
	}
	
	/**
	 * Pretend to write out a map
	 * @throws IOException 
	 */
	public void writeFakeStringMap() throws IOException {
		writeByte(IS_NOT_NULL);
		writeInt(0);
	}

	public void readFakeStringMap() throws IOException
	{
		byte b = readByte();
		if (b == IS_NOT_NULL)
		{
			int size = readInt();
			for (int i = 0; i < size; i++)
			{
			
				readString();
				readString();
			}
		}
	}
	
	/*public void writeStringKeyValue(ArrayList<String> keys, ArrayList<String> values) throws IOException
	{
		showProgress("writeStringMap");
		if (keys == null || values == null)
		{
			//this never happens
			writeByte(IS_NULL);
		}
		else
		{
			writeByte(IS_NOT_NULL);
			int size = keys.size();
			writeInt(keys.size());
			
			for (int i = 0; i < size; i++)
			{
				writeString(keys.get(i));
				writeString(values.get(i));
			}
		}
	}*/
	
	private static final String inbound = "Inbound";
	private static final String outbound = "Outbound";
	
	
	/*public Object[] readStringKeyValue(boolean optimizeForInbound) throws IOException
	{
		showProgress("readStringMap");
		byte b = readByte();
		if (b == IS_NULL)
		{
			//do nothing
			return new Object[]{new ArrayList<String>(), new ArrayList<String>()};
		}
		else
		{
			int size = readInt();
			ArrayList<String> keys = new ArrayList<String>(size);
			ArrayList<String> values = new ArrayList<String>(size);
			for (int i = 0; i < size; i++)
			{
				String key = readString();
				String value = readString();
				
				if (optimizeForInbound)
				{
					if (inbound.equals(value))
					{
						value = inbound;
					}
					else if (outbound.equals(value))
					{
						value = outbound;
					}
				}
				
				keys.add(key);
				values.add(value);
			}
			
			return new Object[]{keys, values};
		}
	}*/
	
	public void writeStringMap(Map<String, String> map) throws IOException
	{
		showProgress("writeStringMap");
		if (map == null)
		{
			writeByte(IS_NULL);
		}
		else
		{
			writeByte(IS_NOT_NULL);
			writeInt(map.size());
			
			for (String s : map.keySet())
			{
				writeString(s);
				writeString(map.get(s));
			}
			
			
			
		}
	}
	
	public HashMap<String, String> readStringMap() throws IOException
	{
		showProgress("readStringMap");
		byte b = readByte();
		if (b == IS_NULL)
		{
			//do nothing
			return new HashMap<String, String>(0);
		}
		else
		{
			int size = readInt();
			HashMap<String, String> map = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++)
			{
				String key = readString();
				String value = readString();
				
				map.put(key, value);
			}
			
			return map;
		}
	}

	public void writeStopsMap(Map<String, StopLocation> stops) throws IOException {
		showProgress("writeStopsMap");
		int size = stops.size();
		writeInt(size);
		
		for (String key : stops.keySet())
		{
			writeString(key);
			StopLocation value = stops.get(key);
			value.serialize(this);
		}
	}

	private final HashMap<String, StopLocation> stopMap;
	
	public HashMap<String, StopLocation> readStopsMap(RouteConfig routeConfig, Drawable busStop, 
			HashMap<String, String> routeKeysToTitles) throws IOException {
		showProgress("readStopsMap");
		int size = readInt();
		
		HashMap<String, StopLocation> stops = new HashMap<String, StopLocation>(size);
		for (int i = 0; i < size; i++)
		{
			String key = readString();
			StopLocation value = new StopLocation(this, busStop, routeKeysToTitles);
			if (stopMap.containsKey(key))
			{
				stops.put(key, stopMap.get(key));
			}
			else
			{
				stops.put(key, value);
				stopMap.put(key, value);
			}
		}
		
		return stops;
	}

	public void writePathsList(ArrayList<Path> paths) throws IOException {
		showProgress("writePathsMap");
		int size = paths.size();
		writeInt(size);
		
		for (Path path : paths)
		{
			path.serialize(this);
		}
	}

	public ArrayList<Path> readPathsList() throws IOException {
		showProgress("readPathsMap");
		int size = readInt();
		
		ArrayList<Path> paths = new ArrayList<Path>(size);
		
		for (int i = 0; i < size; i++)
		{
			Path value = new Path(this);
			paths.add(value);
		}
		
		return paths;
	}

	public void writeDouble(double d) throws IOException {
		showProgress("writeDouble");
		outputStream.writeDouble(d);
	}
	
	public double readDouble() throws IOException
	{
		showProgress("readDouble");
		return inputStream.readDouble();
	}

	public void writeFloat(float f) throws IOException
	{
		showProgress("writeFloat");
		outputStream.writeFloat(f);
	}
	
	public float readFloat() throws IOException
	{
		showProgress("readFloat");
		return inputStream.readFloat();
	}
	
	public void writeBoolean(boolean b) throws IOException
	{
		showProgress("writeBoolean");
		outputStream.writeBoolean(b);
	}
	
	public boolean readBoolean() throws IOException
	{
		showProgress("readBoolean");
		return inputStream.readBoolean();
	}
	
	public void writeLong(long i) throws IOException {
		showProgress("writeLong");
		outputStream.writeLong(i);
	}
	
	public long readLong() throws IOException
	{
		showProgress("readLong");
		return inputStream.readLong();
	}

	private void showProgress(String string) {
		/*if (outputStream != null)
		{
			progress.add(string + " " + outputStream.size());
		}
		else
		{
			progress.add(string);
		}*/
		
	}

	public byte[] getBlob() throws IOException {
		outputStream.flush();
		outputStream.close();
		return innerOutputStream.toByteArray();
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void writeStrings(ArrayList<String> routes) throws IOException {
		writeInt(routes.size());
		for (String route : routes)
		{
			writeString(route);
		}
		
	}

	public ArrayList<String> readStrings() throws IOException
	{
		int size = readInt();
		ArrayList<String> ret = new ArrayList<String>(size);
		for (int i = 0; i < size; i++)
		{
			ret.add(readString());
		}
		return ret;
	}
	
}
