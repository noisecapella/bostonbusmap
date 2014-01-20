package boston.Bus.Map.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import boston.Bus.Map.provider.FavoritesContentProvider;

/**
 * Created by schneg on 1/19/14.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			AlarmReceiver.scheduleAlarm(context, 5);
		}
	}
}
