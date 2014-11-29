package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

// Adapter for holding devices found through scanning.
public class LeBandListAdapter extends BaseAdapter {
    public static final int GREEN_STATUS = R.drawable.greenlight_status;
    public static final int RED_STATUS = R.drawable.redlight_status;
    public static final int YELLOW_STATUS = R.drawable.yellowlight_status;

    public static final int s_defaultBGImage = R.drawable.band_bar;
    public static final int s_redBGImage = R.drawable.band_bar_red;
    public static final int s_yellowBGImage = R.drawable.band_bar_yellow;
    public static final int s_grayBGImage = R.drawable.band_bar;

    public static final int ARROW_IMAGE = R.drawable.band_arrow;
    public static final int ADD_IMAGE = R.drawable.band_add;

    private Activity mParentActivity;


    private ArrayList<WahoooBand> mLeBands;
    private LayoutInflater mInflator;

    public LeBandListAdapter(Activity parentActivity) {
        super();

        mLeBands = new ArrayList<WahoooBand>();
        mInflator = LayoutInflater.from(parentActivity);

        mParentActivity = parentActivity;
    }

    public void replaceWith(final ArrayList<WahoooBand> devices) {
        mParentActivity.runOnUiThread(new Runnable() {
                          @Override
                        public void run() {
                              mLeBands.clear();
                              mLeBands.addAll(devices);
                              //Logger.log("replaceWith : %d", mLeBands.size());
                              notifyDataSetChanged();
                          }
                      });
    }

    public WahoooBand getBand(int position) {
        return mLeBands.get(position);
    }

    public void setBand(int position, WahoooBand band) {
        mLeBands.set(position, band);

        if (band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral )
        {
            /*
            PeripheralBand* prphBand = (PeripheralBand*)b;

            switch(prphBand.peripheral.type )
            {
                case kPeripheralSource_BLE:
                    break;

                case kPeripheralSource_WiFi:
                    break;

                default:

                    break;
            }
            */
        }

        refreshUI();
    }

    public void onEventMainThread(SEvent e) {
        if (WahoooBand.kWahoooBandDataUpdatedNotification.equalsIgnoreCase(e.name)) {
            _bandUpdated((WahoooBand)e.object);
        }
    }

    protected void _bandUpdated(WahoooBand band) {
        //if ()
        //{
            refreshUI();
        //}
    }

    public void clear() {
        mLeBands.clear();
    }

    @Override
    public int getCount() {
        return mLeBands.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeBands.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind((WahoooBand) getItem(position), view);

        return view;
    }

    protected void bind(WahoooBand band, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (band != null) {
            holder.nameTextView.setText(band.displayName());

            if ( band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral) {
                SubViewMeter.setValue(band.normalRange(), holder.rssiSignal, SubViewMeter.barImageRes);
            }

            SubViewMeter.setValue(0.01f * band.batteryLevel(), holder.batteryLevel, SubViewMeter.batteryImageRes);

            updateStatusAccessory(holder);

            updateActionAccessory(band, holder);

            _updateAccessories(band, holder);

            if (holder.backgroundImage != null) {
                switch( band.bandState() )
                {
                    case kWahoooBandState_Connecting:
                    case kWahoooBandState_Connected:
                        holder.backgroundImage.setBackgroundResource(s_defaultBGImage);
                        break;

                    case kWahoooBandState_Alarm:
                    case kWahoooBandState_OutOfRange:
                        holder.backgroundImage.setBackgroundResource(s_redBGImage);
                        break;

                    case kWahoooBandState_Caution:
                        holder.backgroundImage.setBackgroundResource(s_yellowBGImage);
                        break;

                    default:
                        holder.backgroundImage.setBackgroundResource(s_grayBGImage);

                }
            }


        }
    }

