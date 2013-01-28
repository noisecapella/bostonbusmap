package boston.Bus.Map.main;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;

import boston.Bus.Map.R;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.RouteOverlay;
import boston.Bus.Map.util.Constants;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
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
	private long lastUpdateTime;

	public final static int fetchDelay = 15000;
	
	private final int maxOverlays = 75;

	private final int IMMEDIATE_REFRESH = 1;

	private int updateConstantlyInterval;
	private boolean hideHighlightCircle;
	private boolean showUnpredictable;
	private AdjustUIAsyncTask minorUpdate;
	
	private final UpdateArguments guiArguments;
	private boolean allRoutesBlue = TransitSystem.defaultAllRoutesBlue;

	public static final int UPDATE_INTERVAL_INVALID = 9999;
	public static final int UPDATE_INTERVAL_SHORT = 15;
	public static final int UPDATE_INTERVAL_MEDIUM = 50;
	public static final int UPDATE_INTERVAL_LONG = 100;
	public static final int UPDATE_INTERVAL_NONE = 0;
	

	public UpdateHandler(UpdateArguments guiArguments)
	{
		this.guiArguments = guiArguments;
		lastUpdateTime = TransitSystem.currentTimeMillis();
	}
	
	public int getUpdateInterval(SharedPreferences prefs) {
		Context context = guiArguments.getContext();
		String intervalString = prefs.getString(context.getString(R.string.updateContinuouslyInterval), "");
		int interval;
		if (intervalString.length() == 0) {
			interval = prefs.getBoolean(context.getString(R.string.runInBackgroundCheckbox), true) ? UPDATE_INTERVAL_SHORT : UPDATE_INTERVAL_NONE;
		}
		else
		{
			interval = Integer.parseInt(intervalString);
		}
		return interval;
	}

	
    public void populateHandlerSettings() {
    	Context context = guiArguments.getContext();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	
    	int updateInterval = getUpdateInterval(prefs);
    	setUpdateConstantlyInterval(updateInterval);
    	setShowUnpredictable(prefs.getBoolean(context.getString(R.string.showUnpredictableBusesCheckbox), false));
    	setHideHighlightCircle(prefs.getBoolean(context.getString(R.string.hideCircleCheckbox), false));
    	boolean allRoutesBlue = prefs.getBoolean(context.getString(R.string.allRoutesBlue), TransitSystem.defaultAllRoutesBlue);
    	setAllRoutesBlue(allRoutesBlue);
        guiArguments.getOverlayGroup().getRouteOverlay().setDrawLine(prefs.getBoolean(context.getString(R.string.showRouteLineCheckbox), false));
    	boolean showCoarseRouteLineCheckboxValue = prefs.getBoolean(context.getString(R.string.showCoarseRouteLineCheckbox), true); 
    	
    	boolean alwaysUpdateLocationValue = prefs.getBoolean(context.getString(R.string.alwaysShowLocationCheckbox), true);
    	
    	String intervalString = Integer.valueOf(updateInterval).toString();
    	//since the default value for this flag is true, make sure we let the preferences know of this
    	prefs.edit().
    		putBoolean(context.getString(R.string.alwaysShowLocationCheckbox), alwaysUpdateLocationValue).
    		putString(context.getString(R.string.updateContinuouslyInterval), intervalString).
    		putBoolean(context.getString(R.string.showCoarseRouteLineCheckbox), showCoarseRouteLineCheckboxValue).
    		putBoolean(context.getString(R.string.allRoutesBlue), allRoutesBlue)
    		.commit();
    }

	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what)
		{
		case MAJOR:
			//remove duplicates
			long currentTime = TransitSystem.currentTimeMillis();
			
			int interval = getUpdateConstantlyInterval() * 1000;
			
			if (currentTime - lastUpdateTime > interval)
			{
				//if not too soon, do the update
				runUpdateTask();
			}
			else if (currentTime - lastUpdateTime > fetchDelay && msg.arg1 == IMMEDIATE_REFRESH)
			{
				runUpdateTask();
			}

			//make updateBuses execute every 10 seconds (or whatever fetchDelay is)
			//to disable this, the user should go into the settings and uncheck 'Run in background'
			if (msg.arg1 != IMMEDIATE_REFRESH && interval != 0)
			{
				removeMessages(MAJOR);
				sendEmptyMessageDelayed(MAJOR, interval);
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

			GeoPoint geoPoint = guiArguments.getMapView().getMapCenter();
			double centerLatitude = geoPoint.getLatitudeE6() * Constants.InvE6;
			double centerLongitude = geoPoint.getLongitudeE6() * Constants.InvE6;
			
			//remove duplicate messages
			removeMessages(MINOR);
			
			Integer toSelect = null;
			if (msg.arg1 != 0) {
				toSelect = msg.arg1;
			}
			Selection selection = guiArguments.getBusLocations().getSelection();
			minorUpdate = new AdjustUIAsyncTask(guiArguments, getShowUnpredictable(),
					maxOverlays,
					hideHighlightCircle == false, allRoutesBlue,
					selection, this, toSelect);
			

			minorUpdate.runUpdate();
			
			break;
		}		
	}

	public void removeAllMessages() {
		removeMessages(MAJOR);
		removeMessages(MINOR);
		//removeMessages(LOCATION_NOT_FOUND);
		//removeMessages(LOCATION_FOUND);
	}

	
	public void kill()
	{
		if (guiArguments.getMajorHandler() != null)
		{
			guiArguments.getMajorHandler().cancel(true);
		}

		if (minorUpdate != null)
		{
			minorUpdate.cancel(true);
		}
	}

	/**
	 * executes the update
	 */
	private void runUpdateTask() {
		//make sure we don't update too often
		lastUpdateTime = TransitSystem.currentTimeMillis();

		//don't do two updates at once
		if (guiArguments.getMajorHandler() != null)
		{
			if (guiArguments.getMajorHandler().getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
			{
				//task is not finished yet
				return;
			}
			
		}
		
		Selection selection = guiArguments.getBusLocations().getSelection();
		final RefreshAsyncTask updateAsyncTask = new RefreshAsyncTask(guiArguments, getShowUnpredictable(), maxOverlays,
				hideHighlightCircle == false, allRoutesBlue, 
				selection, this);
		guiArguments.setMajorHandler(updateAsyncTask);
		updateAsyncTask.runUpdate();
		
	}

	public boolean instantRefresh() {
		//removeAllMessages();
		
		if(getUpdateConstantlyInterval() != UPDATE_INTERVAL_NONE)
		{
			//if the runInBackground checkbox is clicked, start the handler updating
			removeMessages(MAJOR);
			sendEmptyMessageDelayed(MAJOR, getUpdateConstantlyInterval() * 1000);
		}
		
		if (TransitSystem.currentTimeMillis() - lastUpdateTime < fetchDelay)
		{
			return false;
		}

		runUpdateTask();
		return true;

	}

	public int getUpdateConstantlyInterval() {
		return updateConstantlyInterval;
	}
	
	public void setUpdateConstantlyInterval(int updateConstantlyInterval)
	{
		this.updateConstantlyInterval = updateConstantlyInterval;
	}
	
	public void setHideHighlightCircle(boolean b)
	{
		hideHighlightCircle = b;
	}
	
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
		
	}


	public long getLastUpdateTime() {
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
	
	public void triggerUpdate(int millis) {
		sendEmptyMessageDelayed(MINOR, millis);
		
	}
	
	public void triggerUpdate() {
		sendEmptyMessage(MINOR);
		
	}
	
	public void triggerUpdateThenSelect(int id)
	{
		Message msg = new Message();
		msg.arg1 = id;
		msg.what = MINOR;
		sendMessage(msg);
	}

	public void resume() {
		//removeAllMessages();
		populateHandlerSettings();
		if(getUpdateConstantlyInterval() != UPDATE_INTERVAL_NONE)
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

	public void nullifyProgress() {
		if (guiArguments.getMajorHandler() != null)
		{
			guiArguments.getMajorHandler().nullifyProgress();
		}
		
		if (minorUpdate != null)
		{
			//probably not in the middle of something but just in case
			minorUpdate.nullifyProgress();
		}
	}


	public void setAllRoutesBlue(boolean b) {
		allRoutesBlue = b;
	}
}
