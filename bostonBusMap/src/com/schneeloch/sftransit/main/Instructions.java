package com.schneeloch.sftransit.main;

import com.schneeloch.sftransit.R;

import android.app.Activity;
import android.os.Bundle;

public class Instructions extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions);
		
		setTitle("Instructions");
	}
}
