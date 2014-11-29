package com.aquaticsafetyconceptsllc.iswimband.Utils;

import java.util.UUID;

import android.content.Context;
import android.os.Build;

public class Settings {
	
	private static Settings _instance = null;
	private Context context = null;
	private AppPreferences preferences;
	
	private String       _netServiceName;
    private String       _netServiceID;
    
    public static final String kNetServiceNameKey = "Settings_NetServiceNameKey";
    public static final String kNetServiceIDKey = "Settings_NetServiceIDKey";
    
    public static Settings initialize(Context context) {
    	if (_instance == null)
    		_instance = new Settings(context);
    	return _instance;
    }
    
    public static Settings sharedSettings() {
    	return _instance;
    }
    
    private Settings(Context context) {
    	this.context = context;
    	preferences = AppPreferences.sharedInstance(this.context);
    	
    	load();
    }
    
	public String netServiceName() {
		return _netServiceName;
	}
	
	public String netServiceID() {
		return _netServiceID;
	}
	
	public void setNetServiceName(String val) {
		this._netServiceName = val;
		save();
	}
	
	private void load() {
		_netServiceName = preferences.getString(kNetServiceNameKey, getDeviceName());
		_netServiceID = preferences.getString(kNetServiceIDKey, "");
		if (_netServiceID.length() == 0) {
			_netServiceID = UUID.randomUUID().toString();
			save();
		}
	}
	
	private void save() {
		preferences.setString(kNetServiceIDKey, _netServiceID);
		preferences.setString(kNetServiceNameKey, _netServiceName);
	}
	
	
	private String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

}
