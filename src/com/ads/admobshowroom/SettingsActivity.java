package com.ads.admobshowroom;


import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	private SettingsFragment settingsFragment;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settingsFragment = new SettingsFragment();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment )
                .commit();
        
    }
	
	@Override
	public void onStart() {
		super.onStart();
	    EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}
