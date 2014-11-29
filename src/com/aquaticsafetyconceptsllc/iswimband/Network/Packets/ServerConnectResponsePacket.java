package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;

import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacketException.NetPacketErrorCode;
import com.aquaticsafetyconceptsllc.iswimband.Utils.CommonUtils;


public class ServerConnectResponsePacket extends NetPacket {
	public Boolean approved;
	public String serverName;
	public String serverID;
	
	public ServerConnectResponsePacket() {
		super(PacketType.kPacketType_ServerConnectResponse);
	}
	
	@Override
	protected byte[] _pack() {
	    byte[] data = super._pack();
	    
	    byte byte1 = (byte) ((this.approved == true)?0:1);
	    
	    ConnectionNameData nameData = new ConnectionNameData();
	    
	    nameData.setConnectionName(serverName);
	    nameData.setConnectionID(serverID);
	    
	    byte[] nameDataBytes = nameData.pack();
	    data = CommonUtils.appendData(data, nameDataBytes);
	    byte[] b1 = {byte1};
	    data = CommonUtils.appendData(data, b1);
	    
	    return data;
	}

	@Override
	protected int _unpack(byte[] data, int offset) throws NetPacketException {
	    offset = super._unpack(data, offset);
	    
        int availableBytes = 0;

        byte byte1;
        ConnectionNameData nameData = new ConnectionNameData();
        
        offset = nameData.unpack(data, offset);

        serverName = nameData.connectionName();
        serverID = nameData.connectionID();
        
        availableBytes = data.length - offset;
        
        if ( availableBytes < 1 /* sizeof(byte1) */ )
            throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InsufficientData);

        byte1 = data[offset];
        
        offset += 1 /*sizeof( byte1 )*/;
        
        approved = (byte1 == 0)?false:true;
    
    
	    return offset;
	}
}
