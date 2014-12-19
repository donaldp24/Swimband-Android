package com.aquaticsafetyconceptsllc.iswimband.band;

import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

import de.greenrobot.event.EventBus;

public class WahoooBand {
	public static enum WahoooBandType
	{
	    kWahoooBand_None,
	    kWahoooBand_Peripheral,
	    kWahoooBand_Remote
	}
	
	public static enum WahoooBandState_t
	{
	    kWahoooBandState_NotConnected(0),

	    kWahoooBandState_OutOfRange(1),

	    kWahoooBandState_Caution(2),
	    kWahoooBandState_Alarm(3),
	    
	    kWahoooBandState_Connecting(4),
	    kWahoooBandState_Connected(5),
	    
	    kWahoooBandState_OTAUpgrade(6),
	    
	    kWahoooBandState_Total(8);
	    
	    private int value;
	    WahoooBandState_t(int val) {
	    	value = val;
	    }
	    public int getValue() {
	    	return value;
	    }
	    
	    public boolean lessThan(WahoooBandState_t b) {
	    	if (this.value < b.getValue())
	    		return true;
	    	return false;
	    }

		public static WahoooBandState_t fromInt(int state) {
			if (state == 0)
				return kWahoooBandState_NotConnected;
			else if (state == 1)
				return kWahoooBandState_OutOfRange;
			else if (state == 2)
				return kWahoooBandState_Caution;
			else if (state == 3)
				return kWahoooBandState_Alarm;
			else if (state == 4)
				return kWahoooBandState_Connecting;
			else if (state == 5)
				return kWahoooBandState_Connected;
			else if (state == 6)
				return kWahoooBandState_OTAUpgrade;
			return kWahoooBandState_NotConnected;
		}
	    
	}
	
	public static final String kWahoooBandDataUpdatedNotification = "kWahoooBandDataUpdatedNotification";
	public static final String kWahoooBandAlertNotification = "kWahoooBandAlertNotification";
	public static final String kWahoooBandKey = "kWahoooBandKey";

	public static final float kOORRSSIValue = -94.f;
	public static final int   kMedianRSSIEntryCount = 5;
	public static final float kBaseRSSIValue = -100.f;
	public static final float kBaseDistance = 100.f;
	public static final Boolean  kUseMedianRSSI = true;

	public static enum WahoooAlertType
	{
	    kWahoooAlertType_NonSwimmer(0),
	    kWahoooAlertType_Swimmer(1),
	    
	    kWahoooAlertType_Total(2);

		int value;
		WahoooAlertType(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	}

	public static int WAHOOBAND_DEFAULT_ALERT_TIME = 20;
	public static int WAHOOBAND_DEFAULT_WARNING_TIME = 10;
	public static int WAHOOBAND_NONSWIMMER_ALERT_TIME = 5; //(3)
	
	
	protected WahoooBandState_t               _bandState;
    protected int                     _rssi;
    protected double                  _rssiVelocity;
    protected String                _bandID;
    protected int                     _updateProgress;
    protected float _range;

    protected int _warningTime;
    protected int _alertTime;
    protected WahoooAlertType _alertType;
    protected int _batteryLevel;
    
    protected String _fwVersion;
    protected String _name;
    protected WahoooBandType _type;
    
    public WahoooBand() {
		_alertTime = WAHOOBAND_DEFAULT_ALERT_TIME;

		// commented on 2014-12-20, Paul Newcomb required to allow only "immediate" for warningTime
		//_warningTime = WAHOOBAND_DEFAULT_WARNING_TIME;

		_warningTime = _alertTime;

		_bandState = WahoooBandState_t.kWahoooBandState_NotConnected;

		_name = "";

		_rssiVelocity = 0;
		_rssi = 0;
		_range = kBaseDistance;
		_bandID = "MISSING";
	}

	public WahoooBandState_t bandState() {
		return _bandState;
	}

	public void setBatteryLevel(int level) {
		_batteryLevel = level;
	}
	
	public String fwVersion() {
		return _fwVersion;
	}
	public void fwVersion(String fwVersion) {
		_fwVersion = fwVersion;
	}
	
	public String name() {
		return _name;
	}
	public void setName(String value) {
		_name = value;
	}
	
	public String displayName() {
		return _name;
	}
	
	public String defaultName() {
		return _name;
	}
	
	public String bandID() {
		return _bandID;
	}

	public WahoooAlertType alertType() {
		return _alertType;
	}
	public void setAlertType(WahoooAlertType type) {
		_alertType = type;
	}
	public void setAlertType(int type) {
		if (type == 0)
			setAlertType(WahoooAlertType.kWahoooAlertType_NonSwimmer);
		else
			setAlertType(WahoooAlertType.kWahoooAlertType_Swimmer);
	}
	
	public int warningTime() {
		return _warningTime;
	}
	public void setWarningTime(int warningTime) {
		_warningTime = warningTime;
	}
	
	public int alertTime() {
		return _alertTime;
	}
	public void setAlertTime(int alertTime) {
		_alertTime = alertTime;
	}
	
	public int batteryLevel() {
		return _batteryLevel;
	}
	
	public int RSSI() {
		return _rssi;
	}
	
	public double RSSIVelocity() {
		return _rssiVelocity;
	}
	
	public float range() {
		return _range;
	}
	
	public float normalRange() {
		float range = _range / kBaseDistance;

		range = 1.f - range;

		if (range < 0)
			range = 0;
		if (range > 1)
			range = 1;

		return range;
	}
	
	public WahoooBandType type() {
		return _type;
	}
	

	public static int sortValueForBandState(WahoooBandState_t state) {
		int[] values = {20, 1, 4, 2, 10, 10, 15};
	    
	    if ( state.lessThan(WahoooBandState_t.kWahoooBandState_Total)) {
	        return values[state.getValue()];
	    }
	    
	    return 1000;
	}

	public void changeName(String newName) {
		_name = newName;
	}

	public void disconnect() {
		Logger.log("WahoooBand.disconnect() - bandState - notconnected");
		 _bandState = WahoooBandState_t.kWahoooBandState_NotConnected;

		 //[[WahoooBandManager sharedManager] broadcastSingleBand:self];
	}

	public void panicAlert() {
		String message = null;
	    
	    if( _bandState != WahoooBandState_t.kWahoooBandState_OutOfRange )
	    {
			Logger.log("WahoooBand.panicAlert() - bandState - kWahoooBandState_Alarm");
	        _bandState = WahoooBandState_t.kWahoooBandState_Alarm;
	    }
	    
	    if( _bandState == WahoooBandState_t.kWahoooBandState_Alarm )
	    {
	        message = _underWaterMessage();
	    }
	    else if (_bandState == WahoooBandState_t.kWahoooBandState_OutOfRange )
	    {
	        message = _outOfRangeString();
	    }
	    else
	    {
	        return;
	    }


	    EventBus.getDefault().post(new SEvent(kWahoooBandAlertNotification, this));
	    EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
	}

	public void cancelPanicAlert() {
		//
	}

	public void startUpgrade() {
		//
	}

	public int upgradeProgress() {
		return 0;
	}
	
	private String _underWaterMessage() {
	
	    String format = "Device %s is under water!";
	    return String.format(format, displayName());
	}

	private String _outOfRangeString() {
	    
	    String format = "Device %s is out of range!";
	    return String.format(format, displayName());
	}


}
