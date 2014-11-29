package com.aquaticsafetyconceptsllc.iswimband.Utils;

public class CommonUtils {

	public static int packInt(int val, byte[] data, int offset) {
    	data[offset] = (byte)((val & 0xFF000000) >> 24);
    	data[offset + 1] = (byte)((val & 0x00FF0000) >> 16);
    	data[offset + 2] = (byte)((val & 0x0000FF00) >> 8);
    	data[offset + 3] = (byte)((val & 0x000000FF));
    	return offset + 4;
    }
    
	public static int packShort(short val, byte[] data, int offset) {
    	data[offset] = (byte)((val & 0xFF00) >> 8);
    	data[offset + 1] = (byte)((val & 0x00FF) >> 16);
    	return offset + 2;
    }
	
	public static int getHostInt(byte[] little) {
		int ret = 0;
		ret = ((little[0] << 24) + (little[1] << 16) + (little[2] << 8) + little[3]);
		return ret;
	}
	
	public static int getHostInt(byte little0, byte little1, byte little2, byte little3) {
		int ret = 0;
		ret = ((little0 << 24) + (little1 << 16) + (little2 << 8) + little3);
		return ret;
	}
	
	public static short getHostShort(byte little0, byte little1) {
		short ret = 0;
		ret = (short)(((little0 << 8) + little1));
		return ret;
	}
	
	public static int CFSwapInt32HostToLittle(int value) {
		return ((value & 0xFF000000) >> 24) + ((value & 0x00FF0000) >> 8) + ((value & 0x0000FF00) << 8) + ((value & 0x000000FF) << 24);  
	}
	
	public static short CFSwapInt16HostToLittle(short value) {
		return (short)(((value & 0xFF00) >> 8) + ((value & 0x00FF) << 8));
	}
	
	public static byte[] appendData(byte[] a, byte[] b) {

	    byte[] ret = new byte[a.length + b.length];

	    System.arraycopy(a, 0, ret, 0, a.length);
	    System.arraycopy(b, 0, ret, a.length, b.length);
	    
	    return ret;
	}
    
}
