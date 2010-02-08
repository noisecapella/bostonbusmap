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

import java.util.ArrayList;
import java.util.List;


import boston.Bus.Map.BusLocations.BusLocation;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 
 * The bus overlay on the MapView
 * Much of this was borrowed from the helpful tutorial at http://developer.android.com/guide/tutorials/views/hello-mapview.html
 * 
 */
public class BusOverlay extends com.google.android.maps.ItemizedOverlay<com.google.android.maps.OverlayItem> {

	private final ArrayList<com.google.android.maps.OverlayItem> overlays = new ArrayList<com.google.android.maps.OverlayItem>();
	private final Context context;
	private final Drawable busPicture;
	private final List<BusLocation> busLocations;
	private int selectedBusIndex;
	private final Drawable arrow;
	
	private final Drawable tooltip;
	
	public BusOverlay(Drawable busPicture, Context context, List<BusLocation> busLocations, int selectedBusId, Drawable arrow, Drawable tooltip) {
		super(boundCenterBottom(busPicture));

		this.context = context;
		this.busPicture = busPicture;
		this.arrow = arrow;
		this.busLocations = busLocations;
		this.selectedBusIndex = -1;
		
		this.tooltip = tooltip;
		
		
		for (int i = 0; i < busLocations.size(); i++)
		{
			BusLocation busLocation = busLocations.get(i);

			if (busLocation.id == selectedBusId)
			{
				selectedBusIndex = i;
				break;
			}
		}

		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChanged(ItemizedOverlay overlay,
					OverlayItem newFocus) {
				//if you click on a bus, it would normally draw the selected bus without this code
				//but in certain cases (you click away from any bus, then click on the bus again) it got confused and didn't draw
				//things right. This corrects that (hopefully)
				setLastFocusedIndex(-1);
				setFocus(newFocus);
			}
		});

	}

	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlays.size();
	}
	
	public void addOverlay(com.google.android.maps.OverlayItem item)
	{
		overlays.add(item);
		
		populate();
	}
	
	@Override
	protected com.google.android.maps.OverlayItem createItem(int i)
	{
		return overlays.get(i);
	}


	public void clear() {
		overlays.clear();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		int lastFocusedIndex = getLastFocusedIndex();
		for (int i = 0; i < overlays.size(); i++)
		{
			OverlayItem item = overlays.get(i);
			BusLocation busLocation = busLocations.get(i);

			Drawable drawable = busPicture;
			if (shadow == false)
			{
				//to make life easier we won't draw shadows except for the bus
				//the tooltip has some weird error where the shadow draws a little left and up from where it should draw
				
				Drawable arrowArg = null, tooltipArg = null;
				TextView textViewArg = null;
				
				//if selected, draw the tooltip
				if (lastFocusedIndex == i)
				{
					TextView textView = new TextView(context);
					String title = busLocation.makeTitle();
					textView.setText(title);
					tooltipArg = tooltip;
					textViewArg = textView;
				}

				//if it has a direction, draw the arrow
				if (busLocation.hasHeading())
				{
					arrowArg = arrow;
				}
				
				//the constructor should ignore the arrow and tooltip if these arguments are null
				drawable = new BusDrawable(busPicture, busLocation.getHeading(), arrowArg, tooltipArg, textViewArg);
			}
			
			item.setMarker(drawable);
			
		}
		
		if (selectedBusIndex != -1)
		{
			//make sure that selected buses are preserved during refreshes
			setFocus(overlays.get(selectedBusIndex));
			selectedBusIndex = -1;
		}
			
		super.draw(canvas, mapView, shadow);
	}

	
	
	public int getSelectedBusId() {
		int selectedBusIndex = getLastFocusedIndex();
		if (selectedBusIndex == -1)
		{
			return -1;
		}
		else
		{
			return busLocations.get(selectedBusIndex).id;
		}
		
	}
}
