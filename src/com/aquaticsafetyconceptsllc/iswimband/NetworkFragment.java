package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection;
import com.aquaticsafetyconceptsllc.iswimband.Network.NetManager;
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
            mSettingsCell.setDeviceName(NetManager.sharedManager().serviceName());
        }

        EventBus.getDefault().register(this);

        //self.navigationItem.rightBarButtonItem = [WahoooAppDelegate createInfoButton];

        // Req# 14.	Wifi disconnect while in network range, alert tone
        // Add self as observer to listen to network change notification
        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_networkStatusChanged:) name:kReachabilityChangedNotification object:nil];

        //_reachingInternet = [Reachability reachabilityForLocalWiFi];

        //NetworkStatus internetStatus = [_reachingInternet currentReachabilityStatus];

        //if (internetStatus != ReachableViaWiFi) {

        //    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Network Notification" message:@"This feature is only available if you have a Network Connection" delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];

        //    [alert show];
        //}

        //[_reachingInternet startNotifier];
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
        if ("networkshareswitchchanged".equals(e.name)) {
            networkShareSwitchChanged();
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
                for (int i = 0; i < NetManager.sharedManager().numberOfAvailableServices(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NsdServiceInfo service = NetManager.sharedManager().availableServiceAtIndex(i);
                    deviceItem.deviceName = service.getServiceName();
                    deviceItem.isAddHidden = false;
                    deviceItem.isRemoveHidden = true;
                    deviceItemArrayList.add(deviceItem);
                }
                break;
            case kNetworkFilter_Clients:
                for (int i = 0; i < NetManager.sharedManager().numberOfServerConnections(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NetConnection connection = NetManager.sharedManager().serverConnectionAtIndex(i);
                    deviceItem.deviceName = connection.serviceName();
                    deviceItem.isAddHidden = true;
                    deviceItem.isRemoveHidden = false;
                    deviceItemArrayList.add(deviceItem);
                }
                break;

            case kNetworkFilter_Servers:
                for (int i = 0; i < NetManager.sharedManager().numberOfClientConnections(); i++) {
                    DeviceItem deviceItem = new DeviceItem();
                    NetConnection connection = NetManager.sharedManager().clientConnectionAtIndex(i);
                    deviceItem.deviceName = connection.serviceName();
                    deviceItem.isAddHidden = true;
                    deviceItem.isRemoveHidden = false;
                    deviceItemArrayList.add(deviceItem);
                }
                break;
        }

        mLeDeviceListAdapter.replaceWith(deviceItemArrayList);
    }

    public void networkShareSwitchChanged() {

        //NetworkStatus internetStatus = [_reachingInternet currentReachabilityStatus];

        //if ( [sender isKindOfClass:[UISwitch class]] )
        {
            //UISwitch* networkSwitch = (UISwitch*)sender;
            //[_settingsCell.deviceNameField resignFirstResponder];
            boolean isOn = mSettingsCell.switchChecked();

            if ( isOn ) {
                if ( mSettingsCell.getDeviceName().length() > 0 ) {
                    //if (internetStatus == ReachableViaWiFi) {

                        NetManager.sharedManager().setServiceName(mSettingsCell.getDeviceName());
                        mSettingsCell.enableNameField(false);

                        NetManager.sharedManager().startServer();
                        NetManager.sharedManager().startBrowser();

                    //}
                    //else
                    //{

                     //   UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Network Notification" message:@"You are not currently connected to a WiFi Network" delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];

                     //   [alert show];

                     //   [self _turnOffSwitch];

                    //}
                }
                else
                {
                    _invalidServiceNameAlert(mSettingsCell.getDeviceName());
                    _turnOffSwitch();
                }


            }
            else {
                mSettingsCell.enableNameField(true);
                NetManager.sharedManager().stopSharing();
            }
        }
    }

    protected void _serviceNameCollision() {
        _turnOffSwitch();

        //String title = NSLocalizedStringWithDefaultValue(@"NETWORK_SERVICE_NAME_COLLISION_ALERT_TITLE", nil, [NSBundle mainBundle], @"Network", @"Title of duplicate service name alert");
        //String message = NSLocalizedStringWithDefaultValue(@"NETWORK_SERVICE_NAME_COLLISION_ALERT_MESSAGE", nil, [NSBundle mainBundle], @"Unable to start sharing!  Someone else is already using this device name", @"Message for service name collision alert");
        String message = "Unable to start sharing!  Someone else is already using this device name";
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

        //UIAlertView* alert = [[UIAlertView alloc] initWithTitle:title message:message delegate:nil cancelButtonTitle:[CommonStrings ok] otherButtonTitles: nil];
        //[alert show];
    }

    protected void _servicePublishError() {
        _turnOffSwitch();
    }


    protected void _turnOffSwitch() {

        mSettingsCell.setSwitchChecked(false, true);
        mSettingsCell.enableNameField(true);
        NetManager.sharedManager().stopServer();
        NetManager.sharedManager().stopBrowser();
    }


    protected void _filterOptionChanged() {
        _reloadData();
    }

    protected void _invalidServiceNameAlert(String name) {
        //NSString* title = NSLocalizedStringWithDefaultValue(@"NETWORK_INVALID_SERVICE_NAME_ALERT_TITLE", nil, [NSBundle mainBundle], @"Network", @"Title of invalid service name alert");
        //NSString* message = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"NETWORK_INVALID_SERVICE_NAME_ALERT_MESSAGE", nil, [NSBundle mainBundle], @"%@ is not a valid device name.", @"Format string for invalid service name alert message"), name];
        String message = String.format("%@ is not a valid device name.", name);

        //UIAlertView* alert = [[UIAlertView alloc] initWithTitle:title message:message delegate:nil cancelButtonTitle:[CommonStrings ok] otherButtonTitles: nil];
        //[alert show];

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (selectedSegmentIndex) {
                    case kNetworkFilter_Available:
                        String serviceName = NetManager.sharedManager().nameOfAvailableServiceAtIndex(position);
                        if (serviceName != null && serviceName.length() > 0) {
                            NetManager.sharedManager().openConnectionToService(serviceName);
                        }
                        break;
                    case kNetworkFilter_Servers: {
                        NetConnection connection = NetManager.sharedManager().clientConnectionAtIndex(position);
                        NetManager.sharedManager().closeConnection(connection);
                    }
                        break;

                    case kNetworkFilter_Clients: {
                        NetConnection connection = NetManager.sharedManager().serverConnectionAtIndex(position);
                        NetManager.sharedManager().closeConnection(connection);
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
                removeImageView = (ImageView)view.findViewWithTag("actoinRemove");
            }
        }
    }
}
