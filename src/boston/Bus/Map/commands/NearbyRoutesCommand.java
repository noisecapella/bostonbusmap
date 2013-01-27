package boston.Bus.Map.commands;

import com.google.android.gms.maps.model.Marker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

public class NearbyRoutesCommand implements Command {
	private final IntersectionLocation location;

	public NearbyRoutesCommand(IntersectionLocation location) {
		this.location = location;
	}

	@Override
	public String getDescription() {
		return "Show nearby routes";
	}

	@Override
	public void execute(final Main main, final UpdateHandler handler, final Locations locations,
			final RouteTitles routeKeysToTitles, Marker marker) throws Exception {
		IntersectionLocation intersectionLocation = (IntersectionLocation)location;

		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("Routes nearby " + intersectionLocation.getName());

		ListView listView = new ListView(main);
		listView.setClickable(false);
		builder.setView(listView);

		final String[] routeTitles = intersectionLocation.getNearbyRouteTitles().toArray(new String[0]);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(main, android.R.layout.select_dialog_item, routeTitles);
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
