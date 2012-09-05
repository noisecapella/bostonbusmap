package boston.Bus.Map.data;

import java.util.ArrayList;

/**
 * Temporary class used in populating database
 * @author schneg
 *
 */
public class DirectionWithStopTags extends Direction {
	private final ArrayList<String> stopTags = new ArrayList<String>();
	
	public DirectionWithStopTags(String name, String title, String route,
			boolean useForUI) {
		super(name, title, route, useForUI);
	}

	public void addStopTag(String tag) {
		stopTags.add(tag);
	}

}
