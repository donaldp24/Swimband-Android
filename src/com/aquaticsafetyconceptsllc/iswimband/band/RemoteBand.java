package com.aquaticsafetyconceptsllc.iswimband.band;

import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import de.greenrobot.event.EventBus;

public class RemoteBand extends WahoooBand {
	private WahoooDevice _parent;


	public WahoooDevice parentDevice() {
		return _parent;
	}
	public void setParentDevice(WahoooDevice parentDevice) {
		_parent = parentDevice;
	}
	
	public WahoooBandType type() {
	    return WahoooBandType.kWahoooBand_Remote;
	}

	public void setBandState(WahoooBandState_t state) {
	    WahoooBandState_t preState = _bandState;
	    
	    _bandState = state;
	    
	    if ( preState != _bandState ) {
	        if ( _bandState == WahoooBandState_t.kWahoooBandState_OutOfRange || _bandState == WahoooBandState_t.kWahoooBandState_Alarm ) {
	            panicAlert();
	        }
	        
	        EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
	        //[[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];
	    }
	}

	public void setBandID(String bid) {
	    _bandID = bid;
	}
}
