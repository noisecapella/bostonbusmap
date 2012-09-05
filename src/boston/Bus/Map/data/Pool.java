package boston.Bus.Map.data;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public abstract class Pool<K, V> {
	private final LinkedList<K> priorities = new LinkedList<K>();
	private final MyHashMap<K, V> pool = new MyHashMap<K, V>();
	
	private final int limit;
	
	public Pool(int limit) {
		this.limit = limit;
	}
	
	protected void clearAll() {
		priorities.clear();
		pool.clear();
	}
	
	protected abstract V create(K key) throws IOException;
	
	protected Collection<V> values() {
		return pool.values();
	}

	public V get(K key) throws IOException {
		if (pool.containsKey(key)) {
			return pool.get(key);
		}
		else
		{
			V value = create(key);
			if (value == null) {
				return null;
			}
			else
			{
				if (priorities.size() >= limit) {
					K firstKey = priorities.get(0);
					priorities.remove(0);
					pool.remove(firstKey);
				}
				
				priorities.add(key);
				pool.put(key, value);
				return value;
			}
		}
	}
}
