package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

import boston.Bus.Map.provider.DatabaseContentProvider.DatabaseAgent;


import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

public class Directions {
	private final ConcurrentHashMap<String, Direction> directions
		= new ConcurrentHashMap<String, Direction>();
	
	//TODO: convert to Table
	private final ConcurrentHashMap<String, String[]> dirTagToStops
		= new ConcurrentHashMap<String, String[]>();;
	private final ConcurrentHashMap<String, String[]> stopsToDirTag
		= new ConcurrentHashMap<String, String[]>();;
	
	private boolean isRefreshed = false;

	private Context context;
	
	public Directions(Context context) {
		this.context = context;
	}

	public void add(String dirTag, Direction direction) {
		directions.putIfAbsent(dirTag, direction);
	}
	
	public boolean hasDirection(String dirTag) {
		return directions.containsKey(dirTag);
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

	public void writeToDatabase() throws RemoteException, OperationApplicationException {
		DatabaseAgent.writeDirections(context, directions);
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

	public Map<String, Direction> getDirectionsForStop(String stopTag) {
		String[] dirTags = stopsToDirTag.get(stopTag);
		if (dirTags != null) {
			return getDirections(Arrays.asList(dirTags));
		}
		else
		{
			ArrayList<String> dirTagSet = DatabaseAgent.getDirectionTagsForStop(context.getContentResolver(), stopTag);
			return getDirections(dirTagSet);
		}
	}

	private ImmutableMap<String, Direction> getDirections(Collection<String> dirTags) {
		Builder<String, Direction> ret = ImmutableMap.builder();
		for (String dirTag : dirTags) {
			ret.put(dirTag, getDirection(dirTag));
		}
		return ret.build();
	}

	public List<String> getStopTagsForDirTag(String dirTag) {
		String[] stopTags = dirTagToStops.get(dirTag);
		if (stopTags != null) {
			return Arrays.asList(stopTags);
		}
		else
		{
			List<String> ret = DatabaseAgent.getStopTagsForDirTag(context.getContentResolver(), dirTag);
			dirTagToStops.put(dirTag, ret.toArray(new String[0]));
			return ret;
		}
	}
}
