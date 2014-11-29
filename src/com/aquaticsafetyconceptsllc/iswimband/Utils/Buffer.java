package com.aquaticsafetyconceptsllc.iswimband.Utils;

public class Buffer {
	private int readPos;
	private int writePos;
	private int capacity;
	private byte[] bytes;
	public Buffer(int capacity) {
		bytes = new byte[capacity];
		readPos = 0;
		writePos = 0;
	}
	synchronized public int length() {
		return writePos - readPos;
	}
	synchronized public void put(byte[] data) {
		int dataLen = data.length;
		if (writePos + dataLen > capacity)
			return;
		System.arraycopy(data, 0, bytes, writePos, dataLen);
	}
	
	synchronized public int get(byte[] dst, int length) {
		int len = length;
		if (writePos - readPos < length)
			len = writePos - readPos;
		System.arraycopy(bytes, readPos, dst, 0, len);
		
		readPos += len;
		if (readPos == writePos) {
			readPos = 0;
			writePos = 0;
		} else {
			
			// shift remains
			int j = 0;
			for (int i = readPos; i < writePos; i++) {
				bytes[j] = bytes[i];
				j++;
			}
			readPos = 0;
			writePos = readPos + j;
		}
		return len;
	}
}