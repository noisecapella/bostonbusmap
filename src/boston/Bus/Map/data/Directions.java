package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.provider.DatabaseAgent;


public class Directions {
	private final ConcurrentHashMap<String, Direction> directions
		= new ConcurrentHashMap<String, Direction>();
	
	private boolean isRefreshed = false;

	private DatabaseAgent databaseAgent;
	
	public Directions(DatabaseAgent databaseAgent) {
		this.databaseAgent = databaseAgent;
	}

	public void add(String dirTag, Direction direction) {
		directions.putIfAbsent(dirTag, direction);
	}
	
	public Direction getDirection(String dirTag)
	{
		if (dirTag == null)
		{
			return null;
		}
		Direction direction = directions.get(dirTag);
		if (direction == null)
		{
			// Log.i("BostonBusMap", "strange, dirTag + " + dirTag + " doesnt exist. If you see this many times, we're having trouble storing the data in the database. Too much DB activity causes objects to persist which causes a crash");
			doRefresh();
			
			return directions.get(dirTag);
		}
		else
		{
			return direction;
		}
	}
	

	private void doRefresh() {
		if (isRefreshed == false)
		{
			databaseAgent.refreshDirections(directions);
			isRefreshed = true;
		}
		
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
}
