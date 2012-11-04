package boston.Bus.Map.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class NonClickableListAdapter extends ArrayAdapter<String> {

	public NonClickableListAdapter(Context context,
			String[] objects) {
		super(context, android.R.layout.select_dialog_item, objects);
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}
}
