package boston.Bus.Map.data;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.collect.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.SparseArray;

/**
 * Drawables for a particular TransitSource
 * @author schneg
 *
 */
public class TransitDrawables {
	private final Context context;
	private final Drawable intersection;
	private final Drawable arrow;
	private final int arrowTopDiff;
	private final Drawable stop;
	private final Drawable stopUpdated;
	private final Drawable vehicle;
	
	private final SparseArray<Drawable> vehicles = new SparseArray<Drawable>();
	
	private static final int[][] validStates = new int[][] {
		new int[]{android.R.attr.state_focused}, new int[0]
	};
	
	public TransitDrawables(Context context, Drawable stop, Drawable stopUpdated, Drawable vehicle,
			Drawable arrow, int arrowTop, Drawable intersection) {
		this.stop = stop;
		this.stopUpdated = stopUpdated;
		this.vehicle = vehicle;
		this.arrow = arrow;
		this.arrowTopDiff = arrowTop;
		this.intersection = intersection;
		this.context = context;
	}

	public Drawable getVehicle(int heading) {
		Drawable drawable = vehicles.get(heading);
		if (drawable == null) {
			drawable = createBusDrawable(heading);
			vehicles.put(heading, drawable);
		}
		return drawable;
	}

	private Drawable createBusDrawable(int heading) {
		//NOTE: 0, 0 is the bottom center point of the bus icon
		
		StateListDrawable stateListDrawable = new StateListDrawable();
		for (int[] state : validStates) {
			Bitmap bitmap = Bitmap.createBitmap(vehicle.getIntrinsicWidth(),
					vehicle.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			//first draw the bus
			vehicle.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			vehicle.draw(canvas);

			//then draw arrow
			if (arrow != null && heading != BusLocation.NO_HEADING)
			{
				//put the arrow in the bus window

				int arrowLeft = -(arrow.getIntrinsicWidth() / 4);
				int arrowTop = -vehicle.getIntrinsicHeight() + arrowTopDiff;

				//NOTE: use integer division when possible. This code is frequently executed
				int arrowWidth = (arrow.getIntrinsicWidth() * 6) / 10;  
				int arrowHeight = (arrow.getIntrinsicHeight() * 6) / 10;
				int arrowRight = arrowLeft + arrowWidth;
				int arrowBottom = arrowTop + arrowHeight;

				arrow.setBounds(arrowLeft, arrowTop, arrowRight, arrowBottom);

				canvas.save();
				//set rotation pivot at the center of the arrow image
				canvas.rotate(heading, arrowLeft + arrowWidth/2, arrowTop + arrowHeight / 2);

				Rect rect = arrow.getBounds();
				arrow.draw(canvas);
				arrow.setBounds(rect);

				canvas.restore();

			}
			BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
			stateListDrawable.addState(state, bitmapDrawable);
		}
		
		return stateListDrawable;
	}

	public Drawable getStop() {
		return stop;
	}

	public Drawable getStopUpdated() {
		return stopUpdated;
	}

	public Drawable getIntersection() {
		return intersection;
	}
}
