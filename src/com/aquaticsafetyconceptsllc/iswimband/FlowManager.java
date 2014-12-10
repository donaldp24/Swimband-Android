package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.widget.Toast;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Sound.SoundManager;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import com.aquaticsafetyconceptsllc.iswimband.band.PeripheralBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBand;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooBandManager;
import de.greenrobot.event.EventBus;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by donaldpae on 11/27/14.
 */
public class FlowManager implements SerialNoDialogInterface {
    private Context mContext;
    private Object presentedObject;
    private Activity mTopActivity;

    private static FlowManager _instance;

    public WahoooBand _band;


    public boolean isFlashing;
    public boolean isFlashLEDOn;

    private Camera mCamera;
    private Camera.Parameters param;
    private Timer flashTimer;

    public static FlowManager initialize(Context context) {
        if (_instance == null)
            _instance = new FlowManager(context);
        return _instance;
    }

    public static FlowManager sharedInstance() {
        return _instance;
    }

    private FlowManager(Context context) {
        this.mContext = context;

        EventBus.getDefault().register(this);
    }

    public void setTopActivity(Activity activity) {
        mTopActivity = activity;
    }

    public void onEventMainThread(SEvent e) {
        if (PeripheralBand.kPeripheralBandRequestingAuthenticationNotification.equalsIgnoreCase(e.name)) {
            _requestAuthenticationKey((PeripheralBand)e.object);
        } else if (PeripheralBand.kPeripheralBandFirstTimeSetupNotification.equalsIgnoreCase(e.name)) {
            _requestFirstTimeSetup((PeripheralBand)e.object);
        } else if (PeripheralBand.kPeripheralBandConfirmSettingsNotification.equalsIgnoreCase(e.name)) {
            _requestBandSettingConfirmation((PeripheralBand)e.object);
        } else if (WahoooBand.kWahoooBandAlertNotification.equalsIgnoreCase(e.name)) {
            _panicAlert((WahoooBand)e.object);
        }
    }

    protected void _requestAuthenticationKey(PeripheralBand band) {
        Logger.log("FlowManager._requestAuthenticationKey with band(%s)(%s)", band.name(), band.address());
        if ( band != null) {
            if ( presentedObject == null ) {
                Logger.log("FlowManager._requestAuthenticationKey : presentedObject == null, show dialog");
                // show dialog for serial number
                SerialNoDialog dialog = new SerialNoDialog(mTopActivity, band, this);
                presentedObject = dialog;
                dialog.show();
            }
            else {
                Logger.log("FlowManager._requestAuthenticationKey : presentedObject != null, disconnect band, calling bandmanager.disconnect()");
                WahoooBandManager.sharedManager().disconnect(band);
            }
        } else {
            Logger.log("FlowManager._requestAuthenticationKey : error - param band is null");
        }
    }

    protected void _requestFirstTimeSetup(PeripheralBand band) {
        Logger.log("_requestFirstTimeSetup : band (%s)", band.address());
        if ( band != null ) {
            if ( presentedObject == null ) {
                _band = band;
                //iPhone_FirstTimeSetupVC* firstTimeVC = [[iPhone_FirstTimeSetupVC alloc] initWithNibName:@"iPhone_FirstTimeSetupVC" bundle:[NSBundle mainBundle]];
                //firstTimeVC.band = band;
                //presentedObject =
            }
        }
    }

    protected void _requestBandSettingConfirmation(PeripheralBand band) {
        Logger.log("_requestBandSettingConfirmation : band (%s)", band.address());
        if ( band != null ) {
            if ( presentedObject == null ) {
                _band = band;
                //iPhone_UpdateBandSettingsVC* updateVC = [[iPhone_UpdateBandSettingsVC alloc] initWithNibName:@"iPhone_UpdateBandSettingsVC" bundle:[NSBundle mainBundle]];
                //updateVC.band = band;
            }
        }
    }


