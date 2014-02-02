package boston.Bus.Map.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import boston.Bus.Map.R;
import boston.Bus.Map.util.LogUtil;

/**
 * Created by schneg on 1/20/14.
 */
public class FullScreenAlarmActivity extends Activity {
	public static final String ROUTE_TITLE_KEY = "_route";
	public static final String STOP_TITLE_KEY = "_stop";

	private Vibrator vibrator;

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
				String route = extras.getString(ROUTE_TITLE_KEY);
				String stop = extras.getString(STOP_TITLE_KEY);

				if (route != null && stop != null) {
					// TODO: stop title
					text = "Vehicle arriving<br /><br />at " + stop + "<br /><br />for route " + route;
				}
			}
		}
		textView.setText(Html.fromHtml(text));

		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(new long[]{500, 500}, 0);
		}


		try
		{
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (notification != null) {
				Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
				if (ringtone != null) {
					ringtone.play();
				}
			}
		}
		catch (Exception e) {
			LogUtil.e(e);
		}

		Button dismissButton = (Button)findViewById(R.id.alarmDismissButton);
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (vibrator != null) {
			vibrator.cancel();
		}
		super.onDestroy();
	}
}
