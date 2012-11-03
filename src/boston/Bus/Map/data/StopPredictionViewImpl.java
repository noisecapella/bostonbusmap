package boston.Bus.Map.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import android.content.Context;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Immutable view of prediction information, for sharing between threads
 * @author schneg
 *
 */
public class StopPredictionViewImpl extends StopPredictionView {
	private final String snippetTitle;
	private final String[] titles;
	private final String[] routeTitles;
	private final String stops;
	private final String snippet;
	private final Prediction[] predictions;
	private final Alert[] alerts;
	
	/**
	 * Note that this makes defensive copies of all containers. It doesn't use ImmutableList
	 * because Parcelables use arrays when transferring data
	 * @param routeTitles
	 * @param stops
	 * @param predictions. This should be sorted
	 * @param ifOnlyOneRoute
	 * @param routeKeysToTitles
	 * @param context
	 * @param alerts
	 */
	public StopPredictionViewImpl(Set<String> routeTags, Collection<StopLocation> stops,
			SortedSet<Prediction> predictions, RouteConfig ifOnlyOneRoute,
			RouteTitles routeKeysToTitles, Context context, Set<Alert> alerts) {
		Set<String> stopTitles = Sets.newTreeSet();
		SortedSet<String> stopIds = Sets.newTreeSet();
		for (StopLocation stop : stops) {
			stopTitles.add(stop.getTitle());
			stopIds.add(stop.getStopTag());
		}

		boolean isBeta = false;
		for (StopLocation stop : stops) {
			if (stop.isBeta()) {
				isBeta = true;
				break;
			}
		}
		
		snippetTitle = makeSnippetTitle(stopTitles);
		SortedSet<String> routeTitles = Sets.newTreeSet();
		for (String tag : routeTags) {
			String title = routeKeysToTitles.getTitle(tag);
			routeTitles.add(title);
		}
		
		this.routeTitles = routeTitles.toArray(nullStrings);
		this.titles = stopTitles.toArray(nullStrings);
		this.alerts = alerts.toArray(nullAlerts);
		StringBuilder ret = new StringBuilder();
		if (isBeta) {
			ret.append("<font color='red' size='1'>Commuter rail predictions are experimental</font><br />");
		}
		makeSnippet(ifOnlyOneRoute, predictions, context, ret);
		
		snippet = ret.toString();
		
		
		this.stops = Joiner.on(", ").join(stopIds);
		
		this.predictions = predictions.toArray(nullPredictions);
	}
	
	private String makeSnippetTitle(Collection<String> titles) {
		StringBuilder ret = new StringBuilder();
		int count = 0;
		for (String title : titles) {
			if (count >= 1) {
				ret.append(", ...");
				break;
			}
			else
			{
				ret.append(title);
			}
			
			count++;
		}
		return ret.toString();
	}

	private static void makeSnippet(RouteConfig routeConfig,
			Collection<Prediction> predictions,
			Context context, StringBuilder ret)
	{
		if (predictions == null || predictions.isEmpty()) {
			return;
		}

		final int max = 3;
		int count = 0;



		for (Prediction prediction : predictions)
		{
			if (routeConfig != null && routeConfig.getRouteName().equals(prediction.getRouteName()) == false)
			{
				continue;
			}

			if (prediction.getMinutes() < 0)
			{
				continue;
			}



			if (count != 0)
			{
				ret.append("<br />");
			}

			ret.append("<br />");

			prediction.makeSnippet(context, ret);

			count++;
			if (count >= max)
			{
				break;
			}
		}
	}
	
	@Override
	public String[] getTitles() {
		return titles;
	}

	@Override
	public String[] getRouteTitles() {
		return routeTitles;
	}

	@Override
	public String getStops() {
		return stops;
	}

	@Override
	public String getSnippet() {
		return snippet;
	}

	@Override
	public String getSnippetTitle() {
		return snippetTitle;
	}

	/**
	 * Do not modify this!
	 */
	@Override
	public Prediction[] getPredictions() {
		return predictions;
	}
	
	@Override
	public Alert[] getAlerts() {
		return alerts;
	}
}
