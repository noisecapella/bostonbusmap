package com.schneeloch.bostonbusmap_library.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.schneeloch.bostonbusmap_library.annotations.KeepSorted;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.math.Geometry;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.util.Constants;

public class StopLocation implements Location
{
	private final float latitudeAsDegrees;
	private final float longitudeAsDegrees;
	
	private final String tag;
	
	private final String title;
	
	private Predictions predictions;
	
	private Favorite isFavorite;
	protected boolean recentlyUpdated;
    /**
     * If route is in here, the stop has been updated for that route
     */
    protected ImmutableList<String> everUpdated = ImmutableList.of();

    private final Optional<String> parent;

	/**
	 * A set of routes the stop belongs to
	 */
	@KeepSorted
	@IsGuardedBy("this")
	private final RouteSet routes = new RouteSet();

	private static final int LOCATIONTYPE = 3;

    protected StopLocation(Builder builder)
	{
		this.latitudeAsDegrees = builder.latitudeAsDegrees;
		this.longitudeAsDegrees = builder.longitudeAsDegrees;
		this.tag = builder.tag;
		this.title = builder.title;
        this.parent = builder.parent;
	}

    public Updated wasEverUpdated(RouteConfig routeConfig) {
        if (routeConfig != null) {
            return everUpdated.contains(routeConfig.getRouteName()) ? Updated.All : Updated.None;
        }
        else {
            int updatedCount = 0;
            for (String route : routes.getRoutes()) {
                if (everUpdated.contains(route)) {
                    updatedCount++;
                }
            }
            if (updatedCount == routes.getRoutes().size()) {
                return Updated.All;
            }
            else if (updatedCount == 0) {
                return Updated.None;
            }
            else {
                return Updated.Some;
            }
        }
    }

    public static class Builder {
		private final float latitudeAsDegrees;
		private final float longitudeAsDegrees;
		private final String tag;
		private final String title;
        private final Optional<String> parent;

        public Builder(float latitudeAsDegrees, float longitudeAsDegrees,
			String tag, String title, Optional<String> parent) {
			this.latitudeAsDegrees = latitudeAsDegrees;
			this.longitudeAsDegrees = longitudeAsDegrees;
			this.tag = tag;
			this.title = title;
            this.parent = parent;
		}
		
		public StopLocation build() {
			return new StopLocation(this);
		}
	}
	
	@Override
	public float distanceFrom(double centerLatitude, double centerLongitude)
	{
        float latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
        float longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		return Geometry.computeCompareDistance(latitude, longitude, centerLatitude, centerLongitude);
	}

	public double distanceFromInMiles(double latitudeAsRads,
			double longitudeAsRads) {
        float latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
        float longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		return Geometry.computeDistanceInMiles(latitude, longitude, latitudeAsRads, longitudeAsRads);
	}

	public void clearRecentlyUpdated()
	{
		recentlyUpdated = false;
	}
	
	@Override
	public int getHeading() {
		return 0;
	}

	@Override
	public int getId() {
		return (tag.hashCode() & 0xffffff) | LOCATIONTYPE << 24;
	}

	@Override
	public float getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public float getLongitudeAsDegrees() {
		return longitudeAsDegrees;
	}

	@Override
	public boolean hasHeading() {
		return false;
	}

	public Predictions getPredictions()
	{
		return predictions;
	}
	
