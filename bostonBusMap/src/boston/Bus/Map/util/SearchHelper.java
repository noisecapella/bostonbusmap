package boston.Bus.Map.util;

import java.util.Arrays;
import java.util.HashMap;

import com.google.android.maps.MapView;

import android.content.Context;
import android.widget.Toast;
import boston.Bus.Map.main.GeocoderAsyncTask;
import boston.Bus.Map.main.Main;

public class SearchHelper
{
	private final Main context;
	private final String[] dropdownRoutes;
	private final HashMap<String, String> dropdownRouteKeysToTitles;
	private final MapView mapView;
	
	public SearchHelper(Main context, String[] dropdownRoutes, HashMap<String, String> dropdownRouteKeysToTitles,
			MapView mapView)
	{
		this.context = context;
		this.dropdownRoutes = dropdownRoutes;
		this.dropdownRouteKeysToTitles = dropdownRouteKeysToTitles;
		this.mapView = mapView;
	}
	
	/**
	 * Search for query and do whatever actions we do when that happens
	 * @param query
	 */
	public void runSearch(String query)
	{
		if (dropdownRoutes == null || dropdownRouteKeysToTitles == null)
		{
			return;
		}

		int routeIndex = searchRoutes(query);
		if (routeIndex == IS_GREEN_LINE)
		{
			//we know what this is, don't try to search for it
		}
		else if (routeIndex == IS_NUMBER)
		{
			//user probably mistyped a route number
		}
		else if (routeIndex == IS_NOTHING)
		{
			//ok, try geocoding

			GeocoderAsyncTask geocoderAsyncTask = new GeocoderAsyncTask(context, mapView, query);
			geocoderAsyncTask.execute();
		}
		else
		{
			//it's a route!
			context.setNewRoute(routeIndex);
		}
	}

	private static final int IS_GREEN_LINE = -1;
	private static final int IS_NOTHING = -2;
	private static final int IS_NUMBER = -3;
	
	/**
	 * Try a search on the list of routes. If it matches, do that. Else, it's a geocode
	 * 
	 * @param query
	 * @return
	 */
	private int searchRoutes(String query) {
		String lowercaseQuery = query.toLowerCase();
		
		//remove these words from the search
		String[] wordsToRemove = new String[] {"route", "subway", "bus", "line"};

		for (String wordToRemove : wordsToRemove)
		{
			if (lowercaseQuery.contains(wordToRemove))
			{
				query = query.replaceAll(wordToRemove, "");
				lowercaseQuery = query.toLowerCase();
			}
		}
		
		if (query.contains(" "))
		{
			query = query.replaceAll(" ", "");
			lowercaseQuery = query.toLowerCase();
		}
		
		//indexingQuery is query which may be slightly altered to match one of the route keys
		String indexingQuery = lowercaseQuery;
		if (indexingQuery.length() >= 2)
		{
			//this is kind of a hack. We need subway lines to start with a capital letter to search for them properly
			indexingQuery = indexingQuery.substring(0, 1).toUpperCase() + query.substring(1);
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
				if (title != null && title.toLowerCase().equals(lowercaseQuery))
				{
					return i;
				}
			}
			
			//else, we don't know what it is
			if (lowercaseQuery.equals("green"))
			{
				Toast.makeText(context, "Sorry, green line information isn't available yet", Toast.LENGTH_LONG).show();
				return IS_GREEN_LINE;
			}
			else
			{
				try
				{
					int x = Integer.parseInt(lowercaseQuery);
					Toast.makeText(context, "Route number '" + x + "' doesn't exist. Did you mistype it?", Toast.LENGTH_LONG).show();
					return IS_NUMBER;
				}
				catch (NumberFormatException e)
				{
					//no need to log this, it's mostly expected. I wish Java had a TryParse method like C# does so we don't have to
					//use try catch for flow control
					
				}
				
				return IS_NOTHING;
			}
		}
	}

}
