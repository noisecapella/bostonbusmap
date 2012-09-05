package boston.Bus.Map.main;

import boston.Bus.Map.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class GetDirectionsDialog extends Activity {
	public static final int EVERYTHING_OK = 3;
	public static final int NEEDS_INPUT_TO = 1;
	public static final int NEEDS_INPUT_FROM = 2;
	
	public static final int GETDIRECTIONS_REQUEST_CODE = 5;
	
	public static final String START_DISPLAY_KEY = "start_display_key";
	public static final String STOP_DISPLAY_KEY = "stop_display_key";
	public static final String START_TAG_KEY = "start_tag_key";
	public static final String STOP_TAG_KEY = "stop_tag_key";
	
	public static final String CURRENT_LOCATION_TAG = "current_location_tag";
	
	// these are either null, meaning lack of answer, CURRENT_LOCATION_TAG meaning my current location,
	// or a legit StopLocation tag
	private String startTag;
	private String stopTag;

	private EditText fromField;
	private EditText toField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_directions_dialog);

		fromField = (EditText) findViewById(R.id.fromField); 
		toField = (EditText) findViewById(R.id.toField);
		
		final String[] pickerItems = new String[] {
				getString(R.string.pickCurrentLocation),
				getString(R.string.chooseAStop)
			};
		
		fromField.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GetDirectionsDialog.this);
				builder.setItems(pickerItems, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: //current location
							startTag = CURRENT_LOCATION_TAG;
							fromField.setText(getString(R.string.getDirectionsCurrentLocation));
							break;
						case 1: //pick a stop
							setResult(NEEDS_INPUT_FROM);
							finish();
							break;
						}
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		toField.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GetDirectionsDialog.this);
				builder.setItems(pickerItems, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: //current location
							stopTag = CURRENT_LOCATION_TAG;
							toField.setText(getString(R.string.getDirectionsCurrentLocation));
							
							break;
						case 1: //pick a stop
							setResult(NEEDS_INPUT_TO);
							finish();
							break;
						}
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		
		fromField.setText(savedInstanceState.getString(START_DISPLAY_KEY));
		toField.setText(savedInstanceState.getString(STOP_DISPLAY_KEY));
		
		startTag = savedInstanceState.getString(START_TAG_KEY);
		stopTag = savedInstanceState.getString(STOP_TAG_KEY);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(START_DISPLAY_KEY, fromField.getText().toString());
		outState.putString(STOP_DISPLAY_KEY, toField.getText().toString());
		outState.putString(START_TAG_KEY, startTag);
		outState.putString(STOP_TAG_KEY, stopTag);
	}
}
