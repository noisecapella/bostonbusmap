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


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RouteOverlay extends Overlay
{
	private ArrayList<Path> paths = new ArrayList<Path>();
	private final Projection projection;
	private boolean showRouteLine;
	
	private static final int FINE_INCREMENT = 1;
	private static final int COARSE_INCREMENT = 20;
	
	/**
	 * Should we skip any points? (This can drastically slow down the app if we don't).
	 */
	private int increment = FINE_INCREMENT;
	
	public RouteOverlay(Projection projection)
	{
		this.projection = projection;
	}
	
	public RouteOverlay(RouteOverlay routeOverlay, Projection projection)
	{
		this(projection);
		
		paths.addAll(routeOverlay.paths);
	}
	
	public void setPaths(ArrayList<Path> paths)
	{
		this.paths.clear();
		this.paths.addAll(paths);
	}
	
	public void setIncrement(int i)
	{
		this.increment = i;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow || showRouteLine == false)
		{
			return;
		}
		
		Paint paint = new Paint();
		paint.setColor(Color.argb(0x99, 0x00, 0x00, 0xff));
		paint.setStrokeWidth(5);
		paint.setAntiAlias(true);

		int floatCount = 0;
		for (Path path : paths)
		{
			int size = path.getPoints().size();
			
			if (size > 0)
			{
				floatCount += size / increment;
				if (size % increment != 1 && increment != 1)
				{
					floatCount += 1;
				}
			}
		}

		//swap these two back and forth so we don't 
		Point pixelPoint1 = new Point();
		Point pixelPoint2 = new Point();

		float[] floats = new float[floatCount * 4];
		int floatIndex = 0;
		
		//make sure the JVM knows this doesn't change unexpectedly
		final int increment = this.increment;
		
		for (Path path : paths)
		{
			int pointsSize = path.getPoints().size();

			Point previousPoint = null;
			
			//skip over some points for efficiency's sake
			for (int i = 0; i < pointsSize; i += increment)
			{
				boston.Bus.Map.data.Point point = path.getPoints().get(i);
				
				Point pixelPoint;
				if (pixelPoint1 == previousPoint)
				{
					pixelPoint = pixelPoint2;
				}
				else
				{
					pixelPoint = pixelPoint1;
				}

				GeoPoint geoPoint = new GeoPoint((int)(point.lat * Main.E6), (int)(point.lon * Main.E6));
				
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
			
			if (pointsSize > 1 && increment != 1 && pointsSize % increment != 1)
			{
				//if we didn't already draw a line to the last point, make sure we do that to make things go together
				Point pixelPoint = new Point();
				
				boston.Bus.Map.data.Point point = path.getPoints().get(pointsSize - 1);
				GeoPoint geoPoint = new GeoPoint((int)(point.lat * Main.E6), (int)(point.lon * Main.E6));
				projection.toPixels(geoPoint, pixelPoint);
				
				if (previousPoint != null)
				{
					floats[floatIndex + 0] = (float)previousPoint.x;
					floats[floatIndex + 1] = (float)previousPoint.y;
					floats[floatIndex + 2] = (float)pixelPoint.x;
					floats[floatIndex + 3] = (float)pixelPoint.y;

					floatIndex += 4;
				}
			}

		}
		
		Log.v("BostonBusMap", "Number of floats in array drawn in RouteOverlay: " + floats.length);
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
