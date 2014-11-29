package com.aquaticsafetyconceptsllc.iswimband.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by donaldpae on 11/27/14.
 */
public class OSDate extends Date {
    public OSDate() {
        super();
    }

    public OSDate(Date d) {
        super(d.getTime());
    }

    public OSDate offsetDay(int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.DAY_OF_YEAR, days);
        Date dt = cal.getTime();
        return new OSDate(dt);
    }

    public OSDate offsetMonth(int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.MONTH, months);
        Date dt = cal.getTime();
        return new OSDate(dt);
    }

    public OSDate offsetSecond(int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.SECOND, seconds);
        Date dt = cal.getTime();
        return new OSDate(dt);
    }

    public int compareWithoutHour(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(dt);
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);

        if (year == year2) {
            if (month == month2) {
                if (day == day2)
                    return 0;
                else if (day < day2)
                    return -1;
                else
                    return 1;
            }
            else if (month < month2)
                return -1;
            else
                return 1;
        }
        else if (year < year2)
            return -1;
        else
            return 1;
    }

    // Get difference of day between two date values
    public static long getDiffDay(Date dt_start, Date dt_end) {
        if (dt_start == null || dt_end == null)
            return 0;

        Calendar cal_start = Calendar.getInstance();
        Calendar cal_end = Calendar.getInstance();
        cal_start.setTime(dt_start);
        cal_end.setTime(dt_end);

        cal_start.set(Calendar.MINUTE, 0);
        cal_start.set(Calendar.SECOND, 0);

        cal_end.set(Calendar.MINUTE, 0);
        cal_end.set(Calendar.SECOND, 0);

        long nHours = 0;

        long diff = cal_end.getTimeInMillis() - cal_start.getTimeInMillis();
        nHours = diff / 1000 / 60 / 60;

        long nDay = nHours / 24;

        return nDay;
    }

    public static long daysBetweenDates(Date dt_start, Date dt_end) {
        return getDiffDay(dt_start, dt_end);
    }

    public static Date fromStringWithFormat(String szTime, String szFormat) {
        if (szTime == null || szTime.equals(""))
            return null;

        DateFormat df = null;
        Date dtValue = null;

        try {
            df = new SimpleDateFormat(szFormat);
            dtValue = df.parse(szTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return dtValue;
    }

    // Date to String conversion
    public String toStringWithFormat(String format) {
        String szResult = "";

        DateFormat df = null;
        df = new SimpleDateFormat(format);
        szResult = df.format(this);
        return szResult;
    }
}
