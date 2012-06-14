package boston.Bus.Map.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import boston.Bus.Map.R;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.VehicleLocation;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.LocationGroup;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.MultipleStopLocations;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopLocationGroup;
import boston.Bus.Map.data.VehicleLocationGroup;
import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.StringUtil;

import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;


public class BusPopupView extends BalloonOverlayView<BusOverlayItem>
{
	private ImageView favorite;
	private TextView moreInfo;
	private TextView reportProblem;
	private TextView alertsTextView;
	private final Locations locations;
	private final MyHashMap<String, String> routeKeysToTitles;
	private LocationGroup locationGroup;
	private final Spanned moreInfoText;
	private final Spanned reportProblemText;
	private final Spanned noAlertsText;
	private ArrayList<Alert> alertsList;
	
	public BusPopupView(final Context context, int balloonBottomOffset, Locations locations,
			MyHashMap<String, String> routeKeysToTitles, float density)
	{
		super(context, balloonBottomOffset, density);
		
		this.locations = locations;
		this.routeKeysToTitles = routeKeysToTitles;
		
		favorite = (ImageView) layoutView.findViewById(R.id.balloon_item_favorite);

		moreInfo = (TextView) layoutView.findViewById(R.id.balloon_item_moreinfo);
		moreInfoText = Html.fromHtml("\n<a href='com.bostonbusmap://moreinfo'>More info</a>\n");
		
		reportProblem = (TextView) layoutView.findViewById(R.id.balloon_item_report);
		reportProblemText = Html.fromHtml("\n<a href='com.bostonbusmap://reportproblem'>Report<br/>Problem</a>\n");
		
		alertsTextView = (TextView) layoutView.findViewById(R.id.balloon_item_alerts);
		alertsTextView.setVisibility(View.GONE);
		noAlertsText = Html.fromHtml("<font color='grey'>No alerts</font>");
		
		favorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (locationGroup instanceof StopLocationGroup)
				{
					StopLocationGroup stopLocationGroup = (StopLocationGroup)locationGroup;

					int result = BusPopupView.this.locations.toggleFavorite(stopLocationGroup);
					favorite.setBackgroundResource(result);
				}
			}
		});
		
		moreInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (BusPopupView.this.routeKeysToTitles == null)
				{
					//ignore for now, we can't print route information without it
				}
				//user shouldn't be able to click this if this is a BusLocation, but just in case...
				if (locationGroup instanceof StopLocationGroup)
				{
					StopLocationGroup stopLocationGroup = (StopLocationGroup)locationGroup;
					Intent intent = new Intent(context, MoreInfo.class);

					List<Prediction> predictionArray = stopLocationGroup.getCombinedPredictions();
					if (predictionArray != null)
					{
						intent.putExtra(MoreInfo.predictionsKey, predictionArray.toArray(new Prediction[0]));
					}
					
					String[] keys = BusPopupView.this.routeKeysToTitles.keySet().toArray(new String[0]);
					String[] values = new String[keys.length];
					for (int i = 0; i < keys.length; i++)
					{
						values[i] = BusPopupView.this.routeKeysToTitles.get(keys[i]);
					}

					intent.putExtra(MoreInfo.routeKeysKey, keys);
					intent.putExtra(MoreInfo.routeTitlesKey, values);

					List<String> combinedTitles = stopLocationGroup.getCombinedTitles();
					intent.putExtra(MoreInfo.titleKey, combinedTitles.toArray(new String[0]));

					List<String> combinedRoutes = stopLocationGroup.getCombinedRoutes();
					intent.putExtra(MoreInfo.routeKey, combinedRoutes.toArray(new String[0]));

					String combinedStops = stopLocationGroup.getCombinedStops();
					intent.putExtra(MoreInfo.stopsKey, combinedStops);

					intent.putExtra(MoreInfo.stopIsBetaKey, stopLocationGroup.isBeta());
					
					context.startActivity(intent);
				}
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
		
		alertsTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final ArrayList<Alert> alerts = alertsList;
				
				Intent intent = new Intent(context, AlertInfo.class);
				if (alerts != null)
				{
					Alert[] alertArray = alerts.toArray(new Alert[0]);
					intent.putExtra(AlertInfo.alertsKey, alertArray);
					
					context.startActivity(intent);
				}
				else
				{
					Log.i("BostonBusMap", "alertsList is null");
				}
				
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
		case Main.BUS_PREDICTIONS_ALL:
			otherText.append("bus predictions for all routes. ");
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
		if (locationGroup instanceof StopLocationGroup)
		{
			StopLocationGroup group = (StopLocationGroup)locationGroup;

			List<StopLocation> stops = group.getStops();

			if (selectedBusPredictions == Main.BUS_PREDICTIONS_ONE)
			{
				if (stops.size() <= 1)
				{
					ret.append("The stop id is ").append(group.getFirstStopTag()).append(" (").append(group.getFirstTitle()).append(")");
					ret.append(" on route ").append(selectedRoute).append(". ");
				}
				else
				{
					ArrayList<String> stopTagStrings = new ArrayList<String>();
					for (StopLocation stop : stops)
					{
						String text = stop.getStopTag() + " (" + stop.getTitle() + ")";
						stopTagStrings.add(text);
					}
					String stopTagsList = StringUtil.join(stopTagStrings, ",\n");
					
					ret.append("The stop ids are: ").append(stopTagsList).append(" on route ").append(selectedRoute).append(". ");
				}
			}
			else
			{
				ArrayList<String> pairs = new ArrayList<String>();
				for (StopLocation stop : stops)
				{
					pairs.add(stop.getStopTag() + "(" + stop.getTitle() + ") on route " + stop.getFirstRoute());
				}
				
				String list = StringUtil.join(pairs, ",\n");
				ret.append("The stop ids are: ");
				StringUtil.join(pairs, ", ", ret);
				ret.append(". ");
			}
		}
		else if (locationGroup instanceof VehicleLocationGroup)
		{
			VehicleLocationGroup group = (VehicleLocationGroup)locationGroup;
			String busRouteId = group.getFirstRoute();
			
			ret.append("The bus number is ").append(group.getFirstVehicleNumber());
			ret.append(" on route ").append(locations.getRouteName(busRouteId)).append(". ");
		}

	}
	
	protected String createEmailBody(Context context)
	{
		int selectedBusPredictions = locations != null ? locations.getSelectedBusPredictions() : -1;

		String route = "";
		try
		{
			if (locations != null && locations.getSelectedRouteName() != null)
			{
				String routeKey = locations.getSelectedRouteName();
				route = locations.getRouteName(routeKey);
			}
		}
		catch (IOException e)
		{
			//don't worry about it
		}
		
		StringBuilder otherText = new StringBuilder();
		otherText.append("(What is the problem?\nAdd any other info you want at the beginning or end of this message, and click send.)\n\n");
		otherText.append("\n\n");
		createInfoForAgency(context, otherText, selectedBusPredictions, route);
		otherText.append("\n\n");
		createInfoForDeveloper(context, otherText, selectedBusPredictions, route);

		

		return otherText.toString();
	}

	@Override
	public void setData(BusOverlayItem item) {
		super.setData(item);
		
		//NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener
		moreInfo.setText(moreInfoText);
		reportProblem.setText(reportProblemText);
		ArrayList<Alert> alerts = item.getAlerts();
		alertsList = alerts;
		
		if (alerts != null && alerts.size() != 0)
		{
			int count = alerts.size();
			alertsTextView.setVisibility(View.VISIBLE);
			
			String text;
			if (count == 1)
			{
				text = "<font color='red'><a href=\"com.bostonbusmap://alerts\">1 Alert</a></font>";
			}
			else
			{
				text = "<font color='red'><a href=\"com.bostonbusmap://alerts\">" + count + " Alerts</a></font>";
			}
			
			Spanned alertsText = Html.fromHtml(text);
			alertsTextView.setText(alertsText);
			alertsTextView.setClickable(true);
		}
		else
		{
			alertsTextView.setVisibility(View.GONE);
			alertsTextView.setClickable(false);
		}
	}
	
	public void setState(boolean isFavorite, boolean favoriteVisible, boolean moreInfoVisible,
			LocationGroup locationGroup)
	{
		//TODO: figure out a more elegant way to make the layout use these items even if they're invisible
		if (favoriteVisible)
		{
			favorite.setBackgroundResource(isFavorite ? boston.Bus.Map.R.drawable.full_star : R.drawable.empty_star);
		}
		else
		{
			favorite.setBackgroundResource(boston.Bus.Map.R.drawable.null_star);
		}
		
		if (moreInfoVisible)
		{
			moreInfo.setText(moreInfoText);
		}
		else
		{
			moreInfo.setText("");
		}
		
		this.locationGroup = locationGroup;
	}

}
