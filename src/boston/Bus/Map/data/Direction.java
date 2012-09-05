package boston.Bus.Map.data;

import java.util.ArrayList;

public class Direction {
	private final String name;
	private final String title;
	private final String route;
	private final boolean useForUI;
	private DirectionStops directionStops;
	
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
	
	public boolean containsStop(String stopTag) {
		if (directionStops == null) {
			throw new RuntimeException("Expected directionStops to exist");
		}
		return directionStops.containsStopTag(stopTag);
	}

	public void addStopTag(String stopTag) {
		if (directionStops == null) {
			directionStops = new DirectionStops();
		}
		directionStops.addStopTag(stopTag);
	}

	public ArrayList<String> getStopTags() {
		if (directionStops == null) {
			throw new RuntimeException("Expected directionStops to exist");
		}
		return directionStops.getStopTags();
	}
}
