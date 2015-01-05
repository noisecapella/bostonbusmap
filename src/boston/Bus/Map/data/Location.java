package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Collection;

import boston.Bus.Map.transit.ITransitSystem;
import boston.Bus.Map.transit.TransitSystem;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


import android.content.Context;
import android.graphics.drawable.Drawable;

public interface Location {

	/**
	 * Some unique value for the location
	 * @return
	 */
	int getId();

    LocationType getLocationType();

	boolean hasHeading();

	int getHeading();

	float getLatitudeAsDegrees();
	
	float getLongitudeAsDegrees();

	/**
	 * @return compare distance, not necessarily any particular unit
	 */
	float distanceFrom(double centerLatitudeAsRadians, double centerLongitudeAsRadians);

	float distanceFromInMiles(double centerLatAsRadians, double centerLonAsRadians);
	
	boolean isFavorite();

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

	/**
	 * Does this location match the given id?
	 * @param selectedBusId
	 * @return
	 */
	boolean containsId(int selectedBusId);
	
	PredictionView getPredictionView();

	boolean hasMoreInfo();
	
	boolean hasFavorite();

	boolean hasReportProblem();

	boolean isIntersection();
	
	/**
	 * TODO: one day this might support stops on multiple transit sources
	 * @return
	 */
	int getTransitSourceType();

    boolean isUpdated();
}
