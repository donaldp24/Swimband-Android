package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.aquaticsafetyconceptsllc.iswimband.WheelPicker.ArrayListWheelAdapter;
import com.aquaticsafetyconceptsllc.iswimband.WheelPicker.WheelView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by donaldpae on 11/28/14.
 */
public class RedAlertPickerDlg extends Dialog {
    private Activity mParentActivity;

    private WheelView mView;

    private int defTextSize = 20;
    private RedAlertPickerDlgListener dismissListener = null;
    private ArrayList<PickerItem> mRedAlertList;
    private ArrayList<PickerItem> mWarningList;
    private int _editAlertTime;
    private boolean _isRedAlert = true;

    public interface RedAlertPickerDlgListener {
        public void onOk(RedAlertPickerDlg dlg, int index);
        public void onCancel(RedAlertPickerDlg dlg);
    }

    public RedAlertPickerDlg(Activity parentActivity, int editAlertTime, boolean isRedAlert, RedAlertPickerDlgListener listener) {
        super(parentActivity);
        dismissListener = listener;
        mParentActivity = parentActivity;
        _editAlertTime = editAlertTime;
        _isRedAlert = isRedAlert;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_wheel_redalertpicker);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        createRedAlertList();
        createWarningList();

        TextView temp = new TextView(mParentActivity);
        temp.setTextSize(defTextSize);
        temp.setVisibility(View.GONE);

        RelativeLayout pL = (RelativeLayout) findViewById(R.id.parent_layout);
        pL.addView(temp);

        ResolutionSet._instance.iterateChild(((RelativeLayout)findViewById(R.id.parent_layout)).getChildAt(0));

        defTextSize = (int)temp.getTextSize();

        initControl();
    }

    private void initControl()
    {
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(onClickListener);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(onClickListener);

        ArrayListWheelAdapter<PickerItem> adapter;
        if (_isRedAlert)
            adapter = new ArrayListWheelAdapter<PickerItem>(mRedAlertList);
        else
            adapter = new ArrayListWheelAdapter<PickerItem>(mWarningList);

        mView = (WheelView) findViewById(R.id.year);
        mView.setDefTextSize(defTextSize);
        mView.setAdapter(adapter);
        mView.setLabel(mParentActivity.getString(R.string.nian));

        // set current item
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnSave) {
                int index = mView.getCurrentItem();
                if (dismissListener != null)
                    dismissListener.onOk(RedAlertPickerDlg.this, index);
                dismiss();
            }
            else if (v.getId() == R.id.btnCancel) {
                if (dismissListener != null)
                    dismissListener.onCancel(RedAlertPickerDlg.this);
                dismiss();
            }
        }
    };

    protected void createRedAlertList() {
        mRedAlertList = new ArrayList<PickerItem>();
        int nCount = (DetailActivity.ALERT_TIME_MAX - DetailActivity.ALERT_TIME_MIN) / DetailActivity.ALERT_TIME_INC + 1;
        for (int i = 0; i < nCount; i++) {
            int time = (i * DetailActivity.ALERT_TIME_INC) + DetailActivity.ALERT_TIME_MIN;
            String title = DetailActivity.alertTimeString(time);
            int color = Color.rgb(0, 0, 0);
            if (time > DetailActivity.ALERT_TIME_DEFAULT) {
                color = Color.rgb(255, 0, 0);
            }

            PickerItem item = new PickerItem();
            item.index = i;
            item.title = title;
            item.color = color;

            mRedAlertList.add(item);
        }
    }

    protected void createWarningList() {
        /*
         * create items of roller-list for warning
         */
        // 2014-12-20, commented because Paul Newcomb required only "immediate" for warning time

        /*
        mWarningList = new ArrayList<PickerItem>();
        int count = (_editAlertTime - DetailActivity.WARNING_TIME_MIN) / DetailActivity.ALERT_TIME_INC;
        if (count < 0)
            count = 0;
        count++;

        for (int i = 0; i < count; i++) {
            String title = DetailActivity.warningTimeString(_editAlertTime - (i * DetailActivity.ALERT_TIME_INC), _editAlertTime);
            int color = Color.rgb(0, 0, 0);

            PickerItem item = new PickerItem();
            item.index = i;
            item.title = title;
            item.color = color;

            mWarningList.add(item);
        }
        */

        mWarningList = new ArrayList<PickerItem>();
        int count = 1;
        int i = 0;
        String title = DetailActivity.warningTimeString(_editAlertTime - (i * DetailActivity.ALERT_TIME_INC), _editAlertTime);

        int color = Color.rgb(0, 0, 0);

        PickerItem item = new PickerItem();
        item.index = i;
        item.title = title;
        item.color = color;

        mWarningList.add(item);
    }

    public class PickerItem {
        int index;
        String title;
        int color;

        @Override
        public String toString() {
            return title;
        }
    }

}
