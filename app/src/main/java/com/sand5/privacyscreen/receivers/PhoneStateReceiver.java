package com.sand5.privacyscreen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.events.OnPhoneIdleEvent;
import com.sand5.privacyscreen.events.OnPhoneReceivedEvent;
import com.squareup.otto.Bus;

import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;
    private Bus bus;

    public PhoneStateReceiver() {
        Logger.d("Into phone state constructor");
        bus = PrivacyScreenApplication.bus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("Phone state receiver");
        try {
            //TELEPHONY MANAGER class object to register one listener
            TelephonyManager mTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            //Create Listener
            MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();

            // Register listener for LISTEN_CALL_STATE
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            Logger.e("Phone Receive Error", " " + e);
        }

        /*try {

            Logger.d("Phone Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                //Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show();
                Logger.d("Incoming Call");
                //Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
                Logger.d("Phone Receiver start");


            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
                //Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show();
                Logger.d("Phone Receiver start");

            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show();
                Logger.d("Phone Idle");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {
            // Logger.d("MyPhoneListener: " + state + "\t incoming no: " + incomingNumber);

            switch (state) {
                // 0 Call state idle
                case 0:
                    Logger.d("MyPhoneListener: Call state idle");
                    bus.post(new OnPhoneIdleEvent("Idle"));
                    break;
                // 1 Incoming phone call
                case 1:
                    Logger.d("MyPhoneListener: Incoming phone call: " + incomingNumber);
                    bus.post(new OnPhoneReceivedEvent("Calling"));
                    break;
                // 2 Active phone call
                case 2:
                    Logger.d("MyPhoneListener: Call is active");
                    break;
            }
        }
    }
}
