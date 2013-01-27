package boston.Bus.Map.commands;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

public class DeletePlaceCommand implements Command {
	private final IntersectionLocation location;
	
	public DeletePlaceCommand(IntersectionLocation location) {
		this.location = location;
	}
	
	@Override
	public String getDescription() {
		return "Delete place";
	}

	@Override
	public void execute(Main main, final UpdateHandler handler, final Locations locations,
			RouteTitles routeTitles) throws Exception {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
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
}
