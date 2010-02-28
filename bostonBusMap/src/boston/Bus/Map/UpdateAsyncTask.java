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
package boston.Bus.Map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Handles the heavy work of downloading and parsing the XML in a separate thread from the UI.
 *
 */
public class UpdateAsyncTask extends AsyncTask<Object, String, BusLocations>
{
	private final TextView textView;
	private final Drawable busPicture;
	private final MapView mapView;
	private final String finalMessage;
	private final Drawable arrow;
	private final Drawable tooltip;
	private final Updateable updateable;
	private final boolean doShowUnpredictable;
	private final boolean doRefresh;
	private final int maxOverlays;
	private final boolean drawCircle;
	
	private boolean silenceUpdates;
	
	public UpdateAsyncTask(TextView textView, Drawable busPicture, MapView mapView, String finalMessage,
			Drawable arrow, Drawable tooltip, Updateable updateable, boolean doShowUnpredictable, boolean doRefresh, int maxOverlays,
			boolean drawCircle)
	{
		super();
		
		//NOTE: these should only be used in one of the UI threads
		this.textView = textView;
		this.busPicture = busPicture;
		this.mapView = mapView;
		this.finalMessage = finalMessage;
		this.arrow = arrow;
		this.tooltip = tooltip;
		this.updateable = updateable;
		this.doShowUnpredictable = doShowUnpredictable;
		this.doRefresh = doRefresh;
		this.maxOverlays = maxOverlays;
		this.drawCircle = drawCircle;
	}
	
	/**
	 * A type safe wrapper around execute
	 * @param busLocations
	 */
	public void runUpdate(BusLocations busLocations)
	{
		execute(busLocations);
	}

	@Override
	protected BusLocations doInBackground(Object... args) {
		//number of bus pictures to draw. Too many will make things slow
		BusLocations busLocations = (BusLocations)args[0];
		
		return updateBusLocations(busLocations);
	}

	@Override
	protected void onProgressUpdate(String... strings)
	{
		if (silenceUpdates == false)
		{
			textView.setText(strings[0]);
		}
		
	}

	public BusLocations updateBusLocations(BusLocations busLocations)
	{
		if (doRefresh == false)
		{
			//if doRefresh is false, we just want to resort the overlays for a new center. Don't bother updating the text
			silenceUpdates = true;
		}
		
		publishProgress("Fetching bus location data...");

		if (doRefresh)
		{
			try
			{

				busLocations.Refresh();
			}
			catch (IOException e)
			{
				//this probably means that there is no Internet available, or there's something wrong with the feed
				publishProgress("Bus feed is inaccessable; try again later");
				e.printStackTrace();
				return null;

			} catch (SAXException e) {
				publishProgress("XML parsing exception; cannot update. Maybe there was a hiccup in the feed?");
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				publishProgress("XML parser configuration exception; cannot update");
				e.printStackTrace();
				return null;
			} catch (FactoryConfigurationError e) {
				publishProgress("XML parser factory configuration exception; cannot update");
				e.printStackTrace();
				return null;
			}
			catch (Exception e)
			{
				publishProgress("Unknown exception occurred");
				e.printStackTrace();
				return null;
			}
		}
		publishProgress("Preparing to draw bus overlays...");
		
		publishProgress("Adding bus overlays to map...");
		
    	return busLocations;
    }
	
	@Override
	protected void onPostExecute(BusLocations busLocationsObject)
	{
		if (busLocationsObject == null)
		{
			//we probably posted an error message already; just return
			return;
		}
		
		GeoPoint center = mapView.getMapCenter();
		double latitude = center.getLatitudeE6() / 1000000.0;
		double longitude = center.getLongitudeE6() / 1000000.0;
		
		
		ArrayList<BusLocation> busLocations = new ArrayList<BusLocation>();
		
		busLocations.addAll(busLocationsObject.getBusLocations(maxOverlays, latitude, longitude, doShowUnpredictable));
		
		if (busLocations.size() == 0)
		{
			//no data? oh well
			//sometimes the feed provides an empty XML message; completely valid but without any vehicle elements
			publishProgress("Finished update, no data provided");

			//an error probably occurred; keep buses where they were before, and don't overwrite message in textbox
			return;
		}
		
		List<com.google.android.maps.Overlay> overlays = mapView.getOverlays();
        
		int selectedBusId = -1;
		if (overlays.size() > 0 && overlays.get(0) instanceof BusOverlay)
		{
			BusOverlay oldBusOverlay = (BusOverlay)overlays.get(0);
			selectedBusId = oldBusOverlay.getSelectedBusId();
		}
		
        overlays.clear();
        
        publishProgress("Drawing overlays...");
        
        
    	BusOverlay busOverlay = new BusOverlay(busPicture, textView.getContext(), busLocations, selectedBusId,
    			arrow, tooltip, updateable, drawCircle);
    	
    	
    	//draw the buses on the map
        for (BusLocation busLocation : busLocations)
        {
        	GeoPoint point = new GeoPoint((int)(busLocation.latitudeAsDegrees * 1000000), (int)(busLocation.longitudeAsDegrees * 1000000));
        	
        	String title = busLocation.makeTitle();
        	
        	//the title is displayed when someone taps on the icon
        	OverlayItem overlay = new OverlayItem(point, title, title);
        	busOverlay.addOverlay(overlay);
        }
        overlays.add(busOverlay);

        //make sure we redraw map
        mapView.invalidate();
        
        if (finalMessage != null)
        {
        	publishProgress(finalMessage);
        }
        
	}
}
