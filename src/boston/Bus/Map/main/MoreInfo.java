package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import boston.Bus.Map.R;

import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.ui.TextViewBinder;
import boston.Bus.Map.util.StringUtil;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MoreInfo extends ListActivity {
	public static final String predictionsKey = "predictions";
	public static final String routeKeysKey = "routes";
	public static final String stopsKey = "stops";
	public static final String routeTitlesKey = "titles";
	
	public static final String titleKey = "title";
	public static final String routeKey = "route";
	
	public static final String textKey = "text";
	public static final String routeTextKey = "routeText";
	public static final String stopIsBetaKey = "stopIsBeta";
	
	
	private Prediction[] predictions;
	private TextView title1;
	private TextView title2;
	private Spinner routeSpinner;
	private TransitSourceTitles routeKeysToTitles;
	
	/**
	 * If false, don't try accessing predictions or routeKeysToTitles because they may be being populated
	 */
	private boolean dataIsInitialized;
	private String[] routes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.moreinfo);
		
		Bundle extras = getIntent().getExtras();
		
		
		
		Parcelable[] parcelables = (Parcelable[])extras.getParcelableArray(predictionsKey);
		predictions = new Prediction[parcelables.length];
		for (int i = 0; i < predictions.length; i++)
		{
			predictions[i] = (Prediction)parcelables[i];
		}
		
		String[] keys = extras.getStringArray(routeKeysKey);
		String[] titles = extras.getStringArray(routeTitlesKey);
		boolean stopIsBeta = extras.getBoolean(stopIsBetaKey);
		
		ImmutableBiMap.Builder<String, String> routeKeysToTitlesBuilder =
				ImmutableBiMap.builder();
		for (int i = 0; i < keys.length; i++)
		{
			routeKeysToTitlesBuilder.put(keys[i], titles[i]);
		}
		routeKeysToTitles = new TransitSourceTitles(routeKeysToTitlesBuilder.build());

		title1 = (TextView)findViewById(R.id.moreinfo_title1);
		title2 = (TextView)findViewById(R.id.moreinfo_title2);
		routeSpinner = (Spinner)findViewById(R.id.moreinfo_route_spinner);
		
		routes = extras.getStringArray(routeKey);
		refreshRouteAdapter();
		
		dataIsInitialized = true;
		
		refreshAdapter(null);
		
		routeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (routes == null)
				{
					return;
				}
				
				if (position == 0)
				{
					refreshAdapter(null);
				}
				else
				{
					int index = position - 1;
					if (index < 0 || index >= routes.length)
					{
						Log.e("BostonBusMap", "error, went past end of route list");
					}
					else
					{
						refreshAdapter(routes[index]);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//leave the state the way it is
			}
		});
		
		String[] stopTitles = extras.getStringArray(titleKey);
		
		StringBuilder titleText1 = new StringBuilder();
		for (int i = 0; i < stopTitles.length; i++)
		{
			titleText1.append(stopTitles[i]);
			if (i != stopTitles.length - 1)
			{
				titleText1.append("<br />");
			}
		}
		
		StringBuilder titleText2 = new StringBuilder();
		String stopTags = extras.getString(stopsKey);
		if (routes != null)
		{
			titleText2.append("<br />Stop ids: ").append(stopTags);
			String[] routeTitles = new String[routes.length];
			for (int i = 0; i < routes.length; i++)
			{
				String route = routes[i];
				routeTitles[i] = routeKeysToTitles.getTitle(route);
			}
			String routesText = Joiner.on(", ").join(routeTitles);
			
			titleText2.append("<br />Routes: ").append(routesText);
			
		}
		
		if (stopIsBeta)
		{
			titleText2.append("</b><br /><font color='red'>Commuter rail predictions are experimental</font><b>");
		}
		
		title1.setText(Html.fromHtml("<b>" + titleText1 + "</b>"));
		title2.setText(Html.fromHtml("<b>" + titleText2 + "</b>"));
	}

	private void refreshRouteAdapter()
	{
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		if (routes == null)
		{
			//shouldn't happen, but just in case
			adapter.add("All routes");
		}
		else
		{
			if (routes.length != 1)
			{
				//if there's only one route, don't bother with this
				adapter.add("All routes");
			}
			for (String route : routes)
			{
				if (routeKeysToTitles != null)
				{
					String title = routeKeysToTitles.getTitle(route);
					if (title != null)
					{
						adapter.add("Route " + title);
					}
					else
					{
						adapter.add("Route " + route);
					}
				}
			}
		}
		
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		routeSpinner.setAdapter(adapter);
	}

	private void refreshAdapter(String route)
	{
		if (!dataIsInitialized)
		{
			return;
		}
		
		List<Map<String, Spanned>> data = Lists.newArrayList();
		if (predictions != null)
		{
			for (Prediction prediction : predictions)
			{
				if (prediction != null && prediction.getMinutes() >= 0)
				{
					//if a route is given, filter based on it, else show all routes
					if (route == null || route.equals(prediction.getRouteName()))
					{
						//data.add(prediction.generateMoreInfoMap());
						ImmutableMap<String, Spanned> map = prediction.makeSnippetMap(routeKeysToTitles, this);
						data.add(map);
					}
				}
			}
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.moreinfo_row,
				new String[]{textKey},
				new int[] {R.id.moreinfo_text});
		
		adapter.setViewBinder(new TextViewBinder());
		
		setListAdapter(adapter);
		
	}
}
