package boston.Bus.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

/**
 * This class stores information about the bus. This information is mostly taken from the feed
 */
public class BusLocation implements Location
{
	/**
	 * Current latitude of bus, in radians 
	 */
	public final double latitude;
	/**
	 * Current longitude of bus, in radians
	 */
	public final double longitude;
	
	/**
	 * Current latitude of bus, in degrees
	 */
	public final double latitudeAsDegrees;
	/**
	 * Current longitude of bus, in degrees
	 */
	public final double longitudeAsDegrees;
	
	/**
	 * The bus id. This uniquely identifies a bus
	 */
	public final int id;
	/**
	 * The route number. This may be null if the XML says so
	 */
	public final String route;
	
	/**
	 * seconds since the bus last sent GPS data to the server. This comes from the XML; we don't calculate this
	 */
	public final int seconds;
	/**
	 * Creation time of this bus object
	 */
	public final double lastUpdateInMillis;
	
	/**
	 * Distance in miles of the bus from its previous location, in the x dimension, squared
	 */
	private double distanceFromLastX;
	/**
	 * Distance in miles of the bus from its previous location, in the y dimension, squared
	 */
	private double distanceFromLastY;
	
	/**
	 * What is the heading mentioned for the bus?
	 */
	private final String heading;
	
	/**
	 * Does the bus behave predictably?
	 */
	public final boolean predictable;
	
	/**
	 * Is the bus inbound, or outbound?
	 * This only makes sense if predictable is true
	 */
	private final boolean inBound;
	
	/**
	 * Inferred bus route
	 */
	private final String inferBusRoute;
	
	
	/**
	 * Used in calculating the distance between coordinates
	 */
	private final double radiusOfEarthInKilo = 6371.2;
	private final double kilometersPerMile = 1.609344;
	
	private final double degreesToRadians = Math.PI / 180.0;
	
	private double timeBetweenUpdatesInMillis;
	
	private final Drawable bus;
	private final Drawable arrow;
	private final Drawable tooltip;
	
	
	public BusLocation(double latitude, double longitude, int id, String route, int seconds, double lastUpdateInMillis,
			String heading, boolean predictable, boolean inBound, String inferBusRoute, Drawable bus, Drawable arrow, Drawable tooltip)
	{
		this.latitude = latitude * degreesToRadians;
		this.longitude = longitude * degreesToRadians;
		this.latitudeAsDegrees = latitude;
		this.longitudeAsDegrees = longitude;
		this.id = id;
		this.route = route;
		this.seconds = seconds;
		this.lastUpdateInMillis = lastUpdateInMillis;
		this.heading = heading;
		this.predictable = predictable;
		this.inBound = inBound;
		this.inferBusRoute = inferBusRoute;
		this.bus = bus;
		this.arrow = arrow;
		this.tooltip = tooltip;
	}

