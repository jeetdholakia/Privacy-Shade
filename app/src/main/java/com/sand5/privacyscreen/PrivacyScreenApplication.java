package com.sand5.privacyscreen;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eggheadgames.aboutbox.AboutConfig;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

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
        setUpAboutActivity();
    }

    public Bus getBus() {
        return bus;
    }

    public SharedPreferences getSharedPreferences() {
        return this.preferences;
    }

    private void setUpAboutActivity() {
        AboutConfig aboutConfig = AboutConfig.getInstance();
        aboutConfig.appName = getString(R.string.app_name);
        aboutConfig.appIcon = R.mipmap.ic_launcher;
        aboutConfig.version = "1.10.0";
        aboutConfig.aboutLabelTitle = "Protect your privacy in public places";
        aboutConfig.packageName = getApplicationContext().getPackageName();
        aboutConfig.buildType = AboutConfig.BuildType.GOOGLE;

        aboutConfig.facebookUserName = "Privacy Screen Guard & Filter";
        aboutConfig.twitterUserName = "jayd_1992";
        aboutConfig.webHomePage = "https://www.locaholic.co";

        // app publisher for "Try Other Apps" item
        aboutConfig.appPublisher = "Sand 5 Developers";

        aboutConfig.privacyHtmlPath = "https://jeetdholakia.github.io";
        aboutConfig.acknowledgmentHtmlPath = "https://jeetdholakia.github.io";

        // Contact Support email details
        aboutConfig.emailAddress = "social@locaholic.co";
        aboutConfig.emailSubject = "Privacy Screen Feedback";
        aboutConfig.emailBody = "";
    }
}
