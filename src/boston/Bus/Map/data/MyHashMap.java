package boston.Bus.Map.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper around HashMap, to ensure type safety of contains() and get()
 * @author schneg
 *
 * @param <K>
 * @param <V>
 */
public class MyHashMap<K, V> {
	private final HashMap<K, V> map;

	public MyHashMap(int i) {
		map = new HashMap<K, V>(i);
	}

	public MyHashMap() {
		map = new HashMap<K, V>();
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
		for (K k : ret.keySet()) {
			V v = ret.get(k);
			map.put(k, v);
		}
	}

	public V remove(K k) {
		return map.remove(k);
	}

	public void clear() {
		map.clear();
		
	}

	public int size() {
		return map.size();
	}

	public void putAllFrom(
			ConcurrentHashMap<K, V> from) {
		for (K k : map.keySet()) {
			V v = map.get(k);
			from.put(k, v);
		}
	}
}
