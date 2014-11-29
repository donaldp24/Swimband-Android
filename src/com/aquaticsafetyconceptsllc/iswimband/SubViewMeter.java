package com.aquaticsafetyconceptsllc.iswimband;

import android.widget.ImageView;

/**
 * Created by donaldpae on 11/25/14.
 */
public class SubViewMeter {

    public static int[] barImageRes = {R.drawable.band_meter_0_4, R.drawable.band_meter_1_4,
            R.drawable.band_meter_2_4, R.drawable.band_meter_3_4, R.drawable.band_meter_4_4};
    public static int[] batteryImageRes = {R.drawable.band_battery_red, R.drawable.band_battery_1_4,
            R.drawable.band_battery_2_4, R.drawable.band_battery_3_4, R.drawable.band_battery_4_4};

    public static void setValue(float value, ImageView view, int[] imageRes) {
        int maxBar = (int)(value * imageRes.length);
        if (maxBar < 0)
            maxBar = 0;
        if (maxBar >= imageRes.length)
            maxBar = imageRes.length - 1;
        view.setBackgroundResource(imageRes[maxBar]);
    }
}
