package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;

import com.aquaticsafetyconceptsllc.iswimband.Utils.CommonUtils;

public class ClientConnectRequestPacket extends NetPacket {
	
	class PacketData
	{
	    short clientNameLen;
	    short clientIDLen;
	};
	
	private String _clientName;
    private String _clientID;
	
	public String clientName() {
		return _clientName;
	}
	public void setClientName(String clientName) {
		this._clientName = clientName;
	}
	
	public String clientID() {
		return _clientID;
	}
	public void setClientID(String clientID) {
		this._clientID = clientID;
	}
	
	public ClientConnectRequestPacket() {
		super(PacketType.kPacketType_ClientConnectRequest);
	}
	
	@Override
	protected byte[] _pack() {
		// Order of packed data should start with oldest ancestor
	    byte[] data = super._pack();
	    
	    ConnectionNameData nameData = new ConnectionNameData();
	    
	    nameData.setConnectionName(_clientName);
	    nameData.setConnectionID(_clientID);
	    
	    byte[] nameDataBytes = nameData.pack();
	    
	    data = CommonUtils.appendData(data, nameDataBytes);
	    
	    return data;
	}

	@Override
	protected int _unpack(byte[] data, int offset) throws NetPacketException {
	    offset = super._unpack(data, offset);
        ConnectionNameData nameData = new ConnectionNameData();
        offset = nameData.unpack(data, offset);
                
        _clientName = nameData.connectionName();
        _clientID = nameData.connectionID();

	    return offset;
	}
	
	
}
