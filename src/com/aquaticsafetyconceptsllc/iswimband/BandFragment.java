package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

public class BandFragment extends Fragment {
	protected ListView mBandListView;
    protected ViewGroup mStatusContainer;
    protected StatusMonitor mStatusMonitor;
    protected ArrayList<WahoooBand> bandArrayList;
    protected LeBandListAdapter mLeBandListAdapter;

    public BandFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        initVariables();

        View rootView = inflater.inflate(R.layout.fragment_bands, container, false);

        // create status monitor
        mStatusMonitor = new StatusMonitor(this.getActivity());
        mStatusContainer = (ViewGroup)rootView.findViewById(R.id.rl_statusmonitor);
        mStatusContainer.addView(mStatusMonitor.getView());

        // create list of bands
        mBandListView = (ListView)rootView.findViewById(R.id.band_list);
        mLeBandListAdapter = new LeBandListAdapter(this.getActivity());
        mBandListView.setAdapter(mLeBandListAdapter);
        mBandListView.setDivider(null);

        ResolutionSet._instance.iterateChild(rootView);

        return rootView;
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
}
