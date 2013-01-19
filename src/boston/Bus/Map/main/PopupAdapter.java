package boston.Bus.Map.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import boston.Bus.Map.R;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.PredictionView;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopPredictionView;
import boston.Bus.Map.data.TimeBounds;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.MapManager;
import boston.Bus.Map.util.LogUtil;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.readystatesoftware.mapviewballoons.LimitLinearLayout;

public class PopupAdapter implements InfoWindowAdapter {
	private ImageView favorite;
	private TextView moreInfo;
	private TextView reportProblem;
	private TextView alertsTextView;
	private TextView deleteTextView;
	private TextView editTextView;
	private TextView nearbyRoutesTextView;
	private final Locations locations;
	private final RouteTitles routeKeysToTitles;
	private Location location;
	private Spanned noAlertsText;
	private Alert[] alertsList;
	private final UpdateHandler handler;
	private final Main main;
	private TextView title;
	private TextView snippet;
	private MapManager manager;

	public PopupAdapter(final Main main, UpdateHandler handler, Locations locations,
			RouteTitles routeKeysToTitles, MapManager manager) {
		this.locations = locations;
		this.routeKeysToTitles = routeKeysToTitles;
		this.handler = handler;
		
		this.main = main;
		this.manager = manager;
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		
		LimitLinearLayout parent = new LimitLinearLayout(getContext());
		
		LayoutInflater inflater = (LayoutInflater) main
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layoutView = inflater.inflate(R.layout.balloon_overlay, parent);
		layoutView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layoutView.setBackgroundResource(R.drawable.tooltip);
		title = (TextView) layoutView.findViewById(R.id.balloon_item_title);
		snippet = (TextView) layoutView.findViewById(R.id.balloon_item_snippet);

		favorite = (ImageView) layoutView.findViewById(R.id.balloon_item_favorite);
		favorite.setBackgroundResource(R.drawable.empty_star);

		moreInfo = (TextView) layoutView.findViewById(R.id.balloon_item_moreinfo);
		Spanned moreInfoText = Html.fromHtml("\n<a href='com.bostonbusmap://moreinfo'>More info</a>\n");
		moreInfo.setText(moreInfoText);
		
		reportProblem = (TextView) layoutView.findViewById(R.id.balloon_item_report);
		Spanned reportProblemText = Html.fromHtml("\n<a href='com.bostonbusmap://reportproblem'>Report<br/>Problem</a>\n");
		reportProblem.setText(reportProblemText);
		
		deleteTextView = (TextView)layoutView.findViewById(R.id.balloon_item_delete);
		Spanned deleteText = Html.fromHtml("\n<a href='com.bostonbusmap://deleteplace'>Delete</a>\n");
		deleteTextView.setText(deleteText);
		
		editTextView = (TextView)layoutView.findViewById(R.id.balloon_item_edit);
		Spanned editText = Html.fromHtml("\n<a href='com.bostonbusmap://editplace'>Edit name</a>\n");
		editTextView.setText(editText);
		
		nearbyRoutesTextView = (TextView)layoutView.findViewById(R.id.balloon_item_nearby_routes);
		Spanned nearbyRoutesText = Html.fromHtml("\n<a href='com.bostonbusmap://nearbyroutes'>Nearby<br/>Routes</a>\n");
		nearbyRoutesTextView.setText(nearbyRoutesText);
		
		alertsTextView = (TextView) layoutView.findViewById(R.id.balloon_item_alerts);
		alertsTextView.setVisibility(View.GONE);
		alertsTextView.setText(R.string.noalerts);
		noAlertsText = Html.fromHtml("<font color='grey'>No alerts</font>");
		
		favorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;

					int result;
					try {
						result = PopupAdapter.this.locations.toggleFavorite(stopLocation);
						favorite.setBackgroundResource(result);
					} catch (RemoteException e) {
						LogUtil.e(e);
					}
				}
			}
		});
		
		moreInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (PopupAdapter.this.routeKeysToTitles == null)
				{
					//ignore for now, we can't print route information without it
				}
				//user shouldn't be able to click this if this is a BusLocation, but just in case...
				if (location instanceof StopLocation)
				{
					StopLocation stopLocation = (StopLocation)location;
					Intent intent = new Intent(getContext(), MoreInfo.class);

					StopPredictionView predictionView = (StopPredictionView)stopLocation.getPredictionView();
					Prediction[] predictionArray = predictionView.getPredictions();
					if (predictionArray != null)
					{
						intent.putExtra(MoreInfo.predictionsKey, predictionArray);
					}

					try
					{
						TimeBounds[] bounds = new TimeBounds[predictionView.getRouteTitles().length];
						int i = 0;
						for (String routeTitle : predictionView.getRouteTitles()) {
							String routeKey = routeKeysToTitles.getKey(routeTitle);
							bounds[i] = locations.getRoute(routeKey).getTimeBounds();
							i++;
						}
						intent.putExtra(MoreInfo.boundKey, bounds);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					String[] combinedTitles = predictionView.getTitles();
					intent.putExtra(MoreInfo.titleKey, combinedTitles);

					String[] combinedRoutes = predictionView.getRouteTitles();
					intent.putExtra(MoreInfo.routeTitlesKey, combinedRoutes);

					String combinedStops = predictionView.getStops();
					intent.putExtra(MoreInfo.stopsKey, combinedStops);

					intent.putExtra(MoreInfo.stopIsBetaKey, stopLocation.isBeta());
					
					getContext().startActivity(intent);
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

				
				String otherText = createEmailBody(getContext());

				intent.putExtra(android.content.Intent.EXTRA_TEXT, otherText);
				getContext().startActivity(Intent.createChooser(intent, "Send email..."));
			}
		});
		
		alertsTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Alert[] alerts = alertsList;
				
				Intent intent = new Intent(getContext(), AlertInfo.class);
				if (alerts != null)
				{
					intent.putExtra(AlertInfo.alertsKey, alerts);
					
					getContext().startActivity(intent);
				}
				else
				{
					Log.i("BostonBusMap", "alertsList is null");
				}
				
			}
		});
		
		deleteTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle("Delete Place");
				builder.setMessage("Are you sure?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (location != null && location instanceof IntersectionLocation) {
							IntersectionLocation intersection = (IntersectionLocation)location;
							locations.removeIntersection(intersection.getName());
							locations.setSelection(locations.getSelection().withDifferentIntersection(null));
						}
						handler.triggerUpdate();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				
			}
		});
		
		nearbyRoutesTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (location != null && location instanceof IntersectionLocation) {
					IntersectionLocation intersectionLocation = (IntersectionLocation)location;
					
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Routes nearby " + intersectionLocation.getName());
					
					ListView listView = new ListView(getContext());
					listView.setClickable(false);
					builder.setView(listView);
					
					final String[] routeTitles = intersectionLocation.getNearbyRouteTitles().toArray(new String[0]);
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, routeTitles);
					builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which >= 0 && which < routeTitles.length) {
								String routeTitle = routeTitles[which];
								String routeKey = routeKeysToTitles.getKey(routeTitle);
								Selection newSelection = locations.getSelection().withDifferentRoute(routeKey);
								locations.setSelection(newSelection);
								main.setMode(Selection.BUS_PREDICTIONS_ONE, true, true);
							}
						}
					});
					
					builder.create().show();
				}
				
			}
		});
		
		editTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (location != null && location instanceof IntersectionLocation) {
					IntersectionLocation intersectionLocation = (IntersectionLocation)location;

					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Edit place name");

					final EditText textView = new EditText(getContext());
					textView.setHint("Place name (ie, Home)");
					final String oldName = intersectionLocation.getName();
					textView.setText(oldName);
					builder.setView(textView);
					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String newName = textView.getText().toString();
							if (newName.length() == 0) {
								Toast.makeText(getContext(), "Place name cannot be empty", Toast.LENGTH_LONG).show();
							}
							else
							{
								locations.editIntersection(oldName, newName);
								locations.setSelection(locations.getSelection().withDifferentIntersection(newName));
								handler.triggerUpdate();
							}
							dialog.dismiss();
						}
					});

					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

					builder.create().show();
				}
			}
		});
		
		String id = marker.getId();
		Location location = manager.getLocationFromMarkerId(id);
		if (location != null) {
			populateView(location, layoutView);
		}
		return parent;
	}
	protected Context getContext() {
		return main;
	}

	protected void createInfoForDeveloper(Context context, StringBuilder otherText, int mode, String routeTitle)
	{
		otherText.append("There was a problem with ");
		switch (mode)
		{
		case Selection.BUS_PREDICTIONS_ONE:
			otherText.append("bus predictions on one route. ");
			break;
		case Selection.BUS_PREDICTIONS_STAR:
			otherText.append("bus predictions for favorited routes. ");
			break;
		case Selection.BUS_PREDICTIONS_INTERSECT:
			otherText.append("bus predictions intersecting certain locations. ");
			break;
		case Selection.BUS_PREDICTIONS_ALL:
			otherText.append("bus predictions for all routes. ");
			break;
		case Selection.VEHICLE_LOCATIONS_ALL:
			otherText.append("vehicle locations on all routes. ");
			break;
		case Selection.VEHICLE_LOCATIONS_ONE:
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

		otherText.append("Currently selected route is '").append(routeTitle).append("'. ");
	}
	
	protected void createInfoForAgency(Context context, StringBuilder ret, int mode, String routeTitle)
	{
		if (location instanceof StopLocation)
		{
			StopLocation stopLocation = (StopLocation)location;
			String stopTag = stopLocation.getStopTag();
			ConcurrentMap<String, StopLocation> stopTags = locations.getAllStopsAtStop(stopTag);

			if (mode == Selection.BUS_PREDICTIONS_ONE)
			{
				if (stopTags.size() <= 1)
				{
					ret.append("The stop id is ").append(stopTag).append(" (").append(stopLocation.getTitle()).append(")");
					ret.append(" on route ").append(routeTitle).append(". ");
				}
				else
				{
					List<String> stopTagStrings = Lists.newArrayList();
					for (StopLocation stop : stopTags.values())
					{
						String text = stop.getStopTag() + " (" + stop.getTitle() + ")";
						stopTagStrings.add(text);
					}
					String stopTagsList = Joiner.on(",\n").join(stopTagStrings);
					
					ret.append("The stop ids are: ").append(stopTagsList).append(" on route ").append(routeTitle).append(". ");
				}
			}
			else
			{
				ArrayList<String> pairs = new ArrayList<String>();
				for (StopLocation stop : stopTags.values())
				{
					String routesJoin = Joiner.on(", ").join(stop.getRoutes());
					pairs.add(stop.getStopTag() + "(" + stop.getTitle() + ") on routes " + routesJoin);
				}
				
				//String list = Joiner.on(",\n").join(pairs);
				ret.append("The stop ids are: ");
				ret.append(Joiner.on(", ").join(pairs));
				ret.append(". ");
			}
		}
		else if (location instanceof BusLocation)
		{
			BusLocation busLocation = (BusLocation)location;
			String busRouteId = busLocation.getRouteId();
			ret.append("The bus number is ").append(busLocation.getBusNumber());
			ret.append(" on route ").append(locations.getRouteTitle(busRouteId)).append(". ");
		}

	}
	
	protected String createEmailBody(Context context)
	{
		Selection selection = locations.getSelection();
		if (selection == null) {
			selection = new Selection(-1, null, null);
		}

		String routeTitle = selection.getRoute();
		if (routeTitle != null) {
			routeTitle = locations.getRouteTitle(routeTitle);
		}
		else
		{
			routeTitle = "";
		}
		
		StringBuilder otherText = new StringBuilder();
		otherText.append("(What is the problem?\nAdd any other info you want at the beginning or end of this message, and click send.)\n\n");
		otherText.append("\n\n");
		createInfoForAgency(context, otherText, selection.getMode(), routeTitle);
		otherText.append("\n\n");
		createInfoForDeveloper(context, otherText, selection.getMode(), routeTitle);

		

		return otherText.toString();
	}

	private void updateUIFromState(Location location) {
		//TODO: figure out a more elegant way to make the layout use these items even if they're invisible
		if (location.hasFavorite())
		{
			favorite.setBackgroundResource(location.isFavorite() ? R.drawable.full_star : R.drawable.empty_star);
		}
		else
		{
			favorite.setBackgroundResource(R.drawable.null_star);
		}
		
		if (location.hasMoreInfo())
		{
			moreInfo.setVisibility(View.VISIBLE);
		}
		else
		{
			moreInfo.setVisibility(View.GONE);
		}
		
		if (location.hasReportProblem()) {
			reportProblem.setVisibility(View.VISIBLE);
		}
		else
		{
			reportProblem.setVisibility(View.GONE);
		}
		
		TextView[] intersectionViews = new TextView[] {
				deleteTextView, editTextView, nearbyRoutesTextView
		};
		int intersectionVisibility = location.isIntersection() ? View.VISIBLE : View.GONE;
		for (TextView view : intersectionViews) {
			view.setVisibility(intersectionVisibility);
		}
	}
	
	public void populateView(Location location, View view) {
		//NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener
		
		setState(location);
		PredictionView predictionView = location.getPredictionView();
		snippet.setText(predictionView.getSnippet());
		title.setText(predictionView.getSnippetTitle());
		
		Alert[] alerts = predictionView.getAlerts();
		alertsList = alerts;
		
		if (alerts != null && alerts.length != 0)
		{
			int count = alerts.length;
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
	
	public void setState(Location location)
	{
		this.location = location;
		
		updateUIFromState(location);
	}


}
