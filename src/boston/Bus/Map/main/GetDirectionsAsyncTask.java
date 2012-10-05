package boston.Bus.Map.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

import boston.Bus.Map.algorithms.GetDirections;
import boston.Bus.Map.algorithms.GetDirections.DirectionPath;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.ui.RouteOverlay;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetDirectionsAsyncTask extends AsyncTask<Object, String, ArrayList<DirectionPath>> {
	private final String startTag;
	private final String stopTag;
	private final Directions directions;
	private final RoutePool routePool;
	private final double currentLat;
	private final double currentLon;
	private final UpdateArguments arguments;
	
	public GetDirectionsAsyncTask(UpdateArguments arguments, String startTag, String stopTag,
			Directions directions, RoutePool routePool, double currentLat, double currentLon) {
		this.arguments = arguments;
		this.startTag = startTag;
		this.stopTag = stopTag;
		this.directions = directions;
		this.routePool = routePool;
		this.currentLat = currentLat;
		this.currentLon = currentLon;
	}
	
	@Override
	protected void onPreExecute() {
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(arguments.getContext(), values[0], Toast.LENGTH_LONG).show();
	}
	
	private StopLocation resolve(String tag) {
		if (tag.equals(GetDirectionsDialog.CURRENT_LOCATION_TAG)) {
			// note that this depends on the GPS being on when the app starts, which is
			// the default behavior
			Collection<StopLocation> closestStops = routePool.getClosestStops(currentLat, currentLon, 1);
			return Iterables.getFirst(closestStops, null);
		}
		else
		{
			Map<String, StopLocation> tagMap = routePool.getAllStopTagsAtLocation(tag);
			return tagMap.values().iterator().next();
		}
	}
	
	@Override
	protected ArrayList<DirectionPath> doInBackground(Object... params) {
		StopLocation start = resolve(startTag);
		StopLocation stop = resolve(stopTag);
		
		GetDirections getDirections = new GetDirections(directions, routePool);
		try
		{
			return getDirections.run(start, stop);
		}
		catch (Exception e) {
			LogUtil.e(e);
			return null;
		}
	}

	@Override
	protected void onPostExecute(ArrayList<DirectionPath> result) {
		if (result == null || result.size() == 0) {
			Toast.makeText(arguments.getContext(), "No directions!", Toast.LENGTH_LONG).show();
		}
		else
		{
			RouteOverlay overlay = arguments.getOverlayGroup().getDirectionsOverlay();
			overlay.clearPaths();
			
			List<Path> paths;
			try
			{
				paths = createPath(result);
			}
			catch (IOException e) {
				LogUtil.e(e);
				paths = Collections.emptyList();
			}
			
			overlay.setPathsAndColor(paths.toArray(new Path[0]), Color.RED, StringUtil.buildFromToString(result));
		}
	}

	private List<Path> createPath(ArrayList<DirectionPath> directionPaths) throws IOException {
		ArrayList<Path> ret = new ArrayList<Path>();
		
		for (int i = 0; i < directionPaths.size() - 1; i++) {
			ArrayList<Float> points = new ArrayList<Float>();
			
			DirectionPath path = directionPaths.get(i);
			DirectionPath next = directionPaths.get(i + 1);
			StopLocation start = path.getStop();
			StopLocation stop = next.getStop();
			
			String startRoute = path.getDirection().getRoute();
			RouteConfig startRouteConfig = routePool.get(startRoute);
			
			List<String> stopTagsInDirection = directions.getStopTagsForDirTag(path.getDirTag());
			int startIndex = stopTagsInDirection.indexOf(start.getStopTag());
			String crossStopTag = startRouteConfig.getCrossStopTag(stop, stopTagsInDirection);
			Log.i("BostonBusMap", "crossStopTag for " + stop + " is " + crossStopTag);
			int stopIndex = stopTagsInDirection.indexOf(crossStopTag);
			
			if (startIndex == -1) {
				throw new RuntimeException("startIndex is -1. path = " + path + ", next = " + next);
			}
			if (stopIndex == -1) {
				throw new RuntimeException("stopIndex is -1. path = " + path + ", next = " + next);
			}
			
			if (startIndex < stopIndex) {
				for (int j = startIndex; j <= stopIndex; j++) {
					String thisStopTag = stopTagsInDirection.get(j);
					StopLocation thisStop = startRouteConfig.getStop(thisStopTag);
					points.add(thisStop.getLatitudeAsDegrees());
					points.add(thisStop.getLongitudeAsDegrees());
				}
			} else if (startIndex > stopIndex) {
				for (int j = startIndex; j >= stopIndex; j--) {
					String thisStopTag = stopTagsInDirection.get(j);
					StopLocation thisStop = startRouteConfig.getStop(thisStopTag);
					points.add(thisStop.getLatitudeAsDegrees());
					points.add(thisStop.getLongitudeAsDegrees());
				}

			} else {
				throw new RuntimeException("start and stop are the same");
			}
			
			ret.add(new Path(points));
		}
		return ret;
	}
}
