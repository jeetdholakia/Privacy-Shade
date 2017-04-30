package com.sand5.privacyscreen.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    SwitchPreferenceCompat notificationPreference;
    SharedPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        notificationPreference = (SwitchPreferenceCompat) findPreference("should_show_notification");
        notificationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean switched = ((SwitchPreferenceCompat) preference)
                        .isChecked();
                switched = !switched;
                String state;
                if (switched) {
                    state = "On";
                } else {
                    state = "Off";
                }
                Logger.d("Notifications are: " + state);
                preferences.edit().putBoolean("should_show_notifications", switched).apply();
                return true;
            }
        });

    }

}
