package boston.Bus.Map.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import boston.Bus.Map.R;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.receivers.AlarmReceiver;
import boston.Bus.Map.util.LogUtil;

/**
 * Created by schneg on 1/5/14.
 */
public class AlarmService extends IntentService {
	public static final String TITLE_KEY = "title";
	private static final int ID = 3;
	public static final String TRIGGER_ALARM_ACTION = "ALARM";
	public static final String MINUTES_KEY = "minutes";

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public AlarmService() {
		super("AlarmService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentText(intent.getStringExtra(TITLE_KEY));
		builder.setContentTitle("Alert triggered!");

		Intent resultIntent = new Intent(this, Main.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Main.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		builder.setSmallIcon(R.drawable.appicon);
		builder.setContentIntent(resultPendingIntent);
		builder.setDefaults(Notification.DEFAULT_VIBRATE);

		Notification notification = builder.build();

		try {
			int minutes = intent.getIntExtra(MINUTES_KEY, 0);
			if (minutes > 0) {
				minutes -= 1;
			}
			Thread.sleep(minutes * 60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		notificationManager.notify(ID, notification);
		AlarmReceiver.completeWakefulIntent(intent);
	}
}