    protected View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = mInflator.inflate(R.layout.band_item, null);
            view.setTag(new ViewHolder(view));
            ResolutionSet._instance.iterateChild(view);
        }
        return view;
    }

    public static void updateStatus(ImageView view, WahoooBand.WahoooBandState_t state) {
        view.setVisibility(View.VISIBLE);

        switch( state )
        {
            case kWahoooBandState_Connecting:
            case kWahoooBandState_Connected:
                view.setBackgroundResource(GREEN_STATUS);
                break;

            case kWahoooBandState_Alarm:
            case kWahoooBandState_OutOfRange:
                view.setBackgroundResource(RED_STATUS);
                break;

            case kWahoooBandState_Caution:
                view.setBackgroundResource(YELLOW_STATUS);
                break;

            default:
                view.setVisibility(View.INVISIBLE);
        }
    }

    public void updateStatusAccessory(ViewHolder holder) {

    }

    public void updateActionAccessory(WahoooBand band, ViewHolder holder) {
        if ( holder.actionAccessory != null ) {
            if ( band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral ) {
                holder.actionAccessory.setVisibility(View.VISIBLE);
                switch ( band.bandState() ) {
                    case kWahoooBandState_NotConnected:
                        holder.actionAccessory.setBackgroundResource(ADD_IMAGE);
                        break;

                    case kWahoooBandState_OTAUpgrade:
                        holder.actionAccessory.setVisibility(View.INVISIBLE);
                        break;

                    default:
                        holder.actionAccessory.setBackgroundResource(ARROW_IMAGE);
                        break;
                }
            }
            else {
                holder.actionAccessory.setVisibility(View.INVISIBLE);
            }
        }
    }

    protected void _swapAccessory(ViewHolder holder, View accessory) {
        holder.rssiAccessory.setVisibility(View.INVISIBLE);
        holder.rssiDisconnectedAccessory.setVisibility(View.INVISIBLE);
        holder.wifiAccessory.setVisibility(View.INVISIBLE);
        holder.wifiDisconnectedAccessory.setVisibility(View.INVISIBLE);
        holder.otaUpdateAccessory.setVisibility(View.INVISIBLE);
        holder.connectingAccessory.setVisibility(View.INVISIBLE);

        if (accessory != null)
            accessory.setVisibility(View.VISIBLE);
    }

    public void _updateAccessories(WahoooBand band, ViewHolder holder) {
        _swapAccessory(holder, null);
        // Logger.log("_updateAccessories band (%s)", band.name());
        if (band != null) {
            if ( band.bandState() == WahoooBand.WahoooBandState_t.kWahoooBandState_OTAUpgrade ||
                    !WahoooBandManager.sharedManager().connectedBandsDisplay.contains(band) ) {
                _swapAccessory(holder, null);
            }

            //Clear out container of current accessory

            holder.batteryAccessory.setVisibility(View.INVISIBLE);

            if ( band.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral ) {
                switch (band.bandState())
                {
                    case kWahoooBandState_Connected:
                        _swapAccessory(holder, holder.rssiAccessory);
                        holder.batteryAccessory.setVisibility(View.VISIBLE);
                        break;

                    case kWahoooBandState_Connecting:
                        _swapAccessory(holder, holder.connectingAccessory);
                        break;

                    case kWahoooBandState_OTAUpgrade:
                        _swapAccessory(holder, holder.otaUpdateAccessory);
                        if (holder.otaProgressLabel != null) {
                            holder.otaProgressLabel.setText(String.format("%d%%", band.upgradeProgress()));
                        }
                        break;

                    case kWahoooBandState_Alarm:
                    case kWahoooBandState_Caution:
                    case kWahoooBandState_OutOfRange:
                    case kWahoooBandState_NotConnected:
                        _swapAccessory(holder, null);
                        break;

                    default:
                        _swapAccessory(holder, null);
                        break;
                }
            }
            else
            {
                switch (band.bandState()) {
                    case kWahoooBandState_Connected:
                        holder.batteryAccessory.setVisibility(View.VISIBLE);

                        //Intentional fall through
                    case kWahoooBandState_Connecting:
                    case kWahoooBandState_Alarm:
                    case kWahoooBandState_Caution:
                    case kWahoooBandState_OutOfRange:
                        _swapAccessory(holder, holder.wifiAccessory);
                        break;

                    case kWahoooBandState_OTAUpgrade:
                        _swapAccessory(holder, holder.otaUpdateAccessory);
                        if (holder.otaProgressLabel != null) {
                            holder.otaProgressLabel.setText("");
                        }
                        break;

                    case kWahoooBandState_NotConnected:
                        _swapAccessory(holder, holder.wifiDisconnectedAccessory);
                        break;

                    default:
                        _swapAccessory(holder, null);
                        break;
                }
            }
        }
    }

    protected void refreshUI() {
        notifyDataSetChanged();
    }


    static class ViewHolder {

        final TextView nameTextView;
        final View rssiAccessory;
        final ImageView rssiSignal;
        final View  rssiDisconnectedAccessory;
        final View  wifiAccessory;
        final View  wifiDisconnectedAccessory;
        final View  otaUpdateAccessory;
        final View statusAccessory;
        final View actionAccessory;
        final View  connectingAccessory;

        final View  batteryAccessory;
        final ImageView batteryLevel;

        final TextView otaProgressLabel;

        final ImageView backgroundImage;


        ViewHolder(View view) {
            nameTextView = (TextView)view.findViewWithTag("name");
            rssiAccessory = (View)view.findViewWithTag("rssiAccessory");
            rssiSignal = (ImageView)view.findViewWithTag("rssiSignal");
            rssiDisconnectedAccessory = (View)view.findViewWithTag("rssiDisconnectedAccessory");
            wifiAccessory = (View)view.findViewWithTag("wifiAccessory");
            wifiDisconnectedAccessory = (View)view.findViewWithTag("wifiDisconnectedAccessory");
            otaUpdateAccessory = (View)view.findViewWithTag("otaUpdateAccessory");
            statusAccessory = (View)view.findViewWithTag("statusAccessory");
            actionAccessory = (View)view.findViewWithTag("actionAccessory");
            connectingAccessory = (View)view.findViewWithTag("connectingAccessory");
            batteryAccessory = (View)view.findViewWithTag("batteryAccessory");
            batteryLevel = (ImageView)view.findViewWithTag("batteryLevel");
            otaProgressLabel = (TextView)view.findViewWithTag("otaProgressLabel");
            backgroundImage = (ImageView)view.findViewWithTag("backgroundImage");
        }
    }
}
