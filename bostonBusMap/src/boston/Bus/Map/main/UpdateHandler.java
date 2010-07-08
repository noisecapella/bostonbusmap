package boston.Bus.Map.main;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;

import boston.Bus.Map.data.Locations;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.util.OneTimeLocationListener;

import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateHandler extends Handler {
	/**
	 * An update which refreshes from the internet
	 */
	public static final int MAJOR = 1;
	/**
	 * An update where we just moved the map
	 */
	public static final int MINOR = 2;
	
	public static final int LOCATION_NOT_FOUND = 3;
	public static final int LOCATION_FOUND = 4;

	
	/**
	 * The last time we updated, in milliseconds. Used to make sure we don't update more frequently than
	 * every 10 seconds, to avoid unnecessary strain on their server
	 */
	private double lastUpdateTime;

	/**
	 * The minimum time in milliseconds between updates. The XML feed requires a minimum of 10 seconds,
	 * I'm doing 13 just in case
	 */
	public final static int busLocationsFetchDelay = 13000;
	
	public final static int predictionsFetchDelay = 15000;
	
	private final int maxOverlays = 23;

	private final int IMMEDIATE_REFRESH = 1;

	private boolean updateConstantly;
	private boolean hideHighlightCircle;
	private boolean showUnpredictable;
	private UpdateAsyncTask updateAsyncTask;
	private UpdateAsyncTask minorUpdate;
	
	private final TextView textView;
	private final MapView mapView;
	private boolean inferBusRoutes;
	private final Locations busLocations;
	private OneTimeLocationListener oneTimeLocationListener;
	private final DatabaseHelper helper;
	private final Context context;
	private final BusOverlay busOverlay; 
	
	private boolean isFirstRefresh;
	
	public UpdateHandler(TextView textView, Drawable busPicture, MapView mapView,
			Drawable arrow, Drawable tooltip, Locations busLocations, Context context, DatabaseHelper helper)
	{
		this.textView = textView;
		this.mapView = mapView;
		this.busLocations = busLocations;
		this.helper = helper;
		this.busOverlay = new BusOverlay(busPicture, context, this, mapView);
		lastUpdateTime = System.currentTimeMillis();
		
		this.context = context;
	}
	
	private int currentRoutesSupportedIndex = Locations.NO_CHANGE;
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what)
		{
		case MAJOR:
			//remove duplicates
			double currentTime = System.currentTimeMillis();
			
			int fetchDelay = getCurrentFetchDelay();
			
			if (currentTime - lastUpdateTime > fetchDelay || msg.arg1 == IMMEDIATE_REFRESH)
			{
				//if not too soon, do the update
				runUpdateTask("Finished update!", isFirstRefresh);
				isFirstRefresh = false;
			}

			//make updateBuses execute every 10 seconds (or whatever fetchDelay is)
			//to disable this, the user should go into the settings and uncheck 'Run in background'
			if (msg.arg1 != IMMEDIATE_REFRESH)
			{
				removeMessages(MAJOR);
				sendEmptyMessageDelayed(MAJOR, fetchDelay);
			}


			break;
		case MINOR:
			//don't do two updates at once
			Log.i("MINOR", "started");
			if (minorUpdate != null)
			{
				if (minorUpdate.getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
				{
					//task is not finished yet
					Log.i("MINOR", "not finished yet");
					return;
				}
				
			}

			//remove duplicate messages
			removeMessages(MINOR);
			
			minorUpdate = new UpdateAsyncTask(textView, mapView, null, getShowUnpredictable(), false, maxOverlays,
					getHideHighlightCircle() == false, getInferBusRoutes(), busOverlay, helper, currentRoutesSupportedIndex, false);
			

			minorUpdate.runUpdate(busLocations);
			
			Log.i("MINOR", "done currentRoutesSupportedIndex == " + currentRoutesSupportedIndex);

			break;
		case LOCATION_NOT_FOUND:
			Toast.makeText(context, "Cannot find location, try again later", Toast.LENGTH_LONG).show();
			
			busLocations.clearCurrentLocation();
			
			if (oneTimeLocationListener != null)
			{
				oneTimeLocationListener.release();
				oneTimeLocationListener = null;
			}
			else
			{
				//we normally do this in release()
				removeMessages(LOCATION_NOT_FOUND);
				removeMessages(LOCATION_FOUND);
			}
			
			
			break;
		case LOCATION_FOUND:
			busLocations.setCurrentLocation(msg.arg1, msg.arg2);
			
			removeMessages(LOCATION_NOT_FOUND);
			
			break;
		}
		
	}



	private int getCurrentFetchDelay() {
		if (currentRoutesSupportedIndex == 0)
		{
			return busLocationsFetchDelay;
		}
		else
		{
			return predictionsFetchDelay;
		}
	}



	public void removeAllMessages() {
		removeMessages(MAJOR);
		removeMessages(MINOR);
		removeMessages(LOCATION_NOT_FOUND);
		removeMessages(LOCATION_FOUND);
	}


	/**
	 * executes the update
	 */
	private void runUpdateTask(String finalMessage, boolean isFirstTime) {
		//make sure we don't update too often
		lastUpdateTime = System.currentTimeMillis();

		//don't do two updates at once
		if (updateAsyncTask != null)
		{
			if (updateAsyncTask.getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
			{
				//task is not finished yet
				return;
			}
			
		}
		
		
		updateAsyncTask = new UpdateAsyncTask(textView, mapView, finalMessage,
				getShowUnpredictable(), true, maxOverlays,
				getHideHighlightCircle() == false, getInferBusRoutes(), busOverlay, helper, currentRoutesSupportedIndex, isFirstTime);
		updateAsyncTask.runUpdate(busLocations);
	}

	public boolean instantRefresh() {
		//removeAllMessages();
		
		int fetchDelay = getCurrentFetchDelay();
		
		if(getUpdateConstantly())
		{
			//if the runInBackground checkbox is clicked, start the handler updating
			removeMessages(MAJOR);
			sendEmptyMessageDelayed(MAJOR, (long)(fetchDelay * 1.5));
		}
		
		if (System.currentTimeMillis() - lastUpdateTime < fetchDelay)
		{
			return false;
		}

		runUpdateTask("Finished update!", isFirstRefresh);
		isFirstRefresh = false;
		return true;

	}

	public boolean getUpdateConstantly() {
		return updateConstantly;
	}
	
	public void setUpdateConstantly(boolean b)
	{
		updateConstantly = b;
	}
	
	public boolean getHideHighlightCircle()
	{
		return hideHighlightCircle;
	}
	
	public void setHideHighlightCircle(boolean b)
	{
		hideHighlightCircle = b;
	}
	
	public void setLastUpdateTime(double lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
		
	}


	public double getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public void setShowUnpredictable(boolean b)
	{
		showUnpredictable = b;
	}
	
	public boolean getShowUnpredictable()
	{
		return showUnpredictable;
	}
	
	public void setInferBusRoutes(boolean b)
	{
		inferBusRoutes = b;
	}

	public boolean getInferBusRoutes()
	{
		return inferBusRoutes;
	}
	
	public void setInitAllRouteInfo(boolean b)
	{
		isFirstRefresh = b;
	}
	
	public boolean getInitAllRouteInfo()
	{
		return isFirstRefresh;
	}
	
	public void triggerUpdate(int millis) {
		Log.i("TRIGGER", "UPDATE, " + millis);
		sendEmptyMessageDelayed(MINOR, millis);
		
	}
	
	public void triggerUpdate() {
		Log.i("TRIGGER", "UPDATE");
		sendEmptyMessage(MINOR);
		
	}


	public void resume() {
		removeAllMessages();
		if(getUpdateConstantly())
		{
			//if the runInBackground checkbox is clicked, start the handler updating
		    instantRefresh();
		}
	}


	public void setLocationListener(
			OneTimeLocationListener oneTimeLocationListener) {
		this.oneTimeLocationListener = oneTimeLocationListener;
		
	}
	
	public void setRoutesSupportedIndex(int index)
	{
		currentRoutesSupportedIndex = index;
	}



	public void immediateRefresh() {
		Message msg = new Message();
		msg.arg1 = IMMEDIATE_REFRESH;
		msg.what = MAJOR;
		sendMessage(msg);
	}
}
