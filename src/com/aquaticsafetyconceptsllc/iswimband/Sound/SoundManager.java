package com.aquaticsafetyconceptsllc.iswimband.Sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

/**
 * Created by donaldpae on 11/24/14.
 */
public class SoundManager {
    private static SoundManager _instance = null;
    private Context mContext;
    private MediaPlayer soundPlayer = null;

    public static SoundManager initialize(Context context) {
        if (_instance == null)
            _instance = new SoundManager(context);
        return _instance;
    }

    public static SoundManager sharedInstance() {
        return _instance;
    }

    private SoundManager(Context context) {
        this.mContext = context;
    }

    public void playAlertSound(int nRawRes, boolean vibrate, boolean looping) {
        if (vibrate) {
            Vibrator vib = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(1000);
        }

        //soundPlayer = new MediaPlayer();
        soundPlayer = MediaPlayer.create(mContext, nRawRes);

        try {
            if (soundPlayer != null) {
                soundPlayer.stop();
            }
            soundPlayer.prepare();
            soundPlayer.setLooping(looping);
            soundPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAlertSound() {
        try {
            if (soundPlayer != null)
                soundPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            soundPlayer = null;
        }
    }
}
