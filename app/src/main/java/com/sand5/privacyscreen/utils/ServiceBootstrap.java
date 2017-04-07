package com.sand5.privacyscreen.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.sand5.privacyscreen.receivers.AlarmBroadcastReceiver;
import com.sand5.privacyscreen.receivers.PhoneBootReceiver;
import com.sand5.privacyscreen.services.PrivacyShadeService;

import java.util.Calendar;

public class ServiceBootstrap {

    private static String TAG = ServiceBootstrap.class.getSimpleName();

    public static synchronized void startAlwaysOnService(Context context,
                                                         String loadedFrom) {

        if (PrivacyShadeService.isRunning == false) {
            // start service
            Intent pIntent = new Intent(context, PrivacyShadeService.class);
            pIntent.putExtra(Constants.STARTUP_ACTION_NAME, loadedFrom);
            context.startService(pIntent);

            // enable 10 secs restart
            Intent mIntent = new Intent(context, AlarmBroadcastReceiver.class);
            mIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mIntent.putExtra(Constants.STARTUP_ACTION_NAME, loadedFrom);
            mIntent.setAction(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                    mIntent, 0);
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(System.currentTimeMillis());
            time.add(Calendar.SECOND, 1);

            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            am.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                    Constants.ALARM_REPEAT_INTERVAL * 1000, sender);

            // enable boot/powerkey restart
            setBootupListen(context, true);
        }
    }

    public static synchronized void stopAlwaysOnService(Context context) {

        // stop service
        Intent pIntent = new Intent(context, PrivacyShadeService.class);
        context.stopService(pIntent);

        // cancel alarm restart
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        // cancel boot/power key restart
        setBootupListen(context, false);
    }

    private static void setBootupListen(Context context, boolean isEnabled) {
        int flag = (isEnabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        ComponentName component = new ComponentName(context,
                PhoneBootReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component, flag,
                PackageManager.DONT_KILL_APP);
    }
}