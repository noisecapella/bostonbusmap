package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.math.Geometry;

import android.os.AsyncTask;
import android.util.Log;

public class GetDirectionsAsyncTask extends AsyncTask<String, Object, ArrayList<StopLocation>> {
	private final RoutePool routePool;
	private final HashSet<StopLocation> closedSet = new HashSet<StopLocation>();
	private final HashSet<StopLocation> openSet = new HashSet<StopLocation>();
	private final HashMap<StopLocation, StopLocation> cameFrom = new HashMap<StopLocation, StopLocation>();
	
	private final HashMap<StopLocation, Float> gScore = new HashMap<StopLocation, Float>();
	private final HashMap<StopLocation, Float> fScore = new HashMap<StopLocation, Float>();
	
	public GetDirectionsAsyncTask(Locations locations) {
		this.routePool = locations.getRoutePool();
	}
	
	@Override
	protected ArrayList<StopLocation> doInBackground(String... params) {
		if (params.length != 2)
		{
			throw new RuntimeException("params must be of length 2");
		}
		
		StopLocation start = routePool.getAllStopTagsAtLocation(params[0]).values().toArray(new StopLocation[0])[0];
		StopLocation end = routePool.getAllStopTagsAtLocation(params[1]).values().toArray(new StopLocation[0])[0];
		return getDirections(start, end);
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (values.length >= 1) {
			Log.i("BostonBusMap", "Progress update: " + values[0].toString());
		}
	}
	
	private ArrayList<StopLocation> getDirections(StopLocation start, StopLocation goal) {
		openSet.add(start);
		
		fScore.put(start, 0 + heuristicCostEstimate(start, goal));
		
		StopLocation current = null;
		while (openSet.size() != 0) {
			current = getNodeWithLowestFScore();
			if (current == goal) {
				return doReconstructPath(goal);
			}
			
			publishProgress("At " + current.getStopTag() + ", " + current.getTitle());
			
			openSet.remove(current);
			closedSet.add(current);
			
			Collection<StopLocation> neighborNodes = getNeighborNodes(current);
			for (StopLocation neighbor : neighborNodes) {
				if (closedSet.contains(neighbor))
				{
					continue;
				}
				float tentativeGScore = getGScore(current) + distanceBetween(current, neighbor);
				
				if (openSet.contains(neighbor) == false || tentativeGScore < getGScore(neighbor)) {
					openSet.add(neighbor);
					cameFrom.put(neighbor, current);
					gScore.put(neighbor, tentativeGScore);
					fScore.put(neighbor, tentativeGScore + heuristicCostEstimate(neighbor, goal));
				}
			}
		}
		throw new RuntimeException("No path found from " + start.getStopTag() + " to " + goal.getStopTag());
	}

	private Collection<StopLocation> getNeighborNodes(StopLocation current) {
		List<StopLocation> ret = routePool.getClosestStops(new Double(current.getLatitudeAsDegrees()), new Double(current.getLongitudeAsDegrees()), 10);
		return ret;
	}

	private float getGScore(StopLocation neighbor) {
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
				current.getLongitudeAsDegrees(), neighbor.getLatitudeAsDegrees(), neighbor.getLongitudeAsDegrees());
	}

	private static void reconstructPath(HashMap<StopLocation, StopLocation> cameFrom, StopLocation currentNode, ArrayList<StopLocation> reverseRet) {
		if (cameFrom.containsKey(currentNode)) {
			reconstructPath(cameFrom, currentNode, reverseRet);
		}
		reverseRet.add(currentNode);
	}
	
	private ArrayList<StopLocation> doReconstructPath(StopLocation currentNode) {
		ArrayList<StopLocation> reverseRet = new ArrayList<StopLocation>();
		reconstructPath(cameFrom, currentNode, reverseRet);
		Collections.reverse(reverseRet);
		return reverseRet;
	}

	private StopLocation getNodeWithLowestFScore() {
		// TODO: use a priority queue
		StopLocation lowest = null;
		float lastFScore = Float.MAX_VALUE;
		
		for (StopLocation stopLocation : openSet) {
			Float f = fScore.get(stopLocation);
			if (f != null && f.floatValue() < lastFScore) {
				lowest = stopLocation;
				lastFScore = f.floatValue();
			}
		}
		return lowest;
	}

	private static float heuristicCostEstimate(StopLocation start, StopLocation end) {
		return distanceBetween(start, end);
	}

	public void runTask(String start, String end) {
		execute(start, end);
	}

	@Override
	protected void onPostExecute(ArrayList<StopLocation> result) {
		for (StopLocation stopLocation : result) {
			Log.i("BostonBusMap", stopLocation.getStopTag());
		}
	}
}
