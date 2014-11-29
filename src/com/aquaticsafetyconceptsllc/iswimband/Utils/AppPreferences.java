package com.aquaticsafetyconceptsllc.iswimband.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
	private static AppPreferences _instance = null;
	public static final String APP_SHARED_PREFS = "iSwimband";
	
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mSharedPreferencesEditor;
	
	private AppPreferences(Context context) {
		mSharedPreferences = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		mSharedPreferencesEditor = mSharedPreferences.edit();
	}
	
	public static AppPreferences sharedInstance(Context context) {
		if (_instance == null) 
			_instance = new AppPreferences(context);
		return _instance;
	}
	
	public void setString(String key, String value) {
		mSharedPreferencesEditor.putString(key, value);
		mSharedPreferencesEditor.commit();
	}
	
	public String getString(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
	
	public void setBoolean(String key, Boolean value) {
		mSharedPreferencesEditor.putBoolean(key, value);
		mSharedPreferencesEditor.commit();
	}
	
	public Boolean getBoolean(String key, Boolean defValue) {
		return mSharedPreferences.getBoolean(key, defValue);
	}
}
