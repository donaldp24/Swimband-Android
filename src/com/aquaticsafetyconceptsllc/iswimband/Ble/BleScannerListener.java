package com.aquaticsafetyconceptsllc.iswimband.Ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by donaldpae on 11/24/14.
 */
public interface BleScannerListener {
    public void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord);
    public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
}
