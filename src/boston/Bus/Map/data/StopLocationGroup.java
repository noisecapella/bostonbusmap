package boston.Bus.Map.data;

import java.util.List;

public interface StopLocationGroup extends LocationGroup
{
	List<String> getAllTitles();
	String getFirstTitle();

	/**
	 * Use when performance doesn't matter
	 */
	List<StopLocation> getStops();
}
