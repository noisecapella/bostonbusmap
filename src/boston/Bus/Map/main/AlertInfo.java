package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boston.Bus.Map.R;

import com.google.common.base.Joiner;
import com.schneeloch.bostonbusmap_library.data.Alert;
import boston.Bus.Map.ui.TextViewBinder;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spanned;
import android.util.Pair;
import android.view.Window;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schneeloch.bostonbusmap_library.util.AlertInfoConstants;

public class AlertInfo extends ListActivity
{

	private Parcelable[] alerts;
	private TextView title;
	public static final String alertsKey = "alerts";

    public static final String snippetTitleKey = "snippetTitle";
	public static final String titleKey = "title";
	public static final String routeKey = "route";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.alert);

		Bundle extras = getIntent().getExtras();

        setTitle(extras.getString(snippetTitleKey));

		alerts = extras.getParcelableArray(alertsKey);

        List<Map.Entry<String, List<Alert>>> pairs = Lists.newArrayList();

		if (alerts != null)
		{
			HashMap<String, List<Alert>> commonAlerts = Maps.newHashMap();

			for (Object alertObj : alerts)
			{
				if (alertObj != null)
				{
					Alert alert = (Alert)alertObj;
					if (commonAlerts.containsKey(alert.getDescription()) == false) {
						List<Alert> alertList = Lists.newArrayList();
						commonAlerts.put(alert.getDescription(), alertList);
					}
					commonAlerts.get(alert.getDescription()).add(alert);
				}
			}

            for (Map.Entry<String, List<Alert>> entry : commonAlerts.entrySet()) {
                pairs.add(entry);
            }

            Collections.sort(pairs, new Comparator<Map.Entry<String, List<Alert>>>() {
                @Override
                public int compare(Map.Entry<String, List<Alert>> lhs, Map.Entry<String, List<Alert>> rhs) {
                    return lhs.getKey().toLowerCase().compareTo(rhs.getKey().toLowerCase());
                }
            });

		}

        ArrayList<Map<String, Spanned>> data = Lists.newArrayList();
        for (Map.Entry<String, List<Alert>> entry : pairs) {
            List<Alert> alertList = entry.getValue();
            Map<String, Spanned> map = Alert.makeSnippetMap(alertList);
            data.add(map);
        }
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.moreinfo_row,
				new String[]{AlertInfoConstants.textKey},
				new int[] {R.id.moreinfo_text});

		adapter.setViewBinder(new TextViewBinder());

		setListAdapter(adapter);

	}

}
