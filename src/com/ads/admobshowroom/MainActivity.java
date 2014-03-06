package com.ads.admobshowroom;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.*;
import com.google.analytics.tracking.android.EasyTracker;


public class MainActivity extends Activity  {
	
	public final static String BUTTON_TYPE_MESSAGE = "com.terryahn.admobshowroom.BUTTON_TYPE_MESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
		  	  Intent intent = new Intent(this, SettingsActivity.class);
		  	  startActivity(intent);
			break;
			
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onButtonClick(View view)
	{
  	  Intent intent = new Intent(this, AdViewActivity.class);
  	  intent.putExtra(BUTTON_TYPE_MESSAGE, view.getId());
  	  startActivity(intent);
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
