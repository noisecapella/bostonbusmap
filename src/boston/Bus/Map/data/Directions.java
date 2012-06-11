package boston.Bus.Map.data;

import java.util.ArrayList;


import android.util.Log;
import boston.Bus.Map.database.DatabaseHelper;

public class Directions {
	private final MyHashMap<String, Direction> directions = new MyHashMap<String, Direction>();
	
	private boolean isRefreshed = false;
	
	public void add(String dirTag, Direction direction) {
		if (directions.containsKey(dirTag) == false)
		{
			synchronized(directions)
			{
				directions.put(dirTag, direction);
			}
		}
		
	}
	
	public void add(String dirTag, String name, String title, String route)
	{
		add(dirTag, new Direction(name, title, route));
	}
	
	private Direction getDirection(String dirTag)
	{
		return directions.get(dirTag);
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
		
		Direction direction = getDirection(dirTag);
		if (direction == null)
		{
			return null;
		}
		else
		{
			String title = direction.getTitle();
			String name = direction.getName();
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

	public String getDirTag(String routeName, String tag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
