/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.readystatesoftware.mapviewballoons;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import boston.Bus.Map.R;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.main.ReportProblem;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.StringUtil;

import com.google.android.maps.OverlayItem;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView extends FrameLayout {

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
	private ImageView favorite;
	private TextView moreInfo;
	private TextView reportProblem;

	private Location location;
	private Locations locations;
	private HashMap<String, String> routeKeysToTitles;
	
	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(final Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_map_overlay, layout);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);

		favorite = (ImageView) v.findViewById(R.id.balloon_item_favorite);

		moreInfo = (TextView)v.findViewById(R.id.balloon_item_moreinfo);
		
		reportProblem = (TextView)v.findViewById(R.id.balloon_item_report);
		
		favorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v("BostonBusMap", "tapped star icon");
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;

					int result = locations.toggleFavorite(stopLocation);
					favorite.setBackgroundResource(result);
	    			Log.v("BostonBusMap", "setting favorite icon to " +
	    					(result == R.drawable.full_star ? "full star" : "empty star"));
				}
			}
		});
		
		moreInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v("BostonBusMap", "tapped More info link");

				if (routeKeysToTitles == null)
				{
					//ignore for now, we can't print route information without it
				}
				
				StopLocation stopLocation = (StopLocation)location;
				Intent intent = new Intent(context, MoreInfo.class);

				ArrayList<Prediction> predictionArrayList = stopLocation.getCombinedPredictions();
				if (predictionArrayList != null)
				{
					intent.putExtra(MoreInfo.predictionsKey, predictionArrayList.toArray(new Prediction[0]));
				}
				String[] keys = routeKeysToTitles.keySet().toArray(new String[0]);
				String[] values = new String[keys.length];
				for (int i = 0; i < keys.length; i++)
				{
					values[i] = routeKeysToTitles.get(keys[i]);
				}

				intent.putExtra(MoreInfo.routeKeysKey, keys);
				intent.putExtra(MoreInfo.routeTitlesKey, values);

				String[] combinedTitles = stopLocation.getCombinedTitles();
				intent.putExtra(MoreInfo.titleKey, combinedTitles);

				String combinedRoutes = stopLocation.getCombinedRoutes();
				intent.putExtra(MoreInfo.routeKey, combinedRoutes);

				String combinedStops = stopLocation.getCombinedStops();
				intent.putExtra(MoreInfo.stopsKey, combinedStops);

				context.startActivity(intent);
			}
		}
		);

		reportProblem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(context, ReportProblem.class);
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("plain/text");
				
				intent.putExtra(android.content.Intent.EXTRA_EMAIL, TransitSystem.emails);
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, TransitSystem.emailSubject);

				
				String otherText = createEmailBody(context);

				intent.putExtra(android.content.Intent.EXTRA_TEXT, otherText);
				context.startActivity(Intent.createChooser(intent, "Send email..."));
			}
		});
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}
	
	protected void createInfoForDeveloper(Context context, StringBuilder otherText, int selectedBusPredictions, String selectedRoute)
	{
		otherText.append("There was a problem with ");
		switch (selectedBusPredictions)
		{
		case Main.BUS_PREDICTIONS_ONE:
			otherText.append("bus predictions on one route. ");
			break;
		case Main.BUS_PREDICTIONS_STAR:
			otherText.append("bus predictions for favorited routes. ");
			break;
		case Main.VEHICLE_LOCATIONS_ALL:
			otherText.append("vehicle locations on all routes. ");
			break;
		case Main.VEHICLE_LOCATIONS_ONE:
			otherText.append("vehicle locations for one route. ");
			break;
		default:
			otherText.append("something that I can't figure out. ");
		}
		
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String versionText = packageInfo.versionName;
			otherText.append("App version: ").append(versionText).append(". ");
		}
		catch (NameNotFoundException e)
		{
			//don't worry about it
		}
		otherText.append("OS: ").append(android.os.Build.MODEL).append(". ");

		otherText.append("Currently selected route is '").append(selectedRoute).append("'. ");

	}
	
	protected void createInfoForAgency(Context context, StringBuilder ret, int selectedBusPredictions, String selectedRoute)
	{
		if (location instanceof StopLocation)
		{
			StopLocation stopLocation = (StopLocation)location;
			String stopTag = stopLocation.getStopTag();
			HashMap<String, StopLocation> stopTags = locations.getAllStopsAtStop(stopTag);


			if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE)
			{
				if (stopTags.size() <= 1)
				{
					ret.append("The stop id is ").append(stopTag).append(" (").append(stopLocation.getTitle()).append(")");
					ret.append(" on route ").append(selectedRoute).append(". ");
				}
				else
				{
					ArrayList<String> stopTagStrings = new ArrayList<String>();
					for (StopLocation stop : stopTags.values())
					{
						String text = stop.getStopTag() + " (" + stop.getTitle() + ")";
						stopTagStrings.add(text);
					}
					String stopTagsList = StringUtil.join(stopTagStrings, ", ");
					
					ret.append("The stop ids are: ").append(stopTagsList).append(" on route ").append(selectedRoute).append(". ");
				}
			}
			else
			{
				ArrayList<String> pairs = new ArrayList<String>();
				for (StopLocation stop : stopTags.values())
				{
					String routesJoin = StringUtil.join(stop.getRoutes(), ", ");
					pairs.add(stop.getStopTag() + " on routes " + routesJoin);
				}
				
				String list = StringUtil.join(pairs, ", ");
				ret.append("The stop ids are: ");
				StringUtil.join(pairs, ", ", ret);
				ret.append(". ");
			}
		}
		else if (location instanceof BusLocation)
		{
			BusLocation busLocation = (BusLocation)location;
			String busRouteId = busLocation.getRouteId();
			ret.append("The bus number is ").append(busLocation.getBusNumber());
			ret.append(" on route ").append(locations.getRouteName(busRouteId)).append(". ");
		}

	}
	
	protected String createEmailBody(Context context)
	{
		int selectedBusPredictions = locations != null ? locations.getSelectedBusPredictions() : -1;

		String route = "";
		try
		{
			if (locations != null && locations.getSelectedRoute() != null && locations.getSelectedRoute().getRouteName() != null)
			{
				String routeKey = locations.getSelectedRoute().getRouteName();
				route = locations.getRouteName(routeKey);
			}
		}
		catch (IOException e)
		{
			//don't worry about it
		}
		
		StringBuilder otherText = new StringBuilder();
		otherText.append("(Add any other info you want at the beginning or end of this message, and click send.)\n\n");
		otherText.append("\n\nInfo for MBTA:\n");
		createInfoForAgency(context, otherText, selectedBusPredictions, route);
		otherText.append("\n\nInfo for developer:\n");
		createInfoForDeveloper(context, otherText, selectedBusPredictions, route);

		

		return otherText.toString();
	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(OverlayItem item) {
		
		String titleText = item.getTitle();
		String snippetText = item.getSnippet();
		
		layout.setVisibility(VISIBLE);
		if (titleText != null) {
			title.setVisibility(VISIBLE);
			title.setText(Html.fromHtml(titleText));
		} else {
			title.setVisibility(GONE);
		}
		if (snippetText != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(Html.fromHtml(snippetText));
		} else {
			snippet.setVisibility(GONE);
		}
		
		//NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener
		moreInfo.setText(Html.fromHtml("\n<a href='com.bostonbusmap://moreinfo'>More info</a>\n"));
		reportProblem.setText(Html.fromHtml("\n<a href='com.bostonbusmap://reportproblem'>Report<br/>Problem</a>\n"));
	}

	public void setCurrentLocation(Locations locations, Location location, HashMap<String, String> routeKeysToTitles)
	{
		this.locations = locations;
		this.location = location;
		this.routeKeysToTitles = routeKeysToTitles;
	}

	public void setDrawableState(boolean isFavorite, boolean favoriteVisible, boolean moreInfoVisible) {
		favorite.setBackgroundResource(isFavorite ? boston.Bus.Map.R.drawable.full_star : R.drawable.empty_star);
		favorite.setVisibility(favoriteVisible ? View.VISIBLE : View.GONE);
		moreInfo.setVisibility(moreInfoVisible ? View.VISIBLE : View.GONE);
	}
}
