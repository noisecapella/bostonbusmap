package boston.Bus.Map.ui;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.Log;

import com.schneeloch.bostonbusmap_library.data.Path;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import com.schneeloch.bostonbusmap_library.util.Constants;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RouteOverlay extends Overlay
{
	private List<Path> paths = Lists.newLinkedList();
	private final Projection projection;
	private boolean showRouteLine = true;
	private boolean allRoutesBlue = TransitSystem.isDefaultAllRoutesBlue();
	
	private final Paint defaultPaint;
	private final Map<Integer, Paint> paintCache = Maps.newHashMap();
	private String currentRoute;
	
	private static final int DEFAULTCOLOR = 0x000099;
	
	public RouteOverlay(Projection projection)
	{
		this.projection = projection;
		
		defaultPaint = new Paint();
		applyDefaultSettings(defaultPaint);
	}
	
	public RouteOverlay(RouteOverlay routeOverlay, Projection projection)
	{
		this(projection);
		
		addPaths(routeOverlay.paths);
	}
	
	/**
	 * Add the collection of paths, such that the head of one path touches the tail of another when possible
	 * 
	 * Only run this in the UI thread
	 * 
	 * @param paths
	 */
	private void addPaths(Iterable<Path> paths) {
		for (Path path : paths)
		{
			int size = path.getPointsSize();
			float firstLat = path.getPointLat(0);
			float firstLon = path.getPointLon(0);
			float lastLat = path.getPointLat(size - 1);
			float lastLon = path.getPointLon(size - 1);

			ListIterator<Path> iterator = this.paths.listIterator();
			boolean addedItem = false;
			while (iterator.hasNext())
			{
				Path thisPath = (Path)iterator.next();
				
				int thisSize = thisPath.getPointsSize();
				if (thisSize >= 1)
				{
					float thisFirstLat = thisPath.getPointLat(0);
					float thisFirstLon = thisPath.getPointLon(0);
					float thisLastLat = thisPath.getPointLat(thisSize - 1);
					float thisLastLon = thisPath.getPointLon(thisSize - 1);
					
					//if inserting head == existing tail
					if (firstLat == thisLastLat && firstLon == thisLastLon)
					{
						iterator.add(path);
						addedItem = true;
						break;
					}
					else if (lastLat == thisFirstLat && lastLon == thisFirstLon)
					{
						//inserting tail == existing head
						iterator.previous();
						iterator.add(path);
						addedItem = true;
						break;
					}
				}
			}
			
			if (addedItem == false)
			{
				//head or tail doesn't match, just add it to the end of the list
				this.paths.add(path);
			}
		}
	}

	private void populatePaintCache(Path[] paths) {
		for (Path path : paths) {
			int color = path.getColor();
			if (paintCache.containsKey(color) == false) {
				Paint paint = new Paint();
				applyDefaultSettings(paint);
				paint.setColor(color);
				paint.setAlpha(0x99);

				paintCache.put(color, paint);
			}
		}		
	}
	
	private void applyDefaultSettings(Paint paint) {
		paint.setColor(DEFAULTCOLOR);
		paint.setAlpha(0x99);
		paint.setStrokeWidth(5);
		paint.setAntiAlias(true);
		paint.setStrokeMiter(3);
		paint.setStyle(Style.STROKE);
	}

	/**
	 * 
	 * @param paths
	 * @param color assumes something like "1234ef"
	 */
	public void setPathsAndColor(Path[] paths, String newRoute)
	{
		populatePaintCache(paths);
		
		if (newRoute != null && currentRoute != null && currentRoute.equals(newRoute) && this.paths.size() == paths.length)
		{
			//don't delete and add paths if we already have them
		}
		else
		{
			this.paths.clear();
			addPaths(Arrays.asList(paths));
		}
		currentRoute = newRoute;
	}
	
	public void addPathsAndColor(Path[] paths, String newRoute)
	{
		populatePaintCache(paths);
		
		addPaths(Arrays.asList(paths));
		currentRoute = newRoute;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow || showRouteLine == false)
		{
			return;
		}
		

		android.graphics.Path drawingPath = new android.graphics.Path();
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		GeoPoint bottomRight = projection.fromPixels(width, height);
		GeoPoint topLeft = projection.fromPixels(0, 0);
		
		int maxLat = topLeft.getLatitudeE6();
		int minLat = bottomRight.getLatitudeE6();
		int minLon = topLeft.getLongitudeE6();
		int maxLon = bottomRight.getLongitudeE6();
		
		//make sure the JVM knows this doesn't change unexpectedly
		Point pixelPoint = new Point();
		Paint currentPaint = defaultPaint;
		for (Path path : paths)
		{
			int pathColor = allRoutesBlue ? DEFAULTCOLOR : path.getColor();
			if (pathColor != (currentPaint.getColor() & 0xffffff)) {
				Paint paint = paintCache.get(pathColor);
				if (paint == null) {
					Log.e("BostonBusMap", "ERROR: paint not in cache");
					paint = defaultPaint;
				}
				flushDrawingQueue(canvas, drawingPath, paint);
				currentPaint = paint;
			}
			
			int pointsSize = path.getPointsSize();

			boolean prevOutOfBounds = true;
			boolean prevWasMoveTo = true;
			boolean prevOutOfBoundsLeft = true;
			boolean prevOutOfBoundsRight = true;
			boolean prevOutOfBoundsAbove = true;
			boolean prevOutOfBoundsBelow = true;
			
			int moveToLat = Integer.MAX_VALUE;
			int moveToLon = Integer.MAX_VALUE;
			for (int i = 0; i < pointsSize; i++)
			{
				float pointLat = path.getPointLat(i);
				int pointLatInt = (int)(pointLat * Constants.E6);
				float pointLon = path.getPointLon(i);
				int pointLonInt = (int)(pointLon * Constants.E6);
				
				boolean currentOutOfBoundsLeft = pointLatInt < minLat;
				boolean currentOutOfBoundsRight = pointLatInt > maxLat;
				boolean currentOutOfBoundsAbove = pointLonInt < minLon;
				boolean currentOutOfBoundsBelow = pointLonInt > maxLon;
				
				boolean currentOutOfBounds = currentOutOfBoundsLeft || currentOutOfBoundsRight ||
					currentOutOfBoundsAbove || currentOutOfBoundsBelow;
				
				// this boolean won't make sense unless both points are out of bounds
				// don't do any hard work here. We just need to specify the most common impossible cases
				boolean impossibleIntercept = ((currentOutOfBoundsLeft && prevOutOfBoundsLeft) ||
											   (currentOutOfBoundsRight && prevOutOfBoundsRight) ||
											   (currentOutOfBoundsAbove && prevOutOfBoundsAbove) ||
											   (currentOutOfBoundsBelow && prevOutOfBoundsBelow));
						
				if (i == 0 || (prevOutOfBounds && currentOutOfBounds && impossibleIntercept))
				{
					//be lazy about moveTo in case it incurs a performance hit
					moveToLat = pointLatInt;
					moveToLon = pointLonInt;
					prevWasMoveTo = true;
				}
				else
				{
					if (prevWasMoveTo)
					{
						GeoPoint geoPoint = new GeoPoint(moveToLat, moveToLon);
						projection.toPixels(geoPoint, pixelPoint);
						
						drawingPath.moveTo(pixelPoint.x, pixelPoint.y);
						prevWasMoveTo = false;
					}
					GeoPoint geoPoint = new GeoPoint(pointLatInt, pointLonInt);
					projection.toPixels(geoPoint, pixelPoint);
					
					drawingPath.lineTo(pixelPoint.x, pixelPoint.y);
				}

				prevOutOfBounds = currentOutOfBounds;
				prevOutOfBoundsLeft = currentOutOfBoundsLeft;
				prevOutOfBoundsRight = currentOutOfBoundsRight;
				prevOutOfBoundsAbove = currentOutOfBoundsAbove;
				prevOutOfBoundsBelow = currentOutOfBoundsBelow;
			}
		}
		
		flushDrawingQueue(canvas, drawingPath, currentPaint);
	}

	private static void flushDrawingQueue(Canvas canvas, android.graphics.Path drawingPath, Paint paint) {
		canvas.drawPath(drawingPath, paint);
		drawingPath.reset();
	}

	public void setDrawLine(boolean showRouteLine) {
		this.showRouteLine = showRouteLine;
	}

	public boolean isShowLine() {	
		return showRouteLine;
	}

	public void clearPaths() {
		this.paths.clear();
	}

	public void setAllRoutesBlue(boolean allRoutesBlue) {
		this.allRoutesBlue = allRoutesBlue;
	}
}
