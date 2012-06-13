package boston.Bus.Map.data;

import java.util.List;

import boston.Bus.Map.transit.TransitSource;

public interface StopLocationGroup extends LocationGroup
{
	List<String> getAllTitles();
	String getFirstTitle();

	/**
	 * Use when performance doesn't matter
	 */
	List<StopLocation> getStops();
	List<Prediction> getCombinedPredictions();
	List<String> getCombinedTitles();
	String getCombinedStops();
	List<String> getCombinedRoutes();
	/**
	 * Either the transit source, or null if there's a discrepancy
	 * @return
	 */
	TransitSource getTransitSource();
	
}
