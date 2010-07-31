package boston.Bus.Map.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
	
	private final ArrayList<String> progress = new ArrayList<String>();
	
	/**
	 * The serialization version number
	 */
	private final int versionNumber;
	
	public Box(byte[] input, int versionNumber, HashMap<Integer, StopLocation> sharedStops)
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
	
	
	public void writeString(String route) throws IOException {
		showProgress("writeString");
		outputStream.writeUTF(route);
		
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
	
	public String readString() throws IOException
	{
		showProgress("readString");
		return inputStream.readUTF();
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
	
	public void readStringMap(Map<String, String> map) throws IOException
	{
		showProgress("readStringMap");
		byte b = readByte();
		if (b == IS_NULL)
		{
			//do nothing
		}
		else
		{
			int size = readInt();
			for (int i = 0; i < size; i++)
			{
				String key = readString();
				String value = readString();
				
				map.put(key, value);
			}
		}
	}

	public void writeStopsMap(Map<Integer, StopLocation> stops) throws IOException {
		showProgress("writeStopsMap");
		int size = stops.size();
		writeInt(size);
		
		for (Integer key : stops.keySet())
		{
			writeInt(key);
			StopLocation value = stops.get(key);
			value.serialize(this);
		}
	}

	private final HashMap<Integer, StopLocation> stopMap;
	
	public void readStopsMap(HashMap<Integer, StopLocation> stops, RouteConfig routeConfig, Drawable busStop) throws IOException {
		showProgress("readStopsMap");
		int size = readInt();
		
		for (int i = 0; i < size; i++)
		{
			Integer key = readInt();
			StopLocation value = new StopLocation(this, busStop);
			if (stopMap.containsKey(key))
			{
				stops.put(key, stopMap.get(key));
			}
			else
			{
				stops.put(key, value);
				stopMap.put(key, value);
			}
			
			stopMap.get(key).addRoute(routeConfig);
		}
	}

	public void writePathsMap(Map<Integer, Path> stops) throws IOException {
		showProgress("writePathsMap");
		int size = stops.size();
		writeInt(size);
		
		for (Integer key : stops.keySet())
		{
			writeInt(key);
			Path value = stops.get(key);
			value.serialize(this);
		}
	}

	public void readPathsMap(TreeMap<Integer, Path> stops) throws IOException {
		showProgress("readPathsMap");
		int size = readInt();
		
		for (int i = 0; i < size; i++)
		{
			Integer key = readInt();
			Path value = new Path(this);
			stops.put(key, value);
		}
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

	public void writePredictions(SortedSet<Prediction> predictions) throws IOException {
		showProgress("writePredictions");
		writeInt(predictions.size());
		
		//there will never be any predictions; this is here for legacy reasons
	}
	
	public void readPredictions(SortedSet<Prediction> predictions) throws IOException
	{
		showProgress("readPredictions");
		int size = readInt();
		
		//there will never be any predictions; this is here for legacy reasons
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

	
}
