package boston.Bus.Map.data;

import boston.Bus.Map.ui.BusDrawable;
import boston.Bus.Map.util.Constants;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.TagHandler;
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
	public final RouteConfig route;
	
	private final String routeName;
	
	/**
	 * seconds since the bus last sent GPS data to the server. This comes from the XML; we don't calculate this
	 */
	public final int seconds;
	/**
	 * Creation time of this bus object
	 */
	public double lastUpdateInMillis;
	
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
	private final String dirTag;
	
	/**
	 * Inferred bus route
	 */
	private final String inferBusRoute;
	
	private final Directions directions;
	
	private final Drawable bus;
	private final Drawable arrow;
	
	private static final int LOCATIONTYPE = 1;
	
	public BusLocation(double latitude, double longitude, int id, RouteConfig route, int seconds, double lastUpdateInMillis,
			String heading, boolean predictable, String dirTag, String inferBusRoute,
			Drawable bus, Drawable arrow, String routeName, Directions directions)
	{
		this.latitude = latitude * Constants.degreesToRadians;
		this.longitude = longitude * Constants.degreesToRadians;
		this.latitudeAsDegrees = latitude;
		this.longitudeAsDegrees = longitude;
		this.id = id;
		this.route = route;
		this.seconds = seconds;
		this.lastUpdateInMillis = lastUpdateInMillis;
		this.heading = heading;
		this.predictable = predictable;
		this.dirTag = dirTag;
		this.inferBusRoute = inferBusRoute;
		this.bus = bus;
		this.arrow = arrow;
		this.routeName = routeName;
		this.directions = directions;
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

	public double distanceFrom(double lat2, double lon2)
	{
		return LocationConstants.computeCompareDistance(latitude, longitude, lat2, lon2);
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
	}

	@Override
	public String makeSnippet(RouteConfig routeConfig)
	{
		return null;
	}
	
	public String makeTitle() {
    	String title = "Id: " + id + ", route: ";
    	if ((route == null || route.getRouteName() == null || route.getRouteName().equals("null")) && routeName == null)
    	{
    		title += "not mentioned";
    	}
    	else if (route != null)
    	{
    		title += route.getRouteName();
    	}
    	else
    	{
    		title += routeName;
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

    		if (route != null)
    		{
    			String directionName = directions.getTitle(dirTag);
    			if (directionName != null && directionName.length() != 0)
    			{
    				title += "\n" + directionName;
    			}
    		}
    	}
    	else
    	{
    		//TODO: how should we say this?
    		//title += "\nUnpredictable";
    		
    		if ((route == null || "null".equals(route.getRouteName())) && inferBusRoute != null)
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

	@Override
	public int getId()
	{
		return id | LOCATIONTYPE << 24;
	}

	public Drawable getDrawable(Context context, boolean shadow, boolean isSelected) {
		Drawable drawable = bus;
		if (shadow == false && hasHeading())
		{
			//to make life easier we won't draw shadows except for the bus
			//the tooltip has some weird error where the shadow draws a little left and up from where it should draw
			
			//the constructor should ignore the arrow and tooltip if these arguments are null
			drawable = new BusDrawable(bus, getHeading(), arrow);
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

	@Override
	public boolean isFavorite() {
		return false;
	}
}

