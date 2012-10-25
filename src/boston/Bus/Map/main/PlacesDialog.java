package boston.Bus.Map.main;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import boston.Bus.Map.R;
import boston.Bus.Map.ui.TextViewBinder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PlacesDialog extends Activity {

	private ListView listView;
	
	public static final String textKey = "intersectionName";
	
	public static final String extrasIntersectionNames = "intersectionNames";
	
	public static final int PLACES_DIALOG = 4;

	public static final String addNewItem = "ADDNEWITEM";
	public static final String item = "ITEM";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.places_dialog);
		
		listView = (ListView)findViewById(R.id.placesDialogListView);

		Bundle extras = getIntent().getExtras();
		final String[] intersectionNames;
		if (extras == null) {
			intersectionNames = new String[0];
		}
		else
		{
			intersectionNames = extras.getStringArray(extrasIntersectionNames);
		}
		
		//TODO: 
		
		List<Map<String, Spanned>> data = Lists.newArrayList();
		{
			ImmutableMap<String, Spanned> map = ImmutableMap.of(textKey, (Spanned)new SpannedString("Add new place..."));
			data.add(map);
		}
		for (String name : intersectionNames) {
			ImmutableMap<String, Spanned> map = ImmutableMap.of(textKey, (Spanned)new SpannedString(name));
			data.add(map);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.places_dialog_row,
				new String[]{textKey},
				new int[] {R.id.places_dialog_text});
		
		adapter.setViewBinder(new TextViewBinder(Color.BLACK));
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent data = new Intent();
				if (position == 0) {
					// add item
					data.putExtra(addNewItem, true);
				}
				else if (position > 0)
				{
					data.putExtra(item, intersectionNames[position - 1]);
				}

				setResult(RESULT_OK, data);
				finish();
			}
			
		});
		
	}
}
