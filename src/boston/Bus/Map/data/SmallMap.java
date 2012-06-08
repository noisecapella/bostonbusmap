package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SmallMap<K extends Comparable<K>, V> {
	private final ArrayList<K> keys = new ArrayList<K>();
	private final ArrayList<V> values = new ArrayList<V>();
	
	public void put(K k, V v) {
		int index = Collections.binarySearch(keys, k);
		if (index < 0) {
			index = -index - 1;
			keys.add(index, k);
			values.add(index, v);
		}
		else
		{
			keys.set(index, k);
			values.set(index, v);
		}
	}

	public Collection<K> keySet() {
		return keys;
	}

	public V get(K k) {
		int index = Collections.binarySearch(keys, k);
		if (index < 0) {
			return null;
		}
		else
		{
			return values.get(index);
		}
	}
}
