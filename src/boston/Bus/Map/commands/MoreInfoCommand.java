package boston.Bus.Map.commands;

import com.google.android.gms.maps.model.Marker;

import android.content.Context;
import android.content.Intent;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopPredictionView;
import boston.Bus.Map.data.TimeBounds;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.main.UpdateHandler;

public class MoreInfoCommand implements Command {

	private final StopLocation stopLocation;

	public MoreInfoCommand(StopLocation stopLocation) {
		this.stopLocation = stopLocation;
	}
	
	@Override
	public String getDescription() {
		return "More Info";
	}

	@Override
	public void execute(Main main, UpdateHandler handler, Locations locations,
			RouteTitles routeTitles, Marker marker) throws Exception {
		if (routeTitles == null)
		{
			//ignore for now, we can't print route information without it
		}

		Intent intent = new Intent(main, MoreInfo.class);

		StopPredictionView predictionView = (StopPredictionView)stopLocation.getPredictionView();
		Prediction[] predictionArray = predictionView.getPredictions();
		if (predictionArray != null)
		{
			intent.putExtra(MoreInfo.predictionsKey, predictionArray);
		}

		TimeBounds[] bounds = new TimeBounds[predictionView.getRouteTitles().length];
		int i = 0;
		for (String routeTitle : predictionView.getRouteTitles()) {
			String routeKey = routeTitles.getKey(routeTitle);
			bounds[i] = locations.getRoute(routeKey).getTimeBounds();
			i++;
		}
		intent.putExtra(MoreInfo.boundKey, bounds);

		String[] combinedTitles = predictionView.getTitles();
		intent.putExtra(MoreInfo.titleKey, combinedTitles);

		String[] combinedRoutes = predictionView.getRouteTitles();
		intent.putExtra(MoreInfo.routeTitlesKey, combinedRoutes);

		String combinedStops = predictionView.getStops();
		intent.putExtra(MoreInfo.stopsKey, combinedStops);

		intent.putExtra(MoreInfo.stopIsBetaKey, stopLocation.isBeta());

		main.startActivity(intent);
		
	}
	
}
