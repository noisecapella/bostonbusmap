package boston.Bus.Map.commands;

import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

public interface Command {
	String getDescription();
	void execute(Main main, UpdateHandler handler, 
			Locations locations, RouteTitles routeTitles) throws Exception;
}
