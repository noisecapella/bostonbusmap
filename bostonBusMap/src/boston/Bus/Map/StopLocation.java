package boston.Bus.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class StopLocation implements Location
{
	private final double latitude;
	private final double longitude;
	private final double latitudeAsDegrees;
	private final double longitudeAsDegrees;
	private final Drawable busStop;
	private final Drawable tooltip;
	
	private final int id;
	private static final int LOCATIONTYPE = 3; 
	
	public StopLocation(double latitudeAsDegrees, double longitudeAsDegrees, Drawable busStop, Drawable tooltip, int id)
	{
		this.latitude = latitudeAsDegrees * LocationComparator.degreesToRadians;
		this.latitudeAsDegrees = latitudeAsDegrees;
		this.longitude = longitudeAsDegrees * LocationComparator.degreesToRadians;
		this.longitudeAsDegrees = longitudeAsDegrees;
		this.busStop = busStop;
		this.tooltip = tooltip;
		this.id = id;
	}
	
	@Override
	public double distanceFrom(double centerLatitude, double centerLongitude) {
		return LocationComparator.computeDistance(latitude, longitude, centerLatitude, centerLongitude);
	}

	@Override
	public Drawable getDrawable(Context context, boolean shadow,
			boolean isSelected) {
		Drawable drawable = busStop;
		if (shadow == false)
		{
			//to make life easier we won't draw shadows except for the bus stop
			//the tooltip has some weird error where the shadow draws a little left and up from where it should draw
			
			Drawable arrowArg = null, tooltipArg = null;
			TextView textViewArg = null;
			
			//if selected, draw the tooltip
			if (isSelected)
			{
				TextView textView = new TextView(context);
				String title = makeTitle();
				textView.setText(title);
				tooltipArg = tooltip;
				textViewArg = textView;
			}

			//is there a reason to use BusDrawable?

			//the constructor should ignore the arrow and tooltip if these arguments are null
			drawable = new BusDrawable(busStop, getHeading(), arrowArg, tooltipArg, textViewArg);
		}
		return drawable;
	}

	@Override
	public int getHeading() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getId() {
		return id | LOCATIONTYPE << 16;
	}

	@Override
	public double getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public double getLongitudeAsDegrees() {
		return longitudeAsDegrees;
	}

	@Override
	public boolean hasHeading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String makeTitle() {
		return "Stop: " + id;
	}

}
