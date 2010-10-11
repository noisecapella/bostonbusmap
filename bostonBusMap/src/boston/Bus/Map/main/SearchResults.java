package boston.Bus.Map.main;

import boston.Bus.Map.R;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

public class SearchResults extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchresults);
	}
}
