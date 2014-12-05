package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection;
import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnectionManager;
import com.aquaticsafetyconceptsllc.iswimband.Sound.SoundManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

/**
 * Created by donaldpae on 11/29/14.
 */
public class NetworkFragment extends Fragment {

    protected ViewGroup mStatusContainer;
    protected StatusMonitor mStatusMonitor;

    protected ViewGroup mSettingsContainer;
    protected NetworkSettingsCell mSettingsCell;

    protected ListView mDeviceList;
    protected LeDeviceListAdapter mLeDeviceListAdapter;
    protected GenericModalAlertDialog genericModalAlertDisplay;

    protected ArrayList<DeviceItem> deviceItemArrayList;


    // NetworkFilterTypes
    public static final int kNetworkFilter_Available = 0;
    public static final int kNetworkFilter_Servers = 1;
    public static final int kNetworkFilter_Clients = 2;

    private Button mBtnType1;
    private Button mBtnType2;
    private Button mBtnType3;

    protected int selectedSegmentIndex = 0;

    private Handler mHandler;

    public class DeviceItem {
        String deviceName;
        boolean isAddHidden;
        boolean isRemoveHidden;
    }

    public NetworkFragment() {
        //
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initVariables();

        View rootView = inflater.inflate(R.layout.fragment_network, container, false);

        // create status monitor
        mStatusMonitor = new StatusMonitor(this.getActivity());
        mStatusContainer = (ViewGroup)rootView.findViewById(R.id.rl_statusmonitor);
        mStatusContainer.addView(mStatusMonitor.getView());

        // create settings cell
        mSettingsCell = new NetworkSettingsCell(this.getActivity());
        mSettingsContainer = (ViewGroup)rootView.findViewById(R.id.rl_settings);
        mSettingsContainer.addView(mSettingsCell.getView());
        mSettingsCell.mSwitchSharing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                networkShareSwitchChanged();
            }
        });

        // create list of bands
        mDeviceList = (ListView)rootView.findViewById(R.id.device_list);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this.getActivity());
        mDeviceList.setAdapter(mLeDeviceListAdapter);
        mDeviceList.setDivider(null);
        mDeviceList.setOnItemClickListener(createOnItemClickListener());

        ResolutionSet._instance.iterateChild(rootView);

        mBtnType1 = (Button)rootView.findViewById(R.id.btn_type1);
        mBtnType2 = (Button)rootView.findViewById(R.id.btn_type2);
        mBtnType3 = (Button)rootView.findViewById(R.id.btn_type3);

        mBtnType1.setOnClickListener(onClickListener);
        mBtnType2.setOnClickListener(onClickListener);
        mBtnType3.setOnClickListener(onClickListener);

        return rootView;
    }

    protected void initVariables() {
        deviceItemArrayList = new ArrayList<DeviceItem>();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStatusMonitor != null)
            mStatusMonitor.onResume();

        if (mSettingsCell != null) {
            mSettingsCell.onResume();
            mSettingsCell.setDeviceName(NetConnectionManager.sharedManager().serviceName());
        }

        EventBus.getDefault().register(this);

        //self.navigationItem.rightBarButtonItem = [WahoooAppDelegate createInfoButton];


        ConnectivityManager conMan = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            String title = "Network Notification";
            String message = "This feature is only available if you have a Network Connection";
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mStatusMonitor != null)
            mStatusMonitor.onPause();

        if (mSettingsCell != null)
            mSettingsCell.onPause();

        EventBus.getDefault().unregister(this);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBtnType1 == v) {
                selectedSegmentIndex = kNetworkFilter_Available;
                _changeSegmentIndex(0);
                _filterOptionChanged();
            }
            else if (mBtnType2 == v) {
                selectedSegmentIndex = kNetworkFilter_Servers;
                _changeSegmentIndex(1);
                _filterOptionChanged();
            }
            else if (mBtnType3 == v) {
                selectedSegmentIndex = kNetworkFilter_Clients;
                _changeSegmentIndex(2);
                _filterOptionChanged();
            }
        }
    };

    protected void _changeSegmentIndex(int index) {
        if (index == 0) {
            mBtnType1.setTextColor(Color.parseColor("#ffffff"));
            mBtnType2.setTextColor(Color.parseColor("#333333"));
            mBtnType3.setTextColor(Color.parseColor("#333333"));

            mBtnType1.setBackgroundResource(R.drawable.roundleftblueblue_layout);
            mBtnType2.setBackgroundResource(R.drawable.rectbluegray_layout);
            mBtnType3.setBackgroundResource(R.drawable.roundrightbluegray_layout);
        }
        else if (index == 1) {
            mBtnType2.setTextColor(Color.parseColor("#ffffff"));
            mBtnType1.setTextColor(Color.parseColor("#333333"));
            mBtnType3.setTextColor(Color.parseColor("#333333"));

            mBtnType1.setBackgroundResource(R.drawable.roundleftbluegray_layout);
            mBtnType2.setBackgroundResource(R.drawable.rectblueblue_layout);
            mBtnType3.setBackgroundResource(R.drawable.roundrightbluegray_layout);
        }
        else if (index == 2) {
            mBtnType3.setTextColor(Color.parseColor("#ffffff"));
            mBtnType2.setTextColor(Color.parseColor("#333333"));
            mBtnType1.setTextColor(Color.parseColor("#333333"));

            mBtnType3.setBackgroundResource(R.drawable.roundleftblueblue_layout);
            mBtnType2.setBackgroundResource(R.drawable.rectbluegray_layout);
            mBtnType1.setBackgroundResource(R.drawable.roundrightbluegray_layout);
        }
    }

    public void onEventMainThread(SEvent e) {
        if (SEvent.EVENT_NETWORK_STATE_CHANGED.equals(e.name)) {
            _networkStatusChanged();
        }
        else if (NetConnectionManager.kNetConnectionManagerConnectionsUpdated.equals(e.name)) {
            _connectionManagerUpdate();
        }
        else if (NetConnectionManager.kNetConnectionManagerServerPublishNameConflict.equals(e.name)) {
            _serviceNameCollision();
        }
        else if (NetConnectionManager.kNetConnectionManagerServerPublishFailed.equals(e.name)) {
            _servicePublishError();
        }

    }

    protected void _connectionManagerUpdate() {
        if (mDeviceList != null) {
            _reloadData();
        }
    }

    protected void _reloadData() {
        deviceItemArrayList = new ArrayList<DeviceItem>();

        switch ( selectedSegmentIndex )
        {
            case kNetworkFilter_Available:
                for (int i = 0; i < NetConnectionManager.sharedManager().numberOfAvailableServices(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NsdServiceInfo service = NetConnectionManager.sharedManager().availableServiceAtIndex(i);
                    deviceItem.deviceName = service.getServiceName();
                    deviceItem.isAddHidden = false;
                    deviceItem.isRemoveHidden = true;
                    deviceItemArrayList.add(deviceItem);
                }
                break;
            case kNetworkFilter_Clients:
                for (int i = 0; i < NetConnectionManager.sharedManager().numberOfServerConnections(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NetConnection connection = NetConnectionManager.sharedManager().serverConnectionAtIndex(i);
                    deviceItem.deviceName = connection.serviceName();
                    deviceItem.isAddHidden = true;
                    deviceItem.isRemoveHidden = false;
                    deviceItemArrayList.add(deviceItem);
                }
                break;

            case kNetworkFilter_Servers:
                for (int i = 0; i < NetConnectionManager.sharedManager().numberOfClientConnections(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NetConnection connection = NetConnectionManager.sharedManager().clientConnectionAtIndex(i);
                    deviceItem.deviceName = connection.serviceName();
                    deviceItem.isAddHidden = true;
                    deviceItem.isRemoveHidden = false;
                    deviceItemArrayList.add(deviceItem);
                }
                break;
        }

        mLeDeviceListAdapter.replaceWith(deviceItemArrayList);
    }

    protected void _networkStatusChanged() {
        // Only if network sharing is on, detect and alert if WiFi is disconnected
        if(mSettingsCell.mSwitchSharing.isChecked()) {
            ConnectivityManager conMan = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conMan.getActiveNetworkInfo();

            if(networkInfo == null || networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {

                // WiFi disconnected - Alert sound
                SoundManager.sharedInstance().playAlertSound(R.raw.wifidisconnect, false, true);

                // Show the message popup
                _showMessagePopup("You have lost your WIFI Connection");
            }
            else {

                // Close message popup if open and stop alert sound once WiFi come back online
                if (genericModalAlertDisplay != null)
                    genericModalAlertDisplay.dismiss();
            }
        }

    }

    protected void networkShareSwitchChanged() {

        ConnectivityManager conMan = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();

        //if ( sender instanceof Switch) {

            // hide keyboard
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSettingsCell.mEditDeviceName.getWindowToken(), 0);

            boolean isOn = mSettingsCell.switchChecked();

            if ( isOn ) {
                if ( mSettingsCell.getDeviceName().length() > 0 ) {

                    if (networkInfo != null &&
                            networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                        NetConnectionManager.sharedManager().setServiceName(mSettingsCell.getDeviceName());
                        mSettingsCell.enableNameField(false);

                        NetConnectionManager.sharedManager().startServer();
                        NetConnectionManager.sharedManager().startBrowser();
                    }
                    else
                    {
                        String title = "Network Notification";
                        String message = "You are not currently connected to a WiFi Network";
                        new AlertDialog.Builder(getActivity())
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        _turnOffSwitch();
                    }
                }
                else
                {
                    _invalidServiceNameAlert(mSettingsCell.getDeviceName());
                    _turnOffSwitch();
                }
            }
            else {
                mSettingsCell.enableNameField(true);
                NetConnectionManager.sharedManager().stopSharing();
            }
        //}
    }

    protected void _serviceNameCollision() {
        _turnOffSwitch();

        String title = "Network";
        String message = "Unable to start sharing!  Someone else is already using this device name";
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected void _servicePublishError() {
        _turnOffSwitch();
    }


    protected void _turnOffSwitch() {

        mSettingsCell.setSwitchChecked(false, true);
        mSettingsCell.enableNameField(true);
        NetConnectionManager.sharedManager().stopServer();
        NetConnectionManager.sharedManager().stopBrowser();
    }


    protected void _filterOptionChanged() {
        _reloadData();
    }

    protected void _invalidServiceNameAlert(String name) {
        String title = "Network";
        String message = String.format("%@ is not a valid device name.", name);
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected void _showMessagePopup(String message) {
        genericModalAlertDisplay = new GenericModalAlertDialog(getActivity(), message, new DialogDismissInterface() {
            @Override
            public void onDismiss(Dialog dlg, int nRes) {
                onGenericModalAlertDisplay();
            }
        });
    }

    protected void onGenericModalAlertDisplay() {
        if(genericModalAlertDisplay != null) {
            SoundManager.sharedInstance().stopAlertSound();
            genericModalAlertDisplay = null;
        }
    }


    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mHandler = new Handler();

                switch (selectedSegmentIndex) {
                    case kNetworkFilter_Available:
                        final String serviceName = NetConnectionManager.sharedManager().nameOfAvailableServiceAtIndex(position);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (serviceName != null && serviceName.length() > 0) {
                                    NetConnectionManager.sharedManager().openConnectionToService(serviceName);
                                }
                            }
                        });


                        break;
                    case kNetworkFilter_Servers: {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                NetConnection connection = NetConnectionManager.sharedManager().clientConnectionAtIndex(position);
                                NetConnectionManager.sharedManager().closeConnection(connection);
                            }
                        });

                    }
                        break;

                    case kNetworkFilter_Clients: {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                NetConnection connection = NetConnectionManager.sharedManager().serverConnectionAtIndex(position);
                                NetConnectionManager.sharedManager().closeConnection(connection);
                            }
                        });
                    }
                        break;
                }
            }
        };
    }



    public class LeDeviceListAdapter extends BaseAdapter {
        private Activity mParentActivity;

        private ArrayList<DeviceItem> mLeItems;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(Activity parentActivity) {
            super();

            mLeItems = new ArrayList<DeviceItem>();
            mInflator = LayoutInflater.from(parentActivity);

            mParentActivity = parentActivity;
        }

        public void replaceWith(final ArrayList<DeviceItem> devices) {
            mParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeItems.clear();
                    mLeItems.addAll(devices);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mLeItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = inflateIfRequired(view, position, parent);
            bind((DeviceItem) getItem(position), view);

            return view;
        }

        protected void bind(DeviceItem deviceItem, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (deviceItem != null) {
                holder.nameTextView.setText(deviceItem.deviceName);
                if (deviceItem.isAddHidden)
                    holder.addImageView.setVisibility(View.INVISIBLE);
                else
                    holder.addImageView.setVisibility(View.VISIBLE);

                if (deviceItem.isRemoveHidden)
                    holder.removeImageView.setVisibility(View.INVISIBLE);
                else
                    holder.removeImageView.setVisibility(View.VISIBLE);
            }
        }

        protected View inflateIfRequired(View view, int position, ViewGroup parent) {
            if (view == null) {
                view = mInflator.inflate(R.layout.device_item, null);
                view.setTag(new ViewHolder(view));
                ResolutionSet._instance.iterateChild(view);
            }
            return view;
        }

        protected void refreshUI() {
            notifyDataSetChanged();
        }


        class ViewHolder {
            final TextView nameTextView;
            final ImageView addImageView;
            final ImageView removeImageView;

            ViewHolder(View view) {
                nameTextView = (TextView)view.findViewWithTag("name");
                addImageView = (ImageView)view.findViewWithTag("actionAdd");
                removeImageView = (ImageView)view.findViewWithTag("actionRemove");
            }
        }
    }
}
