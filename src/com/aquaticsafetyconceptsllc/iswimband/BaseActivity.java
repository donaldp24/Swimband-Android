package com.aquaticsafetyconceptsllc.iswimband;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Created by donal_000 on 12/20/2014.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
