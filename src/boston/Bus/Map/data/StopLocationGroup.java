package boston.Bus.Map.data;

import java.util.List;

import com.schneeloch.suffixarray.ObjectWithString;

import boston.Bus.Map.transit.TransitSource;

public interface StopLocationGroup extends LocationGroup, ObjectWithString
{
	List<String> getAllTitles();
	String getFirstTitle();

	/**
	 * Use only when performance doesn't matter
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
	void clearRecentlyUpdated();
	String getFirstStopTag();
	void clearPredictions(String route);
	void addPrediction(Prediction prediction);
	void addPrediction(int minutes, long epochTime, String vehicleId,
			String dirTag, RouteConfig currentRoute, Directions directions,
			boolean affectedByLayover, boolean isDelayed, int nullLateness);
	
}
