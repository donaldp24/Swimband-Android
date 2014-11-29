package com.aquaticsafetyconceptsllc.iswimband.Ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import de.greenrobot.event.EventBus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BleManager implements BleScannerListener, BlePeripheralDelegate {

    protected static BleManager _instance = null;
    /*!
     *  \brief Posted when BLEManager receives advertisement from a peripheral.
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     *  - kBLEManagerAdvertisementDataKey
     */
    public static final String kBLEManagerDiscoveredPeripheralNotification = "blemanager discovered peripheral notification";

    /*!
     *  \brief  Posted when BLEManager removes old advertising peripherals.
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     *
     *  This notification posted as a result of calling BLEManager::purgeAdvertisingDevices:
     */
    public static final String kBLEManagerUndiscoveredPeripheralNotification = "blemanager undiscovered peripheral notification";

    /*!
     *  \brief  Posted when a BLEPeripheral connects
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     */
    public static final String kBLEManagerConnectedPeripheralNotification = "blemanager connected peripheral notification";

    /*!
     *  \brief Posted when a BLEPeripheral disconnects
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     */
    public static final String kBLEManagerDisconnectedPeripheralNotification = "blemanager disconnected peripheral notification";

    /*!
     *  \brief Posted when BLEPeripheral fails to connect
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     */
    public static final String kBLEManagerPeripheralConnectionFailedNotification = "blemanager peripheral connection failed notification";

    /*!
     *  \brief Posted when Bluetooth Central restoration occurs and a connected peripheral is restored
     *
     *  userInfo keys:
     *  - kBLEManagerPeripheralKey
     *  - kBLEManagerManagerKey
     */
    public static final String kBLEManagerRestoredConnectedPeripheralNotification = "blemanager restored connected peripheral notification";

    public static final String kBLEManagerPeripheralServiceDiscovered = "blemanager service discovered";

    public static final String kBLEManagerPeripheralDataAvailable = "blemanager data available";
    public static final String kBLEManagerPeripheralRssiUpdated = "blemanager rssi updated";

    /*!
     *  \brief  Notification posted when Bluetooth state changes
     */
    public static final String kBLEManagerStateChanged = "blemanager state changed";

    protected BluetoothAdapter mBluetoothAdapter;
    protected Context mContext;

    protected ArrayList<BlePeripheral> scannedPeripherals;
    protected HashMap<BlePeripheral, Long> peripheralScannedTime;
    protected ArrayList<UUID> services;
    protected boolean mScanStarted;

    public static class CharacteristicData {
        public BlePeripheral peripheral;
        public BluetoothGattCharacteristic characteristic;
        public byte[] value;
    }


    public static BleManager initialize(Context context) {
        if (_instance == null)
            _instance = new BleManager(context);
        return _instance;
    }

    public static BleManager sharedInstance() {
        return _instance;
    }

    private BleManager(Context context) {

        EventBus.getDefault().register(this);

        this.mContext = context;
        checkBluetoothAdapter();
        BleScanner.initialize(context);

        scannedPeripherals = new ArrayList<BlePeripheral>();
        peripheralScannedTime = new HashMap<BlePeripheral, Long>();
        mScanStarted = false;
    }

    private void checkBluetoothAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Logger.log("checkBluetoothAdapter - bluetooth adapter is null, bluetooth is not available");
        } else {
            Logger.log("checkBluetoothAdapter - bluetooth adapter - bluetooth is available");
        }
    }

    public BluetoothAdapter bluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean isBleAvailable() {
        if (!isBleSupported())
            return false;

        if (mBluetoothAdapter == null)
            return false;

        return true;
    }

    public boolean isBleEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isBleSupported() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void onEvent(SEvent e) {
        if (e.name.equals(SEvent.EVENT_BLUETOOTH_STATE_CHANGED)) {
            Integer obj = (Integer)e.object;
            int state = obj.intValue();

            Logger.log(String.format("BleManager : bluetooth state changed : %d", state));

            EventBus.getDefault().post(new SEvent(kBLEManagerStateChanged, obj));
        }
    }

    public void scanForPeripheralsWithServices(ArrayList<UUID> services, boolean allowDuplicates) {
        scannedPeripherals.clear();
        peripheralScannedTime.clear();
        this.services = services;

        BleScanner.sharedInstance().listner = this;
        BleScanner.sharedInstance().start();

        mScanStarted = true;
    }

    public void stopScan() {
        BleScanner.sharedInstance().stop();

        mScanStarted = false;
    }

    @Override
    public void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Long currentTime = System.currentTimeMillis();
        List list = Collections.synchronizedList(scannedPeripherals);
        BlePeripheral existPeripheral = null;
        synchronized(list) {
            Iterator i = list.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                BlePeripheral peripheral = (BlePeripheral)i.next();
                if (peripheral.address().equalsIgnoreCase(device.getAddress())) {
                    existPeripheral = peripheral;
                    break;
                }
            }

            if (existPeripheral != null) {
                peripheralScannedTime.put(existPeripheral, currentTime);
            } else {
                existPeripheral = new BlePeripheral(mContext, device.getAddress());
                existPeripheral.delegate = this;
                list.add(existPeripheral);
                peripheralScannedTime.put(existPeripheral, currentTime);
            }
        }

        EventBus.getDefault().post(new SEvent(kBLEManagerDiscoveredPeripheralNotification, existPeripheral));
    }

    @Override
    public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        ArrayList<UUID> uuids = parseUuids(scanRecord);
        boolean isPeripheral = false;
        for (UUID uuid : uuids) {
            if (services.contains(uuid)) {
                isPeripheral = true;
                break;
            }
        }
        if (isPeripheral) {
            Logger.log("checking device : iSwimband(%s)(%s), true", device.getAddress(), device.getName());
        }
        else {
            Logger.log("checking device : iSwimband(%s)(%s), false", device.getAddress(), device.getName());
        }
        return isPeripheral;
    }

    public boolean connectPeripheral(BlePeripheral peripheral) {
        Logger.log("connectPeripheral (%s) (%s) - calling peripheral.connect()", peripheral.address(), peripheral.name());
        boolean ret = peripheral.connect();
        if (ret == false) {
            Logger.log("connectPeripheral (%s) (%s) - calling peripheral.connect() - returned false", peripheral.address(), peripheral.name());
        }
        return ret;
    }

    public void disconnectPeripheral(BlePeripheral peripheral) {
        Logger.log("disconnectPeripheral (%s) (%s) - calling peripheral.disconnect()", peripheral.address(), peripheral.name());
        peripheral.disconnect();
    }

    public void purgeAdvertisingDevices(int duration) {
        Long currentTime = System.currentTimeMillis();
        List list = Collections.synchronizedList(scannedPeripherals);
        ArrayList<BlePeripheral> purgingDevices = new ArrayList<BlePeripheral>();
        synchronized(list) {
            Iterator i = list.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                BlePeripheral peripheral = (BlePeripheral)i.next();
                Long scannedTime = peripheralScannedTime.get(peripheral);
                if (scannedTime != null && (currentTime - scannedTime) >= TimeUnit.SECONDS.toMillis(duration)) {
                    purgingDevices.add(peripheral);
                }
            }

            for (BlePeripheral purgingDevice : purgingDevices) {
                list.remove(purgingDevice);

                EventBus.getDefault().post(new SEvent(kBLEManagerUndiscoveredPeripheralNotification, purgingDevice));
            }
        }
    }

    private ArrayList<UUID> parseUuids(byte[] advertisedData) {
        ArrayList<UUID> uuids = new ArrayList<UUID>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return uuids;
    }

    @Override
    public void gattConnected(BlePeripheral peripheral) {
        EventBus.getDefault().post(new SEvent(kBLEManagerConnectedPeripheralNotification, peripheral));
    }

    @Override
    public void gattDisconnected(BlePeripheral peripheral) {
        EventBus.getDefault().post(new SEvent(kBLEManagerDisconnectedPeripheralNotification, peripheral));
    }

    @Override
    public void gattServicesDiscovered(BlePeripheral peripheral) {
        EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralServiceDiscovered, peripheral));
    }

    @Override
    public void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
        CharacteristicData data = new CharacteristicData();
        data.peripheral = peripheral;
        data.characteristic = characteristic;
        data.value = value;
        EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralDataAvailable, data));
    }

    @Override
    public void gattReadRemoteRssi(BlePeripheral peripheral, int rssi) {
        EventBus.getDefault().post(new SEvent(kBLEManagerPeripheralRssiUpdated, peripheral));
    }

    public static String getLongUuidFromShortUuid(String shortUuid) {
        return String.format("0000%s-0000-1000-8000-00805f9b34fb", shortUuid);
    }
}
