package boston.Bus.Map.main;

import com.google.common.base.Optional;
import com.schneeloch.bostonbusmap_library.data.GroupKey;
import com.schneeloch.bostonbusmap_library.data.Selection;
import boston.Bus.Map.data.UpdateArguments;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;

import com.schneeloch.bostonbusmap_library.util.Constants;

import com.google.android.maps.GeoPoint;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.Now;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

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

	private final int maxOverlays = 175;

	private final int IMMEDIATE_REFRESH = 1;

	private int updateConstantlyInterval;
	private boolean hideHighlightCircle;
	private boolean showUnpredictable;
    private boolean changeRouteIfSelected;
	private AdjustUIAsyncTask minorUpdate;
	
	private final UpdateArguments guiArguments;
    private boolean showTraffic = false;

    private final ConcurrentLinkedQueue<Runnable> afterRefresh = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> afterUpdate = new ConcurrentLinkedQueue<>();

	public UpdateHandler(UpdateArguments guiArguments)
	{
		this.guiArguments = guiArguments;
		lastUpdateTime = Now.getMillis();
	}
	
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what)
		{
		case MAJOR:
			//remove duplicates
			long currentTime = Now.getMillis();

			int interval = getUpdateConstantlyInterval() * 1000;

            // schedule this before RefreshAsyncTask since that will block other threads
            runMinorUpdateTask();
			runUpdateTask();

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

			//remove duplicate messages
			removeMessages(MINOR);

            runMinorUpdateTask();

			break;
		}		
	}

    private void runMinorUpdateTask() {
        Selection selection = guiArguments.getBusLocations().getSelection();
        minorUpdate = new AdjustUIAsyncTask(guiArguments, getShowUnpredictable(),
                maxOverlays,
                selection, this, afterUpdate.poll());


        minorUpdate.runUpdate();
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
		lastUpdateTime = Now.getMillis();

		//don't do two updates at once
		if (guiArguments.getMajorHandler() != null)
		{
			if (guiArguments.getMajorHandler().getStatus().equals(UpdateAsyncTask.Status.FINISHED) == false)
			{
				//task is not finished yet
                LogUtil.i("MAJOR task is not finished yet");
				return;
			}
			
		}
		
		Selection selection = guiArguments.getBusLocations().getSelection();
		final RefreshAsyncTask updateAsyncTask = new RefreshAsyncTask(
                guiArguments, getShowUnpredictable(), maxOverlays,
				selection, this, afterRefresh.poll()
        );
		guiArguments.setMajorHandler(updateAsyncTask);

        updateAsyncTask.runUpdate();
		
	}

    public void triggerRefresh(long millis) {
        removeMessages(MAJOR);
        sendEmptyMessageDelayed(MAJOR, millis);
        removeMessages(MINOR);
        sendEmptyMessageDelayed(MINOR, millis);
    }

    public void triggerRefreshThen(Runnable runnable) {
        afterRefresh.add(runnable);
        triggerRefresh(0);
    }


    public boolean instantRefresh() {
		//removeAllMessages();
		
		if(getUpdateConstantlyInterval() != Main.UPDATE_INTERVAL_NONE)
		{
			//if the runInBackground checkbox is clicked, start the handler updating
			removeMessages(MAJOR);
			sendEmptyMessageDelayed(MAJOR, getUpdateConstantlyInterval() * 1000);
		}

        runMinorUpdateTask();
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

    public void triggerUpdateThen(Runnable runnable) {
        afterUpdate.add(runnable);

        triggerUpdate();
    }
	
	public void triggerUpdateThenSelect(final Optional<GroupKey> toSelect)
	{
        triggerUpdateThen(new Runnable() {
            @Override
            public void run() {
                guiArguments.getOverlayGroup().setSelectedBusId(toSelect);
                triggerUpdate();
            }
        });
	}

	public void resume() {
		//removeAllMessages();
		if(getUpdateConstantlyInterval() != Main.UPDATE_INTERVAL_NONE)
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

}
