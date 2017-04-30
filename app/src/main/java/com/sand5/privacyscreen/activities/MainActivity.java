package com.sand5.privacyscreen.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.services.PrivacyShadeService;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;
import com.sand5.privacyscreen.utils.Utils;

public class MainActivity extends AppCompatActivity {

    final private static int PERMISSION_PRIVACY_SHADE_OVERLAY = 501;
    //final private static int PERMISSION_USAGE_STATISTICS = 502;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 503;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int numberOfTimesOpened;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        numberOfTimesOpened = preferences.getInt("timesOpened", 1);

        /*if(numberOfTimesOpened != 3){
            numberOfTimesOpened++;
            preferences.edit().putInt("timesOpened",numberOfTimesOpened).apply();
            requestPhoneStateAccess();
        }else{
            numberOfTimesOpened++;
            preferences.edit().putInt("timesOpened",numberOfTimesOpened).apply();
            showRatingDialog();
        }*/

        if (Utils.canDrawOverlays(MainActivity.this)) {
            startPrivacyShadeService();

            /*if (hasUsageStatisticsPermission()) {
                startPrivacyShadeService();
            } else {
                requestUsagePermission(PERMISSION_USAGE_STATISTICS);
            }*/
        } else {
            requestOverLayPermission(PERMISSION_PRIVACY_SHADE_OVERLAY);
        }

        //requestPhoneStateAccess();

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
                            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build();

        ratingDialog.show();
    }


    private void requestPhoneStateAccess() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                Snackbar.make(findViewById(android.R.id.content),
                        "Granting this permission hides the privacy shade when calls are received",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                Snackbar.make(findViewById(android.R.id.content),
                        "Granting this permission hides the privacy shade when calls are received",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        }).show();

            }
        } else {
            if (Utils.canDrawOverlays(MainActivity.this)) {
                startPrivacyShadeService();

            /*if (hasUsageStatisticsPermission()) {
                startPrivacyShadeService();
            } else {
                requestUsagePermission(PERMISSION_USAGE_STATISTICS);
            }*/
            } else {
                requestOverLayPermission(PERMISSION_PRIVACY_SHADE_OVERLAY);
            }
        }
    }

    /*private boolean hasUsageStatisticsPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }*/


    private void startPrivacyShadeService() {
        if (!isMyServiceRunning(PrivacyShadeService.class)) {
            Intent startServiceIntent = new Intent(Constants.STARTFOREGROUND_ACTION);
            startServiceIntent.setClass(this, PrivacyShadeService.class);
            startServiceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
            startService(startServiceIntent);
            ServiceBootstrap.startAlwaysOnService(this, "Main");
            finish();
        } else {
            Toast.makeText(this, getResources().getString(R.string.alert_service_already_running), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void needPermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You need to allow permission for the app to work");
        builder.setPositiveButton("ENABLE",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestOverLayPermission(requestCode);
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }


    private void requestOverLayPermission(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    /*private void requestUsagePermission(int requestCode) {
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                requestCode);
    }*/

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PERMISSION_PRIVACY_SHADE_OVERLAY:
                if (!Utils.canDrawOverlays(MainActivity.this)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Overlay_Permission", "Overlay Permission not granted");
                    mFirebaseAnalytics.logEvent("Permissions", bundle);
                    needPermissionDialog(requestCode);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("Overlay_Permission", "Overlay Permission granted");
                    mFirebaseAnalytics.logEvent("Permissions", bundle);
                    startPrivacyShadeService();
                    /*if (!hasUsageStatisticsPermission()) {
                        requestUsagePermission(PERMISSION_USAGE_STATISTICS);
                    }*/
                }
                break;

            /*case PERMISSION_USAGE_STATISTICS:
                if (hasUsageStatisticsPermission()) {
                    startPrivacyShadeService();
                } else {
                    needPermissionDialog(requestCode);
                }
                break;*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Bundle bundle = new Bundle();
                    bundle.putString("Phone_Access_Permission", "Phone Permission granted");
                    mFirebaseAnalytics.logEvent("Permissions", bundle);

                    if (Utils.canDrawOverlays(MainActivity.this)) {
                        startPrivacyShadeService();
                        /*if (hasUsageStatisticsPermission()) {
                            startPrivacyShadeService();
                        } else {
                            requestUsagePermission(PERMISSION_USAGE_STATISTICS);
                        }*/
                    } else {
                        requestOverLayPermission(PERMISSION_PRIVACY_SHADE_OVERLAY);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("Phone_Access_Permission", "Phone Permission not granted");
                    mFirebaseAnalytics.logEvent("Permissions", bundle);
                    Snackbar.make(findViewById(android.R.id.content), "You can enable the permission from settings",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
            }

        }
    }
}
