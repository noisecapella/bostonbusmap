package boston.Bus.Map.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Debug;
import android.util.Log;
import android.widget.PopupWindow;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.util.Constants;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RouteOverlay extends Overlay
{
	private LinkedList<Path> paths = new LinkedList<Path>();
	private final Projection projection;
	private boolean showRouteLine;
	
	private final Paint paint;
	
	public RouteOverlay(Projection projection)
	{
		this.projection = projection;
		
		paint = new Paint();
		paint.setColor(Color.argb(0x99, 0x00, 0x00, 0xff));
		paint.setStrokeWidth(5);
		paint.setAntiAlias(true);
		paint.setStrokeMiter(3);

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
	private void addPaths(Collection<Path> paths) {
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
	public void setPathsAndColor(ArrayList<Path> paths, String color)
	{
		if (null == color)
		{
			color = "#990000FF";
		}
		else
		{
			color = "#99" + color.toUpperCase();
		}
		
		paint.setColor(Color.parseColor(color));
		
		this.paths.clear();
		addPaths(paths);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow || showRouteLine == false)
		{
			return;
		}
		

		int floatCount = 0;
		float prevLastLon = 0;
		float prevLastLat = 0;
		for (Path path : paths)
		{
			int size = path.getPointsSize();
			
			if (size >= 1)
			{
				if (path.getPointLat(0) == prevLastLat && path.getPointLon(0) == prevLastLon)
				{
					size--;
				}
				
				prevLastLat = path.getPointLat(path.getPointsSize() - 1);
				prevLastLon = path.getPointLon(path.getPointsSize() - 1);
			}
			floatCount += size;
		}

		//swap these two back and forth so we don't 
		Point pixelPoint1 = new Point();
		Point pixelPoint2 = new Point();

		float[] floats = new float[floatCount * 4];
		int floatIndex = 0;
		
		prevLastLat = 0;
		prevLastLon = 0;
		
		//make sure the JVM knows this doesn't change unexpectedly
		for (Path path : paths)
		{
			int pointsSize = path.getPointsSize();

			if (pointsSize >= 1)
			{
				if (path.getPointLat(0) == prevLastLat && path.getPointLon(0) == prevLastLon)
				{
					pointsSize--;
				}
				
				prevLastLat = path.getPointLat(path.getPointsSize() - 1);
				prevLastLon = path.getPointLon(path.getPointsSize() - 1);
			}
			
			Point previousPoint = null;
			
			//skip over some points for efficiency's sake
			for (int i = 0; i < pointsSize; i++)
			{
				
				
				Point pixelPoint;
				if (pixelPoint1 == previousPoint)
				{
					pixelPoint = pixelPoint2;
				}
				else
				{
					pixelPoint = pixelPoint1;
				}

				double pointLat = path.getPointLat(i);
				double pointLon = path.getPointLon(i);
				
				GeoPoint geoPoint = new GeoPoint((int)(pointLat * Constants.E6), (int)(pointLon * Constants.E6));
				
				projection.toPixels(geoPoint, pixelPoint);

				if (previousPoint != null)
				{
					floats[floatIndex + 0] = (float)previousPoint.x;
					floats[floatIndex + 1] = (float)previousPoint.y;
					floats[floatIndex + 2] = (float)pixelPoint.x;
					floats[floatIndex + 3] = (float)pixelPoint.y;

					floatIndex += 4;
				}
				
				previousPoint = pixelPoint;
			}
		}
		
		//Log.v("BostonBusMap", "Number of floats in array drawn in RouteOverlay: " + floats.length);
		canvas.drawLines(floats, paint);
	}

	public void setDrawLine(boolean showRouteLine) {
		this.showRouteLine = showRouteLine;
	}

	/*public void setDrawCoarseLine(boolean showCoarseRouteLine) {
		if (showCoarseRouteLine)
		{
			increment = COARSE_INCREMENT;
		}
		else
		{
			increment = FINE_INCREMENT;
		}
		
	}*/
}
