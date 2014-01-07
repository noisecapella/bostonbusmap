package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import android.content.Context;

/**
 * Info about predictions and snippets. 
 * Separate from StopLocation because most StopLocations won't need to instantiate this
 * Modification functions should be thread safe. Only predictionView should be published, because
 * it's immutable
 * @author schneg
 *
 */
public class Predictions
{
	@IsGuardedBy("modificationLock")
	private PredictionView predictionView = StopPredictionViewImpl.empty();
	@IsGuardedBy("modificationLock")
	private final Set<String> routes = Sets.newHashSet();
	/**
	 * A list of all stops that use this predictions list
	 */
	@IsGuardedBy("modificationLock")
	private final List<StopLocation> allStops = Lists.newArrayList(); 
	@IsGuardedBy("modificationLock")
	private final SortedSet<IPrediction> predictions = Sets.newTreeSet();

	private final Object modificationLock = new Object();
	
	public void makeSnippetAndTitle(RouteConfig routeConfig,
			RouteTitles routeKeysToTitles, Context context,
			RouteSet routes, StopLocation stop, ImmutableCollection<Alert> alerts, Locations locations)
	{
		synchronized (modificationLock) {
			this.routes.clear();
			this.routes.addAll(routes.getRoutes());
			
			allStops.clear();
			allStops.add(stop);

			Set<Alert> alertSet = Sets.newTreeSet(alerts);
			ImmutableList<Alert> alertImmutableSet = ImmutableList.copyOf(alertSet);

			predictionView = new StopPredictionViewImpl(this.routes, allStops,
					predictions,
					routeConfig, routeKeysToTitles, context, alertImmutableSet, locations);
		}
	}
	
	

	
	public void addToSnippetAndTitle(RouteConfig routeConfig, StopLocation stopLocation, 
			RouteTitles routeKeysToTitles,
			Context context, RouteSet routes, ImmutableCollection<Alert> alerts, Locations locations)
	{
		synchronized (modificationLock) {
			allStops.add(stopLocation);

			SortedSet<IPrediction> allPredictions = Sets.newTreeSet();
			for (StopLocation stop : allStops) {
				if (stop.getPredictions() != null) {
					allPredictions.addAll(stop.getPredictions().predictions);
				}
				
			}
			
			this.routes.addAll(routes.getRoutes());
			
			Set<Alert> newAlerts;
			if (alerts.size() == 0) {
				newAlerts = Sets.newTreeSet(predictionView.getAlerts());
			}
			else
			{
				SortedSet<Alert> dupAlerts = Sets.newTreeSet();
				dupAlerts.addAll(predictionView.getAlerts());
				dupAlerts.addAll(alerts);
				newAlerts = dupAlerts;
			}
			
			ImmutableSet.Builder<Alert> immutableNewAlerts = ImmutableSet.builder();
			immutableNewAlerts.addAll(newAlerts);
			
			
			predictionView = new StopPredictionViewImpl(this.routes, allStops,
					allPredictions,
					routeConfig,
					routeKeysToTitles, context, immutableNewAlerts.build(),
					locations);
		}

	}

	/**
	 * Clear all predictions for a single route
	 * @param currentRouteName
	 */
	public void clearPredictions(String currentRouteName)
	{
		synchronized (modificationLock) {
			if (currentRouteName != null) {
				ArrayList<IPrediction> newPredictions = Lists.newArrayList();

				for (IPrediction prediction : predictions)
				{
					if (prediction.getRouteName().equals(currentRouteName) == false)
					{
						newPredictions.add(prediction);
					}
				}
				predictions.clear();
				predictions.addAll(newPredictions);
			}
			else
			{
				predictions.clear();
			}
		}
		
	}

	public void addPredictionIfNotExists(IPrediction prediction)
	{
		synchronized (modificationLock) {
			if (predictions.contains(prediction) == false)
			{
				predictions.add(prediction);
			}
		}
	}

	public boolean containsId(int selectedBusId)
	{
		synchronized (modificationLock) {
			for (StopLocation stop : allStops)
			{
				if (stop.getId() == selectedBusId)
				{
					return true;
				}
			}
			
		}
		return false;
	}

	public PredictionView getPredictionView() {
		synchronized (modificationLock) {
			// in case predictionView is still being constructed
			return predictionView;
		}
	}

	/**
	 * Returns a defensive copy of predictions list
	 */
	public List<IPrediction> getPredictions() {
		return ImmutableList.copyOf(predictions);
	}
}
