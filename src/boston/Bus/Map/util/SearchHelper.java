package boston.Bus.Map.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.android.maps.MapView;
import boston.Bus.Map.main.Main;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.NextBusTransitSource;
import boston.Bus.Map.transit.TransitSystem;

public class SearchHelper
{
	private final Main context;
	private final String[] dropdownRoutes;
	private final HashMap<String, String> dropdownRouteKeysToTitles;
	private final String query;
	private String suggestionsQuery;
	private final DatabaseHelper databaseHelper;
	
	private boolean queryContainsRoute;
	private boolean queryContainsStop;
	private final TransitSystem transitSystem;
	
	public SearchHelper(Main context, String[] dropdownRoutes, HashMap<String, String> dropdownRouteKeysToTitles,
			MapView mapView, String query, DatabaseHelper databaseHelper, TransitSystem transitSystem)
	{
		this.context = context;
		this.dropdownRoutes = dropdownRoutes;
		this.dropdownRouteKeysToTitles = dropdownRouteKeysToTitles;
		this.query = query;
		this.databaseHelper = databaseHelper;
		this.transitSystem = transitSystem;
	}
	
	/**
	 * Search for query and do whatever actions we do when that happens
	 * @param runnable 
	 * @param query
	 */
	public void runSearch(Runnable onFinish)
	{
		if (dropdownRoutes == null || dropdownRouteKeysToTitles == null)
		{
			return;
		}

		searchRoutes(onFinish);
	}

	/**
	 * Try a search on the list of routes. If it matches, do that. Else, it's a geocode
	 * 
	 * @return if >= 0, it's an index to 
	 */
	private void searchRoutes(final Runnable onFinish) {
		String lowercaseQuery = query.toLowerCase();
		String printableQuery = query;
		
		//remove these words from the search
		String[] wordsToRemove = new String[] {"route", "subway", "bus", "line", "stop"};

		queryContainsRoute = false;
		queryContainsStop = false;
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
					adjustedCensoredQuery = censoredQuery.substring(0, 1 + wordToRemove.length());
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
					queryContainsRoute = true;
				}
				else if (wordToRemove.equals("stop"))
				{
					queryContainsStop = true;
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
		
		if (queryContainsRoute && queryContainsStop)
		{
			//contains both stop and route keyword... nonsensical
			queryContainsRoute = false;
			queryContainsStop = false;
		}

		//NOTE: this hardwires the default to be queryContainsRoute, bypassing the popup menu
		//it seems like a good idea for now so people aren't confused
		if (queryContainsStop == false)
		{
			queryContainsRoute = true;
		}
		
		//NOTE: the next section is currently never run since we set queryContainsStop to true if queryContainsRoute was false
		final String finalLowercaseQuery = lowercaseQuery;
		final String finalIndexingQuery = indexingQuery;
		final String finalPrintableQuery = printableQuery;

		returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery, finalPrintableQuery);
	}

	private void returnResults(Runnable onFinish, String indexingQuery, String lowercaseQuery, String printableQuery) {
		if (queryContainsRoute)
		{
			if (printableQuery.startsWith("route ") == false)
			{
				suggestionsQuery = "route " + printableQuery;
			}
			else
			{
				suggestionsQuery = printableQuery;
			}
			
			int position = getAsRoute(indexingQuery, lowercaseQuery);

			if (position >= 0)
			{
				//done!
				context.setNewRoute(position, false);
			}
			else
			{
				Toast.makeText(context, "Route number '" + printableQuery + "' doesn't exist. Did you mistype it?", Toast.LENGTH_LONG).show();
			}
		}
		else if (queryContainsStop)
		{
			if (printableQuery.startsWith("stop ") == false)
			{
				suggestionsQuery = "stop " + printableQuery;
			}
			else
			{
				suggestionsQuery = printableQuery;
			}
			
			ArrayList<String> routesForStop = databaseHelper.isStop(indexingQuery);
			if (routesForStop.size() > 0)
			{
				context.setNewStop(routesForStop.get(0), indexingQuery, false);
			}
			else
			{
				//invalid stop id
				Toast.makeText(context, "Stop number '" + printableQuery + "' doesn't exist. Did you mistype it?", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			//shouldn't happen
			Log.e("BostonBusMap", "Error: query is neither about stops nor routes");
		}
		
		onFinish.run();
	}

	private int getAsRoute(String indexingQuery, String lowercaseQuery)
	{
		String route = transitSystem.searchForRoute(indexingQuery, lowercaseQuery);
		if (route != null)
		{
			for (int i = 0; i < dropdownRoutes.length; i++)
			{
				String potentialRoute = dropdownRoutes[i];
				if (route.equals(potentialRoute))
				{
					return i;
				}
			}
		}
		return -1;
	}

	public String getSuggestionsQuery()
	{
		return suggestionsQuery;
	}

	public static String naiveSearch(String indexingQuery, String lowercaseQuery, String[] routes,
			HashMap<String, String> routeKeysToTitles)
	{
		int position = Arrays.asList(routes).indexOf(indexingQuery);

		if (position != -1)
		{
			return routes[position];
		}
		else
		{
			//try the titles
			for (int i = 0; i < routes.length; i++)
			{
				String title = routeKeysToTitles.get(routes[i]);
				if (title != null)
				{
					String titleWithoutSpaces = title.toLowerCase().replaceAll(" ", "");
					if (titleWithoutSpaces.equals(lowercaseQuery))
					{
						return routes[i];
					}
				}
			}
			
			//no match
			return null;
		}
	}
}
