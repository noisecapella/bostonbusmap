package boston.Bus.Map.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Convenience class to handle mapping routes to route titles
 * @author schneg
 *
 */
public class RouteTitles {
	private final ImmutableMap<String, String> map;
	private final ImmutableMultimap<String, String> inverse;
	
	public RouteTitles(ImmutableMap<String, String> map) {
		this.map = map;
		Multimap<String, String> s = ArrayListMultimap.create();
		for (String key : map.keySet()) {
			String value = map.get(key);
			s.put(value, key);
		}
		inverse = ImmutableMultimap.copyOf(s);
	}

	public Set<String> routeTags() {
		return map.keySet();
	}

	public Collection<String> routeTitles() {
		return map.values();
	}

	public String getTitle(String routeKey) {
		return map.get(routeKey);
	}

	public String getKey(String routeTitle) {
		return Iterables.getFirst(inverse.get(routeTitle), "");
	}

	public List<String> getKeys() {
		return map.keySet().asList();
	}

	public void addSelfTo(Builder<String, String> builder) {
		builder.putAll(map);
	}

	public String[] titleArray() {
		return map.values().toArray(new String[0]);
	}

	public String[] tagArray() {
		return map.keySet().toArray(new String[0]);
	}

	public String getTagUsingIndex(int index) {
        if (index < 0 || index >= map.size())
        {
        	index = map.size() - 1;
        }
		
		return map.keySet().asList().get(index);
	}

	public int size() {
		return map.size();
	}

	public int getIndexForTag(String route) {
		int count = 0;
		for (String routeTag : map.keySet())
		{
			if (route.equals(routeTag))
			{
				return count;
			}
			count++;
		}
		return -1;
	}
}
