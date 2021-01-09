package com.schneeloch.bostonbusmap_library.data;

import java.util.Collection;
import java.util.Collections;

public class Direction {
	private final String name;
	private final String title;
	private final String route;
	private final boolean useForUI;
	
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

	public Collection<String> getStopTags() {
		return Collections.emptyList();
	}
}
