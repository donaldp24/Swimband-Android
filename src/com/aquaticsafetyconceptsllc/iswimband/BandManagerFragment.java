package com.aquaticsafetyconceptsllc.iswimband;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

/**
 * Created by donaldpae on 11/26/14.
 */
public class BandManagerFragment extends BandFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mBandListView.setOnItemClickListener(createOnItemClickListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        _bandListChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WahoooBand band = mLeBandListAdapter.getBand(position);

                int connectedCount = WahoooBandManager.sharedManager().connectedBands.size() +
                WahoooBandManager.sharedManager().remoteBands.size();

                if (  position < connectedCount )
                {
                    if( band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral &&
                            band.bandState() != WahoooBand.WahoooBandState_t.kWahoooBandState_OTAUpgrade )
                    {
                        FlowManager.sharedInstance().pushDetailForBand(band);
                    }
                }
                else
                {
                    WahoooBandManager.sharedManager().connect(band);
                }

            }
        };
    }

    public void onEvent(SEvent e) {
        if (WahoooBandManager.kBandManagerAdvertisingBandsChangedNotification.equalsIgnoreCase(e.name)) {
            //Logger.log("BandManagerFragment.onEvent : %s", e.name);
            _bandListChanged();
        }
        else if (WahoooBandManager.kBandManagerConnectedBandsChangedNotification.equalsIgnoreCase(e.name)) {
            //Logger.log("BandManagerFragment.onEvent : %s", e.name);
            _bandListChanged();
        }
    }

    protected void _bandListChanged() {
        ArrayList<WahoooBand> newList = new ArrayList<WahoooBand>();
        newList.addAll(WahoooBandManager.sharedManager().connectedBandsDisplay);
        newList.addAll(WahoooBandManager.sharedManager().advertisingBands);

        /*
        Logger.log("BandManagerFragment _bandListChanged() : (%d) - connectedBandsDisplay(%d), advertisingBands(%d)",
                newList.size(), WahoooBandManager.sharedManager().connectedBandsDisplay.size(),
                WahoooBandManager.sharedManager().advertisingBands.size());
        */

        setBandArray(newList);
    }


}
