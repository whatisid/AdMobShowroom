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

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.doubleclick.*;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class AdViewActivity extends Activity {

	private InterstitialAd interstitial;
	private PublisherInterstitialAd dfpInterstitial;
	private AdView adView;
	private PublisherAdView dfpAdView;
	
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
	
	protected int getCustomTargetExtras(Bundle bundles, StringBuffer extrasString)
	{
    	String[] keys = new String[1000];
    	String[] values = new String[1000];
    	
    	int count = splitCustomTargeting(keys, values);
    	if (count>0)
    	{    		
    		for (int i=0;i<count;i++)
    		{
    			bundles.putString(keys[i].trim(), values[i]);
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
		
		// Check custom targeting
    	Bundle bundle = new Bundle();
    	StringBuffer bundleString = new StringBuffer();
    	int customTargetingCount = getCustomTargetExtras(bundle, bundleString);
    	
		if (isUsingDefault()) appendStatusText("Start loading with default ad unit id...");
		else appendStatusText("Start loading with ad unit id '"+getAdUnitId()+"'");
    	
	    switch (id) 
	    {
	    case R.id.Button320x50:
	    	adSize = AdSize.BANNER;
	    	strAdSize = "320x50";
	    	break;
	    case R.id.Button300x250:
	    	adSize = AdSize.MEDIUM_RECTANGLE;
	    	strAdSize = "300x250";
	    	break;
	    case R.id.Button468x60:
	    	adSize = AdSize.FULL_BANNER;
	    	strAdSize = "468x60";
	    	break;
	    case R.id.Button728x90:
	    	adSize = AdSize.LEADERBOARD;
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
	    	// create new listener for interstitial	    	
	    	AdListener interstitialListener = new AdListener() {
	    		public void onAdLoaded()
	    		{
	    			displayInterstitial();
	    		}
	    		public void onAdFailedToLoad(int errorCode)
	    		{
	    			failToLoad(errorCode);
	    		}
	    		public void onAdClosed()
	    		{
	    			closeInterstitial();
	    		}
	    		
	    	};
	    	
	    	if (isUsingDfp())
	    	{
	    		// [Interstitial. DFP.]
	    	    dfpInterstitial = new PublisherInterstitialAd(this);
	    	    dfpInterstitial.setAdUnitId(this.getAdUnitId());
		    	// set listener
		    	dfpInterstitial.setAdListener(interstitialListener);
	    	    // Create ad builder
	    	    Builder adBuilder = new PublisherAdRequest.Builder();
	    	  
	        	if (customTargetingCount >0) 
	        	{
	        		adBuilder.addNetworkExtras(new AdMobExtras(bundle));
	        		appendStatusText("Set "+customTargetingCount+" custom value(s). - "+bundleString.toString());
	        	}
	        	
	    	    // Begin loading your interstitial
	        	PublisherAdRequest adRequest = adBuilder.build();
	    	    dfpInterstitial.loadAd(adRequest);
	    	    
	    	    sendAnalytics("DFP","Interstitial");
	    	}
	    	else
	    	{
	    		// [Interstitial. AdMob.]
		    	interstitial = new InterstitialAd(this);
		    	interstitial.setAdUnitId(this.getAdUnitId());
		    	// set listener
		    	interstitial.setAdListener(interstitialListener);	
		    	// Create ad request
		    	AdRequest adRequest = new AdRequest.Builder().build();
		    	
		    	// Begin loading your interstitial
		    	interstitial.loadAd(adRequest);
		    	
		   
		    	if (isUsingDefault()) sendAnalytics("Default","Interstitial"); else sendAnalytics("AdMob","Interstitial");
	    	}
	    	break;
	    	
	      default:
	    	break;
	    }
	    
	    // If it is not interstitial, load from here.
	    if (id != R.id.ButtonInterstitial)
	    {
	    	// create new listener for banner
	    	AdListener bannerListener = new AdListener() {
	    		public void onAdLoaded()
	    		{
	    			succeedInLoading();
	    		}
	    		public void onAdFailedToLoad(int errorCode)
	    		{
	    			failToLoad(errorCode);
	    		}
	    	};
	    	
	    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.bannersLayout);
			if (isUsingDfp()) 
			{	
				// [Banner. DFP.]
				dfpAdView = new PublisherAdView(this);
				dfpAdView.setAdSizes(adSize);
				dfpAdView.setAdUnitId(getAdUnitId());
				dfpAdView.setAdListener(bannerListener);
		    	layout.addView(dfpAdView);
		    	//dfpAdView.setAdListener(this);
	    	    // Create ad builder
	    	    Builder adBuilder = new PublisherAdRequest.Builder();
	    	  
	        	if (customTargetingCount >0) 
	        	{
	        		adBuilder.addNetworkExtras(new AdMobExtras(bundle));
	        		appendStatusText("Set "+customTargetingCount+" custom value(s). - "+bundleString.toString());
	        	}
	        	
	    	    // Begin loading your interstitial
	        	PublisherAdRequest adRequest = adBuilder.build();
		    	appendStatusText("Set ad size - "+strAdSize);
		    	dfpAdView.loadAd(adRequest);
		    	
		    	if (isUsingDefault()) sendAnalytics("Default", strAdSize); else sendAnalytics("DFP", strAdSize);
			}
			else
			{
				// [Banner. AdMob.]
				adView = new AdView(this);
				adView.setAdSize(adSize);
				adView.setAdUnitId(getAdUnitId());
				adView.setAdListener(bannerListener);
		    	layout.addView(adView);
		    	appendStatusText("Set ad size - "+strAdSize);
		    	
		    	AdRequest adRequest = new AdRequest.Builder().build();
		    	adView.loadAd(adRequest);
		    	if (isUsingDefault()) sendAnalytics("Default", strAdSize); else sendAnalytics("AdMob", strAdSize);
			}
	    }
	}
	
	// Invoke displayInterstitial() when you are ready to display an interstitial.
	public void displayInterstitial() 
	{
		boolean isLoaded = true;
		
		if (!isUsingDfp())
	    {
	      if (interstitial.isLoaded()) interstitial.show();
	      else isLoaded = false;
	    }
		if (isUsingDfp())
		{
			if (dfpInterstitial.isLoaded()) dfpInterstitial.show();
			else isLoaded = false;
		}
	    
	    if (isLoaded) appendStatusText("Loaded interstitial successfully.");
	    else appendStatusText("Failed to interstitial load.");
	}
	
	// when ads (excl. interstitial) are loaded.
	public void succeedInLoading()
	{
		appendStatusText("Loaded successfully.");
	}
	
	// when loading is failed.
	public void failToLoad(int errorCode) 
	{
		String strErrorMsg = "ErrorCode="+String.valueOf(errorCode);
		switch (errorCode)
		{
			case AdRequest.ERROR_CODE_INTERNAL_ERROR:
				strErrorMsg = "Internal Error";
				break;
			case AdRequest.ERROR_CODE_INVALID_REQUEST:
				strErrorMsg = "Invalid Request";
				break;
			case AdRequest.ERROR_CODE_NETWORK_ERROR:
				strErrorMsg = "Network Error";
				break;
			case AdRequest.ERROR_CODE_NO_FILL:
				strErrorMsg = "No Fill";
				break;
			default:
				break;	
		}
		
	    appendStatusText("Failed To receive ad. - " + strErrorMsg);		
	}
	    
	// when close ad
	public void closeInterstitial()
	{
		appendStatusText("Closed.");
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