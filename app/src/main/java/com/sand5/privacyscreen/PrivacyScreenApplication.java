package com.sand5.privacyscreen;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by jeetdholakia on 4/1/17.
 */

public class PrivacyScreenApplication extends Application {

    public static Bus bus = new Bus(ThreadEnforcer.MAIN);
    private static PrivacyScreenApplication mInstance;
    private SharedPreferences preferences;

    public static synchronized PrivacyScreenApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public Bus getBus() {
        return bus;
    }

    public SharedPreferences getSharedPreferences() {
        return this.preferences;
    }
}
