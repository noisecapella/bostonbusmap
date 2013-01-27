package boston.Bus.Map.commands;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

public class EditPlaceCommand implements Command {
	private final IntersectionLocation intersectionLocation;

	public EditPlaceCommand(IntersectionLocation intersectionLocation) {
		this.intersectionLocation = intersectionLocation;
	}
	@Override
	public String getDescription() {
		return "Edit Place name";
	}

	@Override
	public void execute(final Main main, final UpdateHandler handler, final Locations locations,
			RouteTitles routeTitles) throws Exception {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("Edit place name");

		final EditText textView = new EditText(main);
		textView.setHint("Place name (ie, Home)");
		final String oldName = intersectionLocation.getName();
		textView.setText(oldName);
		builder.setView(textView);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newName = textView.getText().toString();
				if (newName.length() == 0) {
					Toast.makeText(main, "Place name cannot be empty", Toast.LENGTH_LONG).show();
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
