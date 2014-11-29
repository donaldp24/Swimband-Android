package com.aquaticsafetyconceptsllc.iswimband.Ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

import java.util.ArrayList;

/**
 * Created by donaldpae on 11/24/14.
 */
public class BleScanner {
    private static BleScanner _instance = null;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    public BleScannerListener listner = null;
    private boolean isStarted = false;
    private boolean isForceStopped = false;

    private ArrayList<BluetoothDevice> scannedDevices;

    private Handler mHandler = new Handler();
    private boolean mStartPhase = true;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mStartPhase) {
                _stopScanLocally();
                if (!isForceStopped)
                    mHandler.postDelayed(this, 1000);
                mStartPhase = true;
            }
            else {
                if (!isForceStopped) {
                    _startScanLocally();
                    mHandler.postDelayed(this, 3000);
                }
                mStartPhase = false;
            }
        }
    };

    public static BleScanner initialize(Context context) {
        if (_instance == null)
            _instance = new BleScanner(context);
        return _instance;
    }

    public static BleScanner sharedInstance() {
        return _instance;
    }

    private BleScanner(Context context) {
        this.mContext = context;
        scannedDevices = new ArrayList<BluetoothDevice>();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start() {
        Logger.log("BleScanner start()");

        isForceStopped = false;
        mStartPhase = true;

        mHandler.post(mRunnable);
    }

    public void stop() {

        Logger.log("BleScanner stop()");

        isForceStopped = true;
    }

    protected void _startScanLocally() {
        synchronized (Thread.currentThread()) {
            if (isStarted) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Logger.log("BleScanner already started, stop and restart");
            }

            scannedDevices.clear();

            final BluetoothManager mBluetoothManager = (BluetoothManager) this.mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            if (mBluetoothAdapter == null) {
                Logger.log("BleScanner _startScanLocally() - mBluetoothAdapter == null, not started");
                return;
            }

            if (mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                Logger.log("BleScanner _startScanLocally() - started succesfully");
                isStarted = true;
            } else {
                Logger.log("BleScanner _startScanLocally() - cannot start successfully");
            }
        }
    }

    protected void _stopScanLocally() {
        Logger.log("BleScanner _stopScanLocally()");
        synchronized (Thread.currentThread()) {
            if (mBluetoothAdapter == null) {
                Logger.log("BleScanner _stopScanLocally() - mBluetoothAdapter == null, not stopped");
                return;
            }
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            isStarted = false;
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,byte[] scanRecord) {
            Logger.log("BleScanner mLeScanCallback - device(%s - %s)", device.getAddress(), device.getAddress());
            if (!scannedDevices.contains(device))
                scannedDevices.add(device);

            String strHex = "";
            for (int i = 0; i < scanRecord.length; i++) {
                strHex += String.format("%02X ", scanRecord[i]);
            }
            Logger.log(device.getAddress() + " - " + device.getName() + " : " + strHex);

            if (listner != null) {
                if (listner.shouldCheckDevice(device, rssi, scanRecord))
                    listner.deviceScanned(device, rssi, scanRecord);
            }
        }
    };
}
