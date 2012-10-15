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
	private final String[] routes;
	private final String stops;
	private final String snippet;
	private final Prediction[] predictions;
	private final Alert[] alerts;
	
	/**
	 * Note that this makes defensive copies of all containers. It doesn't use ImmutableList
	 * because Parcelables use arrays when transferring data
	 * @param routes
	 * @param stops
	 * @param predictions. This should be sorted
	 * @param ifOnlyOneRoute
	 * @param routeKeysToTitles
	 * @param context
	 * @param alerts
	 */
	public StopPredictionViewImpl(SortedSet<String> routes, Collection<StopLocation> stops,
			SortedSet<Prediction> predictions, RouteConfig ifOnlyOneRoute,
			RouteTitles routeKeysToTitles, Context context, Set<Alert> alerts) {
		Set<String> titles = Sets.newTreeSet(Collections2.transform(stops, StopLocation.getStopTitleFunction));
		

		boolean isBeta = false;
		for (StopLocation stop : stops) {
			if (stop.isBeta()) {
				isBeta = true;
				break;
			}
		}
		
		snippetTitle = makeSnippetTitle(titles);
		this.routes = routes.toArray(nullStrings);
		this.titles = titles.toArray(nullStrings);
		this.alerts = alerts.toArray(nullAlerts);
		StringBuilder ret = new StringBuilder();
		if (isBeta) {
			ret.append("<font color='red' size='1'>Commuter rail predictions are experimental</font><br />");
		}
		makeSnippet(ifOnlyOneRoute, predictions, routeKeysToTitles, context, ret);
		
		snippet = ret.toString();
		
		
		this.stops = Joiner.on(", ").join(Collections2.transform(stops, StopLocation.getStopTagFunction));
		
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
			RouteTitles routeKeysToTitles, Context context, StringBuilder ret)
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

			prediction.makeSnippet(routeKeysToTitles, context, ret);

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
	public String[] getRoutes() {
		return routes;
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
