package com.aquaticsafetyconceptsllc.iswimband.Network.Packets;


public class NetPacketException extends Exception {
	
	public static enum NetPacketErrorCode
	{
	    kNetPacketError_None,
	    kNetPacketError_InvalidHeaderTag,
	    kNetPacketError_InsufficientData,
	    kNetPacketError_InvalidData,
	    kNetPacketError_InvalidCheckSum,
	    kNetPacketError_UnsupportedVersion,
	    kNetPacketError_UnknownPacketType
	};
	
	private NetPacketErrorCode errorCode;
	
	public NetPacketException(NetPacketErrorCode errorCode) {
		super(getStringForErrorCode(errorCode));
		this.errorCode = errorCode;
	}
	
	public NetPacketErrorCode getErrorCode() {
		return this.errorCode;
	}
	
	private static String getStringForErrorCode(NetPacketErrorCode errorCode) {
		switch (errorCode) {
		case kNetPacketError_None:
			return "None";
		case kNetPacketError_InvalidHeaderTag:
			return "kNetPacketError_InvalidHeaderTag";
		case kNetPacketError_InsufficientData:
			return "kNetPacketError_InsufficientData";
		case kNetPacketError_InvalidData:
			return "kNetPacketError_InvalidData";
		case kNetPacketError_InvalidCheckSum:
			return "kNetPacketError_InvalidCheckSum";
		case kNetPacketError_UnsupportedVersion:
			return "kNetPacketError_UnsupportedVersion";
		case kNetPacketError_UnknownPacketType:
			return "kNetPacketError_UnknownPacketType";

		default:
			return "";
		}
	}
}