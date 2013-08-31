package boston.Bus.Map.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import android.content.Context;
import boston.Bus.Map.util.LogUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
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
	private final TimePrediction[] predictions;
	private final ImmutableCollection<Alert> alerts;
	
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
			SortedSet<TimePrediction> predictions, RouteConfig ifOnlyOneRoute,
			RouteTitles routeKeysToTitles, Context context, ImmutableCollection<Alert> alerts,
			Locations locations) {
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
		SortedSet<String> routeTitles = Sets.newTreeSet(new RouteTitleComparator());
		for (String tag : routeTags) {
			String title = routeKeysToTitles.getTitle(tag);
			routeTitles.add(title);
		}
		
		this.routeTitles = routeTitles.toArray(nullStrings);
		this.titles = stopTitles.toArray(nullStrings);
		this.alerts = alerts;
		StringBuilder ret = new StringBuilder();
		if (isBeta) {
			ret.append("<font color='red' size='1'>Commuter rail predictions are experimental</font><br />");
		}

		try
		{
			SortedSet<String> routeTitlesNotRunning = Sets.newTreeSet();
			for (String routeTag : routeTags) {
				RouteConfig route = locations.getRoute(routeTag);
				if (route.isRouteRunning() == false) {
					routeTitlesNotRunning.add(routeKeysToTitles.getTitle(routeTag));
				}
			}
/*			if (routeTitlesNotRunning.size() == 1) {
				String routeTitle = routeTitlesNotRunning.first();
				ret.append("<font size='1'>Route " + routeTitle + " is not currently running").append("</font><br />");
			}
			else if (routeTitlesNotRunning.size() > 1) {
				String routeTitle = Joiner.on(", ").join(routeTitlesNotRunning);
				ret.append("<font size='1'>Routes " + routeTitle + " are not currently running</font><br />");
			}*/
		}
		catch (IOException e) {
			LogUtil.e(e);
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
			Collection<TimePrediction> predictions,
			Context context, StringBuilder ret)
	{
		if (predictions == null || predictions.isEmpty()) {
			return;
		}

		final int max = 3;
		int count = 0;



		for (TimePrediction prediction : predictions)
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
	public TimePrediction[] getPredictions() {
		return predictions;
	}
	
	@Override
	public ImmutableCollection<Alert> getAlerts() {
		return alerts;
	}
}
