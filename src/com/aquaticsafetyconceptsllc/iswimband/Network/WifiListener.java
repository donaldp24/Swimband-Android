package com.aquaticsafetyconceptsllc.iswimband.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by donaldpae on 12/4/14.
 */
public class WifiListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        int status = -1;
        if (activeNetwork != null) {
            status = activeNetwork.getType();
        }
        else {
            status = -1;
        }

        EventBus.getDefault().post(new SEvent(SEvent.EVENT_NETWORK_STATE_CHANGED, (Integer)status));
    }
}
