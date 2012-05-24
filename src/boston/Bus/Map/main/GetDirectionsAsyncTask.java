package boston.Bus.Map.main;

import java.util.ArrayList;

import boston.Bus.Map.data.StopLocation;

import android.os.AsyncTask;
import android.util.Log;

public class GetDirectionsAsyncTask extends AsyncTask<String, Object, ArrayList<StopLocation>> {

	@Override
	protected ArrayList<StopLocation> doInBackground(String... params) {
		if (params.length != 2)
		{
			throw new RuntimeException("params must be of length 2");
		}
		
		return getDirections(params[0], params[1]);
	}

	private ArrayList<StopLocation> getDirections(String start, String end) {
		// implement A-star here
		throw new RuntimeException("Not implemented");
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
