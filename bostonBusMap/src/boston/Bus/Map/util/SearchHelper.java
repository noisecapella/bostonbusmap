package boston.Bus.Map.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.android.maps.MapView;
import com.schneeloch.latransit.main.Main;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.transit.MBTABusTransitSource;

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
	
	public SearchHelper(Main context, String[] dropdownRoutes, HashMap<String, String> dropdownRouteKeysToTitles,
			MapView mapView, String query, DatabaseHelper databaseHelper)
	{
		this.context = context;
		this.dropdownRoutes = dropdownRoutes;
		this.dropdownRouteKeysToTitles = dropdownRouteKeysToTitles;
		this.query = query;
		this.databaseHelper = databaseHelper;
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
			if (lowercaseQuery.contains(wordToRemove))
			{
				censoredQuery = censoredQuery.replaceAll(wordToRemove, "");
				lowercaseQuery = censoredQuery.toLowerCase();
				
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
		if (queryContainsRoute == false && queryContainsStop == false)
		{
			//route or stop? maybe both? if both, pop up a choice to the user
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Is this a stop or route?");
			builder.setItems(new String[] {"Route " + indexingQuery, "Stop " + indexingQuery},
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0)
					{
						queryContainsRoute = true;
					}
					else
					{
						queryContainsStop = true;
					}
					
					returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery, finalPrintableQuery);
				}
			});
			builder.show();
		}
		else
		{
			returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery, finalPrintableQuery);
		}
	}

	private void returnResults(Runnable onFinish, String indexingQuery, String lowercaseQuery, String printableQuery) {
		if (queryContainsRoute)
		{
			if (printableQuery.startsWith("route ") == false)
			{
				suggestionsQuery = "route " + printableQuery;
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
			ArrayList<String> routesForStop = databaseHelper.isStop(indexingQuery);
			if (routesForStop.size() > 0)
			{
				context.setNewStop(routesForStop.get(0), lowercaseQuery, false);
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
		//TODO: don't hard code this
		if ("sl1".equals(lowercaseQuery) || 
				"sl2".equals(lowercaseQuery) ||
				"sl".equals(lowercaseQuery) ||
				"sl4".equals(lowercaseQuery) ||
				"sl5".equals(lowercaseQuery))
		{
			lowercaseQuery = "silverline" + lowercaseQuery;
		}
		else if (lowercaseQuery.startsWith("silver"))
		{
			//ugh, what a hack
			lowercaseQuery = lowercaseQuery.substring(0, 6) + "line" + lowercaseQuery.substring(6);
		}
		
		int position = Arrays.asList(dropdownRoutes).indexOf(indexingQuery);

		if (position != -1)
		{
			return position;
		}
		else
		{
			//try the titles
			for (int i = 0; i < dropdownRoutes.length; i++)
			{
				String title = dropdownRouteKeysToTitles.get(dropdownRoutes[i]);
				if (title != null)
				{
					String titleWithoutSpaces = title.toLowerCase().replaceAll(" ", "");
					if (titleWithoutSpaces.equals(lowercaseQuery))
					{
						return i;
					}
				}
			}
			
			//no match
			return -1;
		}
	}

	public String getSuggestionsQuery()
	{
		return suggestionsQuery;
	}
}
