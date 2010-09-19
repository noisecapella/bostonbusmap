package boston.Bus.Map.ui;

import java.util.ArrayList;

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
	private ArrayList<Path> paths = new ArrayList<Path>();
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

	}
	
	public RouteOverlay(RouteOverlay routeOverlay, Projection projection)
	{
		this(projection);
		
		paths.addAll(routeOverlay.paths);
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
			color = "#0000FF";
		}
		else
		{
			color = "#" + color.toUpperCase();
		}
		
		paint.setColor(Color.parseColor(color));
		
		this.paths.clear();
		this.paths.addAll(paths);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow || showRouteLine == false)
		{
			return;
		}
		

		int floatCount = 0;
		for (Path path : paths)
		{
			int size = path.getPointsSize();
			
			floatCount += size;
		}

		//swap these two back and forth so we don't 
		Point pixelPoint1 = new Point();
		Point pixelPoint2 = new Point();

		float[] floats = new float[floatCount * 4];
		int floatIndex = 0;
		
		//make sure the JVM knows this doesn't change unexpectedly
		for (Path path : paths)
		{
			int pointsSize = path.getPointsSize();

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
