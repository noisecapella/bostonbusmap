package boston.Bus.Map.ui;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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

		for (Path path : paths)
		{
			Point previousPoint = null;
			int pointsSize = path.getPoints().size();

			float[] floats = new float[(pointsSize - 1) * 4];
			
			//skip over some points for efficiency's sake
			for (int i = 0; i < pointsSize; i++)
			{
				boston.Bus.Map.data.Point point = path.getPoints().get(i);
				
				Point pixelPoint = new Point();
				GeoPoint geoPoint = new GeoPoint((int)(point.lat * Main.E6), (int)(point.lon * Main.E6));
				projection.toPixels(geoPoint, pixelPoint);

				if (previousPoint != null)
				{
					floats[((i - 1) * 4) + 0] = (float)previousPoint.x;
					floats[((i - 1) * 4) + 1] = (float)previousPoint.y;
					floats[((i - 1) * 4) + 2] = (float)pixelPoint.x;
					floats[((i - 1) * 4) + 3] = (float)pixelPoint.y;
				}
				
				previousPoint = pixelPoint;
			}
			

			canvas.drawLines(floats, paint);
			
			if (pointsSize % 3 != 1 && pointsSize > 0 && false)
			{
				//if we didn't end on the last point, we need to connect that point so that the next path will join with it
				
				boston.Bus.Map.data.Point point = path.getPoints().get(pointsSize - 1);
				
				Point pixelPoint = new Point();
				GeoPoint geoPoint = new GeoPoint((int)(point.lat * Main.E6), (int)(point.lon * Main.E6));
				projection.toPixels(geoPoint, pixelPoint);
				
				if (previousPoint != null)
				{
					canvas.drawLine(previousPoint.x, previousPoint.y, pixelPoint.x, pixelPoint.y, paint);
				}
			}
		}
	}

	public void setDrawLine(boolean showRouteLine) {
		this.showRouteLine = showRouteLine;
	}
}
