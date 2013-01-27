package boston.Bus.Map.commands;

import java.io.IOException;

import com.google.android.gms.maps.model.Marker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.StopPredictionView;
import boston.Bus.Map.data.TimeBounds;
import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.main.PopupAdapter;
import boston.Bus.Map.main.UpdateHandler;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;

public class FavoritesCommand implements Command {
	private final StopLocation stopLocation;

	public FavoritesCommand(StopLocation stopLocation) {
		this.stopLocation = stopLocation;
	}
	
	@Override
	public String getDescription() {
		if (stopLocation.isFavorite()) {
			return "Remove from favorites";
		}
		else {
			return "Mark as favorite";
		}
	}

	@Override
	public void execute(Main main, UpdateHandler handler, Locations locations,
			RouteTitles routeTitles, Marker marker) throws Exception {
		locations.toggleFavorite(stopLocation);
		marker.showInfoWindow();
	}
}
