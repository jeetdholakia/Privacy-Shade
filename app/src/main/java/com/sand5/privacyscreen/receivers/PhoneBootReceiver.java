package com.sand5.privacyscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sand5.privacyscreen.services.PrivacyShadeService;

public class PhoneBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent intent11 = new Intent(context, PrivacyShadeService.class);
            context.startService(intent11);
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent intent11 = new Intent(context, PrivacyShadeService.class);
            context.startService(intent11);
        }
    }
}
