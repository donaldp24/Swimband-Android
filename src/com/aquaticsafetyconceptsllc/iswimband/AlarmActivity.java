package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Sound.SoundManager;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

/**
 * Created by donaldpae on 11/29/14.
 */
public class AlarmActivity extends Activity {
    protected ListView mBandListView;
    protected ViewGroup mStatusContainer;
    protected StatusMonitor mStatusMonitor;
    protected ArrayList<WahoooBand> bandArrayList;
    protected LeBandListAdapter mLeBandListAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_alarm);

        initVariables();

        // create status monitor
        mStatusMonitor = new StatusMonitor(this);
        mStatusContainer = (ViewGroup)findViewById(R.id.rl_statusmonitor);
        mStatusContainer.addView(mStatusMonitor.getView());

        // create list of bands
        mBandListView = (ListView)findViewById(R.id.band_list);
        mLeBandListAdapter = new LeBandListAdapter(this);
        mBandListView.setAdapter(mLeBandListAdapter);
        mBandListView.setDivider(null);

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_parent));

        EventBus.getDefault().register(this);

        Button btnDismiss = (Button)findViewById(R.id.btn_dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // set top activity
        FlowManager.sharedInstance().setTopActivity(this);
    }

    protected void initVariables() {
        bandArrayList = new ArrayList<WahoooBand>();
    }

    public void setBandArray(ArrayList<WahoooBand> array) {
        ArrayList<WahoooBand> oldArray = bandArrayList;
        bandArrayList = new ArrayList<WahoooBand>();
        bandArrayList.addAll(array);
        _updateList(oldArray, bandArrayList);
    }

    protected void _updateList(ArrayList<WahoooBand> oldEntries, ArrayList<WahoooBand> newEntries) {
        mLeBandListAdapter.replaceWith(newEntries);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLeBandListAdapter != null)
            EventBus.getDefault().register(mLeBandListAdapter);

        if (mStatusMonitor != null)
            mStatusMonitor.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mLeBandListAdapter != null)
            EventBus.getDefault().unregister(mLeBandListAdapter);

        if (mStatusMonitor != null)
            mStatusMonitor.onPause();
    }

    public void onEventMainThread(SEvent e) {
        if (WahoooBandManager.kBandManagerConnectedBandsChangedNotification.equals(e.name)) {
            _bandListChanged();
        }
    }


    protected void _bandListChanged() {

        ArrayList<WahoooBand> emergencyBands = new ArrayList<WahoooBand>();

        ArrayList<WahoooBand> connected = new ArrayList<WahoooBand>();
        connected.addAll(WahoooBandManager.sharedManager().connectedBandsDisplay);

        for( WahoooBand band : connected ) {
            if ( band.bandState() == WahoooBand.WahoooBandState_t.kWahoooBandState_Alarm ||
                    band.bandState() == WahoooBand.WahoooBandState_t.kWahoooBandState_OutOfRange )
            {
                emergencyBands.add(band);
            }
        }

        setBandArray(emergencyBands);

        // Req#13. During Red Alert, siren must continue playing until iSwimband reconnects or Dismiss button is pushed by user
        // Stop the alert sound when there is no band in emergencyBands list
        if(bandArrayList.size() == 0)
        {
            dismiss();
        }
    }

    public void dismiss() {
        // Req#13. During Red Alert, siren must continue playing until iSwimband reconnects or Dismiss button is pushed by user
        // Stop the alert sound when user dismisses the alarm view
        SoundManager.sharedInstance().stopAlertSound();;

        // Req#16. Is there a way to flash LED for alerts?
        // Stop flashing LED after alert screen is dissmissed
        FlowManager.sharedInstance().stopFlashingLED();

        EventBus.getDefault().unregister(this);

        finish();
        overridePendingTransition(R.anim.fade, R.anim.alpha);
    }
}
