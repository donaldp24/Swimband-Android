package com.aquaticsafetyconceptsllc.iswimband.Utils;

import java.util.Date;

/**
 * Created by donaldpae on 12/3/14.
 */
public class ScheduleNotification {
    /*
    if ( _alertNotification )
        {
            [[UIApplication sharedApplication] cancelLocalNotification:_alertNotification];

            _alertNotification = nil;
        }

        _alertNotification = [[UILocalNotification alloc] init];

        _alertNotification.alertBody = NSLocalizedStringWithDefaultValue(@"WAHOOO_NOTIFICATION_ALERT_BODY", nil, [NSBundle mainBundle], @"An iSwimband requires your attention!", @"Local notification message body");

        _alertNotification.fireDate= [NSDate dateWithTimeIntervalSince1970:fireTime];

        [[UIApplication sharedApplication] scheduleLocalNotification:_alertNotification];
     */
    private int _id;
    private String _alertBody;
    private String _title;
    private Date _fireDate;

    public ScheduleNotification() {
        _id = -1;
        _alertBody = "";
        _title = "";
        _fireDate = new Date();
    }

    public ScheduleNotification(String title, String alertBody, Date fireDate) {
        this._id = -1;
        this._title = title;
        this._alertBody = alertBody;
        this._fireDate = fireDate;
    }

    public int id() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String title() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String alertBody() {
        return _alertBody;
    }

    public void setAlertBody(String alertBody) {
        _alertBody = alertBody;
    }

    public Date fireDate() {
        return _fireDate;
    }

    public void setFireDate(Date fireDate) {
        _fireDate = fireDate;
    }
}