	@Override
	public void makeSnippetAndTitle(RouteConfig routeConfig, RouteTitles routeKeysToTitles, 
			Locations locations)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}

        ITransitSystem transitSystem = locations.getTransitSystem();
		IAlerts alertsObj = transitSystem.getAlerts();
		ImmutableCollection<Alert> alerts = alertsObj.getAlertsByRouteSetAndStop(
				routes.getRoutes(), tag, getTransitSourceType());
		
		predictions.makeSnippetAndTitle(routeConfig, routeKeysToTitles, routes, this, alerts, locations);
	}
	
	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig, Location location, RouteTitles routeKeysToTitles,
			Locations locations)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		StopLocation stopLocation = (StopLocation)location;
        ITransitSystem transitSystem = locations.getTransitSystem();
		IAlerts alertsObj = transitSystem.getAlerts();
		
		ImmutableCollection<Alert> newAlerts = alertsObj.getAlertsByRouteSetAndStop(
				stopLocation.getRoutes(), stopLocation.getStopTag(), 
				stopLocation.getTransitSourceType());
		
		predictions.addToSnippetAndTitle(routeConfig, stopLocation,
                routeKeysToTitles, stopLocation.routes, newAlerts, locations);
	}
	
	public String getStopTag()
	{
		return tag;
	}

	public void clearPredictions(RouteConfig routeConfig)
	{
		if (predictions != null)
		{
			predictions.clearPredictions(routeConfig != null ? routeConfig.getRouteName() : null);
		}
		
		recentlyUpdated = true;
        if (routeConfig != null) {
            if (!everUpdated.contains(routeConfig.getRouteName())) {
                List<String> everUpdatedCopy = Lists.newArrayList(everUpdated);
                // Defensive check since we aren't using synchronize here
                if (!everUpdatedCopy.contains(routeConfig.getRouteName())) {
                    everUpdatedCopy.add(routeConfig.getRouteName());
                }
                everUpdated = ImmutableList.copyOf(everUpdatedCopy);
            }
        }
        else {
            everUpdated = routes.getRoutes();
        }
	}
	
	public void addPrediction(IPrediction prediction)
	{
		if (predictions == null)
		{
			predictions = new Predictions();
		}
		
		predictions.addPredictionIfNotExists(prediction);
	}
	

	public String getTitle()
	{
		return title;
	}

	public void setFavorite(Favorite b)
	{
		isFavorite = b;
	}
	
	@Override
	public Favorite isFavorite() {
		return isFavorite;
	}
	/**
	 * The list of routes that owns the StopLocation. NOTE: this is not in any particular order
	 * @return
	 */
    @Override
	public Collection<String> getRoutes() {
		return routes.getRoutes();
	}

	public String getFirstRoute() {
		return routes.getFirstRoute();
	}
	
	@Override
	public boolean containsId(int selectedBusId) {
		if (getId() == selectedBusId)
		{
			return true;
		}
		else if (predictions != null)
		{
			return predictions.containsId(selectedBusId);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Only list stops once if they share the same location
	 * @param stops
	 * @return
	 */
	public static ImmutableList<StopLocation> consolidateStops(ImmutableList<StopLocation> stops) {
		if (stops.size() < 2)
		{
			return stops;
		}

        ImmutableList.Builder<Location> locationBuilder = ImmutableList.builder();
        locationBuilder.addAll(stops);
		
        ImmutableList<ImmutableList<Location>> groups = Locations.groupLocations(locationBuilder.build(), 1);

        if (groups.size() == 0) {
            return ImmutableList.of();
        }

        ImmutableList<Location> group = groups.get(0);
        ImmutableList.Builder<StopLocation> builder = ImmutableList.builder();

        for (Location location : group) {
            if (location instanceof StopLocation) {
                builder.add((StopLocation)location);
            }
        }

        return builder.build();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", tag).toString();
	}

	/**
	 * Are these predictions experimental?
	 * @return
	 */
	public boolean isBeta()
	{
		return false;
	}

	@Override
	public PredictionView getPredictionView() {
		if (predictions != null) {
			return predictions.getPredictionView();
		}
		else
		{
			return StopPredictionViewImpl.empty();
		}
	}

	public void addRoute(String route) {
		routes.addRoute(route);
	}

	@Override
	public boolean hasMoreInfo() {
		return true;
	}

	@Override
	public boolean hasFavorite() {
		return true;
	}

	@Override
	public boolean hasReportProblem() {
		return true;
	}
	
	@Override
	public boolean isIntersection() {
		return false;
	}

	public boolean hasRoute(String route) {
		return routes.hasRoute(route);
	}
	
	@Override
	public Schema.Routes.SourceId getTransitSourceType() {
		return Schema.Routes.SourceId.Bus;
	}

	public boolean supportsBusPredictionsAllMode() {
		return true;
	}

    @Override
    public LocationType getLocationType() {
        return LocationType.Stop;
    }

    @Override
    public boolean isUpdated() {
        return recentlyUpdated;
    }

    @Override
    public boolean needsUpdating() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StopLocation)) {
            return false;
        }

        StopLocation other = (StopLocation)o;
        return tag.equals(other.tag) &&
                title.equals(other.title) &&
                (int)(latitudeAsDegrees * Constants.E6) == (int)(other.latitudeAsDegrees * Constants.E6) &&
                (int)(longitudeAsDegrees * Constants.E6) == (int)(other.longitudeAsDegrees * Constants.E6) &&
                getTransitSourceType() == other.getTransitSourceType() &&
                parent.equals(other.parent);
    }

    @Override
    public Optional<String> getParent() {
        return parent;
    }
}
