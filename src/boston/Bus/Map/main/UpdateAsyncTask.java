/*
    BostonBusMap
 
    Copyright (C) 2009  George Schneeloch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package boston.Bus.Map.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.CircularRedirectException;
import org.xml.sax.SAXException;



import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.MapManager;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.FeedException;
import boston.Bus.Map.util.LogUtil;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Handles the heavy work of downloading and parsing the XML in a separate thread from the UI.
 *
 */
public abstract class UpdateAsyncTask extends AsyncTask<Object, Object, ImmutableList<Location>>
{
	private final boolean doShowUnpredictable;
	/**
	 * For now this is always false. I need to figure out how to download a 1 megabyte file gracefully
	 */
	private final boolean doInit;
	private final int maxOverlays;
	private final boolean drawCircle;
	private final boolean allRoutesBlue;
	
	private String progressDialogTitle;
	private String progressDialogMessage;
	private int progressDialogMax;
	private int progressDialogProgress;
	private boolean progressDialogIsShowing;
	private boolean progressIsShowing;
	
	protected final UpdateArguments arguments;
	protected final UpdateHandler handler;
	
	protected final Selection selection;
	
	private final Integer toSelect;
	
	/**
	 * The last read center of the map.
	 */
	protected final LatLng currentMapCenter;
	
	public UpdateAsyncTask(UpdateArguments arguments, boolean doShowUnpredictable,
			int maxOverlays, boolean drawCircle, boolean allRoutesBlue,
			boolean doInit, Selection selection, UpdateHandler handler, Integer toSelect)
	{
		super();
		
		this.arguments = arguments;
		//NOTE: these should only be used in one of the UI threads
		this.doShowUnpredictable = doShowUnpredictable;
		this.maxOverlays = maxOverlays;
		this.drawCircle = drawCircle;
		this.allRoutesBlue = allRoutesBlue;
		this.doInit = doInit;
		this.selection = selection;
		this.handler = handler;
		
		currentMapCenter = arguments.getMapView().getCameraPosition().target;
		
		this.toSelect = toSelect;
	}
	
	/**
	 * A type safe wrapper around execute
	 * @param busLocations
	 */
	public void runUpdate()
	{
		execute();
	}

	@Override
	protected ImmutableList<Location> doInBackground(Object... args) {
		//number of bus pictures to draw. Too many will make things slow
		return updateBusLocations();
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		try
		{
			progressUpdate(values);
		}
		catch (Throwable t) {
			LogUtil.e(t);
		}
	}
	
	protected void progressUpdate(Object... objects)
	{
		if (objects.length == 0) {
			return;
		}
		final ProgressDialog progressDialog = arguments.getProgressDialog();
		final ProgressBar progress = arguments.getProgress();
		if (progressDialog == null || progress == null)
		{
			return;
		}

		Object obj = objects[0];
		if (areUpdatesSilenced() == false)
		{

			if (obj instanceof Integer)
			{
				int value = (Integer)obj;
				progressDialog.setProgress(value);
				progressDialogProgress = value;
			}
			else if (obj instanceof ProgressMessage)
			{
				ProgressMessage message = (ProgressMessage)obj;

				switch (message.type)
				{
				case ProgressMessage.PROGRESS_OFF:
					if (progressDialog != null)
					{
						progressDialog.dismiss();
					}
					progressDialogIsShowing = false;

					if (progress != null)
					{
						progress.setVisibility(View.INVISIBLE);
					}
					progressIsShowing = false;
					break;
				case ProgressMessage.PROGRESS_DIALOG_ON:
					if (progressDialog != null)
					{
						progressDialog.setTitle(message.title);
						progressDialog.setMessage(message.message);
						progressDialog.show();
					}
					progressDialogTitle = message.title;
					progressDialogMessage = message.message;
					progressDialogIsShowing = true;

					break;
				case ProgressMessage.SET_MAX:
					if (progressDialog != null)
					{
						progressDialog.setMax(message.max);
					}
					progressDialogMax = message.max;
					break;
				case ProgressMessage.PROGRESS_SPINNER_ON:
					if (progress != null)
					{
						progress.setVisibility(View.VISIBLE);
					}
					progressIsShowing = true;
					break;
				case ProgressMessage.TOAST:
					Log.v("BostonBusMap", "Toast made: " + obj);
					Toast.makeText(arguments.getContext(), message.message, Toast.LENGTH_LONG).show();
					break;
				}
			}
		}
	}

