package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.schneeloch.bostonbusmap_library.data.Alert;
import boston.Bus.Map.ui.TextViewBinder;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.Pair;
import android.view.Window;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.schneeloch.bostonbusmap_library.util.AlertInfoConstants;
import com.schneeloch.torontotransit.R;

public class AlertInfo extends ListActivity
{

	public static final String alertsKey = "alerts";

    public static final String snippetTitleKey = "snippetTitle";
	public static final String titleKey = "title";
	public static final String routeKey = "route";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.alert);

		Bundle extras = getIntent().getExtras();

        setTitle("Alerts for " + Html.fromHtml(extras.getString(snippetTitleKey)));

		Parcelable[] parcelables = extras.getParcelableArray(alertsKey);

        List<Alert> alerts = Lists.newArrayList();
		if (parcelables != null)
		{
            for (Object alertObj : parcelables)
            {
                if (alertObj instanceof Alert)
                {
                    alerts.add((Alert)alertObj);
                }
            }
        }

        List<List<Alert>> alertGroups = Alert.groupAlerts(alerts);

        ArrayList<Map<String, Spanned>> data = Lists.newArrayList();
        for (List<Alert> alertList : alertGroups) {
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
