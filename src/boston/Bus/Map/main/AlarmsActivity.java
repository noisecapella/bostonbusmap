package boston.Bus.Map.main;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boston.Bus.Map.R;
import boston.Bus.Map.data.Alarm;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.ui.TextViewBinder;

/**
 * Created by schneg on 1/20/14.
 */
public class AlarmsActivity extends ListActivity {
	private static final String textKey = "text";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.alarms);

		ContentResolver resolver = getApplicationContext().getContentResolver();
		List<Alarm> alarms = DatabaseContentProvider.DatabaseAgent.getAlarms(resolver);

		ArrayList<HashMap<String, Spanned>> data = Lists.newArrayList();
		for (Alarm alarm : alarms) {
			HashMap<String, Spanned> map = new HashMap<String, Spanned>();

			long nowSeconds = (System.currentTimeMillis()/1000);
			int minutesFromNow = (int)(alarm.getAlarmTime() - nowSeconds)/60;
			String ret = Alarm.makeHtml(alarm.getRouteTitle(), alarm.getStop(), alarm.getDirectionTitle(),
					minutesFromNow);

			map.put(textKey, Html.fromHtml(ret));
			data.add(map);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.moreinfo_row,
				new String[]{textKey},
				new int[] {R.id.moreinfo_text});

		adapter.setViewBinder(new TextViewBinder());

		setListAdapter(adapter);

	}
}
