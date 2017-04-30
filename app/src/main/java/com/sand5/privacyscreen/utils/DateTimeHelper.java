package com.sand5.privacyscreen.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateTimeHelper {

    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public static String getTimeDifference(String startDate, String endDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = simpleDateFormat.parse(startDate);
            date2 = simpleDateFormat.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long difference = 0;
        if (date2 != null & date1 != null) {
            difference = date2.getTime() - date1.getTime();
        }
        int days = (int) (difference / (1000 * 60 * 60 * 24));
        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
        hours = (hours < 0 ? -hours : hours);
        return hours + " hours," + min + " minutes";
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("EEE, MMM d, ''yy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

}
