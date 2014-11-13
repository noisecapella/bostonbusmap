package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


import boston.Bus.Map.database.Schema;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.StringUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * This class stores information about the bus. This information is mostly taken
 * from the feed
 */
public class BusLocation implements Location {
	/**
	 * Current latitude of bus, in degrees
	 */
	public final float latitudeAsDegrees;
	/**
	 * Current longitude of bus, in degrees
	 */
	public final float longitudeAsDegrees;

	/**
	 * Unique id for a vehicle. Never null
	 */
	public final String busId;

	protected final String routeName;

	/**
	 * Time of last refresh of this bus object
	 */
	public final long lastFeedUpdateInMillis;

	/**
	 * What is the heading mentioned for the bus?
	 */
	private String heading;

	/**
	 * Does the bus behave predictably?
	 */
	public final boolean predictable;

	private final String directionOrTag;
    private final boolean isDirTag;

	private SimplePredictionView predictionView = SimplePredictionView.empty();
	
	private ImmutableCollection<Alert> snippetAlerts = ImmutableList.of();
	
	private static final int LOCATIONTYPE = 1;
	public static final int NO_HEADING = -1;

	public BusLocation(float latitude, float longitude, String id,
			long lastFeedUpdateInMillis, String heading, boolean predictable,
			String directionOrTag, boolean isDirTag,
			String routeName) {
		this.latitudeAsDegrees = latitude;
		this.longitudeAsDegrees = longitude;
		this.busId = id;
		this.lastFeedUpdateInMillis = lastFeedUpdateInMillis;
		this.heading = heading;
		this.predictable = predictable;
        this.directionOrTag = directionOrTag;
        this.isDirTag = isDirTag;
		this.routeName = routeName;
	}

	public boolean hasHeading() {
		if (predictable && heading != null) {
			return (getHeading() >= 0);
		} else {
			return false;
		}
	}

	public int getHeading() {
		if (predictable && heading != null) {
			return Integer.parseInt(heading);
		} else {
            return 0;
		}
	}

	@Override
	public float distanceFrom(double lat2, double lon2) {
        double latitude = latitudeAsDegrees * Geometry.degreesToRadians;
        double longitude = longitudeAsDegrees * Geometry.degreesToRadians;
		return Geometry.computeCompareDistance(latitude, longitude, lat2, lon2);
	}

