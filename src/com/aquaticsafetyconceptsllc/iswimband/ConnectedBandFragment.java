package com.aquaticsafetyconceptsllc.iswimband;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

/**
 * Created by donaldpae on 11/24/14.
 */
public class ConnectedBandFragment extends BandFragment {

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
                //[super tableView:tableView didSelectRowAtIndexPath:indexPath];

                //WahoooAppDelegate* appDel = (WahoooAppDelegate*)[UIApplication sharedApplication].delegate;

                //if ( indexPath.row < [super tableView:tableView numberOfRowsInSection:indexPath.section] )
                {
                    WahoooBand band = bandArrayList.get(position);

                    if (band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral &&
                            band.bandState() != WahoooBand.WahoooBandState_t.kWahoooBandState_OTAUpgrade) {
                        FlowManager.sharedInstance().pushDetailForBand(band);
                    }
                }
                //else
                //{
                //    [appDel addBandSelected];
                //}
            }
        };
    }

    public void onEvent(SEvent e) {
        if (WahoooBandManager.kBandManagerConnectedBandsChangedNotification.equalsIgnoreCase(e.name)) {
            _bandListChanged();
        }
    }

    protected void _bandListChanged() {
        setBandArray(WahoooBandManager.sharedManager().connectedBandsDisplay);
    }
}
