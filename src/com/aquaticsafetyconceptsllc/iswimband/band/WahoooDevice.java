package com.aquaticsafetyconceptsllc.iswimband.band;

import java.util.Collection;
import java.util.HashMap;

import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket;

public class WahoooDevice {
	 private String _name;
	 private String  _id;
	 private NetConnection _connection;
	 private HashMap<String, Object>    _bands;
	 
	 public String name() {
		 return _name;
	 }
	 public void setName(String val) {
		 _name = val;
	 }
	 
	 public NetConnection connection() {
		 return _connection;
	 }
	 
	 public String deviceID() {
		 return _id;
	 }
	 
	 public WahoooDevice(String name, String deviceID){
	     super();
	     _name = name;
	     _id = deviceID;
	     _bands = new HashMap<String, Object>();
	 }

	 public void disconnect() {
		 /*
	     if ( _connection )
	     {
	    	 Collection<Object> bands = _bands.values();	         
	         for( RemoteBand band : bands )
	         {
	             [[WahoooBandManager sharedManager] removeRemoteBand:band];
	         }
	         
	         [_bands removeAllObjects];
	     }
	     */
	 }

	 public void lostConnection() {
		 /*
	     NSArray* values = [_bands allValues];
	     
	     for ( RemoteBand* band in values )
	     {
	         [band setBandState:kWahoooBandState_NotConnected];
	     }
	     */
	 }

	 public void processPacket(NetPacket packet) {
		 /*
	     switch( packet.type )
	     {
	         case kPacketType_BandData:
	         {
	             BandDataPacket* bandData = (BandDataPacket*)packet;
	             RemoteBand* remoteBand = [_bands objectForKey:bandData.bandID];
	             
	             if ( bandData.state == kWahoooBandState_NotConnected )
	             {
	                 if ( remoteBand )
	                 {
	                     [[WahoooBandManager sharedManager] removeRemoteBand:remoteBand];
	                 }
	                     
	                 [_bands removeObjectForKey:bandData.bandID];
	                 
	                 return;
	             }
	             
	             if ( remoteBand == nil )
	             {
	                 remoteBand = [[RemoteBand alloc] init];
	                 
	                 remoteBand.parentDevice = self;
	                 
	                 [_bands setObject:remoteBand forKey:bandData.bandID];
	                 
	                 [[WahoooBandManager sharedManager] addRemoteBand:remoteBand];
	                 
	             }
	             
	             remoteBand.bandState = bandData.state;
	             
	             remoteBand.batteryLevel = bandData.batteryLevel;
	             
	             remoteBand.bandID = bandData.bandID;
	             
	             remoteBand.name = bandData.bandName;
	             
	         }
	             break;
	     }
	     */
	 }
}