	public boolean hasHeading()
	{
		if (predictable)
		{
			return (getHeading() >= 0);
		}
		else
		{
			if (distanceFromLastY == 0 && distanceFromLastX == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}
	
	public int getHeading()
	{
		if (predictable)
		{
			return Integer.parseInt(heading);
		}
		else
		{
			//TODO: this repeats code from getDirection(), make a method to reuse code
			double thetaInRadians = Math.atan2(distanceFromLastY, distanceFromLastX);
			
			int degrees = radiansToDegrees(thetaInRadians);
			return degrees;
		}
	}
	
	/**
	 * 
	 * @return a String describing the direction of the bus, or "" if it can't be calculated.
	 * For example: E (90 deg)
	 */
	public String getDirection()
	{
		if (distanceFromLastY == 0 && distanceFromLastX == 0)
		{
			return "";
		}
		else
		{
			double thetaInRadians = Math.atan2(distanceFromLastY, distanceFromLastX);
			
			int degrees = radiansToDegrees(thetaInRadians); 
			return  degrees + " deg (" + convertHeadingToCardinal(degrees) + ")";
		}
		
	}
	/**
	 * 
	 * @param thetaBackup direction in radians, where east is 0 and going counterclockwise
	 * @return a descriptive String showing the direction (for example: E (90 deg))
	 */
	private int radiansToDegrees(double thetaAsRadians)
	{
		//NOTE: degrees will be 0 == north, going clockwise
		int degrees = (int)(thetaAsRadians * 180.0 / Math.PI);
		if (degrees < 0)
		{
			degrees += 360;
		}
		
		//convert to usual compass orientation
		degrees = -degrees + 90;
		if (degrees < 0)
		{
			degrees += 360;
		}
		
		return degrees;
	}

	/**
	 * @param lat2 latitude in radians
	 * @param lon2 longitude in radians
	 * @return distance in miles
	 */
	public double distanceFrom(double lat2, double lon2)
	{
		double lat1 = latitude;
		double lon1 = longitude;
		
		
		//great circle distance
		double dist = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
		dist *= radiusOfEarthInKilo;
		dist /= kilometersPerMile;
		
		return dist;
	}
	
	/**
	 * calculate the distance from the old location
	 * 
	 * @param oldBusLocation
	 */
	public void movedFrom(BusLocation oldBusLocation)
	{
		if (oldBusLocation.latitude == latitude && oldBusLocation.longitude == longitude)
		{
			//ignore
			return;
		}
		distanceFromLastX = distanceFrom(latitude, oldBusLocation.longitude);
		distanceFromLastY = distanceFrom(oldBusLocation.latitude, longitude);
		
		if (oldBusLocation.latitude > latitude)
		{
			distanceFromLastY *= -1;
		}
		if (oldBusLocation.longitude > longitude)
		{
			distanceFromLastX *= -1;
		}
		
		timeBetweenUpdatesInMillis = lastUpdateInMillis - oldBusLocation.lastUpdateInMillis;
	}

	public String makeTitle() {
    	String title = "Id: " + id + ", route: ";
    	if (route == null || route.equals("null"))
    	{
    		title += "not mentioned";
    	}
    	else
    	{
    		title += route;
    	}
    	title += "\nSeconds since update: " + (int)(seconds + (System.currentTimeMillis() - lastUpdateInMillis) / 1000);
    	String direction = getDirection();
    	if (direction.length() != 0 && predictable == false)
    	{
    		title += "\nEstimated direction: " + direction;
    	}
    	
    	if (predictable)
    	{
    		title += "\nHeading: " + heading + " deg (" + convertHeadingToCardinal(Integer.parseInt(heading)) + ")";

    		title += "\n";
    		if (inBound)
    		{
    			title += "Inbound";
    		}
    		else
    		{
    			title += "Outbound";
    		}
    	}
    	else
    	{
    		//TODO: how should we say this?
    		//title += "\nUnpredictable";
    		
    		if ((route == null || route.equals("null")) && inferBusRoute != null)
    		{
    			title += "\nEstimated route number: " + inferBusRoute;
    		}
    	}
    	return title;
	}

	/**
	 * Converts a heading to a cardinal direction string
	 * 
	 * @param degree heading in degrees, where 0 is north and 90 is east
	 * @return a direction (for example "N" for north)
	 */
	private String convertHeadingToCardinal(double degree)
	{
		//shift degree so all directions line up nicely
		degree += 360.0 / 16; //22.5
		if (degree >= 360)
		{
			degree -= 360;
		}
		
		//get an index into the directions array
		int index = (int)(degree / (360.0 / 8.0));
		if (index < 0 || index >= 8)
		{
			return "calculation error";
		}
		
		String[] directions = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		return directions[index];
	}

	public int getId()
	{
		return id;
	}

	public Drawable getDrawable(Context context, boolean shadow, boolean isSelected) {
		Drawable drawable = bus;
		if (shadow == false)
		{
			//to make life easier we won't draw shadows except for the bus
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

			//if it has a direction, draw the arrow
			boolean hasHeading = hasHeading();
			if (hasHeading)
			{
				arrowArg = arrow;
			}
			
			if (isSelected || hasHeading)
			{
				//is there a reason to use BusDrawable?
				
				//the constructor should ignore the arrow and tooltip if these arguments are null
				drawable = new BusDrawable(bus, getHeading(), arrowArg, tooltipArg, textViewArg);
			}
		}
		return drawable;
	}

	@Override
	public double getLatitudeAsDegrees() {
		// TODO Auto-generated method stub
		return latitudeAsDegrees;
	}

	@Override
	public double getLongitudeAsDegrees() {
		// TODO Auto-generated method stub
		return longitudeAsDegrees;
	}
	
}

