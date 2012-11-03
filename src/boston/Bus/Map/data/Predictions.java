package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import boston.Bus.Map.annotations.KeepSorted;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
	private final SortedSet<Prediction> predictions = Sets.newTreeSet();
	@IsGuardedBy("modificationLock")
	private final Set<Alert> alerts = Sets.newTreeSet();
	
	private final Object modificationLock = new Object();
	
	public void makeSnippetAndTitle(RouteConfig routeConfig,
			RouteTitles routeKeysToTitles, Context context,
			RouteSet routes, StopLocation stop, Set<Alert> alerts)
	{
		synchronized (modificationLock) {
			this.routes.clear();
			this.routes.addAll(routes.getRoutes());
			
			this.alerts.clear();
			this.alerts.addAll(alerts);
			
			allStops.clear();
			allStops.add(stop);
			
			predictionView = new StopPredictionViewImpl(this.routes, allStops,
					predictions,
					routeConfig, routeKeysToTitles, context, alerts);
		}
	}
	
	

	
	public void addToSnippetAndTitle(RouteConfig routeConfig, StopLocation stopLocation, 
			RouteTitles routeKeysToTitles,
			Context context, String title, RouteSet routes, Set<Alert> alerts)
	{
		synchronized (modificationLock) {
			allStops.add(stopLocation);

			SortedSet<Prediction> allPredictions = Sets.newTreeSet();
			for (StopLocation stop : allStops) {
				if (stop.getPredictions() != null) {
					allPredictions.addAll(stop.getPredictions().predictions);
				}
				
			}
			
			this.routes.addAll(routes.getRoutes());
			
			Set<Alert> newAlerts;
			if (alerts.size() == 0) {
				newAlerts = Sets.newTreeSet(Arrays.asList(predictionView.getAlerts()));
			}
			else
			{
				SortedSet<Alert> dupAlerts = Sets.newTreeSet();
				dupAlerts.addAll(Arrays.asList(predictionView.getAlerts()));
				dupAlerts.addAll(alerts);
				newAlerts = dupAlerts;
			}
			
			predictionView = new StopPredictionViewImpl(this.routes, allStops,
					allPredictions,
					routeConfig,
					routeKeysToTitles, context, newAlerts);
		}

	}

	/**
	 * Clear all predictions for a single route
	 * @param routeName
	 */
	public void clearPredictions(String currentRouteName)
	{
		synchronized (modificationLock) {
			ArrayList<Prediction> newPredictions = new ArrayList<Prediction>();

			for (Prediction prediction : predictions)
			{
				if (prediction.getRouteName().equals(currentRouteName) == false)
				{
					newPredictions.add(prediction);
				}
			}
			predictions.clear();
			predictions.addAll(newPredictions);
		}
		
	}

	public void addPredictionIfNotExists(Prediction prediction)
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
}
