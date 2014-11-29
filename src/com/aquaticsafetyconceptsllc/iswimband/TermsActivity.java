package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.aquaticsafetyconceptsllc.iswimband.Sound.SoundManager;

/**
 * Created by donaldpae on 11/25/14.
 */
public class TermsActivity extends Activity {
    protected HorizontalPager hpData = null;
    private Button btnTestVolume;
    private boolean isAlarmOn = false;
    protected RelativeLayout mMainLayout = null;
    protected boolean mInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        setLayoutHandler(R.id.layout_parent);

        initControl();
    }

    protected void setLayoutHandler(int layoutId) {
        mMainLayout = (RelativeLayout)findViewById(layoutId);
        mMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                if (mInitialized == false) {
                    Rect r = new Rect();
                    mMainLayout.getLocalVisibleRect(r);
                    ResolutionSet._instance.setResolution(r.width(), r.height(), true);
                    ResolutionSet._instance.iterateChild(mMainLayout);
                    mInitialized = true;
                }
            }
        });
    }

    protected void initControl() {
        hpData = (HorizontalPager)findViewById(R.id.rlTerms);
        hpData.setCurrentScreen(0, false);
        hpData.setOnScreenSwitchListener(new HorizontalPager.OnScreenSwitchListener()	{
            @Override
            public void onScreenSwitched(int oldscreen, int screen) {
                // volume
                if (oldscreen == 1) {
                    if (isAlarmOn) {
                        stopTestAlarm();
                    }
                }

                hpData.setCurrentScreen(screen, false);
            }
        });

        RelativeLayout rlFirst = (RelativeLayout)findViewById(R.id.rlFirst);
        RelativeLayout rlSecond = (RelativeLayout)findViewById(R.id.rlSecond);
        RelativeLayout rlThird = (RelativeLayout)findViewById(R.id.rlThird);

        ViewGroup parentView = (ViewGroup)rlFirst.getParent();
        parentView.removeView(rlFirst);

        parentView = (ViewGroup)rlSecond.getParent();
        parentView.removeView(rlSecond);

        parentView = (ViewGroup)rlThird.getParent();
        parentView.removeView(rlThird);

        hpData.addView(rlFirst);
        hpData.addView(rlSecond);
        hpData.addView(rlThird);

        btnTestVolume = (Button)findViewById(R.id.btnTestVolume);
        btnTestVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAlarmOn) {
                    startTestAlarm();
                } else {
                    stopTestAlarm();
                }
            }
        });

        Button btnAgree = (Button)findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade, R.anim.alpha);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FlowManager.sharedInstance().setTopActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isAlarmOn) {
            stopTestAlarm();
        }
    }

    protected void startTestAlarm() {
        isAlarmOn = true;
        SoundManager.sharedInstance().playAlertSound(R.raw.alarm, true, true);
        btnTestVolume.setText(R.string.btntitle_stop_test_volume);
    }

    protected void stopTestAlarm() {
        isAlarmOn = false;
        SoundManager.sharedInstance().stopAlertSound();
        btnTestVolume.setText(R.string.btntitle_test_volume);
    }
}
