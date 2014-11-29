package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;

import java.io.UnsupportedEncodingException;

import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacketException.NetPacketErrorCode;
import com.aquaticsafetyconceptsllc.iswimband.Utils.CommonUtils;


public class ConnectionNameData {
	
	class NameData
	{
	    short    nameLen;
	    short    idLen;
	    
	    public int length() {
	    	return 4;
	    }
	}
	
	protected String       _name;
    protected String       _id;
    
    public String connectionName() {
		return _name;
	}
	public String connectionID() {
		return _id;
	}
	public void setConnectionName(String val) {
		_name = val;
	}
	public void setConnectionID(String val) {
		_id = val;
	}
	
	public byte[] pack() {
		 NameData nameData = new NameData();
		byte[] nameBuffer = null;
		byte[] idBuffer = null;
		
		nameData.nameLen = 0;
		nameData.idLen = 0;
		
		if ( _name != null && _name.length() > 0 )
		{
		    try {
				nameBuffer = _name.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    nameData.nameLen = (short)nameBuffer.length;
		}
		
		if ( _id != null && _id.length() > 0 )
		{
		    try {
				idBuffer = _id.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    nameData.idLen = (short)idBuffer.length;
		}
		
		nameData.nameLen = CommonUtils.CFSwapInt16HostToLittle(nameData.nameLen);
		nameData.idLen = CommonUtils.CFSwapInt16HostToLittle(nameData.idLen);
		
		byte[] nameBytes = new byte[4];
		int offset = 0;
		offset = CommonUtils.packShort(nameData.nameLen, nameBytes, 0);
		offset = CommonUtils.packShort(nameData.idLen, nameBytes, offset);
		
		byte[] data = new byte[0];
		data = CommonUtils.appendData(data, nameBytes);
		
		if ( nameBuffer != null )
		    data = CommonUtils.appendData(data, nameBuffer);
		
		if ( idBuffer != null )
			data = CommonUtils.appendData(data, idBuffer);
		
		return data;
	}

	public int unpack(byte[] data, int offset) throws NetPacketException {
		int availableBytes = data.length - offset;
	    NameData nameData = new NameData();
	    
	    if ( availableBytes < nameData.length() )
	        throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InsufficientData);
	    
	    nameData.nameLen = CommonUtils.getHostShort(data[offset], data[offset + 1]);
	    nameData.idLen = CommonUtils.getHostShort(data[offset + 2], data[offset + 3]);
	    
	    // Move head
	    offset += nameData.length();
	    
	    if( nameData.nameLen > 0 )
	    {
	        _name = NetPacket.extractUTFString(data, offset, nameData.nameLen);
	        offset += nameData.nameLen;
	    }
	    
	    if ( nameData.idLen > 0 )
	    {
	        _id = NetPacket.extractUTFString(data, offset, nameData.idLen);
	        offset += nameData.idLen;
	    }
	    return offset;
	}
}
