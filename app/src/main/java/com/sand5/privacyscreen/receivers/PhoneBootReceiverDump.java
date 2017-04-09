package com.sand5.privacyscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneBootReceiverDump extends BroadcastReceiver {

   /*
   Empty Class, nothing to do here!
    */

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            /*Intent intent11 = new Intent(context, PrivacyShadeService.class);
            context.startService(intent11);*/
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            /*Intent intent11 = new Intent(context, PrivacyShadeService.class);
            context.startService(intent11);*/
        }
    }
}
