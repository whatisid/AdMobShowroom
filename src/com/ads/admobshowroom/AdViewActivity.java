package com.ads.admobshowroom;

import java.text.SimpleDateFormat;
import java.util.*;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.google.ads.*;
import com.google.ads.doubleclick.*;
import com.google.ads.AdRequest.ErrorCode;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class AdViewActivity extends Activity implements AdListener {

	private InterstitialAd interstitial;
	private DfpInterstitialAd dfpInterstitial;
	private AdView adView;
	private DfpAdView dfpAdView;
	
	public final static String AD_UNIT_ID = "a151d669cbbcb52";
	public final static int DIMENSION_AD_UNIT_TYPE = 1;
	public final static int DIMENSION_AD_SIZE = 2;

	protected boolean isUsingDefault()
	{
		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		return mySharedPreferences.getBoolean("pref_is_default_id", true);
	}
	
	protected boolean isUsingDfp()
	{
		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		return mySharedPreferences.getBoolean("pref_is_dfp", true);	
	}

	protected String getAdUnitId()
	{
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	boolean isUsingDefault = isUsingDefault();
    	String returnString;
    	
    	if (isUsingDefault)
    		returnString = AD_UNIT_ID;
    	else
    		returnString = mySharedPreferences.getString("pref_custom_id", AD_UNIT_ID);
    	
    	return returnString;
	}
	
	protected String getCustomSize()
	{
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	String returnString = mySharedPreferences.getString("pref_custom_size", "");    	
    	
    	return returnString;		
	}
	
	protected int splitCustomTargeting(String[] keys, String[] values)
	{
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
    	String original = mySharedPreferences.getString("pref_custom_targeting", "");
		String[] pairs = original.split(",");
		int index=0;
		for (int i=0;i<pairs.length;i++ )
		{
			String[] token = pairs[i].split("=");
			if (token.length==2)
			{
				keys[index]=token[0]; values[index]=token[1]; index++;
			}
		}
		return index;
	}
	
	protected int getCustomTargetExtras(DfpExtras extras, StringBuffer extrasString)
	{
    	String[] keys = new String[1000];
    	String[] values = new String[1000];
    	
    	int count = splitCustomTargeting(keys, values);
    	if (count>0)
    	{    		
    		for (int i=0;i<count;i++)
    		{
    			extras.addExtra(keys[i].trim(), values[i]);
    			extrasString.append( keys[i].trim()+"="+ values[i]+(i==count-1?"":",") );
    			
    		}
    	}
    	
    	return count;
	}
	
	protected void appendStatusText(String text)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault());
		TextView textStatus = (TextView) findViewById(R.id.TextStatus);
		textStatus.setText(textStatus.getText()+"["+sdf.format(new Date())+"] "+text+"\n");
	}
	
	protected void sendAnalytics(String type, String size)
	{
		Tracker tracker = EasyTracker.getTracker();
		tracker.setCustomDimension(DIMENSION_AD_UNIT_TYPE, type);
		tracker.setCustomDimension(DIMENSION_AD_SIZE, size);
		tracker.sendView();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adview);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		int id = intent.getIntExtra(MainActivity.BUTTON_TYPE_MESSAGE,0);
		AdSize adSize = AdSize.BANNER;
		String strAdSize = "Banner";
		
		if (isUsingDefault()) appendStatusText("Loading with default ad unit id...");
		else appendStatusText("Loading with ad unit id '"+getAdUnitId()+"'");
		
	    switch (id) 
	    {
	    case R.id.Button320x50:
	    	adSize = AdSize.BANNER;
	    	strAdSize = "320x50";
	    	break;
	    case R.id.Button300x250:
	    	adSize = AdSize.IAB_MRECT;
	    	strAdSize = "300x250";
	    	break;
	    case R.id.Button468x60:
	    	adSize = AdSize.IAB_BANNER;
	    	strAdSize = "468x60";
	    	break;
	    case R.id.Button728x90:
	    	adSize = AdSize.IAB_LEADERBOARD;
	    	strAdSize = "728x90";
	    	break;
	    case R.id.ButtonSmartBanner:
	    	adSize = AdSize.SMART_BANNER;
	    	strAdSize = "SmartBanner";
	    	break;
	    case R.id.ButtonCustomSize:
	    	strAdSize = getCustomSize();
	    	String[] sizes = strAdSize.split("x");
	    	int w,h;
	    	
	    	if (sizes.length != 2) {w=0; h=0;}
	    	else
	    	{
	    		try {
	    		    w = Integer.parseInt(sizes[0]);
	    		    h = Integer.parseInt(sizes[1]);
	    		} catch(NumberFormatException nfe) {
	    		  w=0; h=0;
	    		}
	    		
	    		adSize = new AdSize(w,h);
	    	}
	    	strAdSize = String.valueOf(w)+"x"+String.valueOf(h);
	    	break;
	    	
	    case R.id.ButtonInterstitial:
	    	if (isUsingDfp())
	    	{
	    	    // Create the interstitial
	    	    dfpInterstitial = new DfpInterstitialAd(this, getAdUnitId());
	    	    // Create ad request
	    	    AdRequest adRequest = new AdRequest();
	    	    // Begin loading your interstitial
	    	    dfpInterstitial.loadAd(adRequest);
	    	    // Set Ad Listener to use the callbacks below
	    	    dfpInterstitial.setAdListener(this);
	    	    
	    	    sendAnalytics("DFP","Interstitial");
	    	}
	    	else
	    	{
		    	// Create the interstitial
		    	interstitial = new InterstitialAd(this, getAdUnitId());
		    	// Create ad request
		    	AdRequest adRequest = new AdRequest();
		    	// Begin loading your interstitial
		    	interstitial.loadAd(adRequest);
		    	// Set Ad Listener to use the callbacks below
		    	interstitial.setAdListener(this);
		    	if (isUsingDefault()) sendAnalytics("Default","Interstitial"); else sendAnalytics("AdMob","Interstitial");
	    	}
	    	break;
	      default:
	    	  break;
	    }
	    if (id != R.id.ButtonInterstitial)
	    {
	    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.bannersLayout);
			if (isUsingDfp())
			{
				dfpAdView = new DfpAdView(this, adSize, getAdUnitId());
		    	layout.addView(dfpAdView);
		    	dfpAdView.setAdListener(this);
		    	AdRequest request = new AdRequest();
		    	
		    	DfpExtras extras = new DfpExtras();
		    	StringBuffer extrasString = new StringBuffer();
		    	
		    	int count = getCustomTargetExtras(extras, extrasString);
		    	
		    	if (count>0) 
		    	{
		    		request.setNetworkExtras(extras);
		    		appendStatusText("Set "+count+" custom value(s). - "+extrasString.toString());
		    	}
		    	appendStatusText("Set ad size - "+strAdSize);
		    	dfpAdView.loadAd(request);
		    	
		    	if (isUsingDefault()) sendAnalytics("Default", strAdSize); else sendAnalytics("DFP", strAdSize);
			}
			else
			{
				adView = new AdView(this, adSize, getAdUnitId());
		    	layout.addView(adView);
		    	adView.setAdListener(this);
		    	adView.loadAd(new AdRequest());
		    	if (isUsingDefault()) sendAnalytics("Default", strAdSize); else sendAnalytics("AdMob", strAdSize);
			}
	    }
	}

	@Override
	public void onReceiveAd(Ad ad) 
	{
		if (!isUsingDfp() && (ad == interstitial))
	    {
	      interstitial.show();
	    }
		if (isUsingDfp() && (ad == dfpInterstitial))
		{
			dfpInterstitial.show();
		}
	    
	    appendStatusText("Loaded successfully.");
	}

	@Override
	public void onDismissScreen(Ad ad) {
	    appendStatusText("Closed.");
		
	}

	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
	    appendStatusText("Failed To recevie ad. - " + errorCode);		
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

	@Override
	public void onLeaveApplication(Ad ad) {
		
	}

	@Override
	public void onPresentScreen(Ad ad) {
		
	}
}