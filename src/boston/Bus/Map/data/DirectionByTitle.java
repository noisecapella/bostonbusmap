package boston.Bus.Map.data;

import java.util.HashSet;

/**
 * A temporary data structure to handle searching directions efficiently
 * @author schneg
 *
 */
public class DirectionByTitle {
	private final MyHashMap<String, Direction> directionsToUpdate;
	private final HashSet<String> stopTagsAffected = new HashSet<String>();
	private final String title;
	
	public DirectionByTitle(MyHashMap<String, Direction> directionsToUpdate) {
		this.directionsToUpdate = directionsToUpdate;
		
		for (Direction direction : directionsToUpdate.values()) {
			for (String stopTag : direction.getStopTags()) {
				stopTagsAffected.add(stopTag);
			}
		}
		
		title = directionsToUpdate.values().toArray(new Direction[0])[0].getTitle();
	}

	public boolean containsDirTag(String dirTag) {
		return directionsToUpdate.containsKey(dirTag);
	}
	
	public boolean containsStopTag(String stopTag) {
		return stopTagsAffected.contains(stopTag);
	}

	public boolean isEmpty() {
		return directionsToUpdate.size() == 0;
	}

	public String getTitle() {
		return title;
	}
}
