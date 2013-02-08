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
import android.util.AttributeSet;
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
import boston.Bus.Map.data.SimplePredictionView;
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
	private static final int MAX_WIDTH = 480;
	private ImageView favorite;
	private TextView moreInfo;
	private TextView reportProblem;
	private TextView alertsTextView;
	private TextView deleteTextView;
	private TextView editTextView;
	private TextView nearbyRoutesTextView;
	private final Locations locations;
	private final RouteTitles routeKeysToTitles;
	private Spanned noAlertsText;
	private Alert[] alertsList;
	private final UpdateHandler handler;
	private final Main main;
	private TextView title;
	private TextView snippet;
	private MapManager manager;

	/**
	 * The view we create for the popup which may get reused
	 */
	private LimitLinearLayout popupView;

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
		if (popupView == null) {
			LimitLinearLayout parent = new LimitLinearLayout(getContext(), MAX_WIDTH);

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
			
			popupView = parent;
		}
		
		View layoutView = popupView.getChildAt(0);
		
		String id = marker.getId();
		Location location = manager.getLocationFromMarkerId(id);
		populateView(location, layoutView);
		return popupView;
	}
	protected Context getContext() {
		return main;
	}


	private void updateUIFromState(Location location) {
		//TODO: figure out a more elegant way to make the layout use these items even if they're invisible
		if (location != null && location.hasFavorite())
		{
			favorite.setBackgroundResource(location.isFavorite() ? R.drawable.full_star : R.drawable.empty_star);
		}
		else
		{
			favorite.setBackgroundResource(R.drawable.null_star);
		}
		
		if (location != null && location.hasMoreInfo())
		{
			moreInfo.setVisibility(View.VISIBLE);
		}
		else
		{
			moreInfo.setVisibility(View.GONE);
		}
		
		if (location != null && location.hasReportProblem()) {
			reportProblem.setVisibility(View.VISIBLE);
		}
		else
		{
			reportProblem.setVisibility(View.GONE);
		}
		
		TextView[] intersectionViews = new TextView[] {
				deleteTextView, editTextView, nearbyRoutesTextView
		};
		int intersectionVisibility;
		if (location != null && location.isIntersection()) {
			intersectionVisibility = View.VISIBLE;
		}
		else
		{
			intersectionVisibility = View.GONE;
		}
		for (TextView view : intersectionViews) {
			view.setVisibility(intersectionVisibility);
		}
	}
	
	private void populateView(Location location, View view) {
		//NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener
		
		updateUIFromState(location);
		PredictionView predictionView;
		if (location != null) {
			predictionView = location.getPredictionView();
		}
		else
		{
			predictionView = new SimplePredictionView("", "", new Alert[0]);
		}
		snippet.setText(Html.fromHtml(predictionView.getSnippet()));
		title.setText(Html.fromHtml(predictionView.getSnippetTitle()));
		
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
	
}
