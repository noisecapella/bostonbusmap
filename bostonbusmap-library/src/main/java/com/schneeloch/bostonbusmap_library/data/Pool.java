package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A thread-safe pool of objects
 * 
 * TODO: replace with LoadingCache
 * @author schneg
 *
 * @param <K>
 * @param <V>
 */
public abstract class Pool<K, V> {
	private final List<K> priorities = Lists.newLinkedList();
	private final Map<K, V> pool = Maps.newConcurrentMap();
	
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
