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
package boston.Bus.Map.ui;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;


import boston.Bus.Map.R;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.StopLocation;

import boston.Bus.Map.data.Location;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
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
import android.text.method.HideReturnsTransformationMethod;
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
public class BusOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private final ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Main context;
	private final List<Location> locations = new ArrayList<Location>();
	private int selectedBusIndex;
	private UpdateHandler updateable;
	private boolean drawHighlightCircle;
	private final int busHeight;
	private final Drawable busPicture;
	private final Paint paint;
	
	public BusOverlay(BusOverlay busOverlay, Main context, MapView mapView)
	{
		this(busOverlay.busPicture, context, mapView);
		
		this.drawHighlightCircle = busOverlay.drawHighlightCircle;
		
		this.locations.addAll(busOverlay.locations);
		
		for (OverlayItem overlayItem : busOverlay.overlays)
		{
			addOverlay(overlayItem);
		}
		
		populate();
		
		this.selectedBusIndex = busOverlay.getLastFocusedIndex();
		
		
		
		if (selectedBusIndex != -1)
		{
			//Log.v("BostonBusMap", "calling onTap: " + selectedBusIndex);
			onTap(selectedBusIndex);
		}
		
		setLastFocusedIndex(busOverlay.getLastFocusedIndex());
	}
	
	
	public BusOverlay(Drawable busPicture, Main context, 
			MapView mapView) {
		super(boundCenterBottom(busPicture), mapView);

		this.context = context;
		this.selectedBusIndex = -1;
		this.busPicture = busPicture;
		this.busHeight = busPicture.getIntrinsicHeight();
		this.paint = new Paint();
		paint.setColor(Color.rgb(0x77, 0x77, 0xff));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		paint.setAlpha(0x70);


		
		//NOTE: remember to set updateable!
		
		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChanged(ItemizedOverlay overlay,
					OverlayItem newFocus) {
				//if you click on a bus, it would normally draw the selected bus without this code
				//but in certain cases (you click away from any bus, then click on the bus again) it got confused and didn't draw
				//things right. This corrects that (hopefully)
				setLastFocusedIndex(-1);
				setFocus(newFocus);
				if (newFocus == null)
				{
					hideBalloon();
					BusOverlay.this.context.setFavoriteStatus(R.drawable.empty_star);
				}
				else
				{
					int index = getLastFocusedIndex();
					Main context = BusOverlay.this.context;
					Location location = locations.get(index);
					if (context != null)
					{
						boolean b = location.isFavorite();
						Log.v("BostonBusMap", "setting favorite status, " + b);
						context.setFavoriteStatus(b ? R.drawable.full_star : R.drawable.empty_star);
					}
				}
			}
		});
		
		setBalloonBottomOffset(40);
	}

	public void setUpdateable(UpdateHandler updateable)
	{
		this.updateable = updateable;
	}
	
	public void setContext(Main context)
	{
		this.context = context;
	}
	
	/**
	 * Was there a drag between when the finger touched the touchscreen and when it released?
	 */
	private boolean mapMoved;
	
	public void setDrawHighlightCircle(boolean b)
	{
		drawHighlightCircle = b;
	}
	
	public void addLocation(Location location)
	{
		locations.add(location);
	}
	
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
			if (mapMoved && updateable != null)
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
		
		setFocus(null);
		setLastFocusedIndex(-1);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		int lastFocusedIndex = getLastFocusedIndex();
		final int overlaysSize = Math.min(overlays.size(), locations.size());
		for (int i = 0; i < overlaysSize; i++)
		{
			OverlayItem item = overlays.get(i);
			Location busLocation = locations.get(i);

			boolean isSelected = i == lastFocusedIndex;
			Drawable drawable = busLocation.getDrawable(context, shadow, isSelected);
			item.setMarker(drawable);
			if (!(busLocation instanceof BusLocation))
			{
				boundCenterBottom(drawable);
			}
			
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
			int lastDistance = 0;
			for (int i = 1; i < overlaysSize; i++)
			{
				OverlayItem item = overlays.get(i);
				Location location = locations.get(i);
				
				GeoPoint geoPoint = item.getPoint();
				Point point = projection.toPixels(geoPoint, null);

				int dx = circleCenter.x - point.x;
				int dy = circleCenter.y - point.y;
				int distance = dx*dx + dy*dy;
				if (distance > lastDistance)
				{
					lastDistance = distance;  
				}
			}
		

			

			float radius = (float)Math.sqrt(lastDistance);

			//draw a circle showing which buses are currently displayed
			float circleCenterX = circleCenter.x;
			float circleCenterY = circleCenter.y - busHeight / 2; 
			canvas.drawCircle(circleCenterX, circleCenterY, radius, paint);
		}
		super.draw(canvas, mapView, shadow);
	}

	
	
	public int getSelectedBusId() {
		int selectedBusIndex = getLastFocusedIndex();
		//Log.v("BostonBusMap", "getLastFocusedIndex() value is " + selectedBusIndex);
		if (selectedBusIndex == -1)
		{
			return -1;
		}
		else
		{
			if (selectedBusIndex >= locations.size())
			{
				this.selectedBusIndex = -1;
				return -1;
			}
			else
			{
				return locations.get(selectedBusIndex).getId();
			}
		}
		
	}

	public void doPopulate() {
		populate();
	}

	public void refreshBalloons() {
		
		//Log.i("REFRESHBALLOONS", selectedBusIndex + " ");
		if (selectedBusIndex == -1)
		{
			hideBalloon();
		}
		else
		{
			onTap(selectedBusIndex);
		}
	}
	
	public void setSelectedBusId(int selectedBusId)
	{
		selectedBusIndex = -1;
		if (selectedBusId != -1)
		{
			for (int i = 0; i < locations.size(); i++)
			{
				Location busLocation = locations.get(i);

				if (busLocation.getId() == selectedBusId)
				{
					selectedBusIndex = i;
					
					break;
				}
			}
		}
		Log.v("BostonBusMap", "setSelectedBusId param was " + selectedBusId);
	}

	public Drawable getBusPicture() {
		return busPicture;
	}


	public int toggleFavorite(Locations locationsObj) {
		int selectedBusIndex = getLastFocusedIndex();
		
		if (selectedBusIndex >= 0 && selectedBusIndex < locations.size())
		{
			Location location = locations.get(selectedBusIndex);
			if (location instanceof StopLocation)
			{
				return locationsObj.toggleFavorite((StopLocation)location);
			}
			else
			{
				Toast.makeText(context, "You can only favorite bus stops", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Toast.makeText(context, "No bus stop is currently selected", Toast.LENGTH_LONG).show();			
		}
		return R.drawable.empty_star;
	}
	
	
	@Override
	protected boolean onTap(int index) {
		Location location = locations.get(index);
		
		String title = location.getSnippetTitle();
		String snippet = location.getSnippet();
		
		
		return onTap(index, title, snippet);
	}
}
