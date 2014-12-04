package com.aquaticsafetyconceptsllc.iswimband.Ble;

import java.util.List;
import java.util.UUID;

import com.aquaticsafetyconceptsllc.iswimband.CoreData.CoreDataManager;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class BlePeripheral {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothDevice mBluetoothDevice;
    private int _rssi;
    public long scannedTime;
    
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    
    public BlePeripheralDelegate delegate;
    
    private String mAddress;
    private String _name;

    
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    
 // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                
                mConnectionState = STATE_CONNECTED;
                if (delegate != null)
                	delegate.gattConnected(BlePeripheral.this);
                
                Logger.log("Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Logger.log("Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                
                mConnectionState = STATE_DISCONNECTED;
                Logger.log("Disconnected from GATT server.");
                
                if (delegate != null)
                	delegate.gattDisconnected(BlePeripheral.this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (delegate != null)
                	delegate.gattServicesDiscovered(BlePeripheral.this);
            } else {
                Logger.log("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (delegate != null)
                	delegate.gattDataAvailable(BlePeripheral.this, characteristic, characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
           if (delegate != null)
        	   delegate.gattDataAvailable(BlePeripheral.this, characteristic, characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            _rssi = rssi;
            if (delegate != null)
                delegate.gattReadRemoteRssi(BlePeripheral.this, rssi);
        }
    };
    
    public BlePeripheral(Context context, String address) {
        this.mContext = context;
        this.mBluetoothDevice = null;
    	this.mAddress = address;

        this.scannedTime = System.currentTimeMillis();

        mBluetoothAdapter = BleManager.sharedInstance().bluetoothAdapter();
        if (mBluetoothAdapter == null) {
            Logger.log("BlePeripheral(%s) - blemanager.bluetoothadapter is null", address);
            return;
        }

        this.mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress);
        if (this.mBluetoothDevice == null) {
            Logger.log("BlePeripheral(%s) - bluetoothadapter.getremotedevice is null", address);
            return;
        }

        String serialno = CoreDataManager.sharedInstance().getDeviceSerialNo(mAddress);
        if (serialno == null)
            this._name = this.mBluetoothDevice.getName();
        else
            this._name = getNameWithSerialNo(serialno);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * 
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect() {
        if (mBluetoothAdapter == null || mAddress == null) {
            Logger.log("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && mAddress.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Logger.log("Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress);
        if (device == null) {
            Logger.log("Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Logger.log("Trying to create a new connection.");
        mBluetoothDeviceAddress = mAddress;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.log("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public int connectionState() {
        return mConnectionState;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.log("BluetoothAdapter not initialized");
            return;
        }
        Logger.log("BlePeripheral.readCharacteristic (%s)", characteristic.getUuid().toString());
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.log("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public String name() {
        return _name;
    }

    public void setSerialNo(String serialNo) {
        _name = getNameWithSerialNo(serialNo);
        CoreDataManager.sharedInstance().saveDeviceSerialNo(mAddress, serialNo);
    }

    public String address() {
        return mAddress;
    }

    public int rssi() {
        return _rssi;
    }

    public void updateRSSI() {
        if (mConnectionState == STATE_CONNECTED)
            mBluetoothGatt.readRemoteRssi();
    }

    protected String getNameWithSerialNo(String serialno) {
        return String.format("iSwimband-%s", serialno);
    }
}
