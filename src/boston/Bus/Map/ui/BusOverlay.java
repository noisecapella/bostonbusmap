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
import java.util.Collection;
import java.util.List;


import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.PredictionView;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;

import boston.Bus.Map.data.Location;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;
import boston.Bus.Map.util.Constants;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.OnClickListener;


/**
 * 
 * The bus overlay on the MapView
 * Much of this was borrowed from the helpful tutorial at http://developer.android.com/guide/tutorials/views/hello-mapview.html
 * Not in the least bit thread safe. Only access from UI thread!
 */
public class BusOverlay extends BalloonItemizedOverlay<BusOverlayItem> {

	public static final int NOT_SELECTED = -1;
	private final ArrayList<BusOverlayItem> overlays = Lists.newArrayList();
	private Main context;
	private int selectedBusIndex;
	private UpdateHandler updateable;
	private boolean drawHighlightCircle;
	private final int busHeight;
	private final Drawable busPicture;
	private final Paint paint;
	
	private final RouteTitles routeKeysToTitles;
	
	//these two are temporary variables stored here so we don't create a new Point every time we draw
	private final Point circleCenter = new Point();
	private final Point radiusPoint = new Point();
	
	private Locations locationsObj;
	
	public BusOverlay(BusOverlay busOverlay, Main context, MapView mapView, RouteTitles routeKeysToTitles)
	{
		this(busOverlay.busPicture, context, mapView, routeKeysToTitles);
		
		this.drawHighlightCircle = busOverlay.drawHighlightCircle;
		
		overlays.addAll(busOverlay.overlays);
		
		populate();
		
		this.selectedBusIndex = busOverlay.getLastFocusedIndex();
		
		
		if (selectedBusIndex != NOT_SELECTED)
		{
			onTap(selectedBusIndex, false);
		}
		
		setLastFocusedIndex(busOverlay.getLastFocusedIndex());
	}
	
	
	public BusOverlay(Drawable busPicture, Main context, MapView mapView, RouteTitles routeKeysToTitles)
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
	private boston.Bus.Map.ui.BusOverlay.OnClickListener nextTapListener;
	
	public void setDrawHighlightCircle(boolean b)
	{
		drawHighlightCircle = b;
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
		setFocus(null);
		setLastFocusedIndex(NOT_SELECTED);
		clearExceptFocus();
	}
	
	public void clearExceptFocus() {
		overlays.clear();
		populate();
		
		locationsObj = null;

	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		int lastFocusedIndex = getLastFocusedIndex();
		int count = 0;
		for (BusOverlayItem item : overlays)
		{
			Location location = item.getCurrentLocation();

			boolean isSelected = count == lastFocusedIndex;
			Drawable drawable = location.getDrawable(context, shadow, isSelected);
			item.setMarker(drawable);

			boundCenterBottom(drawable);
			count++;
		}
		
		if (selectedBusIndex != NOT_SELECTED)
		{
			//make sure that selected buses are preserved during refreshes
			setFocus(overlays.get(selectedBusIndex));
			selectedBusIndex = NOT_SELECTED;
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
			final int overlaysSize = overlays.size();
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

	
	
	public int getSelectedBusId() {
		int selectedBusIndex = getLastFocusedIndex();
		if (selectedBusIndex == NOT_SELECTED)
		{
			return NOT_SELECTED;
		}
		else
		{
			if (selectedBusIndex >= overlays.size())
			{
				this.selectedBusIndex = NOT_SELECTED;
				return NOT_SELECTED;
			}
			else
			{
				return overlays.get(selectedBusIndex).getCurrentLocation().getId();
			}
		}
		
	}

	public void refreshBalloons() {
		
		Log.i("BostonBusMap", "refreshBalloons, selectedBusIndex: " + selectedBusIndex);
		if (selectedBusIndex == NOT_SELECTED)
		{
			hideBalloon();
			setFocus(null);
			
		}
		else
		{
			onTap(selectedBusIndex, false);
		}
	}
	
	public void setSelectedBusId(int selectedBusId)
	{
		selectedBusIndex = NOT_SELECTED;
		if (selectedBusId != NOT_SELECTED)
		{
			for (int i = 0; i < overlays.size(); i++)
			{
				Location busLocation = overlays.get(i).getCurrentLocation();

				if (busLocation.containsId(selectedBusId))
				{
					Log.e("BostonBusMap", "selectedBusId was set to index " + i + ", id=" + selectedBusId);
					selectedBusIndex = i;
					break;
				}
			}
		}
	}

	public Drawable getBusPicture() {
		return busPicture;
	}

	public void addLocation(Location location) {
		PredictionView predictionView = location.getPredictionView();
		String titleText = predictionView.getSnippetTitle();
		String snippetText = predictionView.getSnippet();
		Alert[] alerts = predictionView.getAlerts();
		int latitudeE6 = (int)(Constants.E6 * location.getLatitudeAsDegrees());
		int longitudeE6 = (int)(Constants.E6 * location.getLongitudeAsDegrees());

		GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
		BusOverlayItem overlayItem = new BusOverlayItem(geoPoint,titleText, snippetText, alerts, location);
		overlays.add(overlayItem);
		populate();
	}

	@Override
	public boolean onTap(int index) {
		return onTap(index, true);
	}
	
	private boolean onTap(int index, boolean triggerListener) {
		boolean ret = super.onTap(index);

		Location location = overlays.get(index).getCurrentLocation();
		if (nextTapListener != null && triggerListener) {
			nextTapListener.onClick(location);
			nextTapListener = null;
		}
		else
		{
			BusPopupView view = (BusPopupView)getBalloonView();
			boolean isVisible = location instanceof StopLocation;
			view.setState(location.isFavorite(), isVisible, isVisible, location);
			setLastFocusedIndex(index);
		}
		return ret;
	}


	protected BalloonOverlayView<BusOverlayItem> createBalloonOverlayView() {
		BusPopupView view = new BusPopupView(getMapView().getContext(), getBalloonBottomOffset(), locationsObj, routeKeysToTitles);
		return view;
	}

	public interface OnClickListener {
		void onClick(Location location);
	}

	public void captureNextTap(OnClickListener onClickListener) {
		this.nextTapListener = onClickListener;
	}
	
	@Override
	protected void animateTo(int index, GeoPoint center) {
		// do nothing
	}


}
