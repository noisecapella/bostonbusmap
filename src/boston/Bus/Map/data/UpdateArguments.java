package boston.Bus.Map.data;

import boston.Bus.Map.main.RefreshAsyncTask;
import boston.Bus.Map.provider.IDatabaseAgent;
import boston.Bus.Map.transit.ITransitSystem;
import boston.Bus.Map.ui.OverlayGroup;

import com.google.android.maps.MapView;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

public class UpdateArguments {
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private MapView mapView;
	private IDatabaseAgent databaseAgent;
	private OverlayGroup overlayGroup;
	private RefreshAsyncTask majorHandler;
	private Locations busLocations;
	private ITransitSystem transitSystem;
    private Context context;
	
	public UpdateArguments(ProgressBar progress,
			ProgressDialog progressDialog, MapView mapView, IDatabaseAgent databaseAgent,
			OverlayGroup overlayGroup, RefreshAsyncTask majorHandler,
			Locations busLocations,
            ITransitSystem transitSystem, Context context) {
		this.progress = progress;
		this.progressDialog = progressDialog;
		this.mapView = mapView;
		this.databaseAgent = databaseAgent;
		this.overlayGroup = overlayGroup;
		this.majorHandler = majorHandler;
		this.busLocations = busLocations;
		this.transitSystem = transitSystem;
        this.context = context;
	}

	public ProgressBar getProgress() {
		return progress;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

    public Context getContext() {
        return context;
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

	public IDatabaseAgent getDatabaseAgent() {
		return databaseAgent;
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

	public ITransitSystem getTransitSystem() {
		return transitSystem;
	}

	public OverlayGroup getOverlayGroup() {
		return overlayGroup;
	}

	public UpdateArguments cloneMe() {
		return new UpdateArguments(progress, progressDialog, mapView, databaseAgent, overlayGroup, majorHandler, busLocations, transitSystem, context);
	}
}
