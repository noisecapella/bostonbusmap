package com.schneeloch.bostonbusmap_library.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import com.schneeloch.bostonbusmap_library.transit.TransitSystem;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

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
	private final IPrediction[] predictions;
	private final ImmutableCollection<Alert> alerts;

	/**
	 * Note that this makes defensive copies of all containers. It doesn't use ImmutableList
	 * because Parcelables use arrays when transferring data
	 * @param stops
	 * @param ifOnlyOneRoute
	 * @param routeKeysToTitles
	 * @param alerts
	 */
	public StopPredictionViewImpl(Set<String> routeTags, Collection<StopLocation> stops,
			SortedSet<IPrediction> predictions, RouteConfig ifOnlyOneRoute,
			RouteTitles routeKeysToTitles, ImmutableCollection<Alert> alerts,
			Locations locations) {
		Set<String> stopTitles = Sets.newTreeSet();
		SortedSet<String> stopIds = Sets.newTreeSet();
        int allCount = 0;
        int someCount = 0;
        int noneCount = 0;
		for (StopLocation stop : stops) {
			stopTitles.add(stop.getTitle());
			stopIds.add(stop.getStopTag());

            Updated updated = stop.wasEverUpdated(ifOnlyOneRoute);
            if (updated == Updated.All) {
                allCount++;
            }
            else if (updated == Updated.Some) {
                someCount++;
            }
            else if (updated == Updated.None) {
                noneCount++;
            }
		}

        Updated updated;
        if (noneCount > 0 && allCount == 0 && someCount == 0) {
            updated = Updated.None;
        }
        else if (allCount > 0 && noneCount == 0 && someCount == 0) {
            updated = Updated.All;
        }
        else {
            updated = Updated.Some;
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

		makeSnippet(ifOnlyOneRoute, predictions, updated, ret);
		
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
			Collection<IPrediction> predictions,
            Updated hasUpdated,
			StringBuilder ret)
	{
		if (predictions == null || predictions.isEmpty()) {
            if (hasUpdated == Updated.All) {
                ret.append("No predictions for this stop");
            }
            else if (hasUpdated == Updated.None) {
                ret.append("No information received yet for this stop");
            }
            return;
		}

		if (hasUpdated == Updated.Some) {
			ret.append("Some information not received yet for this stop<br />");
		}

		final int max = 3;
		int count = 0;

		for (IPrediction prediction : predictions)
		{
			if (routeConfig != null && routeConfig.getRouteName().equals(prediction.getRouteName()) == false)
			{
				continue;
			}

			if (prediction.isInvalid())
			{
				continue;
			}

			if (count != 0)
			{
				ret.append("<br />");
			}

			ret.append("<br />");

			prediction.makeSnippet(ret, TransitSystem.showRunNumber());

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
	public IPrediction[] getPredictions() {
		return predictions;
	}
	
	@Override
	public ImmutableCollection<Alert> getAlerts() {
		return alerts;
	}
}
