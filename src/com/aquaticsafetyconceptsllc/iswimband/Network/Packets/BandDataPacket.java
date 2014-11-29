package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;

import java.util.Arrays;

import com.aquaticsafetyconceptsllc.iswimband.Utils.CommonUtils;


public class BandDataPacket extends NetPacket {

	public static short BANDDATAPACKET_VERSION_0 = 0;
	public static short BANDDATAPACKET_CURRENT_VERSION = BANDDATAPACKET_VERSION_0;
	
	public class BandData
	{
	    short version;
	    byte state;
	    byte batteryLevel;
	    byte updateProgress;
	    byte[] reserved = new byte[3];
	    
	    public byte[] pack() {
	    	byte[] ret = new byte[2 + 1 + 1 + 1 + 3];
	    	int offset = CommonUtils.packShort(version, ret, 0);
	    	ret[offset] = state; offset++;
	    	ret[offset] = batteryLevel; offset++;
	    	ret[offset] = updateProgress; offset++;
	    	ret[offset] = reserved[0]; offset++;
	    	ret[offset] = reserved[1]; offset++;
	    	ret[offset] = reserved[2]; offset++;
	    	return ret;
	    }
	}
	
	
	public String bandName;
	public String bandID;
	public int batteryLevel;
	public int state;
	public int updateProgress;
	
	public BandDataPacket() {
		super(PacketType.kPacketType_BandData);
	}
	
	@Override
	protected byte[] _pack() {
	    byte[] data = super._pack();
	    
	    BandData bandData = new BandData();
	    
	    bandData.version = BANDDATAPACKET_CURRENT_VERSION;
	    bandData.version = CommonUtils.CFSwapInt16HostToLittle(bandData.version);
	    bandData.state = (byte)state;
	    bandData.batteryLevel = (byte)batteryLevel;
	    bandData.updateProgress = 0;
	    Arrays.fill(bandData.reserved, (byte) 0);
	    
	    byte[] bandDataBytes = bandData.pack();
	    data = CommonUtils.appendData(data, bandDataBytes);
	    
	    ConnectionNameData nameData = new ConnectionNameData();
	    nameData.setConnectionName(bandName);
	    nameData.setConnectionID(bandID);
	    
	    data = CommonUtils.appendData(data, nameData.pack());
	    return data;
	}


}
