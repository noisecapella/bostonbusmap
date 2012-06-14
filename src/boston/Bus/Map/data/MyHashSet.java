package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class MyHashSet<K> implements Iterable<K> {
	private final HashSet<K> set = new HashSet<K>();

	@Override
	public Iterator<K> iterator() {
		return set.iterator();
	}

	public boolean add(K object) {
		return set.add(object);
	}

	public boolean contains(K obj) {
		return set.contains(obj);
	}

	public void clear() {
		set.clear();
		
	}

	public boolean remove(K item) {
		return set.remove(item);
	}

	public int size() {
		return set.size();
	}
}
