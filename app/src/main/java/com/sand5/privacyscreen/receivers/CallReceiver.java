package com.sand5.privacyscreen.receivers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.services.PrivacyShadeService;
import com.sand5.privacyscreen.utils.Constants;

import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Logger.d("Incoming call received");
        if (isMyServiceRunning(PrivacyShadeService.class, ctx)) {
            Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
            stopServiceIntent.setAction(Constants.CALLRECEIVED_ACTION);
            ctx.startService(stopServiceIntent);
        }
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Logger.d("Incoming call answered");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Logger.d("Incoming call ended");
        if (isMyServiceRunning(PrivacyShadeService.class, ctx)) {
            Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
            stopServiceIntent.setAction(Constants.CALLENDED_ACTION);
            ctx.startService(stopServiceIntent);
        }
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Logger.d("Outgoing call started");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Logger.d("Outgoing call ended");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Logger.d("Missed Call");
        if (isMyServiceRunning(PrivacyShadeService.class, ctx)) {
            Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
            stopServiceIntent.setAction(Constants.CALLENDED_ACTION);
            ctx.startService(stopServiceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already", "running");
                return true;
            }
        }
        Log.i("Service not", "running");
        return false;
    }

}