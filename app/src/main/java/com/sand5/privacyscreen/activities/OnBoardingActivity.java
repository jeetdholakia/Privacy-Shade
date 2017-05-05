package com.sand5.privacyscreen.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;

public class OnBoardingActivity extends MaterialIntroActivity {

    SharedPreferences preferences;
    boolean isFirstTime;
    Bundle bundle = new Bundle();
    int launchCount;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        isFirstTime = preferences.getBoolean("isFirstTime", true);
        launchCount = preferences.getInt("launchCount", 0);
        preferences.edit().putInt("launchCount", launchCount++).commit();

        if (isFirstTime) {
            showOnBoardingPages();
        } else {
            finishActivity();
        }

    }

    private void showOnBoardingPages() {

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.onboarding_blue)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.onboard22)
                .title("Privacy Screen Guard")
                .description("Prevent people from looking at your screen by blocking out everything except a small part of the screen that you control")
                .build());


        /*addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.onboarding_blue)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.onboard22)
                .title("Total Control")
                .description("Select the view area that you can control, while still interacting with full screen")
                .build());*/

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.onboarding_blue)
                .buttonsColor(R.color.colorAccent)
                .possiblePermissions(new String[]{Manifest.permission.READ_PHONE_STATE})
                .image(R.drawable.onboard44)
                .title("Smart features")
                .description("Granting this permission hides the privacy shade when calls are received")
                .build());


        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.onboarding_blue)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.onboard33)
                .title("Turning on and off")
                .description("Tap on notification tray or quick setting tiles to turn the screen on and off")
                .build(), new MessageButtonBehaviour(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean("isFirstTime", false).apply();
                bundle.putString("OnBoarding", "OnBoarding completed");
                mFirebaseAnalytics.logEvent("OnBoarding", bundle);
                finishActivity();
            }
        }, "SETUP"));


        enableLastSlideAlphaExitTransition(true);
    }

    private void finishActivity() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        preferences.edit().putBoolean("isFirstTime", false).apply();
        bundle.putString("OnBoarding", "OnBoarding completed");
        mFirebaseAnalytics.logEvent("OnBoarding", bundle);
        finishActivity();
    }

}
