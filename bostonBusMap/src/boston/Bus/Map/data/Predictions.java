package boston.Bus.Map.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import boston.Bus.Map.util.StringUtil;

import android.content.Context;

/**
 * Info about predictions and snippets. Separate from StopLocation because most StopLocations won't need to instantiate this
 * @author schneg
 *
 */
public class Predictions
{
	private String snippetTitle;
	private String snippetStop;
	private String snippetRoutes;
	private String snippetPredictions;
	
	/**
	 * Other stops which are temporarily using the same overlay
	 */
	private ArrayList<StopLocation> sharedSnippetStops;
	
	private ArrayList<Prediction> combinedPredictions;
	private TreeSet<String> combinedRoutes;
	private TreeSet<String> combinedTitles;
	
	private final ArrayList<Prediction> predictions = new ArrayList<Prediction>();
	private ArrayList<Alert> snippetAlerts;

	private static final String[] nullStrings = new String[0];
	private static final Prediction[] nullPredictions = new Prediction[0];
	
	public synchronized void makeSnippetAndTitle(RouteConfig routeConfig,
			HashMap<String, String> routeKeysToTitles, Context context, HashMap<String, String> dirTags, String title, String tag)
	{
		ArrayList<String> routes = new ArrayList<String>();
		routes.addAll(dirTags.keySet());
		Collections.sort(routes);
		
		snippetRoutes = makeSnippetRoutes(routes, routeKeysToTitles);
		snippetTitle = title;
		snippetStop = tag;
		snippetAlerts = routeConfig.getAlerts();
		
		snippetPredictions = makeSnippet(routeConfig, predictions, routeKeysToTitles, context);
		sharedSnippetStops = null;
		
	}
	
	private static String makeSnippet(RouteConfig routeConfig, ArrayList<Prediction> predictions,
			HashMap<String, String> routeKeysToTitles, Context context)
	{
		String ret = "";
		
		if (predictions == null)
		{
			return ret;
		}
		
		synchronized (predictions)
		{
			if (predictions.size() == 0)
			{
				return null;
			}

			boolean anyNulls = false;
			for (Prediction prediction : predictions)
			{
				if (prediction == null)
				{
					anyNulls = true;
					break;
				}
			}
			
			if (anyNulls)
			{
				//argh, this shouldn't happen but one person reported a null ref with a prediction in predictions,
				//so I should handle it. This isn't a bottleneck so it shouldn't matter that I'm cloning the list
				
				ArrayList<Prediction> newPredictions = new ArrayList<Prediction>(predictions.size());
				for (Prediction prediction : predictions)
				{
					if (prediction != null)
					{
						newPredictions.add(prediction);
					}
				}
				predictions = newPredictions;
			}
			
			Collections.sort(predictions);

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
					ret += "<br />";
				}
				
				ret += "<br />" + prediction.makeSnippet(routeKeysToTitles, context);

				count++;
				if (count >= max)
				{
					break;
				}
			}
		}
		return ret;
	}
	

	
	private String makeSnippetRoutes(Collection<String> routes, HashMap<String, String> routeKeysToTitles) {
		String ret = "";
		
		//java doesn't have a join function? seriously?
		synchronized (routes)
		{
			ret = StringUtil.join(routes, ", ");
		}
		
		return ret;
	}

	public synchronized void addToSnippetAndTitle(RouteConfig routeConfig, StopLocation stopLocation, HashMap<String, String> routeKeysToTitles,
			Context context, String title, HashMap<String, String> dirTags)
	{
		if (sharedSnippetStops == null)
		{
			sharedSnippetStops = new ArrayList<StopLocation>();
		}
		
		
		
		sharedSnippetStops.add(stopLocation);
		
		combinedTitles = new TreeSet<String>();
		combinedTitles.add(title);
		
		for (StopLocation s : sharedSnippetStops)
		{
			combinedTitles.add(s.getTitle());
		}
		
		if (combinedTitles.size() > 1)
		{
			snippetTitle = title + ", ...";
		}
		else
		{
			snippetTitle = title;
		}
		
		/*
		 * uncomment to show all titles on front page
		snippetTitle = makeSnippetTitle(combinedTitles);
		*/
		
		snippetStop += ", " + stopLocation.getStopTag();
		
		combinedRoutes = new TreeSet<String>();
		combinedRoutes.addAll(dirTags.keySet());
		for (StopLocation s : sharedSnippetStops)
		{
			combinedRoutes.addAll(s.getRoutes());
		}
		snippetRoutes = makeSnippetRoutes(combinedRoutes, routeKeysToTitles);
		
		combinedPredictions = new ArrayList<Prediction>();
		if (predictions != null)
		{
			combinedPredictions.addAll(predictions);
		}
		for (StopLocation s : sharedSnippetStops)
		{
			Predictions predictions = s.getPredictions();
			if (predictions != null)
			{
				ArrayList<Prediction> predictionsList = predictions.getPredictions();
				combinedPredictions.addAll(predictionsList);
			}
		}
		
		snippetPredictions = makeSnippet(routeConfig, combinedPredictions, routeKeysToTitles, context);
		
	}

	private ArrayList<Prediction> getPredictions()
	{
		return predictions;
	}

	public String getSnippetPredictions()
	{
		return snippetPredictions;
	}

	public String getSnippetTitle()
	{
		return snippetTitle;
	}

	/**
	 * Clear all predictions for a single route
	 * @param routeName
	 */
	public synchronized void clearPredictions(String currentRouteName)
	{
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

	public synchronized void addPredictionIfNotExists(Prediction prediction)
	{
		if (predictions.contains(prediction) == false)
		{
			predictions.add(prediction);
		}
	}

	public synchronized Prediction[] getCombinedPredictions()
	{
		if (combinedPredictions == null)
		{
			return predictions.toArray(nullPredictions);
		}
		else
		{
			return combinedPredictions.toArray(nullPredictions);
		}
	}

	public String getSnippetRoutes()
	{
		return snippetRoutes;
	}

	public synchronized String[] getCombinedTitles()
	{
		if (combinedTitles != null)
		{
			return combinedTitles.toArray(nullStrings);
		}
		else
		{
			return null;
		}
	}

	public String getCombinedStops()
	{
		if (snippetStop != null)
		{
			return snippetStop;
		}
		else
		{
			return "";
		}
	}

	public boolean containsId(int selectedBusId)
	{
		if (sharedSnippetStops != null)
		{
			for (StopLocation stop : sharedSnippetStops)
			{
				if (stop.getId() == selectedBusId)
				{
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Alert> getSnippetAlerts() {
		return snippetAlerts;
	}
}
