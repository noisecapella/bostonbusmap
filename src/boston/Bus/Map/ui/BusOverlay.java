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

import java.util.ArrayList;
import java.util.List;


import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.LocationGroup;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.StopLocation;

import boston.Bus.Map.data.Location;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.view.MotionEvent;


/**
 * 
 * The bus overlay on the MapView
 * Much of this was borrowed from the helpful tutorial at http://developer.android.com/guide/tutorials/views/hello-mapview.html
 * 
 */
public class BusOverlay extends BalloonItemizedOverlay<BusOverlayItem> {

	public static final int NOT_SELECTED_INDEX = -1;
	private final ArrayList<BusOverlayItem> overlays = new ArrayList<BusOverlayItem>();
	private Main context;
	private final List<LocationGroup> locationGroups = new ArrayList<LocationGroup>();
	private int selectedBusIndex;
	private UpdateHandler updateable;
	private boolean drawHighlightCircle;
	private final int busHeight;
	private final Drawable busPicture;
	private final Paint paint;
	
	private final MyHashMap<String, String> routeKeysToTitles;
	private final float density;
	
	//these two are temporary variables stored here so we don't create a new Point every time we draw
	private final Point circleCenter = new Point();
	private final Point radiusPoint = new Point();
	
	private Locations locationsObj;
	
