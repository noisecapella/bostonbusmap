package boston.Bus.Map.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import boston.Bus.Map.algorithms.GetDirections.DirectionPath;

public class StringUtil {
	public static String join(String[] array, String join)
	{
		if (array == null || array.length == 0)
		{
			return "";
		}
		if (join == null)
		{
			join = ""; 
		}
		
		StringBuilder ret = new StringBuilder(array[0]);
		
		for (int i = 1; i < array.length; i++)
		{
			ret.append(join);
			ret.append(array[i]);
		}
		
		return ret.toString();
	}
	public static String join(Collection<String> array, String join)
	{
		if (array == null || array.size() == 0)
		{
			return "";
		}
		if (join == null)
		{
			join = ""; 
		}
		
		StringBuilder ret = new StringBuilder();
		int count = 0;
		
		Iterator<String> iterator = array.iterator();
		while (iterator.hasNext())
		{
			if (count != 0)
			{
				ret.append(join);
			}

			String next = iterator.next();
			ret.append(next);
			count++;
		}

		return ret.toString();
	}
	public static void join(Collection<String> array, String join, StringBuilder ret)
	{
		if (array == null || array.size() == 0)
		{
			return;
		}
		if (join == null)
		{
			join = ""; 
		}
		
		int count = 0;
		
		Iterator<String> iterator = array.iterator();
		while (iterator.hasNext())
		{
			if (count != 0)
			{
				ret.append(join);
			}

			String next = iterator.next();
			ret.append(next);
			count++;
		}
	}
	
	/**
	 * Used to create a string which represents the objects in an array
	 * @param result
	 * @return
	 */
	public static <T> String buildFromToString(ArrayList<T> list) {
		StringBuilder ret = new StringBuilder();
		
		int count = 0;
		
		for (T t : list)
		{
			if (count != 0) {
				ret.append(", ");
			}
			ret.append(t.toString());
			count++;
		}
		
		
		return ret.toString();
	}
}
