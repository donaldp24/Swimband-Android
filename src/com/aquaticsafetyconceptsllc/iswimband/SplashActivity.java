package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BleManager;
import com.aquaticsafetyconceptsllc.iswimband.CoreData.CoreDataManager;
import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnectionManager;
import com.aquaticsafetyconceptsllc.iswimband.Sound.SoundManager;
import com.aquaticsafetyconceptsllc.iswimband.Utils.ScheduleNotificationManager;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Settings;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import com.crashlytics.android.Crashlytics;

import com.crittercism.app.Crittercism;

/**
 * Created by donaldpae on 11/25/14.
 */
public class SplashActivity extends Activity {
    private final int LOADINGVIEW_TIMEOUT = 1000;
    private Handler handler;
    private boolean isBleEnabled;

    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        Crittercism.initialize(getApplicationContext(), "5493775f51de5e9f042ec467");
        setContentView(R.layout.activity_splash);

        initVariables();
        setupControls();
        checkBluetooth();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void initVariables() {
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == 0)
                {
                    goNext();
                }
            }
        };
    }

    protected void setupControls() {
    }

    protected void goNext() {

        SoundManager.initialize(getApplicationContext());
        CoreDataManager.initialize(getApplicationContext());
        FlowManager.initialize(getApplicationContext());
        Settings.initialize(getApplicationContext());
        NetConnectionManager.initialize(getApplicationContext());
        ScheduleNotificationManager.initialize(getApplicationContext());

        // start scan
        WahoooBandManager.initialize(getApplicationContext());
        WahoooBandManager.sharedManager().startScan();

        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade, R.anim.alpha);
    }

    protected void checkBluetooth() {
        isBleEnabled = false;

        BleManager.initialize(getApplicationContext());
        if (!BleManager.sharedInstance().isBleSupported()) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!BleManager.sharedInstance().isBleAvailable()) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        isBleEnabled = BleManager.sharedInstance().isBleEnabled();

        if (isBleEnabled) {
            handler.sendEmptyMessageDelayed(0, LOADINGVIEW_TIMEOUT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isBleEnabled) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            } else if (resultCode == Activity.RESULT_OK) {
                handler.sendEmptyMessageDelayed(0, LOADINGVIEW_TIMEOUT);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