	/**
	 * Do the time consuming stuff we need to do in the background thread
	 * 
	 * Returns true for success, false for failure. Errors should handled in function
	 * to provide a helpful error message
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 */
	protected abstract boolean doUpdate() throws RemoteException, OperationApplicationException;
	
	protected abstract boolean areUpdatesSilenced();
	
	protected ImmutableList<Location> updateBusLocations()	{
		final Locations busLocations = arguments.getBusLocations();

		try
		{
			if (doUpdate() == false) {
				return null;
			}
		}
		catch (Throwable e) {
			LogUtil.e(e);
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "Unknown error occurred"));
			return null;
		}

		try {
			LatLng geoPoint = currentMapCenter;
			double centerLatitude = geoPoint.latitude;
			double centerLongitude = geoPoint.longitude;

			return busLocations.getLocations(maxOverlays, centerLatitude, centerLongitude, doShowUnpredictable, selection);
		} catch (IOException e) {
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "Error getting route data from database"));

			LogUtil.e(e);
			
			return null;
		}
    }
	
	@Override
	protected void onPostExecute(final ImmutableList<Location> locations)
	{
		try
		{
			postExecute(locations);
		}
		catch (Throwable t)
		{
			LogUtil.e(t);
		}
	}
	
	protected void postExecute(final ImmutableList<Location> locationsNearCenter)
	{
		if (locationsNearCenter == null)
		{
			//we probably posted an error message already; just return
			return;
		}
		
		//get currently selected location id, or -1 if nothing is selected
		
		MapManager manager = arguments.getOverlayGroup();
		manager.setDrawHighlightCircle(drawCircle);
		
		manager.setAllRoutesBlue(allRoutesBlue);
		//routeOverlay.setDrawCoarseLine(showCoarseRouteLine);
		
		//get a list of lat/lon pairs which describe the route
		Locations locationsObj = arguments.getBusLocations();
		
		RouteConfig selectedRouteConfig;
		int mode = selection.getMode();
		if (mode == Selection.BUS_PREDICTIONS_STAR || 
				mode == Selection.BUS_PREDICTIONS_ALL)
		{
	        Path[] paths = locationsObj.getPaths(selection.getRoute());
			//we want this to be null. Else, the snippet drawing code would only show data for a particular route
			manager.setPathsAndColor(paths, selection.getRoute());
			selectedRouteConfig = null;
		}
		else if (mode == Selection.BUS_PREDICTIONS_INTERSECT) {
			manager.clearPaths();
			String intersectionName = selection.getIntersection();
			IntersectionLocation intersection = locationsObj.getIntersection(intersectionName);
			if (intersection != null) {
				for (String route : intersection.getNearbyRoutes()) {
					Path[] paths = locationsObj.getPaths(route);
					manager.addPathsAndColor(paths, route);
				}
			}
			else
			{
		        Path[] paths = locationsObj.getPaths(selection.getRoute());
				manager.setPathsAndColor(paths, selection.getRoute());
			}
			
			selectedRouteConfig = null;
		}
		else
		{
			Path[] paths;
			try
			{
				paths = locationsObj.getPaths(selection.getRoute());
				String route = selection.getRoute();
				selectedRouteConfig = locationsObj.getRoute(route);
			}
			catch (IOException e) {
				LogUtil.e(e);
				selectedRouteConfig = null;
				paths = RouteConfig.nullPaths;
			}
			
			manager.setPathsAndColor(paths, selection.getRoute());
		}
		

		
		//we need to run populate even if there are 0 busLocations. See this link:
		//http://groups.google.com/group/android-beginners/browse_thread/thread/6d75c084681f943e?pli=1
		final int selectedBusId = manager != null ? manager.getSelectedBusId() : manager.NOT_SELECTED;
		manager.clearMarkers();
		//busOverlay.doPopulate();

		manager.setLocations(locationsObj);
		
		RouteTitles routeKeysToTitles = arguments.getTransitSystem().getRouteKeysToTitles();
		
		//point hash to index in busLocations
		Map<Long, Integer> points = Maps.newHashMap();
		
		//draw the buses on the map
		int newSelectedBusId;
		if (toSelect != null) {
			newSelectedBusId = toSelect;
		}
		else
		{
			newSelectedBusId = selectedBusId;
		}
		List<Location> busesToDisplay = Lists.newArrayList();
		
		// first add intersection points. Not enough of these to affect performance
		if (mode == Selection.BUS_PREDICTIONS_INTERSECT) {
			String intersectionName = selection.getIntersection();
			IntersectionLocation location = locationsObj.getIntersection(intersectionName);
			if (location != null) {
				busesToDisplay.add(location);
			}
		}
		
		// merge stops or buses to single items if necessary
		for (int i = 0; i < locationsNearCenter.size(); i++)
		{
			Location busLocation = locationsNearCenter.get(i);
			
			final int latInt = (int)(busLocation.getLatitudeAsDegrees() * Constants.E6);
			final int lonInt = (int)(busLocation.getLongitudeAsDegrees() * Constants.E6);
					
			//make a hash to easily compare this location's position against others
			//get around sign extension issues by making them all positive numbers
			final int latIntHash = (latInt < 0 ? -latInt : latInt);
			final int lonIntHash = (lonInt < 0 ? -lonInt : lonInt);
			long hash = (long)((long)latIntHash << 32) | (long)lonIntHash;
			Integer index = points.get(hash);
			final Context context = arguments.getContext();
			Locations locations = arguments.getBusLocations();
			if (null != index)
			{
				//two stops in one space. Just use the one overlay, and combine textboxes in an elegant manner
				Location parent = locationsNearCenter.get(index);
				parent.addToSnippetAndTitle(selectedRouteConfig, busLocation, routeKeysToTitles, locations, context);
				
				if (busLocation.getId() == selectedBusId)
				{
					//the thing we want to select isn't available anymore, choose the other icon
					newSelectedBusId = parent.getId();
				}
			}
			else
			{
				busLocation.makeSnippetAndTitle(selectedRouteConfig, routeKeysToTitles, locations, context);
			
			
				points.put(hash, i);
		
				//the title is displayed when someone taps on the icon
				busesToDisplay.add(busLocation);
			}
		}
		// we need to do this here because addLocation creates PredictionViews, which needs
		// to happen after makeSnippetAndTitle and addToSnippetAndTitle
		manager.addAllLocations(busesToDisplay);
		manager.setSelectedBusId(newSelectedBusId);
		//busOverlay.refreshBalloons();
		
		LatLng newCenter = manager.getMap().getCameraPosition().target;
		if (!newCenter.equals(currentMapCenter)) {
			handler.triggerUpdate();
		}
	}
	
	/**
	 * public method exposing protected publishProgress()
	 * @param msg
	 */
	public void publish(ProgressMessage msg)
	{
		publishProgress(msg);
	}
	
	public void publish(int value)
	{
		publishProgress(value);
	}

	
	
	public void nullifyProgress()
	{
		arguments.setProgress(null);
		arguments.setProgressDialog(null);
	}
	
	/**
	 * This must get run in the UI thread. Neither parameter can be null; use nullifyProgress for that
	 * 
	 * @param progress
	 * @param progressDialog
	 */
	public void setProgress(ProgressBar progress, ProgressDialog progressDialog) {
		arguments.setProgress(progress);
		arguments.setProgressDialog(progressDialog);
		
		progress.setVisibility(progressIsShowing ? View.VISIBLE : View.INVISIBLE);
		progressDialog.setTitle(progressDialogTitle);
		progressDialog.setMessage(progressDialogMessage);
		progressDialog.setMax(progressDialogMax);
		if (progressDialogIsShowing)
		{
			progressDialog.show();
		}
	}
}
