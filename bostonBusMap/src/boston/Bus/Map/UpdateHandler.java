package boston.Bus.Map;

import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
	public final static int fetchDelay = 13000;
	
	private final int maxOverlays = 15;
	

	private boolean updateConstantly;
	private boolean hideHighlightCircle;
	private boolean showUnpredictable;
	private UpdateAsyncTask updateAsyncTask;
	private UpdateAsyncTask minorUpdate;
	
	private final TextView textView;
	private final Drawable busPicture;
	private final MapView mapView;
	private final Drawable arrow;
	private final Drawable tooltip;
	private boolean inferBusLocations;
	private final BusLocations busLocations;
	private OneTimeLocationListener oneTimeLocationListener;
	private final Context context;
	
	public UpdateHandler(TextView textView, Drawable busPicture, MapView mapView, Drawable arrow, Drawable tooltip, BusLocations busLocations, Context context)
	{
		this.textView = textView;
		this.busPicture = busPicture;
		this.mapView = mapView;
		this.arrow = arrow;
		this.tooltip = tooltip;
		this.busLocations = busLocations;
		this.context = context;
		lastUpdateTime = System.currentTimeMillis();
	}
	
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what)
		{
		case MAJOR:
			//remove duplicates
			removeMessages(MAJOR);
			double currentTime = System.currentTimeMillis();
			
			if (currentTime - lastUpdateTime > fetchDelay)
			{
				//if not too soon, do the update
				runUpdateTask("Finished update!");
			}

			//make updateBuses execute every 10 seconds (or whatever fetchDelay is)
			//to disable this, the user should go into the settings and uncheck 'Run in background'
			sendEmptyMessageDelayed(MAJOR, fetchDelay);


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

			//remove duplicate messages
			removeMessages(MINOR);
			
			minorUpdate = new UpdateAsyncTask(textView, busPicture, mapView, null, arrow,
					tooltip, this, getShowUnpredictable(), false, maxOverlays, getHideHighlightCircle() == false,
					getInferVehicleRoute());
			

			minorUpdate.runUpdate(busLocations);
			

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



	public void removeAllMessages() {
		removeMessages(MAJOR);
		removeMessages(MINOR);
		removeMessages(LOCATION_NOT_FOUND);
		removeMessages(LOCATION_FOUND);
	}


	/**
	 * executes the update
	 */
	private void runUpdateTask(String finalMessage) {
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
		
		
		updateAsyncTask = new UpdateAsyncTask(textView, busPicture, mapView, finalMessage,
				arrow, tooltip, this, getShowUnpredictable(), true, maxOverlays, getHideHighlightCircle() == false,
				getInferVehicleRoute());
		updateAsyncTask.runUpdate(busLocations);
		
		
	}

	public boolean instantRefresh() {
		removeAllMessages();
		
		if(getUpdateConstantly())
		{
			//if the runInBackground checkbox is clicked, start the handler updating
			sendEmptyMessageDelayed(MAJOR, (long)(fetchDelay * 1.5));
		}
		
		if (System.currentTimeMillis() - lastUpdateTime < fetchDelay)
		{
			return false;
		}

		runUpdateTask("Finished update!");
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

	public void setInferVehicleRoute(boolean b)
	{
		inferBusLocations = b;
	}
	
	public boolean getInferVehicleRoute()
	{
		return inferBusLocations;
	}

	public void triggerUpdate(int millis) {
		sendEmptyMessageDelayed(MINOR, millis);
		
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
}
