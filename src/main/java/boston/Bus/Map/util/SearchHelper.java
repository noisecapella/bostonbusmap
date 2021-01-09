package boston.Bus.Map.util;

import android.util.Log;
import android.widget.Toast;

import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;

import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.main.Main;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;

public class SearchHelper
{
	private final Main context;
	private final RouteTitles dropdownRouteKeysToTitles;
	private final String query;
	private String suggestionsQuery;
	
	private static final int QUERY_NONE = 0;
	private static final int QUERY_ROUTE = 1;
	private static final int QUERY_STOP = 2;
	
	private int queryType = QUERY_NONE;
	
	private final UpdateArguments arguments;
    private final IDatabaseAgent databaseAgent;
	
	public SearchHelper(Main context, RouteTitles dropdownRouteKeysToTitles,
			UpdateArguments arguments, String query, IDatabaseAgent databaseAgent)
	{
		this.context = context;
		this.dropdownRouteKeysToTitles = dropdownRouteKeysToTitles;
		this.query = query;
		this.arguments = arguments;
        this.databaseAgent = databaseAgent;
	}
	
	/**
	 * Search for query and do whatever actions we do when that happens
	 */
	public void runSearch(Runnable onFinish)
	{
		searchRoutes(onFinish);
	}

	/**
	 * Try a search on the list of routes. If it matches, do that. Else, it's a geocode
	 */
	private void searchRoutes(final Runnable onFinish) {
		String lowercaseQuery = query.toLowerCase();
		String printableQuery = query;
		
		//remove these words from the search
		String[] wordsToRemove = new String[] {"route", "subway", "bus", "stop", "direction"};

		queryType = QUERY_NONE;
		String censoredQuery = query;
		for (String wordToRemove : wordsToRemove)
		{
			boolean itEndsWith = lowercaseQuery.endsWith(" " + wordToRemove);
			boolean itStartsWith = lowercaseQuery.startsWith(wordToRemove + " ");
			boolean wholeWord = lowercaseQuery.equals(wordToRemove);
			boolean middleWord = lowercaseQuery.contains(" " + wordToRemove + " ");
			if (itEndsWith || itStartsWith || wholeWord || middleWord)
			{
				String adjustedCensoredQuery;
				if (wholeWord)
				{
					adjustedCensoredQuery = "";
				}
				else if (itEndsWith)
				{
					adjustedCensoredQuery = censoredQuery.substring(0, censoredQuery.length() - (1 + wordToRemove.length()));
				}
				else if (itStartsWith)
				{
					adjustedCensoredQuery = censoredQuery.substring(1 + wordToRemove.length());
				}
				else
				{
					adjustedCensoredQuery = censoredQuery.replace(" " + wordToRemove + " ", "");
				}
				lowercaseQuery = adjustedCensoredQuery.toLowerCase();
				censoredQuery = adjustedCensoredQuery;
				
				if (wordToRemove.equals("route"))
				{
					queryType = QUERY_ROUTE;
				}
				else if (wordToRemove.equals("stop"))
				{
					queryType = QUERY_STOP;
				}
			}
		}
		
		String queryWithoutSpaces = censoredQuery;
		if (censoredQuery.contains(" "))
		{
			queryWithoutSpaces = censoredQuery.replaceAll(" ", "");
			lowercaseQuery = queryWithoutSpaces.toLowerCase();
		}
		
		//indexingQuery is query which may be slightly altered to match one of the route keys
		String indexingQuery = lowercaseQuery;
		if (indexingQuery.length() >= 2)
		{
			//this is kind of a hack. We need subway lines to start with a capital letter to search for them properly
			indexingQuery = indexingQuery.substring(0, 1).toUpperCase() + queryWithoutSpaces.substring(1);
		}
		
		//NOTE: the next section is currently never run since we set queryContainsStop to true if queryContainsRoute was false
		final String finalLowercaseQuery = lowercaseQuery;
		final String finalIndexingQuery = indexingQuery;
		final String finalPrintableQuery = printableQuery;

		returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery, finalPrintableQuery);
	}

	private void returnResults(Runnable onFinish, String indexingQuery, String lowercaseQuery, String printableQuery) {
		final ITransitSystem transitSystem = arguments.getTransitSystem();
		if (queryType == QUERY_NONE || queryType == QUERY_ROUTE)
		{
			String routeKey = getAsRoute(indexingQuery, lowercaseQuery);

			if (routeKey != null)
			{
				//done!
				context.setNewRoute(routeKey, false, true);
				String routeTitle = dropdownRouteKeysToTitles.getTitle(routeKey);
				suggestionsQuery = "route " + routeTitle;
			}
			else
			{
				Toast.makeText(context, "Route '" + printableQuery + "' could not be found.", Toast.LENGTH_LONG).show();
			}
		}
		else if (queryType == QUERY_STOP)
		{
			// ideally we'd use RoutePool instead of DatabaseHelper, since RoutePool will
			// reuse existing stops if they match. But stop is temporary so it doesn't really matter
			String exactQuery;
			if (printableQuery.startsWith("stop "))
			{
				exactQuery = printableQuery.substring(5);
			}
			else
			{
				exactQuery = printableQuery;
			}

			StopLocation stop = databaseAgent.getStopByTagOrTitle(
					lowercaseQuery, exactQuery, transitSystem);
			if (stop != null)
			{	
				context.setNewStop(stop.getFirstRoute(), stop.getStopTag());
				context.setMode(Selection.Mode.BUS_PREDICTIONS_ALL, true, true);
				suggestionsQuery = "stop " + stop.getTitle();
			}
			else
			{
				//invalid stop id, or we just didn't parse it correctly
				Toast.makeText(context, "Stop '" + printableQuery + "' could not be found.", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			//shouldn't happen
			Log.e("BostonBusMap", "Error: query is neither about stops, routes, or directions");
		}
		
		onFinish.run();
	}

	private String getAsRoute(String indexingQuery, String lowercaseQuery)
	{
		return arguments.getTransitSystem().searchForRoute(indexingQuery, lowercaseQuery);
	}

	public String getSuggestionsQuery()
	{
		return suggestionsQuery;
	}

}
