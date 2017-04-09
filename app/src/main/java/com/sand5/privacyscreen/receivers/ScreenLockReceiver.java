package com.sand5.privacyscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.events.OnScreenLockEvent;
import com.sand5.privacyscreen.events.OnScreenUnLockEvent;
import com.squareup.otto.Bus;

public class ScreenLockReceiver extends BroadcastReceiver {

    Bus bus;

    public ScreenLockReceiver() {
        bus = PrivacyScreenApplication.bus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            //Logger.d("Screen Off");
            /*if(isMyServiceRunning(PrivacyShadeService.class,context)){
                pauseService(context);
            }*/
            bus.post(new OnScreenLockEvent("Locked"));
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // Logger.d("Screen On");

        } else if (intent.getAction().equals(Intent.ACTION_ANSWER)) {
            // Logger.d("Call answered");

        }/*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         */ else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            bus.post(new OnScreenUnLockEvent("Unlocked"));
            //resumeService(context);
        }
    }

    /*private void pauseService(Context context){
        Intent stopServiceIntent = new Intent(context, PrivacyShadeService.class);
        stopServiceIntent.setAction(Constants.PAUSEFOREGROUND_ACTION);
        context.stopService(stopServiceIntent);
    }

    private void resumeService(Context context){
            Logger.d("Service is not running, so starting it...");
            Intent stopServiceIntent = new Intent(context, PrivacyShadeService.class);
            stopServiceIntent.setAction(Constants.RESUMEFOREGROUND_ACTION);
            context.startService(stopServiceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }*/
}