	public BusOverlay(BusOverlay busOverlay, Main context, MapView mapView, MyHashMap<String, String> routeKeysToTitles, float density)
	{
		this(busOverlay.busPicture, context, mapView, routeKeysToTitles, density);
		
		this.drawHighlightCircle = busOverlay.drawHighlightCircle;
		
		this.locationGroups.addAll(busOverlay.locationGroups);
		
		overlays.addAll(busOverlay.overlays);
		
		populate();
		
		this.selectedBusIndex = busOverlay.getLastFocusedIndex();
		
		
		if (selectedBusIndex != NOT_SELECTED_INDEX)
		{
			onTap(selectedBusIndex);
		}
		
		setLastFocusedIndex(busOverlay.getLastFocusedIndex());
	}
	
	
	public BusOverlay(Drawable busPicture, Main context, MapView mapView, MyHashMap<String, String> routeKeysToTitles, float density)
	{
		super(boundCenterBottom(busPicture), mapView);

		this.context = context;
		this.selectedBusIndex = NOT_SELECTED_INDEX;
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
				setLastFocusedIndex(NOT_SELECTED_INDEX);
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
	
	public void addLocation(LocationGroup locationGroup)
	{
		locationGroups.add(locationGroup);
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
	
	
	@Override
	protected BusOverlayItem createItem(int i)
	{
		return overlays.get(i);
	}


	public void clear() {
		overlays.clear();
		
		locationGroups.clear();
		
		setFocus(null);
		setLastFocusedIndex(NOT_SELECTED_INDEX);
		locationsObj = null;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		int lastFocusedIndex = getLastFocusedIndex();
		final int overlaysSize = Math.min(overlays.size(), locationGroups.size());
		for (int i = 0; i < overlaysSize; i++)
		{
			OverlayItem item = overlays.get(i);
			LocationGroup locationGroup = locationGroups.get(i);

			boolean isSelected = i == lastFocusedIndex;
			Drawable drawable = locationGroup.getDrawable(context, shadow, isSelected);
			item.setMarker(drawable);

			boundCenterBottom(drawable);
		}
		
		if (selectedBusIndex != NOT_SELECTED_INDEX)
		{
			//make sure that selected buses are preserved during refreshes
			setFocus(overlays.get(selectedBusIndex));
			selectedBusIndex = NOT_SELECTED_INDEX;
		}
			
		if (drawHighlightCircle && overlays.size() > 0)
		{
			final Point circleCenter = this.circleCenter;
			final Point radiusPoint = this.radiusPoint;
			
			//get screen location
			GeoPoint firstPoint = overlays.get(0).getPoint();

			Projection projection = mapView.getProjection();
			projection.toPixels(firstPoint, circleCenter);
			final int circleCenterX = circleCenter.x;
			final int circleCenterY = circleCenter.y;

			//find out farthest point from bus that's closest to center
			//these points are sorted by distance from center of screen, but we want
			//distance from the bus closest to the center, which is not quite the same
			int lastDistance = 0;
			for (int i = 1; i < overlaysSize; i++)
			{
				OverlayItem item = overlays.get(i);
				
				GeoPoint geoPoint = item.getPoint();
				projection.toPixels(geoPoint, radiusPoint);
				final int diffX = radiusPoint.x - circleCenterX;
				final int diffY = radiusPoint.y - circleCenterY;
				final int distance = diffX*diffX + diffY*diffY;
				
				if (lastDistance < distance)
				{
					lastDistance = distance;
				}
			}
		
			//draw a circle showing which buses are currently displayed
			float radius = FloatMath.sqrt(lastDistance);

			canvas.drawCircle(circleCenterX, circleCenterY, radius, paint);
		}
		super.draw(canvas, mapView, shadow);
	}

	
	
	public LocationGroup getSelectedBus() {
		int selectedBusIndex = getLastFocusedIndex();
		if (selectedBusIndex == NOT_SELECTED_INDEX)
		{
			return null;
		}
		else
		{
			if (selectedBusIndex >= locationGroups.size())
			{
				this.selectedBusIndex = NOT_SELECTED_INDEX;
				return null;
			}
			else
			{
				return locationGroups.get(selectedBusIndex);
			}
		}
		
	}

	public void doPopulate() {
		populate();
	}

	public void refreshBalloons() {
		
		//Log.i("REFRESHBALLOONS", selectedBusIndex + " ");
		if (selectedBusIndex == NOT_SELECTED_INDEX)
		{
			hideBalloon();
		}
		else
		{
			onTap(selectedBusIndex);
		}
	}
	
	public void setSelectedBus(LocationGroup newLocationGroup)
	{
		selectedBusIndex = NOT_SELECTED_INDEX;
		if (newLocationGroup != null)
		{
			for (int i = 0; i < locationGroups.size(); i++)
			{
				LocationGroup group = locationGroups.get(i);

				if (group == newLocationGroup)
				{
					selectedBusIndex = i;
					break;
				}
			}
		}
	}

	public Drawable getBusPicture() {
		return busPicture;
	}

	public void addOverlaysFromLocations(ArrayList<GeoPoint> points)
	{
		overlays.ensureCapacity(overlays.size() + locationGroups.size());
		
		for (int i = 0; i < locationGroups.size(); i++)
		{
			LocationGroup locationGroup = locationGroups.get(i);
			String titleText = locationGroup.getSnippetTitle();
			String snippetText = locationGroup.getSnippet();
			ArrayList<Alert> alerts = locationGroup.getSnippetAlerts();
			BusOverlayItem overlayItem = new BusOverlayItem(points.get(i),titleText, snippetText, alerts);
			overlays.add(overlayItem);
		}
		populate();
	}
	
	@Override
	protected boolean onTap(int index) {
		boolean ret = super.onTap(index);
		
		LocationGroup locationGroup = locationGroups.get(index);
		BusOverlayItem item = currentFocussedItem;
		item.setCurrentLocation(locationGroup);
		
		BusPopupView view = (BusPopupView)balloonView;
		boolean isStarVisible = !locationGroup.isVehicle();
		boolean isFavorite = locationsObj.isFavorite(locationGroup);
		view.setState(isFavorite, isStarVisible, isStarVisible, locationGroup);
		
		return ret;
	}
	
	protected BalloonOverlayView<BusOverlayItem> createBalloonOverlayView() {
		BusPopupView view = new BusPopupView(getMapView().getContext(), getBalloonBottomOffset(), locationsObj, routeKeysToTitles,
				density);
		return view;
	}


	public LocationGroup getItemWithLatLon(int latAsInt, int lonAsInt) {
		for (LocationGroup locationGroup : locationGroups) {
			if (locationGroup.getLatAsInt() == latAsInt && locationGroup.getLonAsInt() == lonAsInt) {
				return locationGroup;
			}
		}
		return null;
	}
}
