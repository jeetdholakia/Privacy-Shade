package com.sand5.privacyscreen.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.activities.MainActivity;
import com.sand5.privacyscreen.utils.Constants;


@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {

    private static final String SERVICE_STATUS_FLAG = "serviceStatus";
    private static final String PREFERENCES_KEY =
            "com.sand5.android_quick_settings";
    // Check to see if the device is currently locked.
    //boolean isCurrentlyLocked = this.isLocked();

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("Tile on create");
        //updateTile();
        //Toast.makeText(QuickSettingsService.this,"Tile onCreate",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(QuickSettingsService.this,"Tile onDestroy",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTileAdded() {
        Logger.d("On tile added");
        //Toast.makeText(QuickSettingsService.this,"Tile onAdded",Toast.LENGTH_SHORT).show();
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        //Logger.d("On tile removed");
        //Toast.makeText(QuickSettingsService.this,"Tile onRemoved",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Logger.d("On Start Listening");
        updateTile();
        //Toast.makeText(QuickSettingsService.this,"Tile onStartListening",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Logger.d("On stop listening");
        //Toast.makeText(QuickSettingsService.this,"Tile onStopListening",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick() {
        Logger.d("On Tile Clicked");
        boolean isActive = isMyServiceRunning(PrivacyShadeService.class, QuickSettingsService.this);
        if (isActive) {
            //kill service
            Intent stopServiceIntent = new Intent(QuickSettingsService.this, PrivacyShadeService.class);
            stopServiceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
            this.startService(stopServiceIntent);
            forceSetInactiveTile();
        } else {
            Intent i = new Intent(QuickSettingsService.this, MainActivity.class);
            startActivityAndCollapse(i);
        }

        //Start main activity
        /*if (!isCurrentlyLocked) {

            Resources resources = getApplication().getResources();

            Tile tile = getQsTile();
            String tileLabel = tile.getLabel().toString();
            String tileState = (tile.getState() == Tile.STATE_ACTIVE) ?
                    resources.getString(R.string.service_active) :
                    resources.getString(R.string.service_inactive);

            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);

            /*intent.putExtra(MainActivity.RESULT_ACTIVITY_NAME_KEY,
                    tileLabel);
            intent.putExtra(MainActivity.RESULT_ACTIVITY_INFO_KEY,
                    tileState);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivityAndCollapse(intent);
        }*/
    }

    private void updateTile() {

        Tile tile = this.getQsTile();
        boolean isActive = isMyServiceRunning(PrivacyShadeService.class, QuickSettingsService.this);
        String activeLabel = "Privacy Shade ON";
        String inActiveLabel = "Privacy Shade OFF";
        Icon activeIcon = Icon.createWithResource(QuickSettingsService.this, R.drawable.ic_visibility_off_white_24dp);
        Icon inActiveIcon = Icon.createWithResource(QuickSettingsService.this, R.drawable.ic_visibility_off_grey_500_24dp);

        if (isActive) {
            Logger.d("Setting Active Tile Mode");
            tile.setLabel(activeLabel);
            tile.setIcon(activeIcon);
            tile.setState(Tile.STATE_ACTIVE);

        } else {
            Logger.d("Setting Inactive tile mode");
            tile.setLabel(inActiveLabel);
            tile.setState(Tile.STATE_INACTIVE);
        }

        tile.updateTile();
    }

    private void forceSetInactiveTile() {
        Tile tile = this.getQsTile();
        String inActiveLabel = "Privacy Shade OFF";
        tile.setLabel(inActiveLabel);
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already", "running");
                return true;
            }
        }
        Log.i("Service not", "running");
        return false;
    }
}
