package boston.Bus.Map.data;

import java.util.ArrayList;

public class Direction {
	private final String name;
	private final String title;
	private final String route;
	private final boolean useForUI;
	private final ArrayList<String> stopTags = new ArrayList<String>();
	
	public Direction(String name, String title, String route, boolean useForUI) {
		this.name = name;
		this.title = title;
		this.route = route;
		this.useForUI = useForUI;
	}
	
	public String getName() {
		return name;
	}
	public String getTitle() {
		return title;
	}
	public String getRoute() {
		return route;
	}

	public boolean isUseForUI() {
		return useForUI;
	}
	
	public void addStopTag(String tag) {
		stopTags.add(tag);
	}	

	public ArrayList<String> getStopTags() {
		return stopTags;
	}
}
