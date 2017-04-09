package com.sand5.privacyscreen.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.services.PrivacyShadeService;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;
import com.sand5.privacyscreen.utils.Utils;

public class MainActivity extends AppCompatActivity {

    final private static int PERMISSION_PRIVACY_SHADE_OVERLAY = 501;
    final private static int PERMISSION_USAGE_STATISTICS = 502;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 503;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPhoneStateAccess();
    }


    private void requestPhoneStateAccess() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

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
                // No explanation needed, we can request the permission.
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
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        requestOverLayPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

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

    private void requestUsagePermission(int requestCode) {
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                requestCode);
    }

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
                    needPermissionDialog(requestCode);
                } else {
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
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
