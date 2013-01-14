package boston.Bus.Map.data;

import com.google.android.gms.maps.GoogleMap;

import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.RefreshAsyncTask;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.MapManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

public class UpdateArguments {
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private GoogleMap mapView;
	private Context context;
	private RefreshAsyncTask majorHandler;
	private Locations busLocations;
	private TransitSystem transitSystem;
	private MapManager manager;
	
	public UpdateArguments(ProgressBar progress,
			ProgressDialog progressDialog, GoogleMap mapView, Context context,
			RefreshAsyncTask majorHandler,
			Locations busLocations, MapManager manager,
			TransitSystem transitSystem) {
		this.progress = progress;
		this.progressDialog = progressDialog;
		this.mapView = mapView;
		this.context = context;
		this.majorHandler = majorHandler;
		this.manager = manager;
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
	
	public GoogleMap getMapView() {
		return mapView;
	}

	public Context getContext() {
		return context;
	}

	public RefreshAsyncTask getMajorHandler() {
		return majorHandler;
	}
	
	public void setMajorHandler(RefreshAsyncTask majorHandler) {
		this.majorHandler = majorHandler;
	}

	public Locations getBusLocations() {
		return busLocations;
	}

	public TransitSystem getTransitSystem() {
		return transitSystem;
	}

	public UpdateArguments cloneMe() {
		return new UpdateArguments(progress, progressDialog, mapView, context, majorHandler, busLocations, manager, transitSystem);
	}

	public MapManager getOverlayGroup() {
		return manager;
	}
}
