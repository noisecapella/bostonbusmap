package boston.Bus.Map.main;

import java.util.ArrayList;
import java.util.HashMap;

import boston.Bus.Map.R;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.ui.TextViewBinder;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AlertInfo extends ListActivity
{

	private Parcelable[] alerts;
	private TextView title;
	public static final String alertsKey = "alerts";

	public static final String titleKey = "title";
	public static final String routeKey = "route";
	public static final String textKey = "text";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.moreinfo);

		Bundle extras = getIntent().getExtras();



		alerts = (Parcelable[])extras.getParcelableArray(alertsKey);

		ArrayList<HashMap<String, Spanned>> data = new ArrayList<HashMap<String,Spanned>>();
		if (alerts != null)
		{
			for (Object alertObj : alerts)
			{
				if (alertObj != null)
				{
					Alert alert = (Alert)alertObj;
					HashMap<String, Spanned> map = alert.makeSnippetMap(this);
					data.add(map);
				}
			}
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.moreinfo_row,
				new String[]{textKey},
				new int[] {R.id.moreinfo_text});

		adapter.setViewBinder(new TextViewBinder());

		setListAdapter(adapter);

		title = (TextView)findViewById(R.id.moreinfo_title);

		String[] stopTitles = extras.getStringArray(titleKey);
		String routes = extras.getString(routeKey);

		StringBuilder titleText = new StringBuilder();
		for (int i = 0; i < stopTitles.length; i++)
		{
			titleText.append(stopTitles[i]);
			if (i != stopTitles.length - 1)
			{
				titleText.append("<br />");
			}
		}

		title.setText(Html.fromHtml("<b>" + titleText + "</b>"));
	}

}
