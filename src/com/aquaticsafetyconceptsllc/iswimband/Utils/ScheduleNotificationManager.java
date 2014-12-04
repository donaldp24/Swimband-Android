package com.aquaticsafetyconceptsllc.iswimband.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import com.aquaticsafetyconceptsllc.iswimband.R;

import java.util.*;

/**
 * Created by donaldpae on 12/3/14.
 */
public class ScheduleNotificationManager {

    private static ScheduleNotificationManager _instance = null;

    private Context mContext;
    private ArrayList<ScheduleNotification> notifications;
    private static int _nextId;
    private NotificationManager mNotificationManager;
    private Handler mHandler;
    private Runnable mRunnable;

    private static final int NOTIFICATION_INTERVAL  = 500;

    public static ScheduleNotificationManager initialize(Context context) {
        if (_instance == null) {
            _instance = new ScheduleNotificationManager(context);
            _nextId = 0;
        }
        return _instance;
    }

    public static ScheduleNotificationManager sharedInstance() {
        return _instance;
    }

    private ScheduleNotificationManager(Context context) {
        mContext = context;
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new ArrayList<ScheduleNotification>();

        mRunnable = new Runnable() {
            @Override
            public void run() {
                processProc();

                mHandler.postDelayed(mRunnable, NOTIFICATION_INTERVAL);
            }
        };

        mHandler = new Handler(context.getMainLooper());
        mHandler.postDelayed(mRunnable, NOTIFICATION_INTERVAL);
    }

    public void cancelNotification(ScheduleNotification notification) {
        mNotificationManager.cancel(notification.id());
    }

    public void scheduleNotification(ScheduleNotification notification) {
        notification.setId(_nextId++);

        List<ScheduleNotification> syncedList = Collections.synchronizedList(notifications);
        synchronized (syncedList) {
            syncedList.add(notification);
        }
    }

    protected void processProc() {
        Date now = new Date();

        ArrayList<ScheduleNotification> currNotifications = new ArrayList<ScheduleNotification>();
        List<ScheduleNotification> syncedList = Collections.synchronizedList(notifications);
        synchronized (syncedList) {
            Iterator<ScheduleNotification> iterator = syncedList.iterator();
            while (iterator.hasNext()) {
                ScheduleNotification notification = iterator.next();
                if (notification.fireDate().compareTo(now) >= 0) {
                    currNotifications.add(notification);
                }
            }

            for (ScheduleNotification notification : currNotifications) {
                syncedList.remove(notification);
            }
        }

        // create notification
        for (ScheduleNotification notification : currNotifications) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(notification.title())
                            .setContentText(notification.alertBody());
            mNotificationManager.notify(notification.id(), mBuilder.build());
        }
    }

}
