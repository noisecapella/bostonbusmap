package boston.Bus.Map.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.SparseArray;

import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.LocationType;

import boston.Bus.Map.ui.BusDrawables;

/**
 * Drawables for a particular TransitSource
 * @author schneg
 *
 */
public class TransitDrawables implements ITransitDrawables {
	private final Context context;
	private final Drawable intersection;
	private final Drawable arrow;
	private final int arrowTopDiff;
	private final Drawable stop;
	private final Drawable stopUpdated;
	private final Drawable vehicle;

    public static ITransitDrawables busDrawables;
    public static ITransitDrawables commuterRailDrawables;
    public static ITransitDrawables hubwayDrawables;
    public static ITransitDrawables subwayDrawables;

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

	@Override
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
			vehicle.setState(state);
			vehicle.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			vehicle.draw(canvas);

			//then draw arrow
			if (arrow != null && heading != BusLocation.NO_HEADING)
			{
				//put the arrow in the bus window

				int arrowLeft = vehicle.getIntrinsicWidth()/2 - (arrow.getIntrinsicWidth() * 6 / 10 / 2);
				int arrowTop = arrowTopDiff;

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


    @Override
    public Drawable getDrawable(Location location) {
        LocationType locationType = location.getLocationType();
        boolean isUpdated = location.isUpdated();
        if (locationType == LocationType.Intersection) {
            return intersection;
        }
        else if (locationType == LocationType.Stop) {
            if (isUpdated) {
                return stopUpdated;
            }
            else {
                return stop;
            }
        }
        else if (locationType == LocationType.Vehicle) {
            return context.getResources().getDrawable(BusDrawables.getIdFromAngle(location.getHeading(), false));
        }
        else {
            throw new RuntimeException("Unexpected location type");
        }
    }
}
