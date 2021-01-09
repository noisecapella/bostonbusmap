package com.schneeloch.bostonbusmap_library.data;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.schneeloch.bostonbusmap_library.database.Schema;

/**
 * Convenience class to handle mapping routes to route titles
 * @author schneg
 *
 */
public class RouteTitles extends TransitSourceTitles
{
	private final ImmutableMap<String, Integer> tagToIndex;
	private final ImmutableMap<Schema.Routes.SourceId, TransitSourceTitles> transitSourceMaps;
	private final ImmutableMap<String, Schema.Routes.SourceId> transitSourceIds;
	
	public RouteTitles(ImmutableBiMap<String, String> map, 
			ImmutableMap<String, Schema.Routes.SourceId> transitSourceIds) {
		super(map);

		ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
		int count = 0;
		for (String key : map.keySet()) {
			builder.put(key, count);
			count++;
		}
		tagToIndex = builder.build();

		Map<Schema.Routes.SourceId, ImmutableBiMap.Builder<String, String>> transitSourceBuilder
				= Maps.newHashMap();
		
		for (String name : map.keySet()) {
			String title = map.get(name);
			Schema.Routes.SourceId transitSourceId = transitSourceIds.get(name);

			ImmutableBiMap.Builder<String, String> miniBuilder;
			if (transitSourceBuilder.containsKey(transitSourceId) == false) {
				miniBuilder = ImmutableBiMap.builder();
				transitSourceBuilder.put(transitSourceId, miniBuilder);
			}
			else
			{
				miniBuilder = transitSourceBuilder.get(transitSourceId);
			}
			miniBuilder.put(name, title);
		}

		Map<Schema.Routes.SourceId, TransitSourceTitles> transitionMap = Maps.newHashMap();
		for (Schema.Routes.SourceId transitSourceId : transitSourceBuilder.keySet()) {
			ImmutableBiMap.Builder<String, String> value = transitSourceBuilder.get(transitSourceId);

			TransitSourceTitles transitSourceTitles = new TransitSourceTitles(value.build());
			transitionMap.put(transitSourceId, transitSourceTitles);
		}

		this.transitSourceMaps = ImmutableMap.copyOf(transitionMap);
		this.transitSourceIds = transitSourceIds;
	}
	
	public int getIndexForTag(String route) {
		if (tagToIndex.containsKey(route)) {
			return tagToIndex.get(route);
		}
		else
		{
			return -1;
		}
	}

	public String getTagUsingIndex(int index) {
        if (index < 0 || index >= map.size())
        {
        	index = map.size() - 1;
        }
		
		return map.keySet().asList().get(index);
	}

	public TransitSourceTitles getMappingForSources(
			Schema.Routes.SourceId[] transitSourceIds) {
		ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
		
		for (Schema.Routes.SourceId id : transitSourceIds) {
			TransitSourceTitles titles = transitSourceMaps.get(id);
			builder.putAll(titles.map);
		}
		
		return new TransitSourceTitles(builder.build());
	}

	public TransitSourceTitles getMappingForSource(Schema.Routes.SourceId transitSourceId) {
		return transitSourceMaps.get(transitSourceId);
	}
	
	public Schema.Routes.SourceId getTransitSourceId(String routeName) {
		return transitSourceIds.get(routeName);
	}

	public ImmutableMap<String,Schema.Routes.SourceId> getSourceIdMap() {
		return transitSourceIds;
	}
}
