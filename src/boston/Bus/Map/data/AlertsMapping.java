package boston.Bus.Map.data;

import java.util.Map;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;



public class AlertsMapping {
	public static final String alertUrlPrefix = "http://talerts.com/rssfeed/alertsrss.aspx?";

	private final ImmutableMap<String, Integer> routeDescriptionToAlertKey;
	
	public AlertsMapping(String alertsMappingData)
	{
		String[] lines = alertsMappingData.split("\n");
		ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
		for (String line : lines)
		{
			line = line.trim();
			
			String[] fields = line.split(",");
			int alertKey = Integer.parseInt(fields[fields.length - 1]);
			String routeDescription = fields[0];
			
			builder.put(routeDescription, alertKey);
		}
		routeDescriptionToAlertKey = builder.build();				
	}

	public ImmutableMap<String, Integer> getAlertNumbers(RouteTitles routeKeysToTitles)
	{
		Map<String, Integer> ret = Maps.newHashMap();
		
		for (String routeName : routeKeysToTitles.routeTitles())
		{
			for (String routeDescription : routeDescriptionToAlertKey.keySet())
			{
				if (routeDescription.equals(routeName))
				{
					int value = routeDescriptionToAlertKey.get(routeDescription);
					ret.put(routeName, value);
					break;
				}
			}

			//try startwith
			if (ret.containsKey(routeName) == false)
			{
				for (String routeDescription : routeDescriptionToAlertKey.keySet())
				{
					if (routeDescription.startsWith(routeName + " ") ||
							routeDescription.startsWith(routeName + "/"))
					{
						int value = routeDescriptionToAlertKey.get(routeDescription);
						ret.put(routeName, value);
						break;
					}
				}
			}
		}
		
		//special cases
		addToList("CT1", 50, routeKeysToTitles, ret);
		addToList("CT2", 51, routeKeysToTitles, ret);
		addToList("CT3", 52, routeKeysToTitles, ret);

		addToList("Silver Line SL1", 20, routeKeysToTitles, ret);
		addToList("Silver Line SL2", 28, routeKeysToTitles, ret);
		addToList("Silver Line SL4", 53, routeKeysToTitles, ret);
		//which alert index number is SL5?
		//addToList("Silver Line SL5", 50, routeKeysToTitles, ret);

		ret.put("Red", 15);
		ret.put("Orange", 16);
		ret.put("Blue", 18);

		return ImmutableMap.copyOf(ret);
	}

	private void addToList(String routeTitle, int alertIndex, RouteTitles routeKeysToTitles,
			Map<String, Integer> alertsMapping) {
		for (String routeKey : routeKeysToTitles.routeTitles())
		{
			String potentialRouteTitle = routeKeysToTitles.getTitle(routeKey);
			if (routeTitle.equals(potentialRouteTitle))
			{
				alertsMapping.put(routeKey, alertIndex);
				return;
			}
		}
	}
	
	
}
