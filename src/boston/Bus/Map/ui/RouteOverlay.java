package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Debug;
import android.util.Log;
import android.widget.PopupWindow;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.util.Constants;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import boston.Bus.Map.main.Main;

public class RouteOverlay extends Overlay
{
	private LinkedList<Path> paths = new LinkedList<Path>();
	private final Projection projection;
	private boolean showRouteLine;
	
	private final Paint paint;
	private String currentRoute;
	
	public RouteOverlay(Projection projection)
	{
		this.projection = projection;
		
		paint = new Paint();
		paint.setColor(Color.argb(0x99, 0x00, 0x00, 0xff));
		paint.setStrokeWidth(5);
		paint.setAntiAlias(true);
		paint.setStrokeMiter(3);
		paint.setStyle(Style.STROKE);
	}
	
	public RouteOverlay(RouteOverlay routeOverlay, Projection projection)
	{
		this(projection);
		
		addPaths(routeOverlay.paths);
	}
	
	/**
	 * Add the collection of paths, such that the head of one path touches the tail of another when possible
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

			//can this attach onto a head or tail?
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

	/**
	 * 
	 * @param paths
	 * @param color assumes something like "1234ef"
	 */
	public void setPathsAndColor(Path[] paths, int color, String newRoute)
	{
		paint.setColor(color);
		paint.setAlpha(0x99);
		
		if (newRoute != null && currentRoute != null && currentRoute.equals(newRoute) && this.paths.size() == paths.length)
		{
			//don't delete and add paths if we already have them
		}
		else
		{
			this.paths.clear();
			addPaths(new CopyOnWriteArrayList<Path>(paths));
		}
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
		for (Path path : paths)
		{
			int pointsSize = path.getPointsSize();

			boolean prevOutOfBounds = true;
			boolean prevWasMoveTo = true;
			boolean prevOutOfBoundsLeft = true;
			boolean prevOutOfBoundsRight = true;
			boolean prevOutOfBoundsAbove = true;
			boolean prevOutOfBoundsBelow = true;
			
			int prevPointLonInt = 0;
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
		
		canvas.drawPath(drawingPath, paint);
	}

	public void setDrawLine(boolean showRouteLine) {
		this.showRouteLine = showRouteLine;
	}
}
