package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.math.Geometry;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;

import java.util.Collection;

/**
 * This class stores information about the bus. This information is mostly taken
 * from the feed
 */
public class BusLocation implements Location {
	/**
	 * Current latitude of bus, in radians
	 */
	public final float latitude;
	/**
	 * Current longitude of bus, in radians
	 */
	public final float longitude;

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
	 * When the feed says the information was last updated.
	 */
	protected final long lastFeedUpdateInMillis;

    protected final String headsign;

	/**
	 * What is the heading mentioned for the bus?
	 */
	private Optional<Integer> heading;

	private SimplePredictionView predictionView = SimplePredictionView.empty();
	
	private ImmutableCollection<Alert> snippetAlerts = ImmutableList.of();
	
	private static final int LOCATIONTYPE = 1;

	public BusLocation(float latitude, float longitude, String id,
			long lastFeedUpdateInMillis, Optional<Integer> heading,
			String routeName, String headsign) {
		this.latitude = (float) (latitude * Geometry.degreesToRadians);
		this.longitude = (float) (longitude * Geometry.degreesToRadians);
		this.latitudeAsDegrees = latitude;
		this.longitudeAsDegrees = longitude;
		this.busId = id;
		this.lastFeedUpdateInMillis = lastFeedUpdateInMillis;
        if (heading == null) {
            throw new RuntimeException("heading must not be null");
        }
		this.heading = heading;
		this.routeName = routeName;
        this.headsign = headsign;
	}

	public boolean hasHeading() {
		return heading.isPresent();
	}

	public int getHeading() {
		if (heading.isPresent()) {
			return heading.get();
		} else {
            return -1;
		}
	}

	@Override
	public float distanceFrom(double lat2, double lon2) {
		return Geometry.computeCompareDistance(latitude, longitude, lat2, lon2);
	}

	@Override
	public double distanceFromInMiles(double centerLatAsRadians,
			double centerLonAsRadians) {
		return Geometry.computeDistanceInMiles(latitude, longitude, centerLatAsRadians, centerLonAsRadians);
	}

	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig,
			Location location, RouteTitles routeKeysToTitles, Locations locations) {
		BusLocation busLocation = (BusLocation) location;

		PredictionView oldPredictionView = predictionView;
		String snippet = oldPredictionView.getSnippet() + "<br /><br />" +
				busLocation.makeSnippet();

		String snippetTitle;
		snippetTitle = oldPredictionView.getSnippetTitle() + ", " + headsign;

		//TODO: support alerts on multiple routes at once
		predictionView = new SimplePredictionView(snippet, snippetTitle, snippetAlerts);
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig routeConfig,
			RouteTitles routeKeysToTitles, Locations locations) {
		String snippet = makeSnippet();
		String snippetTitle = makeTitle(routeKeysToTitles);
		ITransitSystem transitSystem = locations.getTransitSystem();
		IAlerts alerts = transitSystem.getAlerts();
		snippetAlerts = getAlerts(alerts);
		
		predictionView = new SimplePredictionView(snippet, snippetTitle, snippetAlerts);
	}

	protected String getBetaWarning()
	{
		return "";
	}
	
	private String makeSnippet() {
		String snippet = getBetaWarning();
		snippet += getBusNumberMessage();

		int secondsAgo = (int) (System.currentTimeMillis() - lastFeedUpdateInMillis) / 1000; 
		snippet += "Last update: " + secondsAgo	+ " seconds ago";
		if (heading.isPresent()) {
			snippet += "<br />Heading: " + heading.get() + " deg ("
					+ convertHeadingToCardinal(heading.get()) + ")";
		}

		return snippet;
	}

	protected String getBusNumberMessage()
	{
		return "Bus number: " + busId + "<br />";
	}

	private String makeTitle(RouteTitles routeTitles) {
		String title = "";
		title += "Route ";
        String routeTitle = routeTitles.getTitle(routeName);
		if (routeTitle == null) {
			title += "not mentioned";
		} else {
			title += routeTitle;
		}

		title += "<br/>" + headsign;

		return title;
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
	public Favorite isFavorite() {
		return Favorite.IsNotFavorite;
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
	public Schema.Routes.SourceId getTransitSourceType() {
		return Schema.Routes.SourceId.Bus;
	}

    @Override
    public boolean isUpdated() {
        // this is only relevant for stops
        return false;
    }

    @Override
    public boolean needsUpdating() {
        return true;
    }

    protected ImmutableCollection<Alert> getAlerts(IAlerts alerts) {
		return alerts.getAlertsByRoute(routeName, getTransitSourceType());
	}

    @Override
    public LocationType getLocationType() {
        return LocationType.Vehicle;
    }

    @Override
    public Collection<String> getRoutes() {
        return ImmutableList.of(routeName);
    }
}
