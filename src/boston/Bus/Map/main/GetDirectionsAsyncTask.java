package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.Arrays;

import boston.Bus.Map.algorithms.GetDirections;
import boston.Bus.Map.algorithms.GetDirections.DirectionPath;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.util.LogUtil;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class GetDirectionsAsyncTask extends AsyncTask<Object, String, ArrayList<DirectionPath>> {
	private final Context context;
	private final String startTag;
	private final String stopTag;
	private final Directions directions;
	private final RoutePool routePool;
	private final double currentLat;
	private final double currentLon;
	
	public GetDirectionsAsyncTask(Context context, String startTag, String stopTag,
			Directions directions, RoutePool routePool, double currentLat, double currentLon) {
		this.context = context;
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
		Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
	}
	
	private StopLocation resolve(String tag) {
		if (tag.equals(GetDirectionsDialog.CURRENT_LOCATION_TAG)) {
			// note that this depends on the GPS being on when the app starts, which is
			// the default behavior
			ArrayList<StopLocation> closestStops = routePool.getClosestStops(currentLat, currentLon, 1);
			return closestStops.get(0);
		}
		else
		{
			MyHashMap<String, StopLocation> tagMap = routePool.getAllStopTagsAtLocation(tag);
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
		for (DirectionPath path : result) {
			Toast.makeText(context, "Direction " + path.getDirection().getTitle() +
					"\nStop: " + path.getStop().getTitle(), Toast.LENGTH_LONG).show();
		}
	}
}
