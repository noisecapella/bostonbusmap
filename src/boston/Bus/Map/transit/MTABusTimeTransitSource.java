package boston.Bus.Map.transit;

import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;

public class MTABusTimeTransitSource extends SIRITransitSource {

	public MTABusTimeTransitSource(TransitSystem transitSystem,
			TransitDrawables drawables,
			TransitSourceTitles routeTitles, RouteTitles allRouteTitles) {
		super(transitSystem, drawables, "mta", routeTitles, allRouteTitles);
	}

}
