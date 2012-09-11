package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;


import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

public class Directions {
	private final ConcurrentHashMap<String, Direction> directions
		= new ConcurrentHashMap<String, Direction>();
	
	private boolean isRefreshed = false;

	private Context context;
	
	public Directions(Context context) {
		this.context = context;
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
			Log.i("BostonBusMap", "strange, dirTag + " + dirTag + " doesnt exist. If you see this many times, we're having trouble storing the data in the database. Too much DB activity causes objects to persist which causes a crash");
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
			DatabaseAgent.refreshDirections(context.getContentResolver(), directions);
			isRefreshed = true;
		}
		
	}

	public void writeToDatabase(boolean wipe) throws RemoteException, OperationApplicationException {
		DatabaseAgent.writeDirections(context.getContentResolver(), wipe, directions);
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

	public MyHashMap<String, Direction> getDirectionsForStop(String stopTag) {
		HashSet<String> dirTags = DatabaseAgent.getDirectionTagsForStop(context.getContentResolver(), stopTag);
		MyHashMap<String, Direction> ret = new MyHashMap<String, Direction>();
		for (String dirTag : dirTags) {
			ret.put(dirTag, getDirection(dirTag));
		}
		return ret;
	}

	public HashSet<String> getStopTagsForDirTag(String dirTag) {
		return DatabaseAgent.getStopTagsForDirTag(context.getContentResolver(), dirTag);
	}
}
