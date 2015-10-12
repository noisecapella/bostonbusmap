package boston.Bus.Map.data;

import boston.Bus.Map.main.RefreshAsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;

import boston.Bus.Map.ui.MapManager;

import com.google.android.maps.MapView;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

public class UpdateArguments {
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private GoogleMap mapView;
	private IDatabaseAgent databaseAgent;
	private MapManager overlayGroup;
	private RefreshAsyncTask majorHandler;
	private Locations busLocations;
	private ITransitSystem transitSystem;
    private Context context;
    private boolean changeRouteIfSelected;

    public UpdateArguments(ProgressBar progress,
			ProgressDialog progressDialog, GoogleMap mapView, IDatabaseAgent databaseAgent,
			MapManager mapManager, RefreshAsyncTask majorHandler,
			Locations busLocations,
            ITransitSystem transitSystem, Context context) {
		this.progress = progress;
		this.progressDialog = progressDialog;
		this.mapView = mapView;
		this.databaseAgent = databaseAgent;
		this.overlayGroup = mapManager;
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
	
	public GoogleMap getMapView() {
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

	public MapManager getOverlayGroup() {
		return overlayGroup;
	}

    public boolean isChangeRouteIfSelected() {
        return changeRouteIfSelected;
    }
}