    protected void _panicAlert(WahoooBand band) {
        Logger.log("_requestBandSettingConfirmation : WahoooBand (%s)", band.name());

        if ( band != null ) {
            boolean presentView = true;
            if (!mTopActivity.getClass().toString().equals(AlarmActivity.class.getName())) {
                if (presentedObject != null) {
                    Dialog dlg = (Dialog)presentedObject;
                    dlg.dismiss();
                    // hide dialog
                    /*
                    if ( presentedObject.getClass().toString().equals()[tabController.presentedViewController isKindOfClass:[BandSetupModalVC class]] )
                    {
                        BandSetupModalVC* setupVC = (BandSetupModalVC*)tabController.presentedViewController;
                        [setupVC viewInterrupted];
                    }

                    [tabController dismissViewControllerAnimated:NO completion:^{}];
                    */
                }
            }
            else {
                presentView = false;
            }

            if( presentView ) {
                Intent intent = new Intent(mTopActivity, AlarmActivity.class);
                mTopActivity.startActivity(intent);
                mTopActivity.overridePendingTransition(R.anim.fade, R.anim.alpha);

                //
                //moved sound and flashing in here so it would only be kicked off once.
                SoundManager.sharedInstance().playAlertSound(R.raw.alarm, true, true);

                startFlashingLED();
            }
        }
    }



    @Override
    public boolean shouldDismiss(SerialNoDialog dialog) {
        PeripheralBand band = dialog.getBand();
        if( band != null ) {
            //if ( band.setAuthenticationKey(dialog.getSerialNo()) ) {
            if (band.setAuthenticationKey("001daa")) {
                return true;
            }
            else {
                /*
                NSString* okString = NSLocalizedStringWithDefaultValue(@"GENERIC_OK", nil, [NSBundle mainBundle], @"OK", @"Generic OK");
                NSString* title = NSLocalizedStringWithDefaultValue(@"SERIAL_CONFIRM_INVALID_TITLE", nil, [NSBundle mainBundle], @"Invalid", @"Invalid serial number alert title");
                NSString* message = NSLocalizedStringWithDefaultValue(@"SERIAL_CONFIRM_INVALID_MESSAGE", nil, [NSBundle mainBundle], @"Entered value does not match.", @"Invalid serial number alert message");
                //TODO: Throw up alert
                _invalidAlert = [[UIAlertView alloc] initWithTitle:title message:message delegate:self cancelButtonTitle:okString otherButtonTitles: nil];

                [_invalidAlert show];
                */
                Toast.makeText(mContext, "Invalid serial number", Toast.LENGTH_LONG).show();
            }
        }

        return false;
    }

    @Override
    public void onDismiss(SerialNoDialog dialog) {
        presentedObject = null;
        if (dialog.getResponse() == 0) {
            WahoooBandManager.sharedManager().disconnect(dialog.getBand());
        }
    }

    public void startFlashingLED() {
        // If session is successfully setup

        if(isCameraAvailable() && !isFlashing) {
            isFlashing = true;
            flashTimer = new Timer();
            flashTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startFlashing();
                }
            }, 100, 1000);
            //[self.flashLEDSession startRunning];
        }

    }

    public void startFlashing() {

        if(isFlashing) {
            if(!isFlashLEDOn) {
                mCamera = Camera.open();
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                mCamera.startPreview();

                //[UIScreen mainScreen].brightness = 0.7;
                isFlashLEDOn = true;
            }
            else {

                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }

                //[UIScreen mainScreen].brightness = 1.0;
                isFlashLEDOn = false;
            }
        }
    }

    public void stopFlashingLED() {

        isFlashing = false;

        if(flashTimer != null) {
            flashTimer.cancel(); flashTimer.purge();
            flashTimer = null;
        }

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        //[UIScreen mainScreen].brightness = self.defaultScreenBrightness;

        isFlashLEDOn = false;
    }

    public boolean isCameraAvailable() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void pushDetailForBand(WahoooBand band) {

        /*
        if ( self.navigationController )
        {
            [self.navigationController popToRootViewControllerAnimated:NO];
        }

        BandDetailVC* detail = [self detailViewForBand:band];

        if( detail && self.navigationController )
        {
            [self.navigationController pushViewController:detail animated:YES];
        }
        */

        Intent intent = detailViewForBand(band);
        mTopActivity.startActivity(intent);
        mTopActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void pushEditDetailForBand(WahoooBand band) {
        /*
        if ( self.navigationController )
        {
            [self.navigationController popToRootViewControllerAnimated:NO];
        }

        BandDetailVC* detail = [self detailViewForBand:band];

        if( detail && self.navigationController )
        {
            [self.navigationController pushViewController:detail animated:YES];

            [detail editTap:nil];
        }
        */
    }

    public Intent detailViewForBand(WahoooBand band) {
        Intent intent = new Intent(mTopActivity, DetailActivity.class);
        _band = band;
        return intent;
    }

}
