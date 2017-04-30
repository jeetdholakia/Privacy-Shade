package com.sand5.privacyscreen;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.eggheadgames.aboutbox.AboutConfig;
import com.eggheadgames.aboutbox.IDialog;
import com.orhanobut.logger.Logger;
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
        aboutConfig.version = "1.8.0";
        aboutConfig.aboutLabelTitle = "About App";
        aboutConfig.packageName = getApplicationContext().getPackageName();
        aboutConfig.buildType = AboutConfig.BuildType.GOOGLE;

        aboutConfig.facebookUserName = "Privacy Screen Guard & Filter";
        aboutConfig.twitterUserName = "jayd_1992";
        aboutConfig.webHomePage = "https://www.locaholic.co";

        // app publisher for "Try Other Apps" item
        aboutConfig.appPublisher = "Try our other apps";

        // if pages are stored locally, then you need to override aboutConfig.dialog to be able use custom WebView
        aboutConfig.privacyHtmlPath = "https://jeetdholakia.github.io";


        aboutConfig.dialog = new IDialog() {
            @Override
            public void open(AppCompatActivity appCompatActivity, String url, String tag) {
                // handle custom implementations of WebView. It will be called when user click to web items. (Example: "Privacy", "Acknowledgments" and "About")
                Logger.d("opened dialog");
            }
        };

        // Contact Support email details
        aboutConfig.emailAddress = "social@locaholic.co";
        aboutConfig.emailSubject = "Privacy Screen Feedback";
        aboutConfig.emailBody = "";
    }
}
