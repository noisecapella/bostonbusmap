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
import boston.Bus.Map.math.Geometry;


public class GetDirections  {
	private final HashSet<DirectionPath> closedSet = new HashSet<DirectionPath>();
	private final HashSet<DirectionPath> openSet = new HashSet<DirectionPath>();
	private final MyHashMap<String, DirectionPath> cameFrom = new MyHashMap<String, DirectionPath>();
	
	private final MyHashMap<String, Float> gScore = new MyHashMap<String, Float>();
	private final MyHashMap<String, Float> fScore = new MyHashMap<String, Float>();
	
	private final Directions directions;
	private final RoutePool routePool;
	
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
			fScore.put(direction.getDirTag(), 0 + heuristicCostEstimate(start, goal));
		}
		
		DirectionPath current = null;
		while (openSet.size() != 0) {
			current = getNodeWithLowestFScore();
			Collection<String> stopsForDirTag = directions.getStopTagsForDirTag(current.getDirTag());
			if (stopsForDirTag.contains(goal.getStopTag())) {
				return doReconstructPath(current);
			}
			
			publishProgress(current);
			
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
					cameFrom.put(neighbor.getDirTag(), current);
					gScore.put(neighbor.getDirTag(), tentativeGScore);
					fScore.put(neighbor.getDirTag(), tentativeGScore + heuristicCostEstimate(neighbor.getStop(), goal));
				}
			}
		}
		throw new RuntimeException("No path found from " + start.getStopTag() + " to " + goal.getStopTag());
	}

	private void publishProgress(DirectionPath path) {
		// TODO Auto-generated method stub
		//System.out.println(string);
		Log.i("BostonBusMap", "PUBLISHPROGRESS: " + path);
	}

	private Collection<DirectionPath> getNeighborNodes(DirectionPath current) throws IOException {
		ArrayList<DirectionPath> ret = new ArrayList<DirectionPath>();
		HashSet<String> directionsAdded = new HashSet<String>();
		Collection<String> stopsForDirTag = directions.getStopTagsForDirTag(current.getDirTag());
		for (String stopTag : stopsForDirTag) {
			RouteConfig route = routePool.get(current.getDirection().getRoute());
			StopLocation stop = route.getStop(stopTag);
			MyHashMap<String, Direction> directionsForStop = directions.getDirectionsForStop(stopTag);
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
		Float f = gScore.get(neighbor.getDirTag());
		if (f == null) {
			gScore.put(neighbor.getDirTag(), 0f);
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

	private static void reconstructPath(MyHashMap<String, DirectionPath> cameFrom, DirectionPath currentNode, ArrayList<DirectionPath> reverseRet) {
		if (cameFrom.containsKey(currentNode.getDirTag())) {
			reconstructPath(cameFrom, cameFrom.get(currentNode.getDirTag()), reverseRet);
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
			Float f = fScore.get(directionPath.getDirTag());
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

	public ArrayList<DirectionPath> run(StopLocation from, StopLocation to) throws IOException {
		return getDirections(from, to);
	}

	/**
	 * A given stop, and the direction from that stop to some better place
	 * @author schneg
	 *
	 */
	public class DirectionPath {
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
		
		@Override
		public String toString() {
			String directionString = direction != null ? direction.getTitle() + ":" + direction.getRoute() : "NULL";
			String stopString = stop != null ? ", " + stop.getTitle() : "null";
			
			return "[" + directionString + stopString + "]"; 
		}
	}
}