	@Override
	public float distanceFromInMiles(double centerLatAsRadians,
			double centerLonAsRadians) {
        double latitude = latitudeAsDegrees * Geometry.degreesToRadians;
        double longitude = longitudeAsDegrees * Geometry.degreesToRadians;
		return Geometry.computeDistanceInMiles((float)latitude, (float)longitude, centerLatAsRadians, centerLonAsRadians);
	}

	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig,
			Location location, RouteTitles routeKeysToTitles, Locations locations, Context context) {
		BusLocation busLocation = (BusLocation) location;

		PredictionView oldPredictionView = predictionView;
		String snippet = oldPredictionView.getSnippet() + "<br />" +
				busLocation.makeSnippet(routeConfig);

		String snippetTitle;
		if (busLocation.predictable) {
			snippetTitle = oldPredictionView.getSnippetTitle() + makeDirection(busLocation.directionOrTag, busLocation.isDirTag, locations.getDirections());
		}
		else
		{
			snippetTitle = oldPredictionView.getSnippetTitle();
		}

		//TODO: support alerts on multiple routes at once
		predictionView = new SimplePredictionView(snippet, snippetTitle, snippetAlerts);
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig routeConfig,
			RouteTitles routeKeysToTitles, Locations locations, Context context) {
		String snippet = makeSnippet(routeConfig);
        String routeTitle = routeConfig.getRouteTitle();
		String snippetTitle = makeTitle(routeTitle, locations.getDirections());
		TransitSystem transitSystem = locations.getTransitSystem();
		IAlerts alerts = transitSystem.getAlerts();
		snippetAlerts = getAlerts(alerts);
		
		predictionView = new SimplePredictionView(snippet, snippetTitle, snippetAlerts);
	}

	protected String getBetaWarning()
	{
		return "";
	}
	
	private String makeSnippet(RouteConfig routeConfig) {
		String snippet = getBetaWarning();
		snippet += getBusNumberMessage();

		int secondsAgo = (int) (System.currentTimeMillis() - lastFeedUpdateInMillis) / 1000; 
		snippet += "Last update: " + secondsAgo	+ " seconds ago";

		if (predictable && heading != null) {
			snippet += "<br />Heading: " + heading + " deg ("
					+ convertHeadingToCardinal(Integer.parseInt(heading)) + ")";
		} else {
			// TODO: how should we say this?
			// title += "\nUnpredictable";
		}

		return snippet;
	}

	protected String getBusNumberMessage()
	{
		return "Bus number: " + busId + "<br />";
	}

	private String makeTitle(String routeTitle, Directions directions) {
		String title = "";
		title += "Route ";
		if (routeTitle == null) {
			title += "not mentioned";
		} else {
			title += routeTitle;
		}

		if (predictable) {
			title += makeDirection(directionOrTag, isDirTag, directions);
		}

		return title;
	}

	private static String makeDirection(String directionOrTag, boolean isDirTag, Directions directions) {
		String ret = "";

		String directionName = directions.getTitleAndName(directionOrTag);
		if (!StringUtil.isEmpty(directionName)) {
			ret += "<br />" + directionName;
		}
        else if (!isDirTag) {
            ret += "<br />" + directionOrTag;
        }

		return ret;
	}

	/**
	 * Converts a heading to a cardinal direction string
	 * 
	 * @param degree
	 *            heading in degrees, where 0 is north and 90 is east
	 * @return a direction (for example "N" for north)
	 */
	private String convertHeadingToCardinal(double degree) {
		// shift degree so all directions line up nicely
		degree += 360.0 / 16; // 22.5
		if (degree >= 360) {
			degree -= 360;
		}

		// get an index into the directions array
		int index = (int) (degree / (360.0 / 8.0));
		if (index < 0 || index >= 8) {
			return "calculation error";
		}

		String[] directions = new String[] { "N", "NE", "E", "SE", "S", "SW",
				"W", "NW" };
		return directions[index];
	}

	@Override
	public int getId() {
		return (busId.hashCode() & 0xffffff) | LOCATIONTYPE << 24;
	}

	public String getBusNumber()
	{
		return busId;
	}
	
	@Override
	public Drawable getDrawable(TransitSystem transitSystem) {
		TransitSource transitSource = transitSystem.getTransitSource(routeName);
		int headingValue = hasHeading() ? getHeading() : NO_HEADING;
		return transitSource.getDrawables().getVehicle(headingValue);
	}
	
	@Override
	public float getLatitudeAsDegrees() {
		// TODO Auto-generated method stub
		return latitudeAsDegrees;
	}

	@Override
	public float getLongitudeAsDegrees() {
		// TODO Auto-generated method stub
		return longitudeAsDegrees;
	}

	@Override
	public boolean isFavorite() {
		return false;
	}

	/**
	 * The route name
	 * 
	 * @return
	 */
	public String getRouteId() {
		return routeName;
	}

	@Override
	public boolean containsId(int selectedBusId) {
		return selectedBusId == getId();
	}

	@Override
	public PredictionView getPredictionView() {
		return predictionView;
	}

	@Override
	public boolean hasMoreInfo() {
		return false;
	}

	@Override
	public boolean hasFavorite() {
		return false;
	}

	@Override
	public boolean hasReportProblem() {
		return true;
	}

	@Override
	public boolean isIntersection() {
		return false;
	}
	
	@Override
	public int getTransitSourceType() {
		return Schema.Routes.enumagencyidBus;
	}
	
	protected ImmutableCollection<Alert> getAlerts(IAlerts alerts) {
		return alerts.getAlertsByRoute(routeName, getTransitSourceType());
	}
}
