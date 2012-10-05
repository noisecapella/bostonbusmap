package boston.Bus.Map.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import boston.Bus.Map.algorithms.GetDirections.DirectionPath;

public class StringUtil {
	
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
