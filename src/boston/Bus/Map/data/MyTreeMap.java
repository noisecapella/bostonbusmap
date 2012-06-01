package boston.Bus.Map.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class MyTreeMap<K, V> {
	private final TreeMap<K, V> map;

	public MyTreeMap() {
		map = new TreeMap<K, V>();
	}
	
	public V put(K k, V v) {
		return map.put(k, v);
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public V get(K k) {
		return map.get(k);
	}
	public boolean containsKey(K k) {
		return map.containsKey(k);
	}

	public Collection<V> values() {
		return map.values();
	}

	public void putAll(MyHashMap<K, V> ret) {
		for (K k : map.keySet()) {
			V v = map.get(k);
			ret.put(k, v);
		}
	}

	public V remove(K k) {
		return map.remove(k);
	}

	public void clear() {
		map.clear();
		
	}

}
