package com.ilan.screenshare.serversdetails.Utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class Helper {

    private static final String TAG = "AppLog";

    public static String getTimeDifferrence(String lastModifedInMili) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        Date startDate = new Date(Long.parseLong(lastModifedInMili));
        Date endDate = new Date(System.currentTimeMillis());

        String str = null;

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        str = "ימים: " + elapsedDays + ", שעות: " + elapsedHours + ", דקות: " + elapsedMinutes + ", שניות: " + elapsedSeconds;
        if (elapsedHours > 0)
            str += "@1";
        else
            str += "@0";

        return str;
    }

}
