package com.schneeloch.bostonbusmap_library.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import com.schneeloch.bostonbusmap_library.data.Path;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;

public class Box implements IBox  {
	private final DataInputStream inputStream;
	private final DataOutputStream outputStream;
	private final ByteArrayOutputStream innerOutputStream;
	
	private static final byte IS_NULL = 1;
	private static final byte IS_NOT_NULL = 0;
	
	private final byte[] single = new byte[1];

	
	private final BiMap<String, Integer> sharedStringTable = HashBiMap.create();
	
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
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeBytes(byte[])
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeInt(int)
	 */
	@Override
	public void writeInt(int i) throws IOException
	{
		showProgress("writeInt");
		outputStream.writeInt(i);
	}
	
	

	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readInt()
	 */
	@Override
	public int readInt() throws IOException
	{
		showProgress("readInt");
		return inputStream.readInt();
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeShort(short)
	 */
	@Override
	public void writeShort(short s) throws IOException
	{
		showProgress("writeShort");
		outputStream.writeShort(s);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readShort()
	 */
	@Override
	public short readShort() throws IOException
	{
		showProgress("readShort");
		return inputStream.readShort();
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readBytes()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readStringUnique()
	 */
	@Override
	public String readStringUnique() throws IOException
	{
		showProgress("readStringUnique");
		return inputStream.readUTF();
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeStringUnique(java.lang.String)
	 */
	@Override
	public void writeStringUnique(String s) throws IOException
	{
		showProgress("writeStringUnique");
		outputStream.writeUTF(s);
	}
	
	private static final int NULL_STRING = -1;
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readString()
	 */
	@Override
	public String readString() throws IOException
	{
		showProgress("readString");
		int index = inputStream.readInt();
		if (index == NULL_STRING)
		{
			return null;
		}
		
		String s; 
		if (sharedStringTable.containsValue(index) == false)
		{
			//new string
			s = inputStream.readUTF();
			sharedStringTable.put(s, index);
		}
		else
		{
			s = sharedStringTable.inverse().get(index);
		}
		
		return s;
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeString(java.lang.String)
	 */
	@Override
	public void writeString(String s) throws IOException {
		showProgress("writeStringUnique");
		
		if (s == null)
		{
			outputStream.writeInt(NULL_STRING);
		}
		else
		{
			Integer index = sharedStringTable.get(s);
			if (null == index)
			{
				//new string
				int newIndex = sharedStringTable.size();
				sharedStringTable.put(s, newIndex);

				outputStream.writeInt(newIndex);
				outputStream.writeUTF(s);
			}
			else
			{
				//existing string
				outputStream.writeInt(index);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readByte()
	 */
	@Override
	public byte readByte() throws IOException
	{
		showProgress("readByte");
		inputStream.read(single, 0, 1);
		return single[0];
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeByte(byte)
	 */
	@Override
	public void writeByte(byte b) throws IOException
	{
		showProgress("writeByte");
		single[0] = b;
		outputStream.write(single, 0, 1);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeFakeStringMap()
	 */
	@Override
	public void writeFakeStringMap() throws IOException {
		writeByte(IS_NOT_NULL);
		writeInt(0);
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readFakeStringMap()
	 */
	@Override
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
	
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeStringMap(java.util.Map)
	 */
	@Override
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
	
/*	public void readStringMap(Map<String, String> map) throws IOException
	{
		showProgress("readStringMap(map)");
		byte b = readByte();
		if (b == IS_NULL)
		{
			//do nothing
			return;
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
	
	public Map<String, String> readStringMap() throws IOException
	{
		showProgress("readStringMap");
		byte b = readByte();
		if (b == IS_NULL)
		{
			//do nothing
			return Collections.emptyMap();
		}
		else
		{
			int size = readInt();
			MyHashMap<String, String> map = new MyHashMap<String, String>(size);
			for (int i = 0; i < size; i++)
			{
				String key = readString();
				String value = readString();
				
				map.put(key, value);
			}
			
			return map;
		}
	}
*/
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writePathsList(com.schneeloch.bostonbusmap_library.data.Path[])
	 */
	@Override
	public void writePathsList(Path[] paths) throws IOException {
		showProgress("writePathsMap");
		int size = paths.length;
		writeInt(size);
		
		for (Path path : paths)
		{
			path.serialize(this);
		}
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readPathsList()
	 */
	@Override
	public Path[] readPathsList(int color) throws IOException {
		showProgress("readPathsMap");
		if (!isOutput()) {
			//TODO: this actually means it's input, but the input was null
			int size = readInt();

			ArrayList<Path> paths = new ArrayList<Path>(size);

			for (int i = 0; i < size; i++)
			{
				Path value = new Path(this, color);
				paths.add(value);
			}

			return paths.toArray(RouteConfig.nullPaths);
		}
		else
		{
			return new Path[0];
		}
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeDouble(double)
	 */
	@Override
	public void writeDouble(double d) throws IOException {
		showProgress("writeDouble");
		outputStream.writeDouble(d);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readDouble()
	 */
	@Override
	public double readDouble() throws IOException
	{
		showProgress("readDouble");
		return inputStream.readDouble();
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeFloat(float)
	 */
	@Override
	public void writeFloat(float f) throws IOException
	{
		showProgress("writeFloat");
		outputStream.writeFloat(f);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readFloat()
	 */
	@Override
	public float readFloat() throws IOException
	{
		showProgress("readFloat");
		return inputStream.readFloat();
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeBoolean(boolean)
	 */
	@Override
	public void writeBoolean(boolean b) throws IOException
	{
		showProgress("writeBoolean");
		outputStream.writeBoolean(b);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws IOException
	{
		showProgress("readBoolean");
		return inputStream.readBoolean();
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeLong(long)
	 */
	@Override
	public void writeLong(long i) throws IOException {
		showProgress("writeLong");
		outputStream.writeLong(i);
	}
	
	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readLong()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#getBlob()
	 */
	@Override
	public byte[] getBlob() throws IOException {
		outputStream.flush();
		outputStream.close();
		return innerOutputStream.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#writeStrings(java.util.ArrayList)
	 */
	@Override
	public void writeStrings(ArrayList<String> routes) throws IOException {
		writeInt(routes.size());
		for (String route : routes)
		{
			writeString(route);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#readStrings()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see com.schneeloch.bostonbusmap_library.util.IBox2#isOutput()
	 */
	@Override
	public boolean isOutput() {
		return inputStream == null;
	}
	
	@Override
	public boolean isEmpty() {
		// TODO: double check. But usually if we're using this it should be empty
		return false;
	}
	
	public static IBox emptyBox() {
		return new IBox() {
			
			@Override
			public void writeStrings(ArrayList<String> routes) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeStringUnique(String s) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeStringMap(Map<String, String> map) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeString(String s) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeShort(short s) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writePathsList(Path[] paths) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeLong(long i) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeInt(int i) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeFloat(float f) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeFakeStringMap() throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeDouble(double d) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeBytes(byte[] b) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeByte(byte b) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public void writeBoolean(boolean b) throws IOException {
				throw new RuntimeException("Unimplemented");
				
			}
			
			@Override
			public ArrayList<String> readStrings() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public String readStringUnique() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public String readString() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public short readShort() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public Path[] readPathsList(int color) throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public long readLong() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public int readInt() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public float readFloat() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public void readFakeStringMap() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public double readDouble() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public byte[] readBytes() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public byte readByte() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public boolean readBoolean() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public boolean isOutput() {
				return false;
			}
			
			@Override
			public byte[] getBlob() throws IOException {
				throw new RuntimeException("Unimplemented");
			}
			
			@Override
			public boolean isEmpty() {
				return true;
			}
		};
	}
}
