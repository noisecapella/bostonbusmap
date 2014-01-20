package boston.Bus.Map.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import boston.Bus.Map.R;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.services.AlarmService;
import boston.Bus.Map.util.LogUtil;

public class AlarmReceiver extends WakefulBroadcastReceiver {
	// mostly stolen from http://stackoverflow.com/questions/4459058/alarm-manager-example
	public static final int ID = 3;

	public static void triggerNotification(Context context, String title) {
		LogUtil.i("Triggering notification");

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentText(title);
		builder.setContentTitle("Alarm triggered!");

		Intent resultIntent = new Intent(context, Main.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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

		notificationManager.notify(ID, notification);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.i("onReceive Alarm");
		// if alarm is still good, renew alarm for another 30 seconds
		// else trigger notification

		Intent serviceIntent = new Intent(context, AlarmService.class);
		startWakefulService(context, serviceIntent);
	}

	/**
	 * Start checking for alarms. Caller is expected to have already inserted alarm information into database
	 * @param context
	 * @param delay Trigger alarm this number of seconds from now
	 */
	public static void scheduleAlarm(Context context, int delay)
	{
		// turn on boot receiver
		ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + (delay * 1000), pendingIntent);
	}

	public static void cancelAllAlarms(Context context)
	{
		ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}