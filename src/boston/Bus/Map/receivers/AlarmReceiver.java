package boston.Bus.Map.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import boston.Bus.Map.services.AlarmService;
import boston.Bus.Map.util.LogUtil;

public class AlarmReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, AlarmService.class);
		if (intent.getExtras() != null) {
			service.putExtras(intent.getExtras());
		}

		startWakefulService(context, service);
	}
}