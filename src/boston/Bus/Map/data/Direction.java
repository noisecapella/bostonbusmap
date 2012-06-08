package boston.Bus.Map.data;

public class Direction {
	private final String name;
	private final String title;
	private final String route;
	
	public Direction(String name, String title, String route) {
		this.name = name;
		this.title = title;
		this.route = route;
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
}
