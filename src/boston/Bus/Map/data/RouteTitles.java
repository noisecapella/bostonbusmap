package boston.Bus.Map.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

/**
 * Convenience class to handle mapping routes to route titles
 * @author schneg
 *
 */
public class RouteTitles {
	private final ImmutableBiMap<String, String> map;
	
	public RouteTitles(ImmutableBiMap<String, String> map) {
		this.map = map;
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
		return map.inverse().get(routeTitle);
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
