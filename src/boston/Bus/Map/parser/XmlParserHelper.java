package boston.Bus.Map.parser;



import org.xml.sax.Attributes;

import boston.Bus.Map.data.MyHashMap;

public class XmlParserHelper {
	
	public static String getAttribute(String key, Attributes attributes, MyHashMap<String, Integer> tagCache)
	{
		Integer value = tagCache.get(key);
		if (null == value || value.intValue() == -1)
		{
			return null;
		}
		else
		{
			return attributes.getValue(value);
		}
	}

	public static void clearAttributes(Attributes attributes, MyHashMap<String, Integer> tagCache)
	{
		final int attributesLength = attributes.getLength();
		for (String key : tagCache.keySet())
		{
			tagCache.put(key, -1);
		}
		for (int i = 0; i < attributesLength; i++)
		{
			String name = attributes.getLocalName(i);
			tagCache.put(name, i);
		}
		
	}

}
