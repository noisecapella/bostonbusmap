package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.HashMap;

import android.database.sqlite.SQLiteDatabase;
import boston.Bus.Map.database.DatabaseHelper;

public class Directions {
	private final HashMap<String, Integer> indexes = new HashMap<String, Integer>();
	private final ArrayList<String> names = new ArrayList<String>();
	private final ArrayList<String> titles = new ArrayList<String>();
	private final ArrayList<String> routes = new ArrayList<String>();
	
	private final DatabaseHelper helper;
	
	public Directions(DatabaseHelper helper) {
		this.helper = helper;
	}

	public void add(String dirTag, String name, String title, String route)
	{
		if (indexes.containsKey(dirTag) == false)
		{
			synchronized(indexes)
			{
				indexes.put(dirTag, names.size());
				names.add(name);
				titles.add(title);
				routes.add(route);
			}
		}
	}
	
	private Integer getIndex(String dirTag)
	{
		Integer i = indexes.get(dirTag);
		if (i == null)
		{
			synchronized(indexes)
			{
				helper.refreshDirections(indexes, names, titles, routes);
			}

			return indexes.get(dirTag);
		}
		else
		{
			return i;
		}
	}
	
	public String getName(String dirTag)
	{
		Integer i = getIndex(dirTag);
		if (i == null)
		{
			return null;
		}
		else
		{
			return names.get(i);
		}
	}
	

	public String getTitle(String dirTag)
	{
		Integer i = getIndex(dirTag);
		if (i == null)
		{
			return null;
		}
		else
		{
			return titles.get(i);
		}
	}

	public void writeToDatabase(boolean wipe) {
		helper.writeDirections(wipe, indexes, names, titles, routes);
	}

	public String getTitleAndName(String dirTag) {
		Integer i = getIndex(dirTag);
		if (i == null)
		{
			return null;
		}
		else
		{
			String title = titles.get(i);
			String name = names.get(i);
			if (title == null && name == null)
			{
				return null;
			}
			else if (title == null)
			{
				return name;
			}
			else if (name == null)
			{
				return title;
			}
			else
			{
				return title + "\n" + name;
			}
				
		}
	}
	
	
}
