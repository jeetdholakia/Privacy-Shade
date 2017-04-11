package com.sand5.privacyscreen.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;

import java.util.ArrayList;
import java.util.List;

public class OnBoardingActivity extends OnboarderActivity {

    List<OnboarderPage> onBoarderPages;
    SharedPreferences preferences;
    boolean isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBoarderPages = new ArrayList<>();
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        isFirstTime = preferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            showOnBoardingPages();
        } else {
            finishActivity();
        }

    }

    private void showOnBoardingPages() {
        // Create your first page
        OnboarderPage onBoarderPage1 = new OnboarderPage("Privacy Screen", "Prevent people from looking at your screen", R.drawable.ic_screen_lock_portrait_white_48dp);
        OnboarderPage onBoarderPage2 = new OnboarderPage("Total Control", "Select the view area that you can control, while still interacting with full screen", R.drawable.ic_filter_center_focus_white_48dp);
        OnboarderPage onBoarderPage3 = new OnboarderPage("Turning on and off", "Tap on notification tray to turn the screen on and off", R.drawable.ic_clear_all_white_48dp);

        // You can define title and description colors (by default white)
        onBoarderPage1.setTitleColor(R.color.white);
        onBoarderPage1.setDescriptionColor(R.color.white);
        onBoarderPage1.setTitleTextSize(30);
        onBoarderPage2.setTitleColor(R.color.white);
        onBoarderPage2.setDescriptionColor(R.color.white);
        onBoarderPage2.setTitleTextSize(30);
        onBoarderPage3.setTitleColor(R.color.white);
        onBoarderPage3.setDescriptionColor(R.color.white);
        onBoarderPage3.setTitleTextSize(30);

        // Don't forget to set background color for your page
        onBoarderPage1.setBackgroundColor(R.color.grey_700);
        onBoarderPage2.setBackgroundColor(R.color.grey_700);
        onBoarderPage3.setBackgroundColor(R.color.grey_700);

        // Add your pages to the list
        onBoarderPages.add(onBoarderPage1);
        onBoarderPages.add(onBoarderPage2);
        onBoarderPages.add(onBoarderPage3);

        this.setSkipButtonHidden();
        shouldDarkenButtonsLayout(true);
        setFinishButtonTitle("SETUP SCREEN");
        // And pass your pages to 'setOnboardPagesReady' method
        setOnboardPagesReady(onBoarderPages);
    }

    private void finishActivity() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }

    @Override
    public void onFinishButtonPressed() {
        preferences.edit().putBoolean("isFirstTime", false).apply();
        finishActivity();
    }

}
