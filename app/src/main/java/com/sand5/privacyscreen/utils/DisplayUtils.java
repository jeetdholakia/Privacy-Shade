package com.sand5.privacyscreen.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by jeetdholakia on 4/6/17.
 */

public class DisplayUtils {

    public static int getStatusBarHeight(Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        else
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * resources.getDisplayMetrics().density);
    }

    public static ArrayList<Float> getScreenCenterCoordinates(WindowManager windowManager) {
        ArrayList<Float> coordinateList = new ArrayList<>();
        Display display = windowManager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
        float centerX = width / 2;
        float centerY = height / 2;
        coordinateList.add(centerX);
        coordinateList.add(centerY);
        Logger.d("Screen Center X: " + centerX + "Center Y: " + centerY);
        return coordinateList;
    }

    public static float pxToDp(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().getDisplayMetrics());
    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();

    }
}
