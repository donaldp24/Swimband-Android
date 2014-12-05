package com.aquaticsafetyconceptsllc.iswimband;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

public class NetworkSettingsCell {
    public EditText mEditDeviceName;
    public Switch mSwitchSharing;
    public ImageView mImageForDeviceName;

    private View mView;
    private LayoutInflater mInflator;

    // contructor
    public NetworkSettingsCell(Context context) {
        mInflator = LayoutInflater.from(context);
        mView = mInflator.inflate(R.layout.networksettingcell, null);

        mEditDeviceName = (EditText)mView.findViewById(R.id.edit_devicename);
        mEditDeviceName.setBackground(null);

        mSwitchSharing = (Switch)mView.findViewById(R.id.switch_sharing);
        mImageForDeviceName = (ImageView)mView.findViewById(R.id.image_fordevicename);
    }

    public View getView() {
        return mView;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void enableNameField(boolean enable) {
        if( enable ) {
            mEditDeviceName.setEnabled(true);
            mImageForDeviceName.setVisibility(View.VISIBLE);
        }
        else {
            mEditDeviceName.setEnabled(false);
            mImageForDeviceName.setVisibility(View.INVISIBLE);
        }
    }

    public void setSwitchChecked(boolean checked, boolean animated) {
        mSwitchSharing.setChecked(checked);
    }

    public boolean switchChecked() {
        return mSwitchSharing.isChecked();
    }

    public void setDeviceName(String deviceName) {
        mEditDeviceName.setText(deviceName);
    }

    public String getDeviceName() {
        return mEditDeviceName.getText().toString();
    }
}
