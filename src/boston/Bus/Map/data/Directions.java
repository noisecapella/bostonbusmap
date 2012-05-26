package boston.Bus.Map.data;

import java.util.ArrayList;


import android.util.Log;
import boston.Bus.Map.database.DatabaseHelper;

public class Directions {
	private final MyHashMap<String, Integer> indexes = new MyHashMap<String, Integer>();
	private final ArrayList<String> names = new ArrayList<String>();
	private final ArrayList<String> titles = new ArrayList<String>();
	private final ArrayList<String> routes = new ArrayList<String>();
	
	private final DatabaseHelper helper;
	
	private boolean isRefreshed = false;
	
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
		if (dirTag == null)
		{
			return null;
		}
		Integer i = indexes.get(dirTag);
		if (i == null)
		{
			Log.i("BostonBusMap", "strange, dirTag + " + dirTag + " doesnt exist. If you see this many times, we're having trouble storing the data in the database. Too much DB activity causes objects to persist which causes a crash");
			if (isRefreshed == false)
			{
				synchronized(indexes)
				{
					helper.refreshDirections(indexes, names, titles, routes);
				}
				isRefreshed = true;
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

	/**
	 * Returns a displayable HTML string of the direction's title and name
	 * @param dirTag
	 * @return
	 */
	public String getTitleAndName(String dirTag) {
		if (dirTag == null)
		{
			return null;
		}
		
		Integer i = getIndex(dirTag);
		if (i == null)
		{
			return null;
		}
		else
		{
			String title = titles.get(i);
			String name = names.get(i);
			boolean emptyTitle = title == null || title.length() == 0;
			boolean emptyName = name == null || name.length() == 0;
			
			if (emptyName && emptyTitle)
			{
				return null;
			}
			else if (emptyTitle)
			{
				return name;
			}
			else if (emptyName)
			{
				return title;
			}
			else
			{
				return title + "<br />" + name;
			}
				
		}
	}
	
	
}
