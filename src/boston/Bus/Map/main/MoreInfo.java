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
import boston.Bus.Map.data.TimeBounds;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.ui.TextViewBinder;
import boston.Bus.Map.util.StringUtil;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MoreInfo extends ListActivity {
	public static final String predictionsKey = "predictions";
	public static final String stopsKey = "stops";
	
	public static final String titleKey = "title";
	public static final String routeTitlesKey = "route";
	
	public static final String textKey = "text";
	public static final String routeTextKey = "routeText";
	public static final String stopIsBetaKey = "stopIsBeta";
	
	public static final String boundKey = "bounds";
	
	private Prediction[] predictions;
	private TextView title1;
	private TextView title2;
	private Spinner routeSpinner;
	
	/**
	 * If false, don't try accessing predictions or routeKeysToTitles because they may be being populated
	 */
	private boolean dataIsInitialized;
	private String[] routeTitles;
	private TimeBounds[] bounds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.moreinfo);
		
		
		final Bundle extras = getIntent().getExtras();
		
		
		
		{
			Parcelable[] parcelables = (Parcelable[])extras.getParcelableArray(predictionsKey);
			predictions = new Prediction[parcelables.length];
			for (int i = 0; i < predictions.length; i++)
			{
				predictions[i] = (Prediction)parcelables[i];
			}
		}
		
		{
			Parcelable[] boundParcelables = (Parcelable[])extras.getParcelableArray(boundKey);
			bounds = new TimeBounds[boundParcelables.length];
			for (int i = 0; i < bounds.length; i++) {
				bounds[i] = (TimeBounds)boundParcelables[i];
			}
		}
		
		
		title1 = (TextView)findViewById(R.id.moreinfo_title1);
		title2 = (TextView)findViewById(R.id.moreinfo_title2);
		routeSpinner = (Spinner)findViewById(R.id.moreinfo_route_spinner);
		
		routeTitles = extras.getStringArray(routeTitlesKey);
		refreshRouteAdapter();
		
		dataIsInitialized = true;
		
		refreshAdapter(null);
		
		routeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (routeTitles == null)
				{
					return;
				}
				
				if (position == 0)
				{
					refreshAdapter(null);
					refreshText(null);
				}
				else
				{
					int index = position - 1;
					if (index < 0 || index >= routeTitles.length)
					{
						Log.e("BostonBusMap", "error, went past end of route list");
					}
					else
					{
						refreshAdapter(routeTitles[index]);
						refreshText(extras, routeTitles[index]);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//leave the state the way it is
			}
		});

		refreshText(extras, null);
	}

	private void refreshText(Bundle extras, String routeTitle) {
		boolean stopIsBeta = extras.getBoolean(stopIsBetaKey);
		
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
		if (routeTitles != null)
		{
			titleText2.append("<br />Stop ids: ").append(stopTags);
			String routesText = Joiner.on(", ").join(routeTitles);
			
			titleText2.append("<br />Routes: ").append(routesText);
			
		}
		
		if (stopIsBeta)
		{
			titleText2.append("</b><br /><font color='red'>Commuter rail predictions are experimental</font><b>");
		}
		
		for (TimeBounds bound : bounds) {
			if (routeTitle == null || bound.getRouteTitle().equals(routeTitle)) {
				titleText2.append("<br />" + bound.makeSnippet());
			}
		}
		
		title1.setText(Html.fromHtml("<b>" + titleText1 + "</b>"));
		title2.setText(Html.fromHtml("<b>" + titleText2 + "</b>"));
		
	}

	private void refreshRouteAdapter()
	{
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		if (routeTitles == null)
		{
			//shouldn't happen, but just in case
			adapter.add("All routes");
		}
		else
		{
			if (routeTitles.length != 1)
			{
				//if there's only one route, don't bother with this
				adapter.add("All routes");
			}
			for (String routeTitle : routeTitles)
			{
				adapter.add("Route " + routeTitle);
			}
		}
		
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		routeSpinner.setAdapter(adapter);
	}

	private void refreshAdapter(String routeTitle)
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
					if (routeTitle == null || routeTitle.equals(prediction.getRouteTitle()))
					{
						//data.add(prediction.generateMoreInfoMap());
						ImmutableMap<String, Spanned> map = prediction.makeSnippetMap(this);
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
