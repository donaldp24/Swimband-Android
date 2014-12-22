package com.aquaticsafetyconceptsllc.iswimband;

import android.app.ActionBar;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BleManager;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BlePeripheral;
import com.aquaticsafetyconceptsllc.iswimband.CoreData.CoreDataManager;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.band.PeripheralBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

/**
 * Created by donaldpae on 11/28/14.
 */
public class DetailActivity extends BaseActivity implements View.OnClickListener {

    public static final int RANGE_METER_BAR_COUNT = 10;

    public static final int ALERT_TIME_MIN = 10;
    public static final int ALERT_TIME_MAX = 60;
    public static final int ALERT_TIME_INC = 5;
    public static final int ALERT_TIME_DEFAULT = 20;

    public static final int WARNING_TIME_MIN = 10;
    public static final int WARNING_TIME_DEFAULT = 10;

    protected WahoooBand mBand;

    private TextView mNameTextView;
    private TextView mBatteryTextView;
    private TextView mRedAlertTextView;
    private TextView mWarningTextView;
    private TextView mFirmwareTextView;
    private ImageView mStatusImageView;
    private ImageView mSignalImageView;

    private Button mBtnEdit;
    private Button mBtnDisconnect;

    private StatusMonitor mStatusMonitor;
    private ViewGroup mStatusContainer;

    private boolean mEditMode = false;

    private EditText mNameEditTextForEdit;
    private Button mBtnSave;
    private Button mBtnType1;
    private Button mBtnType2;
    private Button mBtnRedAlert;
    private Button mBtnWarning;

    private View mRlStatus;
    private View mRlEdit;
    private View editTimeInfoContainer;

    private int _editAlertTime;
    private int _editWarningTime;

    private int _selectedSegment;

    public static DetailActivity _instance;

    private GenericModalAlertDialog genericModalAlertDisplay;

    public static DetailActivity sharedInstance() {
        return _instance;
    }

    public DetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // set title
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Detail");

        initControls();
        load();

        setBand(FlowManager.sharedInstance()._band);

