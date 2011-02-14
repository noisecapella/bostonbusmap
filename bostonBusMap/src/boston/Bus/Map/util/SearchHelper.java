package boston.Bus.Map.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.android.maps.MapView;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.main.GeocoderAsyncTask;
import boston.Bus.Map.main.Main;

public class SearchHelper
{
	private final Main context;
	private final String[] dropdownRoutes;
	private final HashMap<String, String> dropdownRouteKeysToTitles;
	private final MapView mapView;
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
		this.mapView = mapView;
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

	private static final int IS_GREEN_LINE = -1;
	private static final int IS_NOTHING = -2;
	private static final int IS_NUMBER = -3;
	
	/**
	 * Try a search on the list of routes. If it matches, do that. Else, it's a geocode
	 * 
	 * @return if >= 0, it's an index to 
	 */
	private void searchRoutes(final Runnable onFinish) {
		String lowercaseQuery = query.toLowerCase();
		
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
		
		if (censoredQuery.contains(" "))
		{
			String queryWithoutSpaces = censoredQuery.replaceAll(" ", "");
			lowercaseQuery = queryWithoutSpaces.toLowerCase();
		}
		
		//indexingQuery is query which may be slightly altered to match one of the route keys
		String indexingQuery = lowercaseQuery;
		if (indexingQuery.length() >= 2)
		{
			//this is kind of a hack. We need subway lines to start with a capital letter to search for them properly
			indexingQuery = indexingQuery.substring(0, 1).toUpperCase() + censoredQuery.substring(1);
		}
		
		if (queryContainsRoute && queryContainsStop)
		{
			//contains both stop and route keyword... nonsensical
			queryContainsRoute = false;
			queryContainsStop = false;
		}

		final String finalLowercaseQuery = lowercaseQuery;
		final String finalIndexingQuery = indexingQuery;
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
					
					returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery);
				}
			});
			builder.show();
		}
		else
		{
			returnResults(onFinish, finalIndexingQuery, finalLowercaseQuery);
		}
	}

	private void returnResults(Runnable onFinish, String indexingQuery, String lowercaseQuery) {
		if (queryContainsRoute)
		{
			suggestionsQuery = "route " + indexingQuery;
			int position = getAsRoute(indexingQuery, lowercaseQuery);

			if (position >= 0)
			{
				//done!
				context.setNewRoute(position);
			}
			else
			{
				try
				{
					int x = Integer.parseInt(lowercaseQuery);
					Toast.makeText(context, "Route number '" + x + "' doesn't exist. Did you mistype it?", Toast.LENGTH_LONG).show();
				}
				catch (NumberFormatException e)
				{
					//no need to log this, it's mostly expected. I wish Java had a TryParse method like C# does so we don't have to
					//use try catch for flow control

				}
			}
		}
		else if (queryContainsStop)
		{
			suggestionsQuery = "stop " + indexingQuery;
			ArrayList<String> routesForStop = databaseHelper.isStop(lowercaseQuery);
			if (routesForStop.size() > 0)
			{
				context.setNewStop(routesForStop.get(0), lowercaseQuery);
			}
			else
			{
				//invalid stop id
				Toast.makeText(context, "Stop number '" + indexingQuery + "' doesn't exist. Did you mistype it?", Toast.LENGTH_LONG).show();
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
				if (title != null && title.toLowerCase().equals(lowercaseQuery))
				{
					return i;
				}
			}
			
			//no match
			return -1;
		}
	}

	public String getSuggestionsQuery() {
		return suggestionsQuery;
	}

}
