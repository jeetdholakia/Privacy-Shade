package com.sand5.privacyscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_CUSTOM_ALARM = "alwayson.alarm.action";
    private static final String LOG_TAG = AlarmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM)) {
            String previousAction = intent
                    .getStringExtra(Constants.STARTUP_ACTION_NAME);
            if (previousAction == null || previousAction.length() == 0) {
                previousAction = intent.getAction();
            }
            ServiceBootstrap.startAlwaysOnService(context, previousAction);
        }
    }
}
