package boston.Bus.Map.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import boston.Bus.Map.R;

/**
 * Created by schneg on 1/20/14.
 */
public class FullScreenAlarmActivity extends Activity {
	public static final String ROUTE_KEY = "_route";
	public static final String STOP_KEY = "_stop";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.full_screen_alarm);

		TextView textView = (TextView)findViewById(R.id.full_screen_alarm_text_view);
		Intent intent = getIntent();
		String text = "Vehicle is arriving!";
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String route = extras.getString(ROUTE_KEY);
				String stop = extras.getString(STOP_KEY);

				if (route != null && stop != null) {
					// TODO: stop title
					text = "Vehicle arriving at " + stop + " for route " + route + "!";
				}
			}
		}
		textView.setText(text);

		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(new long[]{500, 500}, 0);

		Button dismissButton = (Button)findViewById(R.id.alarmDismissButton);
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
