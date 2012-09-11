package boston.Bus.Map.data;

import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.RouteOverlay;

import com.google.android.maps.MapView;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

public class UpdateArguments {
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private MapView mapView;
	private Context context;
	private BusOverlay busOverlay;
	private RouteOverlay routeOverlay;
	private LocationOverlay myLocationOverlay;
	private UpdateAsyncTask majorHandler;
	private Locations busLocations;
	private TransitSystem transitSystem;
	
	public UpdateArguments(ProgressBar progress,
			ProgressDialog progressDialog, MapView mapView, Context context,
			BusOverlay busOverlay, RouteOverlay routeOverlay,
			LocationOverlay myLocationOverlay, UpdateAsyncTask majorHandler,
			Locations busLocations,
			TransitSystem transitSystem) {
		this.progress = progress;
		this.progressDialog = progressDialog;
		this.mapView = mapView;
		this.context = context;
		this.busOverlay = busOverlay;
		this.routeOverlay = routeOverlay;
		this.myLocationOverlay = myLocationOverlay;
		this.majorHandler = majorHandler;
		this.busLocations = busLocations;
		this.transitSystem = transitSystem;
	}

	public ProgressBar getProgress() {
		return progress;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgress(ProgressBar progress) {
		this.progress = progress;
	}
	
	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}
	
	public MapView getMapView() {
		return mapView;
	}

	public Context getContext() {
		return context;
	}

	public BusOverlay getBusOverlay() {
		return busOverlay;
	}

	public RouteOverlay getRouteOverlay() {
		return routeOverlay;
	}

	public LocationOverlay getMyLocationOverlay() {
		return myLocationOverlay;
	}

	public UpdateAsyncTask getMajorHandler() {
		return majorHandler;
	}
	
	public void setMajorHandler(UpdateAsyncTask majorHandler) {
		this.majorHandler = majorHandler;
	}

	public Locations getBusLocations() {
		return busLocations;
	}

	public TransitSystem getTransitSystem() {
		return transitSystem;
	}

	public void nullify() {
		this.busLocations = null;
		this.busOverlay = null;
		this.context = null;
		this.majorHandler = null;
		this.mapView = null;
		this.myLocationOverlay = null;
		this.progress = null;
		this.progressDialog = null;
		this.routeOverlay = null;
		this.transitSystem = null;
	}
}
