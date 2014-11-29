package com.aquaticsafetyconceptsllc.iswimband.Ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import de.greenrobot.event.EventBus;

/**
 * Created by donaldpae on 11/24/14.
 */
public class BluetoothAdapterReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Logger.log("BluetoothAdapter.STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Logger.log("BluetoothAdapter.STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Logger.log("BluetoothAdapter.STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Logger.log("BluetoothAdapter.STATE_TURNING_ON");
                    break;
                default:
                    break;
            }
            Integer intstate = state;
            EventBus.getDefault().post(new SEvent(SEvent.EVENT_BLUETOOTH_STATE_CHANGED, intstate));
        }
    }
}
