package com.aquaticsafetyconceptsllc.iswimband.band;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.BandDataPacket;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket;

public class WahoooDevice {
	 private String _name;
	 private String  _id;
	 private NetConnection _connection;
	 private HashMap<String, RemoteBand>    _bands;
	 
	 public String name() {
		 return _name;
	 }
	 public void setName(String val) {
		 _name = val;
	 }
	 
	 public NetConnection connection() {
		 return _connection;
	 }
	public void setConnection(NetConnection connection) {
		_connection = connection;
	}
	 
	 public String deviceID() {
		 return _id;
	 }
	 
	 public WahoooDevice(String name, String deviceID) {
	     super();
	     _name = name;
	     _id = deviceID;
	     _bands = new HashMap<String, RemoteBand>();
	 }

	 public void disconnect() {
	     if ( _connection != null ) {
	    	 Collection<RemoteBand> bands = _bands.values();
	         for( RemoteBand band : bands ) {
	             WahoooBandManager.sharedManager().removeRemoteBand(band);
	         }
	         
	         _bands.clear();
	     }
	 }

	 public void lostConnection() {

		 Collection<RemoteBand> values = _bands.values();
	     
	     for ( RemoteBand band : values ) {
	         band.setBandState(WahoooBand.WahoooBandState_t.kWahoooBandState_NotConnected);
	     }
	 }

	 public void processPacket(NetPacket packet) {
	     switch( packet.type ) {
	         case kPacketType_BandData: {
	             BandDataPacket bandData = (BandDataPacket)packet;
	             RemoteBand remoteBand = _bands.get(bandData.bandID);
	             
	             if ( bandData.state == WahoooBand.WahoooBandState_t.kWahoooBandState_NotConnected.getValue() ) {
	                 if ( remoteBand != null ) {
	                     WahoooBandManager.sharedManager().removeRemoteBand(remoteBand);
	                 }
	                 _bands.remove(bandData.bandID);
	                 return;
	             }
	             
	             if ( remoteBand == null ) {
	                 remoteBand = new RemoteBand();
	                 remoteBand.setParentDevice(this);
	                 
	                 _bands.put(bandData.bandID, remoteBand);
	                 WahoooBandManager.sharedManager().addRemoteBand(remoteBand);
	             }
	             
	             remoteBand.setBandState(WahoooBand.WahoooBandState_t.fromInt(bandData.state));
	             
	             remoteBand.setBatteryLevel(bandData.batteryLevel);
	             
	             remoteBand.setBandID(bandData.bandID);
	             
	             remoteBand.setName(bandData.bandName);
	             
	         }
	             break;
	     }
	 }
}
