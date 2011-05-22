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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


import boston.Bus.Map.R;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Directions;
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
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

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
public class BusOverlay extends BalloonItemizedOverlay<BusOverlayItem> {

	public static final int NOT_SELECTED = -1;
	private final ArrayList<BusOverlayItem> overlays = new ArrayList<BusOverlayItem>();
	private Main context;
	private final List<Location> locations = new ArrayList<Location>();
	private int selectedBusIndex;
	private UpdateHandler updateable;
	private boolean drawHighlightCircle;
	private final int busHeight;
	private final Drawable busPicture;
	private final Paint paint;
	
	private final HashMap<String, String> routeKeysToTitles;
	private final float density;
	
	private Locations locationsObj;
	
	public BusOverlay(BusOverlay busOverlay, Main context, MapView mapView, HashMap<String, String> routeKeysToTitles, float density)
	{
		this(busOverlay.busPicture, context, mapView, routeKeysToTitles, density);
		
		this.drawHighlightCircle = busOverlay.drawHighlightCircle;
		
		this.locations.addAll(busOverlay.locations);
		
		for (BusOverlayItem overlayItem : busOverlay.overlays)
		{
			addOverlay(overlayItem);
		}
		
		populate();
		
		this.selectedBusIndex = busOverlay.getLastFocusedIndex();
		
		
		if (selectedBusIndex != NOT_SELECTED)
		{
			//Log.v("BostonBusMap", "calling onTap: " + selectedBusIndex);
			onTap(selectedBusIndex);
		}
		
		setLastFocusedIndex(busOverlay.getLastFocusedIndex());
	}
	
	
	public BusOverlay(Drawable busPicture, Main context, MapView mapView, HashMap<String, String> routeKeysToTitles, float density)
	{
		super(boundCenterBottom(busPicture), mapView);

		this.context = context;
		this.selectedBusIndex = NOT_SELECTED;
		this.busPicture = busPicture;
		this.busHeight = busPicture.getIntrinsicHeight();
		this.routeKeysToTitles = routeKeysToTitles;
		this.paint = new Paint();
		paint.setColor(Color.rgb(0x77, 0x77, 0xff));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		paint.setAlpha(0x70);

		this.density = density;

		
		//NOTE: remember to set updateable!
		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChanged(ItemizedOverlay overlay,
					OverlayItem newFocus) {
				//if you click on a bus, it would normally draw the selected bus without this code
				//but in certain cases (you click away from any bus, then click on the bus again) it got confused and didn't draw
				//things right. This corrects that (hopefully)
				setLastFocusedIndex(NOT_SELECTED);
				setFocus((BusOverlayItem)newFocus);
				if (newFocus == null)
				{
					hideBalloon();
				}
			}
		});
		
		setBalloonBottomOffset(busPicture.getIntrinsicHeight() + 4);
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
	
	public void setLocations(Locations locations)
	{
		this.locationsObj = locations;
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
	public int size() 
	{
		return overlays.size();
	}
	
	private void addOverlay(BusOverlayItem item)
	{
		overlays.add(item);
		
		populate();
	}

	public void addOverlays(Collection<BusOverlayItem> overlayItems)
	{
		overlays.addAll(overlayItems);
		
		populate();
	}
	
	@Override
	protected BusOverlayItem createItem(int i)
	{
		return overlays.get(i);
	}


	public void clear() {
		overlays.clear();
		locations.clear();
		
		setFocus(null);
		setLastFocusedIndex(NOT_SELECTED);
		locationsObj = null;
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

			boundCenterBottom(drawable);
		}
		
		if (selectedBusIndex != NOT_SELECTED)
		{
			//make sure that selected buses are preserved during refreshes
			setFocus(overlays.get(selectedBusIndex));
			selectedBusIndex = NOT_SELECTED;
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
		if (selectedBusIndex == NOT_SELECTED)
		{
			return NOT_SELECTED;
		}
		else
		{
			if (selectedBusIndex >= locations.size())
			{
				this.selectedBusIndex = NOT_SELECTED;
				return NOT_SELECTED;
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
		if (selectedBusIndex == NOT_SELECTED)
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
		selectedBusIndex = NOT_SELECTED;
		if (selectedBusId != NOT_SELECTED)
		{
			for (int i = 0; i < locations.size(); i++)
			{
				Location busLocation = locations.get(i);

				if (busLocation.containsId(selectedBusId))
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

	public void addOverlaysFromLocations(ArrayList<GeoPoint> points) {
		for (int i = 0; i < locations.size(); i++)
		{
			Location location = locations.get(i);
			String titleText = location.getSnippetTitle();
			String snippetText = location.getSnippet();
			BusOverlayItem overlayItem = new BusOverlayItem(points.get(i),titleText, snippetText);
			addOverlay(overlayItem);
		}
	}
	
	@Override
	protected boolean onTap(int index) {
		boolean ret = super.onTap(index);
		
		Location location = locations.get(index);
		BusOverlayItem item = currentFocussedItem;
		item.setCurrentLocation(location);
		
		BusPopupView view = (BusPopupView)balloonView;
		boolean isVisible = location instanceof StopLocation;
		view.setState(location.isFavorite(), isVisible, isVisible, location);
		
		return ret;
	}
	
	protected BalloonOverlayView<BusOverlayItem> createBalloonOverlayView() {
		BusPopupView view = new BusPopupView(getMapView().getContext(), getBalloonBottomOffset(), locationsObj, routeKeysToTitles,
				density);
		return view;
	}

}
