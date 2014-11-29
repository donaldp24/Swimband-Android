package com.aquaticsafetyconceptsllc.iswimband;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

public class StatusMonitor {
    public TextView mNormalCountLabel;
    public TextView mCautionCountLabel;
    public TextView mUrgentCountLabel;

    private View mView;
    private LayoutInflater mInflator;

    // contructor
    public StatusMonitor(Context context) {
        mInflator = LayoutInflater.from(context);
        mView = mInflator.inflate(R.layout.statusmonitor, null);

        mNormalCountLabel = (TextView)mView.findViewById(R.id.text_normalcount);
        mCautionCountLabel = (TextView)mView.findViewById(R.id.text_cautioncount);
        mUrgentCountLabel = (TextView)mView.findViewById(R.id.text_urgentcount);
    }

    public View getView() {
        return mView;
    }

    // public interface
    public void updateStatus() {
        _updateStatusCounts();
    }

    // private functions
    protected void _setupObservers()  {
        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_deviceListChanged:) name:kBandManagerConnectedBandsChangedNotification object:nil];
        EventBus.getDefault().register(this);
    }

    public void onResume() {
        _setupObservers();
        _updateStatusCounts();
    }

    public void onPause() {
        EventBus.getDefault().unregister(this);
    }

    protected void _deviceListChanged() {
        _updateStatusCounts();
    }

    protected void _updateStatusCounts()  {

        ArrayList<WahoooBand> connected = WahoooBandManager.sharedManager().connectedBands;
        ArrayList<WahoooBand> remote = WahoooBandManager.sharedManager().remoteBands;
        int normalCount = 0;
        int cautionCount = 0;
        int urgentCount = 0;

        for( WahoooBand band : connected ) {
            switch( band.bandState() ) {
                case kWahoooBandState_Caution:
                    cautionCount++;
                    break;

                case kWahoooBandState_OutOfRange:
                case kWahoooBandState_Alarm:
                    urgentCount++;
                    break;

                case kWahoooBandState_Connected:
                    normalCount++;
                    break;

                default:
                    break;

            }
        }

        // Gross copy paste
        for( WahoooBand band : remote ) {
            switch( band.bandState() ) {

                case kWahoooBandState_Caution:
                    cautionCount++;
                    break;

                case kWahoooBandState_OutOfRange:
                case kWahoooBandState_Alarm:
                    urgentCount++;
                    break;

                case kWahoooBandState_Connected:
                    normalCount++;
                    break;

                default:
                    break;

            }
        }

        if ( mNormalCountLabel != null )  {
            mNormalCountLabel.setText(String.format("%02d", normalCount));
        }

        if ( mCautionCountLabel != null ) {
            mCautionCountLabel.setText(String.format("%02d", cautionCount));
        }

        if ( mUrgentCountLabel != null ) {
            mUrgentCountLabel.setText(String.format("%02d", urgentCount));
        }
    }

    public void onEventMainThread(SEvent e) {
        if (WahoooBandManager.kBandManagerConnectedBandsChangedNotification.equalsIgnoreCase(e.name)) {
            _deviceListChanged();
        }
    }
}
