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



import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.text.Html;
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
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.MoreInfo;

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
				
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;
					Intent intent = new Intent(context, MoreInfo.class);

					ArrayList<Prediction> predictionArrayList = stopLocation.getPredictions();
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
					
					intent.putExtra(MoreInfo.routesKey, keys);
					intent.putExtra(MoreInfo.titlesKey, values);
					
					intent.putExtra(MoreInfo.titleKey, stopLocation.getTitle());
					context.startActivity(intent);
				}
			}
		});
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

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
			title.setText(titleText);
		} else {
			title.setVisibility(GONE);
		}
		if (snippetText != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(snippetText);
		} else {
			snippet.setVisibility(GONE);
		}
		
		//NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener
		moreInfo.setText(Html.fromHtml("\n<a href='com.bostonbusmap://moreinfo'>More info</a>\n"));
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
