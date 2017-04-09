package com.sand5.privacyscreen.activities;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.services.PrivacyShadeService;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;
import com.sand5.privacyscreen.utils.Utils;

public class MainActivity extends AppCompatActivity {

    final private static int PERMISSION_PRIVACY_SHADE_OVERLAY = 501;
    final private static int PERMISSION_USAGE_STATISTICS = 502;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        if (Utils.canDrawOverlays(MainActivity.this)) {
            if (hasUsageStatisticsPermission()) {
                startPrivacyShadeService();
            } else {
                requestUsagePermission(PERMISSION_USAGE_STATISTICS);
            }
        } else {
            requestOverLayPermission(PERMISSION_PRIVACY_SHADE_OVERLAY);
        }


    }

    private boolean hasUsageStatisticsPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


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
                    if (!hasUsageStatisticsPermission()) {
                        requestUsagePermission(PERMISSION_USAGE_STATISTICS);
                    }
                }
                break;

            case PERMISSION_USAGE_STATISTICS:
                if (hasUsageStatisticsPermission()) {
                    startPrivacyShadeService();
                } else {
                    needPermissionDialog(requestCode);
                }
                break;
        }
    }
}
