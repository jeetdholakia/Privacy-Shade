package com.sand5.privacyscreen.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// TODO: 5/7/17 Fire base invites (we do not have coupon codes to share)

public class PrivacyScreenActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_INVITE = 101;
    final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    final String websiteURL = "https://goo.gl/forms/tBenYOnLLMQg39nn1";

    @BindView(R.id.imageview_activity_privacyScreen)
    ImageView privacyScreenBackgroundImageView;

    @BindView(R.id.button_start_shade)
    Button buttonStartShade;

    @BindView(R.id.button_customize_shade)
    Button buttonCustomizeShade;

    @BindView(R.id.button_settings_shade)
    Button buttonSettingsShade;

    @BindView(R.id.button_feedback_shade)
    Button buttonFeedbackShade;

    @BindView(R.id.button_share_shade)
    Button buttonShareShade;

    @BindView(R.id.fab)
    FloatingActionButton startShadeFab;

    CustomTabsClient mCustomTabsClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent mCustomTabsIntent;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_screen);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_privacyScreen_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle("");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPrivacyShade();
            }
        });

        showRatingDialog();
        createGoogleAPIClient();
    }

    private void createGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        //boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, true)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                Logger.d("getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    //Intent intent = result.getInvitationIntent();
                                    //String deepLink = AppInviteReferral.getDeepLink(intent);
                                    //String invitationId = AppInviteReferral.getInvitationId(intent);

                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
                                }
                            }
                        });
    }



    @OnClick({R.id.button_start_shade, R.id.button_customize_shade, R.id.button_settings_shade, R.id.button_feedback_shade, R.id.button_share_shade, R.id.fab})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_start_shade:
                startPrivacyShade();
                break;
            case R.id.button_customize_shade:
                openVinylActivity();
                break;
            case R.id.button_settings_shade:
                openSettingsActivity();
                break;
            case R.id.button_feedback_shade:
                submitFeedBack();
                break;
            case R.id.button_share_shade:
                shareApp();
                break;
            case R.id.fab:
                startPrivacyShade();
                break;
        }
    }

    private void startPrivacyShade() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void openVinylActivity() {
        Intent i = new Intent(this, VinylActivity.class);
        startActivity(i);
    }

    private void openSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void submitFeedBack() {
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .addDefaultShareMenuItem()
                .setToolbarColor(ContextCompat.getColor(this, R.color.black))
                .setShowTitle(true)
                .build();

        mCustomTabsIntent.launchUrl(this, Uri.parse(websiteURL));
    }

    private void shareApp() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_INVITE:
                    Logger.d("Invite sent");
                    // Get the invitation IDs of all sent messages
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                    for (String id : ids) {
                        Logger.d("onActivityResult: sent invitation " + id);
                    }
                    break;
            }
        } else {
            switch (requestCode) {
                case REQUEST_INVITE:
                    Logger.d("onActivityResult: sending invitation failed");
                    break;
            }
        }
    }

    private void showRatingDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(3)
                .session(3)
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
                            Toast.makeText(PrivacyScreenActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build();

        ratingDialog.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.d("On google client connection failed: " + connectionResult.toString());
    }
}
