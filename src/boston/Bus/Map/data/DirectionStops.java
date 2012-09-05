package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.HashSet;

public class DirectionStops {
	private final ArrayList<String> stopTags = new ArrayList<String>();
	private final HashSet<String> stopTagsLookup = new HashSet<String>();
	
	public void addStopTag(String tag) {
		stopTags.add(tag);
		stopTagsLookup.add(tag);
	}	

	public ArrayList<String> getStopTags() {
		return stopTags;
	}
	
	public boolean containsStopTag(String tag) {
		return stopTagsLookup.contains(tag);
	}
}
