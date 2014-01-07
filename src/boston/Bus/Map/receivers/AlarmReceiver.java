package boston.Bus.Map.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import boston.Bus.Map.R;
import boston.Bus.Map.data.TimePrediction;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.util.LogUtil;

public class AlarmReceiver extends BroadcastReceiver {
	// mostly stolen from http://stackoverflow.com/questions/4459058/alarm-manager-example
	public static final int ID = 3;

	public void triggerNotification(Context context, String title) {
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
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		try
		{
			wakeLock.acquire();

			// if alarm is still good, renew alarm for another 30 seconds
			// else trigger notification

			//setAlarm(context);
			triggerNotification(context, "alarm");
		}
		finally
		{
			wakeLock.release();
		}
	}

	public static void setAlarm(Context context, TimePrediction timePrediction)
	{
		LogUtil.i("Setting alarm");
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (30 * 1000), pendingIntent);
	}

	public static void cancelAlarm(Context context)
	{
		LogUtil.i("Cancelling alarm");
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, ID, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}}