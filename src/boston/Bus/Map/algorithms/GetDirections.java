package boston.Bus.Map.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.util.Log;
import android.util.Pair;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.math.Geometry;


public class GetDirections  {
	private final HashSet<DirectionPath> closedSet = new HashSet<DirectionPath>();
	private final HashSet<DirectionPath> openSet = new HashSet<DirectionPath>();
	private final MyHashMap<DirectionPath, DirectionPath> cameFrom = new MyHashMap<DirectionPath, DirectionPath>();
	
	private final MyHashMap<DirectionPath, Float> gScore = new MyHashMap<DirectionPath, Float>();
	private final MyHashMap<DirectionPath, Float> fScore = new MyHashMap<DirectionPath, Float>();
	
	private final Directions directions;
	private final RoutePool routePool;
	
	private final MyHashMap<String, MyHashMap<String, Direction>> stopsToDirections = 
			new MyHashMap<String, MyHashMap<String,Direction>>();
	
	private final MyHashMap<String, HashSet<String>> dirTagsToStopTags = 
			new MyHashMap<String, HashSet<String>>();
	
	public GetDirections(Directions directions, RoutePool routePool) {
		this.directions = directions;
		this.routePool = routePool;
	}
	
	private ArrayList<DirectionPath> getDirections(StopLocation start, StopLocation goal) throws IOException {
		MyHashMap<String, Direction> directionsForStop = directions.getDirectionsForStop(start.getStopTag());
		
		for (String dirTag : directionsForStop.keySet()) {
			Direction direction = directionsForStop.get(dirTag);
			openSet.add(new DirectionPath(start, dirTag, direction));
		}
		
		for (DirectionPath direction : openSet) {
			fScore.put(direction, 0 + heuristicCostEstimate(start, goal));
		}
		
		DirectionPath current = null;
		while (openSet.size() != 0) {
			current = getNodeWithLowestFScore();
			HashSet<String> stopsForDirTag = getStopsForDirTag(current.getDirTag());
			if (stopsForDirTag.contains(goal.getStopTag())) {
				return doReconstructPath(new DirectionPath(goal, null, null));
			}
			
			//publishProgress("At " + current.getTitle());
			
			openSet.remove(current);
			closedSet.add(current);
			
			Collection<DirectionPath> neighborNodes = getNeighborNodes(current);
			for (DirectionPath neighbor : neighborNodes) {
				if (closedSet.contains(neighbor))
				{
					continue;
				}
				float tentativeGScore = getGScore(current) + distanceBetween(current.getStop(), neighbor.getStop());
				
				if (openSet.contains(neighbor) == false || tentativeGScore < getGScore(neighbor)) {
					openSet.add(neighbor);
					cameFrom.put(neighbor, current);
					gScore.put(neighbor, tentativeGScore);
					fScore.put(neighbor, tentativeGScore + heuristicCostEstimate(neighbor.getStop(), goal));
				}
			}
		}
		throw new RuntimeException("No path found from " + start.getStopTag() + " to " + goal.getStopTag());
	}

	private HashSet<String> getStopsForDirTag(String dirTag) {
		HashSet<String> ret = dirTagsToStopTags.get(dirTag);
		if (ret == null) {
			ret = directions.getStopTagsForDirTag(dirTag);
			dirTagsToStopTags.put(dirTag, ret);
		}
		return ret;
	}


	private MyHashMap<String, Direction> getDirectionsForStop(String stopTag) {
		MyHashMap<String, Direction> ret = stopsToDirections.get(stopTag);
		if (ret == null)
		{
			ret = directions.getDirectionsForStop(stopTag);
			stopsToDirections.put(stopTag, ret);
		}
		return ret;
	}

	private void publishProgress(String string) {
		// TODO Auto-generated method stub
		//System.out.println(string);
	}

	private Collection<DirectionPath> getNeighborNodes(DirectionPath current) throws IOException {
		ArrayList<DirectionPath> ret = new ArrayList<DirectionPath>();
		HashSet<String> directionsAdded = new HashSet<String>();
		HashSet<String> stopsForDirTag = getStopsForDirTag(current.getDirTag());
		for (String stopTag : stopsForDirTag) {
			RouteConfig route = routePool.get(current.getDirection().getRoute());
			StopLocation stop = route.getStop(stopTag);
			MyHashMap<String, Direction> directionsForStop = getDirectionsForStop(stopTag);
			for (String dirTag : directionsForStop.keySet()) {
				if (directionsAdded.contains(dirTag) == false) {
					Direction direction = directionsForStop.get(dirTag);
					ret.add(new DirectionPath(stop, dirTag, direction));
					directionsAdded.add(dirTag);
				}
			}
		}
		return ret;
	}

	private float getGScore(DirectionPath neighbor) {
		Float f = gScore.get(neighbor);
		if (f == null) {
			gScore.put(neighbor, 0f);
			return 0f;
		}
		else
		{
			return f;
		}
	}

	private static Float distanceBetween(StopLocation current, StopLocation neighbor) {
		return Geometry.computeCompareDistanceFloat(current.getLatitudeAsDegrees(), 
				current.getLongitudeAsDegrees(),
				neighbor.getLatitudeAsDegrees(), 
				neighbor.getLongitudeAsDegrees());
	}

	private static void reconstructPath(MyHashMap<DirectionPath, DirectionPath> cameFrom, DirectionPath currentNode, ArrayList<DirectionPath> reverseRet) {
		if (cameFrom.containsKey(currentNode)) {
			reconstructPath(cameFrom, cameFrom.get(currentNode), reverseRet);
		}
		reverseRet.add(currentNode);
	}
	
	private ArrayList<DirectionPath> doReconstructPath(DirectionPath currentNode) {
		ArrayList<DirectionPath> reverseRet = new ArrayList<DirectionPath>();
		reconstructPath(cameFrom, currentNode, reverseRet);
		Collections.reverse(reverseRet);
		return reverseRet;
	}

	private DirectionPath getNodeWithLowestFScore() {
		// TODO: use a priority queue
		DirectionPath lowest = null;
		float lastFScore = Float.MAX_VALUE;
		
		for (DirectionPath directionPath : openSet) {
			Float f = fScore.get(directionPath);
			if (f != null && f.floatValue() < lastFScore) {
				lowest = directionPath;
				lastFScore = f.floatValue();
			}
		}
		return lowest;
	}

	private static float heuristicCostEstimate(StopLocation start, StopLocation end) {
		return distanceBetween(start, end);
	}

	public void run(StopLocation from, StopLocation to) throws IOException {
		ArrayList<DirectionPath> path = getDirections(from, to);
		
		for (DirectionPath directionPath : path) {
			Log.i("BostonBusMap", "Stop: " + directionPath.getStop().getStopTag() + ", " + 
		directionPath.getStop().getTitle() + ", dirPath: " + directionPath.getDirTag());
		}
	}

	/**
	 * A given stop, and the direction from that stop to some better place
	 * @author schneg
	 *
	 */
	private class DirectionPath {
		private final StopLocation stop;
		private final String dirTag;
		private final Direction direction;
		
		public DirectionPath(StopLocation stop, String dirTag, Direction direction) {
			this.stop = stop;
			this.dirTag = dirTag;
			this.direction = direction;
		}
		
		public StopLocation getStop() {
			return stop;
		}
		
		public String getDirTag() {
			return dirTag;
		}
		
		public Direction getDirection() {
			return direction;
		}
		
		@Override
		public int hashCode() {
			return dirTag.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DirectionPath) {
				return ((DirectionPath)o).dirTag.equals(dirTag);
			}
			else
			{
				return false;
			}
		}
	}
}
