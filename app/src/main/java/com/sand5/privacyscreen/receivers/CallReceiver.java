package com.sand5.privacyscreen.receivers;

import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.services.PrivacyShadeService;
import com.sand5.privacyscreen.utils.Constants;

import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Logger.d("Incoming call received");
        Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
        stopServiceIntent.setAction(Constants.CALLRECEIVED_ACTION);
        ctx.startService(stopServiceIntent);

    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Logger.d("Incoming call answered");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Logger.d("Incoming call ended");
        Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
        stopServiceIntent.setAction(Constants.CALLENDED_ACTION);
        ctx.startService(stopServiceIntent);

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
        Intent stopServiceIntent = new Intent(ctx, PrivacyShadeService.class);
        stopServiceIntent.setAction(Constants.CALLENDED_ACTION);
        ctx.startService(stopServiceIntent);
    }

}