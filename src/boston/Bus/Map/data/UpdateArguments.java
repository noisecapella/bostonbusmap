package boston.Bus.Map.data;

import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.RefreshAsyncTask;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.ui.BusOverlay;
import boston.Bus.Map.ui.LocationOverlay;
import boston.Bus.Map.ui.OverlayGroup;
import boston.Bus.Map.ui.RouteOverlay;


import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;

public class UpdateArguments {
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private MapView mapView;
	private Context context;
	private OverlayGroup overlayGroup;
	private RefreshAsyncTask majorHandler;
	private Locations busLocations;
	private TransitSystem transitSystem;
	
	public UpdateArguments(ProgressBar progress,
			ProgressDialog progressDialog, MapView mapView, Context context,
			OverlayGroup overlayGroup, RefreshAsyncTask majorHandler,
			Locations busLocations,
			TransitSystem transitSystem) {
		this.progress = progress;
		this.progressDialog = progressDialog;
		this.mapView = mapView;
		this.context = context;
		this.overlayGroup = overlayGroup;
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

	public OverlayGroup getOverlayGroup() {
		return overlayGroup;
	}

	public UpdateArguments cloneMe() {
		return new UpdateArguments(progress, progressDialog, mapView, context, overlayGroup, majorHandler, busLocations, transitSystem);
	}
}
