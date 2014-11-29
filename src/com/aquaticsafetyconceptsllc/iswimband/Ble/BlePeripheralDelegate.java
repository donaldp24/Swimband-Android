package com.aquaticsafetyconceptsllc.iswimband.Ble;

import android.bluetooth.BluetoothGattCharacteristic;

public interface BlePeripheralDelegate {
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    
    public void gattConnected(BlePeripheral peripheral);
    public void gattDisconnected(BlePeripheral peripheral);
    public void gattServicesDiscovered(BlePeripheral peripheral);
    public void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value);
    public void gattReadRemoteRssi(BlePeripheral peripheral, int rssi);
}
