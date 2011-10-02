package com.schneeloch.sftransit.main;

import com.schneeloch.sftransit.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

public class SearchResults extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchresults);
		
		Intent intent = new Intent(this, Main.class);
		
		startActivity(intent);
	}
}
