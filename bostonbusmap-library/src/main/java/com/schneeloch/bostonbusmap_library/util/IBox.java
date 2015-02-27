package com.schneeloch.bostonbusmap_library.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.schneeloch.bostonbusmap_library.data.Path;

public interface IBox {

	public abstract void writeBytes(byte[] b) throws IOException;

	public abstract void writeInt(int i) throws IOException;

	public abstract int readInt() throws IOException;

	public abstract void writeShort(short s) throws IOException;

	public abstract short readShort() throws IOException;

	public abstract byte[] readBytes() throws IOException;

	/**
	 * Writes a string to the stream
	 * @return
	 * @throws IOException
	 */
	public abstract String readStringUnique() throws IOException;

	/**
	 * Reads a string from the stream
	 * @param s
	 * @throws IOException
	 */
	public abstract void writeStringUnique(String s) throws IOException;

	/**
	 * If it's a new string, reads a string from the stream, else it takes it from the hashtable
	 * @return
	 * @throws IOException
	 */
	public abstract String readString() throws IOException;

	/**
	 * If it's a new string, it adds the string to the hashtable and writes its value to the stream,
	 * else it just writes its index
	 * @param s
	 * @throws IOException
	 */
	public abstract void writeString(String s) throws IOException;

	public abstract byte readByte() throws IOException;

	public abstract void writeByte(byte b) throws IOException;

	/**
	 * Pretend to write out a map
	 * @throws IOException 
	 */
	public abstract void writeFakeStringMap() throws IOException;

	public abstract void readFakeStringMap() throws IOException;

	public abstract void writeStringMap(Map<String, String> map)
			throws IOException;

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
	public abstract void writePathsList(Path[] paths) throws IOException;

	public abstract Path[] readPathsList(int color) throws IOException;

	public abstract void writeDouble(double d) throws IOException;

	public abstract double readDouble() throws IOException;

	public abstract void writeFloat(float f) throws IOException;

	public abstract float readFloat() throws IOException;

	public abstract void writeBoolean(boolean b) throws IOException;

	public abstract boolean readBoolean() throws IOException;

	public abstract void writeLong(long i) throws IOException;

	public abstract long readLong() throws IOException;

	public abstract byte[] getBlob() throws IOException;

	public abstract int getVersionNumber();

	public abstract void writeStrings(ArrayList<String> routes)
			throws IOException;

	public abstract ArrayList<String> readStrings() throws IOException;

	public abstract boolean isOutput();

	public abstract boolean isEmpty();

}