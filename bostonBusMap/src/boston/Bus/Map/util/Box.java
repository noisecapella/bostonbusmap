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
	
	private final ArrayList<String> progress = new ArrayList<String>();
	
	public Box(byte[] input)
	{
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
		return (byte)inputStream.read();
	}
	
	public void writeByte(byte b) throws IOException
	{
		showProgress("writeByte");
		outputStream.write(new byte[]{b});
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

	public void readStopsMap(HashMap<Integer, StopLocation> stops, RouteConfig routeConfig, Drawable busStop) throws IOException {
		showProgress("readStopsMap");
		int size = readInt();
		
		for (int i = 0; i < size; i++)
		{
			Integer key = readInt();
			StopLocation value = new StopLocation(this, routeConfig, busStop);
			stops.put(key, value);
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
		
		for (Prediction prediction : predictions)
		{
			prediction.serialize(this);
		}
		
	}
	
	public void readPredictions(SortedSet<Prediction> predictions) throws IOException
	{
		showProgress("readPredictions");
		int size = readInt();
		
		for (int i = 0; i < size; i++)
		{
			Prediction prediction = new Prediction(this);
			predictions.add(prediction);
		}
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
	
}
