package boston.Bus.Map.main;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;

import boston.Bus.Map.data.Locations;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.RouteOverlay;

import com.google.android.maps.GeoPoint;
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
	private final DatabaseHelper helper;
	private final Context context;
	
	private final BusOverlay busOverlay; 
	private final RouteOverlay routeOverlay;
	private final LocationOverlay locationOverlay;
	
	private boolean isFirstRefresh;
	private boolean showRouteLine;
	private boolean showCoarseRouteLine;
	
	public UpdateHandler(TextView textView, MapView mapView,
			Drawable arrow, Drawable tooltip, Locations busLocations, Context context, DatabaseHelper helper, BusOverlay busOverlay,
			RouteOverlay routeOverlay,  LocationOverlay locationOverlay,
			UpdateAsyncTask majorHandler)
	{
		this.textView = textView;
		this.mapView = mapView;
		this.busLocations = busLocations;
		this.helper = helper;
		this.busOverlay = busOverlay;
		this.routeOverlay = routeOverlay;
		this.locationOverlay = locationOverlay;
		lastUpdateTime = System.currentTimeMillis();
		
		this.context = context;
		this.updateAsyncTask = majorHandler;
	}
	
	private int selectedRouteIndex = Locations.NO_CHANGE;
	private int selectedBusPredictions;
	
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
			if (minorUpdate != null)
			{
				if (minorUpdate.getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
				{
					//task is not finished yet
					return;
				}
				
			}

			GeoPoint geoPoint = mapView.getMapCenter();
			double centerLatitude = geoPoint.getLatitudeE6() / (float)Main.E6;
			double centerLongitude = geoPoint.getLongitudeE6() / (float)Main.E6;
			
			//remove duplicate messages
			removeMessages(MINOR);
			
			minorUpdate = new UpdateAsyncTask(textView, mapView, locationOverlay, null, getShowUnpredictable(), false, maxOverlays,
					getHideHighlightCircle() == false, getInferBusRoutes(), busOverlay, routeOverlay, helper,
					selectedRouteIndex, selectedBusPredictions, false, getShowRouteLine(), getShowCoarseRouteLine());
			

			minorUpdate.runUpdate(busLocations, centerLatitude, centerLongitude);
			
			break;
		}		
	}



	private int getCurrentFetchDelay() {
		return busLocationsFetchDelay;
	}



	public void removeAllMessages() {
		removeMessages(MAJOR);
		removeMessages(MINOR);
		//removeMessages(LOCATION_NOT_FOUND);
		//removeMessages(LOCATION_FOUND);
	}

	
	public void kill()
	{
		if (updateAsyncTask != null)
		{
			updateAsyncTask.cancel(true);
		}

		if (minorUpdate != null)
		{
			minorUpdate.cancel(true);
		}
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
		
		GeoPoint geoPoint = mapView.getMapCenter();
		double centerLatitude = geoPoint.getLatitudeE6() / (float)Main.E6;
		double centerLongitude = geoPoint.getLongitudeE6() / (float)Main.E6;

		
		updateAsyncTask = new UpdateAsyncTask(textView, mapView, locationOverlay, finalMessage,
				getShowUnpredictable(), true, maxOverlays,
				getHideHighlightCircle() == false, getInferBusRoutes(), busOverlay, routeOverlay, helper,
				selectedRouteIndex, selectedBusPredictions, isFirstTime, showRouteLine, showCoarseRouteLine);
		updateAsyncTask.runUpdate(busLocations, centerLatitude, centerLongitude);
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
	

	public void setShowRouteLine(boolean b) {
		showRouteLine = b;
	}

	public boolean getShowRouteLine()
	{
		return showRouteLine;
	}
	
	public void setShowCoarseRouteLine(boolean b) {
		showCoarseRouteLine = b;
	}

	public boolean getShowCoarseRouteLine()
	{
		return showCoarseRouteLine;
	}
	
	
	
	public void triggerUpdate(int millis) {
		//Log.v("BostonBusMap", "minor update triggered in, " + millis);
		sendEmptyMessageDelayed(MINOR, millis);
		
	}
	
	public void triggerUpdate() {
		//Log.v("BostonBusMap", "minor update triggered");
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



	public void immediateRefresh() {
		Message msg = new Message();
		msg.arg1 = IMMEDIATE_REFRESH;
		msg.what = MAJOR;
		sendMessage(msg);
	}



	public void setRouteIndex(int selectedRouteIndex) {
		this.selectedRouteIndex = selectedRouteIndex;
	}

	public void setSelectedBusPredictions(int b)
	{
		selectedBusPredictions = b; 
	}



	public UpdateAsyncTask getMajorHandler() {
		return updateAsyncTask;
	}


}
