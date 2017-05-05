package com.sand5.privacyscreen.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;

public class RatingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        boolean isRated = preferences.getBoolean("isRated", false);
        if (!isRated) {
            showRatingDialog();
        } else {
            finish();
        }

    }

    private void showRatingDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(3)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"social@locaholic.co"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "To Privacy shade developers");
                        i.putExtra(Intent.EXTRA_TEXT, feedback);
                        try {
                            startActivity(Intent.createChooser(i, "Send an e-mail to developer"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(RatingsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build();

        ratingDialog.show();
        preferences.edit().putBoolean("isRated", true).apply();
    }
}
