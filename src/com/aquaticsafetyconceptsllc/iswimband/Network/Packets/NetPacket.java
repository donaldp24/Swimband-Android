package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;

import java.io.UnsupportedEncodingException;


import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacketException.NetPacketErrorCode;
import com.aquaticsafetyconceptsllc.iswimband.Utils.CommonUtils;

public class NetPacket {

	public PacketType type;
	
	public static int NETPACKET_HEADER_TAG = 0x504E4469;//'iDNP';
	public static short NETPACKET_VERSION_1_0 = 1;
	public static short NETPACKET_HEADER_CURRENT_VERSION = NETPACKET_VERSION_1_0;
	
	public enum PacketType
	{
	    kPacketType_ClientConnectRequest(1),
	    
	    kPacketType_ServerConnectResponse(0x1000),
	    kPacketType_ServerStoppedSharing(0x1001),
	    kPacketType_ClientStoppedObserving(0x1002),
	    kPacketType_KeepAlive(0x1003),
	    
	    kPacketType_BandData(0x2000);
	    
	    int value;
	    PacketType(int value) {
	    	this.value = value;
	    }
	    public short getValue() {
	    	return (short)this.value;
	    }
	    public static PacketType fromValue(short value) {
	    	if (value == kPacketType_BandData.getValue())
	    		return kPacketType_BandData;
	    	else if (value == kPacketType_ServerConnectResponse.getValue())
	    		return kPacketType_ServerConnectResponse;
	    	else if (value == kPacketType_ServerStoppedSharing.getValue())
	    		return kPacketType_ServerStoppedSharing;
	    	else if (value == PacketType.kPacketType_ClientStoppedObserving.getValue())
	    		return kPacketType_ClientStoppedObserving;
	    	else if (value == PacketType.kPacketType_ClientConnectRequest.getValue())
	    		return kPacketType_ClientConnectRequest;
			return kPacketType_KeepAlive;
	    }
	}
	
	public static class NetPacketHeader {
		int    tag;
	    int    packetSize;
	    short    version;
	    short    type;
	    
	    public NetPacketHeader() {
	    	//
	    }
	    
	    public int length() {
	    	return 4 + 4 + 2 + 2;
	    }
	    
	    public byte[] pack() {
	    	byte[] ret = new byte[length()];
	    	int offset = 0;
	    	offset = CommonUtils.packInt(tag, ret, offset);
	    	offset = CommonUtils.packInt(packetSize, ret, offset);
	    	offset = CommonUtils.packShort(version, ret, offset);
	    	offset = CommonUtils.packShort(type, ret, offset);
	    	return ret;
	    }
	}
	
	public interface IFPacketAllocator {
		public NetPacket alloc(short packetType);
	}
	
	public static NetPacket unpack(byte[] data, IFPacketAllocator packetAllocator) throws NetPacketException {
		NetPacketHeader header = new NetPacketHeader();
	    NetPacket packet = null;
	    
	    // Not enough data
	    if ( data.length < header.length())
	        throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InsufficientData);

	    
	    header.tag = CommonUtils.getHostInt(data);
	    header.packetSize = CommonUtils.getHostInt(data[4], data[5], data[6], data[7]);
	    header.type = CommonUtils.getHostShort(data[8], data[9]);
	    header.version = CommonUtils.getHostShort(data[10], data[11]);
	    
	    
	    // Invalid header tag
	    if ( header.tag != NETPACKET_HEADER_TAG )
	    	throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InvalidHeaderTag);
	    
	    
	    // not enough data for payload
	    // Add one for checksum
	    if ( data.length < (header.length() + 1) )
	    	throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InsufficientData);
	    
	    //TODO:: Add load sub method for older packet versions
	    if( header.version != NETPACKET_HEADER_CURRENT_VERSION )
	    	throw new NetPacketException(NetPacketErrorCode.kNetPacketError_UnsupportedVersion);
	    
	    byte checkSum = _calculateCheckSum(data, header.packetSize);
	    
	    if ( checkSum != data[header.packetSize] )
	    	throw new NetPacketException(NetPacketErrorCode.kNetPacketError_InvalidCheckSum);
	    
	    if ( packetAllocator != null)
	    {
	        packet = packetAllocator.alloc(header.type);
	    }

	    if ( packet == null )
	    	throw new NetPacketException(NetPacketErrorCode.kNetPacketError_UnknownPacketType);
	    
	    
	    int offset = header.length();	    
	    offset = packet._unpack(data, offset);

	    
	    // Add one for the checksum byte
	    // *outBytes = offset + 1;
	       
	    return packet;
	}

	public static int scanForPacketHeader(byte[] data, int offset) {
		int currOffset = offset;
	    NetPacketHeader header = new NetPacketHeader();
	    while(currOffset < data.length )
	    {
	        int space = data.length - currOffset;
	        
	        if ( space < /*sizeof(header.tag)*/ 4 )
	        {
	            return -1;
	        }
	        
	        header.tag = (data[currOffset] << 24) + (data[currOffset + 1] << 16) + (data[currOffset + 2] << 8) + data[currOffset + 3];
	        if ( header.tag != NETPACKET_HEADER_TAG )
	        {
	            currOffset += 1;
	        }
	        else
	        {
	            return currOffset;
	        }
	    }

	    return -1;
	}

	public static String extractUTFString(byte[] data, int offset, int len) {
		byte[] stringBytes = new byte[len];
		System.arraycopy(data, offset, stringBytes, 0, len);
		String strRet = null;
		try {
			strRet = new String(stringBytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strRet;
	}

	public NetPacket(PacketType type) {
		this.type = type;
	}

	public byte[] pack() {
		NetPacketHeader header = new NetPacketHeader();
	    int packetSize = header.length();
	    header.tag = CommonUtils.CFSwapInt32HostToLittle(NETPACKET_HEADER_TAG);
	    header.type = CommonUtils.CFSwapInt16HostToLittle(this.type.getValue());
	    header.version = CommonUtils.CFSwapInt16HostToLittle(NETPACKET_HEADER_CURRENT_VERSION);
	    
	    byte[] payLoad = _pack();
	    
	    packetSize += payLoad.length;
	    
	    header.packetSize = CommonUtils.CFSwapInt32HostToLittle(packetSize);
	    
	    byte[] headerData = header.pack();
	    
	    byte[] packetData = new byte[headerData.length + payLoad.length + 1];

	    System.arraycopy(headerData, 0, packetData, 0, headerData.length);
	    System.arraycopy(payLoad, 0, packetData, headerData.length, payLoad.length);

	    byte checkSum = _calculateCheckSum(packetData, packetSize);
	    packetData[packetSize] = checkSum;    
	    return packetData;
	}


	// Only call this from child class
	protected byte[] _pack() {
		return new byte[0];
	}
	
	// Only call this from child class
	protected int _unpack(byte[] data, int offset) throws NetPacketException {
		return offset;
	}
	
	private static byte _calculateCheckSum(byte[] bytes, int length) {
		int index = 1;
	    byte checkSum = bytes[0];

	    
	    while( index < length )
	    {
	        checkSum ^= bytes[index++];
	    }
	    
	    return checkSum;
	}
}
