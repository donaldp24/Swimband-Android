package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by donaldpae on 11/27/14.
 */
public class GenericModalAlertDialog extends Dialog implements View.OnClickListener {
    private Activity mParentActivity;

    private Button btnContinue;
    private TextView textMessage;

    private DialogDismissInterface dismissListener;
    private String mMessage;

    private int nRes = 0; // cancel

    public GenericModalAlertDialog(Activity parentActivity, String message, DialogDismissInterface dismissListener) {
        super(parentActivity);

        mParentActivity = parentActivity;
        this.dismissListener = dismissListener;

        mMessage = message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_genericmodalalert);

        ResolutionSet._instance.iterateChild((RelativeLayout)findViewById(R.id.layout_parent));

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initControl();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void initControl() {
        btnContinue = (Button)findViewById(R.id.btn_ok);
        btnContinue.setOnClickListener(this);

        textMessage = (TextView)findViewById(R.id.text_message);
        textMessage.setText(mMessage);
    }

    @Override
    public void onClick(View v) {
        if (btnContinue == v) {
            nRes = 1;
            if (dismissListener != null)
                dismissListener.onDismiss(this, nRes);
            dismiss();
        }
    }
}
