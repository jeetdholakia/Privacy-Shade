package com.sand5.privacyscreen.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PersistentNotificationService extends Service {

    private final String TAG = "NotificationService";
    private boolean isRunning = false;


    public PersistentNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("PrivacyShadeService.onStartCommand()");
        if (intent != null) {
            Logger.d("Intent is not null");
            if (intent.getAction() != null) {
                Logger.d("Intent action is not null");
                if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
                    if (!isRunning) {
                        ScheduledExecutorService backgroundService = Executors.newSingleThreadScheduledExecutor();
                        backgroundService.scheduleAtFixedRate(new TimerIncreasedRunnable(
                                this), 0, 1000, TimeUnit.MILLISECONDS);
                        isRunning = true;
                    }
                    startForegroundService();
                } else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
                    //Stop the service
                    isRunning = false;
                    stopSelf();
                    ServiceBootstrap.stopAlwaysOnService(this);
                }
            }
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        Intent stopServiceIntent = new Intent(this, PersistentNotificationService.class);
        stopServiceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.notification_joiner_shade_is_off))
                .setContentText(getResources().getString(R.string.notification_turn_on_shade))
                .setSmallIcon(android.R.color.transparent)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(stopServicePendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
        startForeground(9999, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("NotificationService.onStop, starting privacy shade service");
        Intent startServiceIntent = new Intent(Constants.STARTFOREGROUND_ACTION);
        startServiceIntent.setClass(this, PrivacyShadeService.class);
        startServiceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(startServiceIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private class TimerIncreasedRunnable implements Runnable {
        private SharedPreferences currentSharedPreferences;

        TimerIncreasedRunnable(Context context) {
            this.currentSharedPreferences = context.getSharedPreferences(
                    Constants.SHAREDPREF_APP_STRING, MODE_PRIVATE);
        }

        @Override
        public void run() {
            int timeCount = this.readTimeCount() + 1;
            this.writeTimeCount(timeCount);
            int currentEpochTimeInSeconds = (int) (System.currentTimeMillis() / 1000L);
            Log.v(TAG, "Count:" + timeCount + " at time:"
                    + currentEpochTimeInSeconds);
        }

        private int readTimeCount() {
            return this.currentSharedPreferences.getInt(
                    Constants.SHAREDPREF_RUNNINGTIMECOUNT_STRING, 0);
        }

        private void writeTimeCount(int timeCount) {
            this.currentSharedPreferences.edit().putInt(
                    Constants.SHAREDPREF_RUNNINGTIMECOUNT_STRING,
                    timeCount).apply();
        }
    }
}