        ResolutionSet._instance.iterateChild(findViewById(R.id.layout_parent));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        _instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mStatusMonitor != null)
            mStatusMonitor.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mStatusMonitor != null)
            mStatusMonitor.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        _instance = null;
    }

    public void onEventMainThread(SEvent e) {
        if (WahoooBandManager.kWahoooBandDataUpdatedNotification.equalsIgnoreCase(e.name)) {
            _bandUpdated((WahoooBand)e.object);
        } else if (BleManager.kBLEManagerDisconnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
            _peripheralDisconnected((BlePeripheral)e.object);
        }
    }

    public void setBand(WahoooBand b) {
        mBand = b;
        if (mBand == null || mBand.type() == WahoooBand.WahoooBandType.kWahoooBand_Remote)
            mBtnEdit.setVisibility(View.INVISIBLE);

        refreshUI();
    }

    protected void initControls() {
        mNameTextView = (TextView)findViewById(R.id.text_name);
        mBatteryTextView = (TextView)findViewById(R.id.text_batterylevel);
        mRedAlertTextView = (TextView)findViewById(R.id.text_redalert);
        mWarningTextView = (TextView)findViewById(R.id.text_warning);
        mFirmwareTextView = (TextView)findViewById(R.id.text_firmware);

        mStatusImageView = (ImageView)findViewById(R.id.image_status);
        mSignalImageView = (ImageView)findViewById(R.id.image_signal);

        // --
        mBtnEdit = (Button)findViewById(R.id.btn_edit); mBtnEdit.setOnClickListener(this);
        mBtnDisconnect = (Button)findViewById(R.id.btn_disconnect); mBtnDisconnect.setOnClickListener(this);
        // --

        mNameEditTextForEdit = (EditText)findViewById(R.id.edit_nameforedit);
        mBtnSave = (Button)findViewById(R.id.btn_save); mBtnSave.setOnClickListener(this);
        mBtnType1 = (Button)findViewById(R.id.btn_type1); mBtnType1.setOnClickListener(this);
        mBtnType2 = (Button)findViewById(R.id.btn_type2); mBtnType2.setOnClickListener(this);
        mBtnRedAlert = (Button)findViewById(R.id.btn_redalert); mBtnRedAlert.setOnClickListener(this);
        mBtnWarning = (Button)findViewById(R.id.btn_warning); mBtnWarning.setOnClickListener(this);


        mRlStatus = (View)findViewById(R.id.rl_status);
        mRlEdit = (View)findViewById(R.id.rl_edit);

        editTimeInfoContainer = (View)findViewById(R.id.rl_editTimeInfoContainer);


    }

    protected void load() {

        if (mBand == null ||
                mBand.type() != WahoooBand.WahoooBandType.kWahoooBand_Peripheral)
            mBtnEdit.setVisibility(View.INVISIBLE);

        //self.editAlertTimeField.inputView = self.pickerInputView;
        //self.editWarningTimeField.inputView = self.pickerInputView;

        /*
        if ( self.pickerInputView )
        {
            self.pickerInputView.delegate = self;
            self.pickerInputView.pickerView.delegate = self;
            self.pickerInputView.pickerView.dataSource = self;
        }
        */


        // create status monitor
        mStatusMonitor = new StatusMonitor(this);
        mStatusContainer = (ViewGroup)findViewById(R.id.rl_statusmonitor);
        mStatusContainer.addView(mStatusMonitor.getView());

        // (i) button
        /*
        UIBarButtonItem* infoItem = [WahoooAppDelegate createInfoButton];

        self.navigationItem.rightBarButtonItem = infoItem;
        */

        /*
        UIImage* btnImage = [editButton backgroundImageForState:UIControlStateNormal];

        UIEdgeInsets insets;

        insets.bottom = (int)btnImage.size.height / 2;
        insets.right = (int)btnImage.size.width / 2;
        insets.top = btnImage.size.height - insets.bottom;
        insets.left = btnImage.size.width - insets.right;
        */



        //MRN: iOS 7 fix
        if (mEditMode) {
            _enterEditMode(false);
        }

        refreshUI();


        //self.editNameField.placeholder = self.band.defaultName;
    }

    @Override
    public void onClick(View v) {
        if (mBtnEdit == v) {
            editTap();
        }
        else if (mBtnSave == v) {
            doneTap();
        }
        else if (mBtnDisconnect == v) {
            disconnectAction();
        }
        else if (mBtnType1 == v) {
            setSegment(0);
        }
        else if (mBtnType2 == v) {
            setSegment(1);
        }
        else if (mBtnRedAlert == v) {
            onSelectRedAlert();
        }
        else if (mBtnWarning == v) {
            onSelectWarning();
        }
    }

    protected void updateStatus() {
        if (mBand != null)
            LeBandListAdapter.updateStatus(mStatusImageView, mBand.bandState());
    }

    protected void refreshUI() {
        if ( mBand == null )
            return;

        if (mEditMode) {
            //
        }
        else {
            if (mBand.displayName().equalsIgnoreCase(mNameTextView.getText().toString())) {
                mNameEditTextForEdit.setText(mBand.displayName());
            }

            if (mNameEditTextForEdit != null) {
                //self.editNameField.placeholder = self.band.defaultName;
                if (mNameEditTextForEdit.getText().toString().length() == 0)
                    mNameEditTextForEdit.setText(mBand.defaultName());
            }
        }

        mNameTextView.setText(mBand.displayName());

        mFirmwareTextView.setText(mBand.fwVersion());

        if( mBand.alertType() == WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer ) {
            mRedAlertTextView.setText(alertTimeString(WahoooBand.WAHOOBAND_NONSWIMMER_ALERT_TIME,
                    WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer));

            mWarningTextView.setText("");

            findViewById(R.id.text_titleforwarning).setVisibility(View.INVISIBLE);
            mWarningTextView.setVisibility(View.INVISIBLE);
        }
        else {
            mRedAlertTextView.setText(alertTimeString(mBand.alertTime(), mBand.alertType()));
            mWarningTextView.setText(warningTimeString(mBand.warningTime(), mBand.alertTime()));

            findViewById(R.id.text_titleforwarning).setVisibility(View.VISIBLE);
            mWarningTextView.setVisibility(View.VISIBLE);
        }

        mBatteryTextView.setText(_batteryLevelString());


        updateStatus();



        if ( mBand.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral ) {
            mSignalImageView.setVisibility(View.VISIBLE);

            SubViewMeter.setValue(mBand.normalRange(), mSignalImageView, SubViewMeter.barImageRes);
        }
        else {
            mSignalImageView.setVisibility(View.INVISIBLE);
        }


        if ( mBand.type() == WahoooBand.WahoooBandType.kWahoooBand_Remote ) {
            mBtnDisconnect.setVisibility(View.INVISIBLE);
        }
        else {
            mBtnDisconnect.setVisibility(View.VISIBLE);
        }

        if( mBand.bandState() != WahoooBand.WahoooBandState_t.kWahoooBandState_Connected ) {
            mBtnEdit.setVisibility(View.INVISIBLE);
        }
        else if ( !mEditMode ) {
            if ( mBand.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral )
                mBtnEdit.setVisibility(View.VISIBLE);
        }
    }

    protected void _enterEditMode(boolean animated) {
        if (mBand == null)
            return;

        //self.navigationItem.leftBarButtonItem = _bbCancel;
        mNameEditTextForEdit.setText(mBand.displayName());
        mBtnRedAlert.setText(alertTimeString(mBand.alertTime()));
        mBtnWarning.setText(warningTimeString(mBand.warningTime(), mBand.alertTime()));

        //editView.frame = viewContainer.bounds;
        changeSegmentUI(mBand.alertType().getValue());
        if ( animated )
        {
            //[UIView transitionFromView:detailView toView:editView duration:0.3 options:UIViewAnimationOptionTransitionFlipFromRight completion:nil];
            mRlEdit.setVisibility(View.VISIBLE);
            mRlStatus.setVisibility(View.INVISIBLE);
        }
        else
        {
            mRlEdit.setVisibility(View.VISIBLE);
            mRlStatus.setVisibility(View.INVISIBLE);
        }

        _editAlertTime = mBand.alertTime();
        _editWarningTime = mBand.warningTime();

        mEditMode = true;

        if ( editTimeInfoContainer != null) {
            if( mBand.alertType() == WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer) {
                editTimeInfoContainer.setVisibility(View.INVISIBLE);
            }
            else {
                editTimeInfoContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void _exitEditMode(boolean animated) {
        //self.navigationItem.leftBarButtonItem = nil;

        //detailView.frame = self.viewContainer.bounds;

        if ( animated )
        {
            //[UIView transitionFromView:editView toView:detailView duration:0.3 options:UIViewAnimationOptionTransitionFlipFromLeft completion:nil];
            mRlEdit.setVisibility(View.INVISIBLE);
            mRlStatus.setVisibility(View.VISIBLE);
        }
        else
        {
            //[self.viewContainer addSubview:detailView];
            //[editView removeFromSuperview];
            mRlEdit.setVisibility(View.INVISIBLE);
            mRlStatus.setVisibility(View.VISIBLE);
        }

        mEditMode = false;
    }

    protected void changeSegmentUI(int index) {
        if (index == 0) {
            mBtnType1.setTextColor(Color.parseColor("#ffffff"));
            mBtnType2.setTextColor(Color.parseColor("#333333"));

            mBtnType1.setBackgroundResource(R.drawable.roundleftblueblue_layout);
            mBtnType2.setBackgroundResource(R.drawable.roundrightbluegray_layout);
        }
        else {
            mBtnType1.setTextColor(Color.parseColor("#333333"));
            mBtnType2.setTextColor(Color.parseColor("#ffffff"));

            mBtnType1.setBackgroundResource(R.drawable.roundleftbluegray_layout);
            mBtnType2.setBackgroundResource(R.drawable.roundrightblueblue_layout);
        }
    }

    protected void setSegment(int index) {
        changeSegmentUI(index);

        _selectedSegment = index;

        if ( editTimeInfoContainer != null )
        {
            if ( index == WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer.getValue()) {
                editTimeInfoContainer.setVisibility(View.INVISIBLE);
            }
            else {
                editTimeInfoContainer.setVisibility(View.VISIBLE);
            }
        }

        if ( index == WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer.getValue() ) {
            _showAlert(nonSwimmerMessageText());
        }
        else {
            _showAlert(swimmerMessageText());
        }
    }

    public static String alertTimeString(int time) {
        if ( time > 1 || time == 0 ) {
            return String.format("%d seconds", time);
        }
        return "1 second";
    }

    public static String alertTimeString(int time, WahoooBand.WahoooAlertType type) {
        String seconds;
        String result;

        if( type == WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer ) {
            seconds = alertTimeString(WahoooBand.WAHOOBAND_NONSWIMMER_ALERT_TIME);
            result = String.format("Non-Swimmer - %s", seconds);
        }
        else {
            seconds = alertTimeString(time);
            result = String.format("Swimmer - %s", seconds);
        }

        return result;
    }

    public static String warningTimeString(int time, int alertTime) {
        if ( time < alertTime ) {
            return String.format("%d seconds before red alert", time);
        }
        return "Immediate";
    }

    protected String _batteryLevelString() {
        return String.format("%d %%", mBand.batteryLevel());
    }

    public static String nonSwimmerMessageText() {
        return "Non-Swimmer setting is to be used only with wristband\n•	Headband may be removed by toddler";
    }

    public static String swimmerMessageText() {
        return "Swimmer setting is to be used only with headband\n•	Swimmer setting on the wristband may result in high number of submersion alerts or physical injury due to prolonged submersion";
    }

    public static String warningTimeTooLowMessageText() {
        return "Decreasing warning time below 10 seconds may result in physical harm due to prolonged submersion times before warning";
    }

    public static String warningTimeTooHighMessageText() {
        return "Increasing warning time above 10 seconds may cause increase in frequency of warnings";
    }

    public static String alertTimeTooHighMessageText() {
        return "Increasing alert time above 20 seconds may have severe physical repercussions due to prolonged submersion times before alert";
    }

    protected void _registerNotifications() {
        /*
        [[NSNotificationCenter defaultCenter] removeObserver:self name:kWahoooBandDataUpdatedNotification object:nil];


        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_bandUpdated:) name:kWahoooBandDataUpdatedNotification object:nil];

        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_peripheralDisconnected:) name:kBLEManagerDisconnectedPeripheralNotification object:nil];
        */
    }

    protected void _bandUpdated(WahoooBand band) {
        if (mBand != band)
            return;

        refreshUI();

        if( mBand.bandState() != WahoooBand.WahoooBandState_t.kWahoooBandState_Connected ) {
            if( mEditMode ) {
                //self.navigationItem.leftBarButtonItem = nil;
                //[self _exitEditMode:YES];
                _exitEditMode(true);
            }
        }
    }

    protected void _peripheralDisconnected(BlePeripheral peripheral) {
        if (mBand.type() != WahoooBand.WahoooBandType.kWahoooBand_Peripheral)
            return;

        PeripheralBand pband =(PeripheralBand)mBand;

        if ( pband.peripheral() != peripheral )
            return;

        mBtnEdit.setVisibility(View.INVISIBLE);

        if( mEditMode ) {
            //self.navigationItem.leftBarButtonItem = nil;
            //[self _exitEditMode:YES];
            _exitEditMode(true);
        }
    }

    public void editTap() {
        _enterEditMode(true);
    }

    public void cancelTap() {
        _exitEditMode(false);
    }

    public void doneTap() {
        // Commit changes

        if( !mNameEditTextForEdit.getText().toString().equals(mBand.displayName()) ) {
            mBand.changeName(mNameEditTextForEdit.getText().toString());
        }

        //MRN: Can't assume this will always work if there are non-numeric characters in string (or localized)
        mBand.setAlertTime(_editAlertTime);
        mBand.setWarningTime(_editWarningTime);
        mBand.setAlertType((_selectedSegment == 0)? WahoooBand.WahoooAlertType.kWahoooAlertType_NonSwimmer: WahoooBand.WahoooAlertType.kWahoooAlertType_Swimmer);

        refreshUI();

        if (mBand.type() == WahoooBand.WahoooBandType.kWahoooBand_Peripheral) {
            PeripheralBand peripheralBand = (PeripheralBand)mBand;
            // Save Core Data changes
            CoreDataManager.sharedInstance().saveSwimbandData(peripheralBand.bandData());
        }

        _exitEditMode(true);
    }

    public void disconnectAction() {
        if (mBand != null)
            WahoooBandManager.sharedManager().disconnect(mBand);

        onBackPressed();
    }

    protected void onSelectRedAlert() {
        RedAlertPickerDlg dlg = new RedAlertPickerDlg(this, _editAlertTime, true, new RedAlertPickerDlg.RedAlertPickerDlgListener() {
            @Override
            public void onOk(RedAlertPickerDlg dlg, int index) {
                _editAlertTime =  index * ALERT_TIME_INC + ALERT_TIME_MIN;

                if ( _editAlertTime  < _editWarningTime )
                {
                    _editWarningTime = _editAlertTime;
                }

                if ( _editAlertTime > ALERT_TIME_DEFAULT )
                {
                    // Updated 04/05/14: For Requirement #2
                    //[self _showMessageBubble:[BandDetailVC alertTimeTooHighMessageText]];
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    _showAlert(alertTimeTooHighMessageText());
                                }
                            });
                        }
                    }, 100);
                }

                mBtnRedAlert.setText(alertTimeString(_editAlertTime));
                mBtnWarning.setText(warningTimeString(_editWarningTime, _editAlertTime));
            }

            @Override
            public void onCancel(RedAlertPickerDlg dlg) {
                //
            }
        });
        dlg.show();
    }

    protected void onSelectWarning() {
        RedAlertPickerDlg dlg = new RedAlertPickerDlg(this, _editAlertTime, false, new RedAlertPickerDlg.RedAlertPickerDlgListener() {
            @Override
            public void onOk(RedAlertPickerDlg dlg, int index) {
                _editWarningTime = _editAlertTime - (index * ALERT_TIME_INC);
                if ( _editWarningTime < WARNING_TIME_DEFAULT ) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    _showAlert(warningTimeTooLowMessageText());
                                }
                            });
                        }
                    }, 100);
                }
                else if (_editWarningTime > WARNING_TIME_DEFAULT )
                {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    _showAlert(warningTimeTooHighMessageText());
                                }
                            });
                        }
                    }, 100);
                }

                mBtnRedAlert.setText(alertTimeString(_editAlertTime));
                mBtnWarning.setText(warningTimeString(_editWarningTime, _editAlertTime));
            }

            @Override
            public void onCancel(RedAlertPickerDlg dlg) {
                //
            }
        });
        dlg.show();
    }

    protected void _showAlert(String message) {

        genericModalAlertDisplay = new GenericModalAlertDialog(this, message, new DialogDismissInterface() {
            @Override
            public void onDismiss(Dialog dlg, int nRes) {
                //
            }
        });

        genericModalAlertDisplay.show();
    }

}
