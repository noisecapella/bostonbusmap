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


import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
	private final List<Location> busLocations;
	private int selectedBusIndex;
	private final UpdateHandler updateable;
	private final boolean drawHighlightCircle;
	private final int busHeight;
	
	public BusOverlay(Drawable busPicture, Context context, List<Location> busLocations,
			int selectedBusId, UpdateHandler updateable, boolean drawHighlightCircle) {
		super(boundCenterBottom(busPicture));

		this.context = context;
		this.busLocations = new ArrayList<Location>();
		this.busLocations.addAll(busLocations);
		this.selectedBusIndex = -1;
		this.drawHighlightCircle = drawHighlightCircle;
		this.busHeight = busPicture.getIntrinsicHeight();
		
		this.updateable = updateable;
		
		for (int i = 0; i < busLocations.size(); i++)
		{
			Location busLocation = busLocations.get(i);

			if (busLocation.getId() == selectedBusId)
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

	/**
	 * Was there a drag between when the finger touched the touchscreen and when it released?
	 */
	private boolean mapMoved;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// TODO Auto-generated method stub

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			mapMoved = false;
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			mapMoved = true;
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			if (mapMoved)
			{
				updateable.triggerUpdate(250);
			}
		}
		

		
		return false;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlays.size();
	}
	
	public void addOverlay(OverlayItem item)
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
			Location busLocation = busLocations.get(i);

			boolean isSelected = i == lastFocusedIndex;
			Drawable drawable = busLocation.getDrawable(context, shadow, isSelected);
			item.setMarker(drawable);
			
		}
		
		if (selectedBusIndex != -1)
		{
			//make sure that selected buses are preserved during refreshes
			setFocus(overlays.get(selectedBusIndex));
			selectedBusIndex = -1;
		}
			
		if (drawHighlightCircle && overlays.size() > 0)
		{
			//draw a circle showing the area where overlays are currently shown
			//first overlayitem will be closest to center
			Projection projection = mapView.getProjection();

			//get screen location
			OverlayItem first = overlays.get(0);
			GeoPoint firstPoint = first.getPoint();
			Point circleCenter = projection.toPixels(firstPoint, null); 

			//find out farthest point from bus that's closest to center
			//these points are sorted by distance from center of screen, but we want
			//distance from the bus closest to the center, which is not quite the same
			OverlayItem last = first;
			int lastDistance = 0;
			Point circleRadius = circleCenter;
			for (int i = 1; i < overlays.size(); i++)
			{
				OverlayItem item = overlays.get(i);
				GeoPoint geoPoint = item.getPoint();
				Point point = projection.toPixels(geoPoint, null);

				int dx = circleCenter.x - point.x;
				int dy = circleCenter.y - point.y;
				int distance = dx*dx + dy*dy;
				if (distance > lastDistance)
				{
					lastDistance = distance;  
					last = item;
					circleRadius = point;
				}
			}
		

			

			float radius = (float)Math.sqrt(lastDistance);

			//draw a circle showing which buses are currently displayed
			Paint paint = new Paint();
			paint.setColor(Color.rgb(0x77, 0x77, 0xff));
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);
			paint.setAntiAlias(true);

			float circleCenterX = circleCenter.x;
			float circleCenterY = circleCenter.y - busHeight / 2; 
			canvas.drawCircle(circleCenterX, circleCenterY, radius, paint);
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
			return busLocations.get(selectedBusIndex).getId();
		}
		
	}

	public void doPopulate() {
		// TODO Auto-generated method stub
		populate();
	}
}
