package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.database.Schema;

import java.util.Collection;

public interface Location {

	/**
	 * Some unique value for the location
	 * @return
	 */
	GroupKey makeGroupKey();

    LocationType getLocationType();

	boolean hasHeading();

	int getHeading();

	float getLatitudeAsDegrees();
	
	float getLongitudeAsDegrees();

	/**
	 * @return compare distance, not necessarily any particular unit
	 */
	float distanceFrom(double centerLatitudeAsRadians, double centerLongitudeAsRadians);

	double distanceFromInMiles(double centerLatAsRadians, double centerLonAsRadians);
	
	Favorite isFavorite();

	/**
	 * Prepare the textbox text and store it in the class
	 * @param selectedRoute show only this route, if not null
	 */
	void makeSnippetAndTitle(RouteConfig selectedRoute, RouteTitles routeKeysToTitles, Locations locations);

	/**
	 * In case two locations share the same space, combine the textbox text in a nice way
	 * @param routeConfig show only this route, if not null
	 * @param location whose textbox info you're adding to this class
	 */
	void addToSnippetAndTitle(RouteConfig routeConfig, Location location, RouteTitles routeKeysToTitles, Locations locations);

	PredictionView getPredictionView();

	boolean hasMoreInfo();
	
	boolean hasFavorite();

	boolean hasReportProblem();

	boolean isIntersection();
	
    boolean isUpdated();

    boolean needsUpdating();

    Collection<String> getRoutes();

    Optional<String> getParent();
}
