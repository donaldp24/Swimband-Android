package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.aquaticsafetyconceptsllc.iswimband.band.PeripheralBand;

import java.util.Timer;

/**
 * Created by donaldpae on 11/27/14.
 */
public class SerialNoDialog extends Dialog implements View.OnClickListener, Dialog.OnShowListener {
    private Context mContext;
    private PeripheralBand mBand;
    private Button btnContinue;
    private Button btnCancel;
    private TextView textName;
    private EditText editSerialNo;
    private SerialNoDialogInterface dismissListener;

    private String mSerialNo;
    private int nRes = 0; // cancel
    private Timer _nameTimer;
    private Handler mHandler;
    private Runnable mRunnable;

    public SerialNoDialog(Context context, PeripheralBand band, SerialNoDialogInterface dismissListener) {
        super(context);

        mContext = context;
        mBand = band;
        this.dismissListener = dismissListener;

        mHandler = new Handler(context.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if( mBand != null ) {
                    textName.setText(mBand.defaultName());
                }

                mHandler.postDelayed(this, 100);
            }
        };

        setOnShowListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_serialno);

        ResolutionSet._instance.iterateChild((RelativeLayout)findViewById(R.id.layout_parent));

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initControl();

        //_nameTimer = new Timer();
        /*_nameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                _updateNameTimer();
            }
        }, 0, 100);
        */

        setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    nRes = 0;
                    if (dismissListener != null) {
                        dismissListener.onDismiss(SerialNoDialog.this);
                    }
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();

        /*
        _nameTimer.cancel();
        _nameTimer.purge();
        _nameTimer = null;
        */
        mHandler.removeCallbacks(mRunnable);
    }

    private void initControl() {
        btnContinue = (Button)findViewById(R.id.btn_continue);
        btnCancel = (Button)findViewById(R.id.btn_cancel);

        btnContinue.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        textName = (TextView)findViewById(R.id.text_name);
        textName.setText("");

        editSerialNo = (EditText)findViewById(R.id.edit_serialno);
    }

    @Override
    public void onClick(View v) {
        if (btnContinue == v) {
            nRes = 1;
            mSerialNo = editSerialNo.getText().toString();

            if (dismissListener != null) {
                if (dismissListener.shouldDismiss(this)) {
                    dismissListener.onDismiss(this);
                    dismiss();
                }
            }
        }
        else if (btnCancel == v) {
            nRes = 0;
            if (dismissListener != null) {
                dismissListener.onDismiss(this);
            }
            dismiss();
        }
    }


    protected void _updateNameTimer() {
        /*
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBand != null) {
                    textName.setText(mBand.defaultName());
                }
            }
        });
        */
    }

    public int getResponse() {
        return nRes;
    }

    public String getSerialNo() {
        return mSerialNo;
    }

    public PeripheralBand getBand() {
        return mBand;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mHandler.postDelayed(mRunnable, 100);
    }

}
