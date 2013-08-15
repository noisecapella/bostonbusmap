/*
    BostonBusMap
 
    Copyright (C) 2009  George Schneeloch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package boston.Bus.Map.main;

import com.schneeloch.mta.R;

import boston.Bus.Map.provider.TransitContentProvider;
import boston.Bus.Map.transit.TransitSystem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Preferences extends PreferenceActivity 
{

	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference != null)
		{
			String preferenceKey = preference.getKey();
			if ("about".equals(preferenceKey))
			{
				Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TransitSystem.getWebSite()));
				startActivity(viewIntent);
			}
			else if ("clearHistory".equals(preferenceKey))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to clear your search history?")
				       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								SearchRecentSuggestions suggestions = new SearchRecentSuggestions(Preferences.this,
								        TransitContentProvider.AUTHORITY, TransitContentProvider.MODE);
								suggestions.clearHistory();

								Toast.makeText(Preferences.this, "Search history is cleared for this app", Toast.LENGTH_LONG).show();
				        	   
				        	   
				           }
				       })
				       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							
						}
					});
				AlertDialog alert = builder.create();
				alert.show();
			}
			else if ("showTutorial".equals(preferenceKey)) {
				getPreferenceManager().getSharedPreferences().edit().putInt(Main.tutorialStepKey, 0).commit();
				// then the screen will go back to Main, which will 
				// show the tutorial since the key is set to false
				finish();
			}
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
