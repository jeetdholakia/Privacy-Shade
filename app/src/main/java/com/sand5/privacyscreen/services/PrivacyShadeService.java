package com.sand5.privacyscreen.services;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.activities.SettingsActivity;
import com.sand5.privacyscreen.activities.VinylActivity;
import com.sand5.privacyscreen.events.OnScreenLockEvent;
import com.sand5.privacyscreen.events.OnScreenUnLockEvent;
import com.sand5.privacyscreen.receivers.ScreenLockReceiver;
import com.sand5.privacyscreen.utils.ArrayUtils;
import com.sand5.privacyscreen.utils.BitmapUtils;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;
import com.sand5.privacyscreen.utils.VisibleToggleClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sand5.privacyscreen.utils.Constants.REFRESH_RATE;
import static com.sand5.privacyscreen.utils.Constants.defaultRectangleBorderWidth;
import static com.sand5.privacyscreen.utils.Constants.maximumDefaultBrightness;
import static com.sand5.privacyscreen.utils.DateTimeHelper.getCurrentDate;
import static com.sand5.privacyscreen.utils.DateTimeHelper.getCurrentTime;
import static com.sand5.privacyscreen.utils.DateTimeHelper.getTimeDifference;
import static com.sand5.privacyscreen.utils.DisplayUtils.dpToPx;
import static com.sand5.privacyscreen.utils.DisplayUtils.getStatusBarHeight;
import static com.sand5.privacyscreen.utils.DisplayUtils.pxToDp;

public class PrivacyShadeService extends Service {

    // TODO: 5/6/17 Fix sporadic touches
    // TODO: 5/6/17 Move everything to settings/ new activity
    // TODO: 4/17/17 Walk-through
    // TODO: 5/3/17 Custom imageView with canvas
    // TODO: 4/22/17 Vinyl Store IAP
    // TODO: 4/24/17 Make menu movable
    // TODO: 4/26/17 Add Fire Base invites
    // TODO: 5/2/17 Add color/vinyl/wallpaper selector
    // TODO: 5/3/17 Auto night-mode
    // TODO: 5/3/17 Check network settings in vinyl activity
    // TODO: 5/3/17 Load bitmap asynchronously
    // TODO: 5/3/17 Ratings Activity
    // TODO: 5/3/17 Find new Vinyls
    // TODO: 5/6/17 Generate vinyl thumbnails

    public static boolean isRunning = false;
    static String endTime;
    static String currentDate;
    private static String startTime;
    final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    final String websiteURL = "https://goo.gl/forms/tBenYOnLLMQg39nn1";
    private final String TAG = "PrivacyShadeService";
    int circleViewMarginX;
    int circleViewMarginY;
    int circleViewDestinationX;
    int circleViewDestinationY;
    int counter = 0;
    ImageView topLineDragView;
    ImageView topLineZoomView;
    ImageView bottomLineDragView;
    ImageView bottomLineZoomView;
    LinearLayout toggleShapeLinearLayout;
    boolean isFirstTimeOpen;
    boolean shouldShowNotification;
    RelativeLayout dragTooltipHolder;
    RelativeLayout menuParent;
    CustomTabsClient mCustomTabsClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent mCustomTabsIntent;
    int cx, cy, x, y;
    Runnable bottomLineRunnable;
    Runnable topLineRunnable;
    Thread topLineThread;
    Thread bottomLineThread;
    private int screenHeight;
    private int screenWidth;
    private WindowManager windowManager;
    private RelativeLayout privacyShadeView;
    private RelativeLayout brightnessSeekBarView;
    private RelativeLayout menuView;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private LayoutInflater inflater;
    private RelativeLayout circleView;
    private ImageView circleImageView;
    private Canvas privacyShadeCanvas;
    private Bitmap privacyShadeBitmap;
    private Paint transparentPaint;
    private SharedPreferences preferences;
    private Rect transparentRect;
    private RelativeLayout bottomLineView;
    private Bitmap scaledBitmap;
    private RelativeLayout topLineView;
    private int defaultRectangleHeight = dpToPx(100);
    private float defaultOpacity;
    private WindowManager.LayoutParams privacyShadeParams;
    private LinearLayout brightnessSeekBarHolderLayout;
    private Matrix matrix = new Matrix();
    private BitmapDrawable bitmapDrawable;
    private FirebaseAnalytics mFireBaseAnalytics;
    private Handler handler;
    private int touchedViewId;
    private ImageView privacyShadeImageView;
    private RelativeLayout zoomHelpRelativeLayout;
    private RelativeLayout dragHelpRelativeLayout;
    private RelativeLayout menuHelpRelativeLayout;
    View.OnTouchListener bottomLineTouchListener = new View.OnTouchListener() {

        int numberOfFingers;
        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (isFirstTimeOpen) {
                removeWalkThroughLayouts();
            }
            setTouchedViewId(v.getId());

            hideHamburgerMenu();
            numberOfFingers = event.getPointerCount();
            WindowManager.LayoutParams bottomLineLayoutParams = (WindowManager.LayoutParams) bottomLineView.getLayoutParams();
            WindowManager.LayoutParams topLineLayoutParams = (WindowManager.LayoutParams) topLineView.getLayoutParams();
            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    if (brightnessSeekBarHolderLayout.getVisibility() == View.VISIBLE) {
                        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    }
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;
                    x_init_margin = bottomLineLayoutParams.x;
                    y_init_margin = bottomLineLayoutParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // fadeInRectangleWindow();
                    counter += 1;
                    if ((REFRESH_RATE % counter) == 0) {
                        if (y_cord > y_init_cord) {
                            Logger.d("Swipe Down");
                        } else {
                            Logger.d("Swipe up");
                            if (v.getId() == R.id.bottomLine_zoomLineView & transparentRect.height() <= (dpToPx(64))) {
                                break;
                            }
                        }
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        bottomLineLayoutParams.x = x_cord_Destination;
                        bottomLineLayoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(bottomLineView, bottomLineLayoutParams);

                        if (v.getId() == R.id.bottomLine_dragLineView) {

                            Bundle bundle = new Bundle();
                            bundle.putString("Bottom_Line_Dragged_to", "" + y_cord_Destination);
                            mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);

                            topLineLayoutParams.x = x_cord_Destination;
                            topLineLayoutParams.y = y_cord_Destination - defaultRectangleHeight - 90;
                            windowManager.updateViewLayout(topLineView, topLineLayoutParams);
                        }

                        handler.post(bottomLineRunnable);
                        //bottomLineThread.start();
                    }
                    counter = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    Bundle bundle = new Bundle();
                    bundle.putString("Rectangle_Position", "Rectangle Position at time: " + getCurrentTime() + "\t" + getCurrentDate() + "\n Top: " + transparentRect.top + "\n Left: " + transparentRect.left + "\n Bottom: " + transparentRect.bottom + "\n Right: " + transparentRect.right);
                    mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);
                    // handler.removeCallbacks(bottomLineRunnable);
                    // fadeOutRectangleWindow();
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    View.OnTouchListener topLineTouchListener = new View.OnTouchListener() {


        int numberOfFingers;
        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (isFirstTimeOpen) {
                removeWalkThroughLayouts();
            }
            setTouchedViewId(v.getId());
            hideHamburgerMenu();
            numberOfFingers = event.getPointerCount();
            WindowManager.LayoutParams bottomLineLayoutParams = (WindowManager.LayoutParams) bottomLineView.getLayoutParams();
            WindowManager.LayoutParams topLineLayoutParams = (WindowManager.LayoutParams) topLineView.getLayoutParams();
            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    if (brightnessSeekBarHolderLayout.getVisibility() == View.VISIBLE) {
                        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    }
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;
                    x_init_margin = topLineLayoutParams.x;
                    y_init_margin = topLineLayoutParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (y_cord > y_init_cord) {
                        Logger.d("Swipe Down");
                        if (v.getId() == R.id.topLine_zoomLineView & transparentRect.height() <= (dpToPx(64))) {
                            break;
                        }
                    } else {
                        Logger.d("Swipe up");

                    }
                    int x_diff_move = x_cord - x_init_cord;
                    int y_diff_move = y_cord - y_init_cord;

                    x_cord_Destination = x_init_margin + x_diff_move;
                    y_cord_Destination = y_init_margin + y_diff_move;

                    topLineLayoutParams.x = x_cord_Destination;
                    topLineLayoutParams.y = y_cord_Destination;

                    windowManager.updateViewLayout(topLineView, topLineLayoutParams);

                    if (v.getId() == R.id.topLine_dragLineView) {

                        Bundle bundle = new Bundle();
                        bundle.putString("Top_Line_Drag", "Top Line Dragged to:" + y_cord_Destination);
                        mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);

                        bottomLineLayoutParams.x = x_cord_Destination;
                        bottomLineLayoutParams.y = y_cord_Destination + defaultRectangleHeight + 90;
                        windowManager.updateViewLayout(bottomLineView, bottomLineLayoutParams);
                    }
                    handler.post(topLineRunnable);
                    //topLineThread.start();
                    break;
                case MotionEvent.ACTION_UP:
                    // handler.removeCallbacks(topLineRunnable);
                    Bundle bundle = new Bundle();
                    bundle.putString("Rectangle_Position", "Rectangle Position at time: " + getCurrentTime() + "\t" + getCurrentDate() + "\n Top: " + transparentRect.top + "\n Left: " + transparentRect.left + "\n Bottom: " + transparentRect.bottom + "\n Right: " + transparentRect.right);
                    mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);

                    //fadeOutRectangleWindow();

                    break;
                default:
                    break;
            }
            return true;
        }
    };
    private int defaultCircleDiameter = dpToPx(150);
    private RelativeLayout circlePullView;
    private RelativeLayout circleZoomView;
    private String backgroundType;
    View.OnTouchListener circleEyeTouchListener = new View.OnTouchListener() {

        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            hideHamburgerMenu();
            WindowManager.LayoutParams circleViewParams = (WindowManager.LayoutParams) circleView.getLayoutParams();
            WindowManager.LayoutParams circlePullViewParams = (WindowManager.LayoutParams) circlePullView.getLayoutParams();
            WindowManager.LayoutParams circleZoomViewParams = (WindowManager.LayoutParams) circleZoomView.getLayoutParams();
            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    if (brightnessSeekBarHolderLayout.getVisibility() == View.VISIBLE) {
                        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    }
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;

                    x_init_margin = circlePullViewParams.x;
                    y_init_margin = circlePullViewParams.y;

                    circleViewMarginX = circleViewParams.x;
                    circleViewMarginY = circleViewParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    int x_diff_move = x_cord - x_init_cord;
                    int y_diff_move = y_cord - y_init_cord;

                    x_cord_Destination = x_init_margin + x_diff_move;
                    y_cord_Destination = y_init_margin + y_diff_move;

                    circleViewDestinationX = circleViewMarginX + x_diff_move;
                    circleViewDestinationY = circleViewMarginY + y_diff_move;

                    circlePullViewParams.x = x_cord_Destination;
                    circlePullViewParams.y = y_cord_Destination;

                    circleViewParams.x = circleViewDestinationX;
                    circleViewParams.y = circleViewDestinationY;

                    circleZoomViewParams.x = x_cord_Destination;
                    circleZoomViewParams.y = y_cord_Destination + dpToPx(17) + defaultCircleDiameter;

                    windowManager.updateViewLayout(circlePullView, circlePullViewParams);
                    windowManager.updateViewLayout(circleView, circleViewParams);
                    windowManager.updateViewLayout(circleZoomView, circleZoomViewParams);

                    circleView.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] circleLocation = new int[2];
                            circleView.getLocationOnScreen(circleLocation);
                            int x = circleLocation[0];
                            int y = circleLocation[1];

                            resetBitmapColor();
                            privacyShadeCanvas.drawCircle(x + (circleImageView.getWidth() / 2), y + (circleImageView.getWidth() / 2), circleImageView.getWidth() / 2 - dpToPx(5), transparentPaint);
                            bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                            privacyShadeView.setBackground(bitmapDrawable);
                        }
                    });
                    break;
                case MotionEvent.ACTION_UP:
                    Bundle bundle = new Bundle();
                    bundle.putString("Circle_Drag", "Circle dragged to at time: " + getCurrentTime() + "\t" + getCurrentDate() + "\n Diameter: " + circleViewParams.width + "\n X: " + circleViewParams.x + "\n Y: " + circleViewParams.y);
                    mFireBaseAnalytics.logEvent("Circle_Motion_Events", bundle);
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    View.OnTouchListener circleZoomTouchListener = new View.OnTouchListener() {

        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hideHamburgerMenu();
            WindowManager.LayoutParams circleViewParams = (WindowManager.LayoutParams) circleView.getLayoutParams();
            WindowManager.LayoutParams circlePullViewParams = (WindowManager.LayoutParams) circlePullView.getLayoutParams();
            WindowManager.LayoutParams circleZoomViewParams = (WindowManager.LayoutParams) circleZoomView.getLayoutParams();

            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();


            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:

                    //get initial circle coordinates
                    int[] circleLocation = new int[2];
                    circleView.getLocationOnScreen(circleLocation);
                    x = circleLocation[0];
                    y = circleLocation[1];

                    Logger.d("Location of circle is x: " + x + "\t y: " + y);

                    //find the center point
                    cx = x + (circleView.getWidth() / 2);
                    cy = y + (circleView.getWidth() / 2);
                    Logger.d("Circle view width: " + circleView.getWidth());
                    Logger.d("Center of circle cx is: " + cx + " cy: " + cy);

                    if (brightnessSeekBarHolderLayout.getVisibility() == View.VISIBLE) {
                        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    }
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;

                    x_init_margin = circleZoomViewParams.x;
                    y_init_margin = circleZoomViewParams.y;

                    circleViewMarginX = circleViewParams.x;
                    circleViewMarginY = circleViewParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (y_cord > y_init_cord) {
                        Logger.d("Swipe Down");
                        if (circleView.getWidth() >= (screenWidth - dpToPx(64))) {
                            break;
                        }
                    } else {
                        Logger.d("Swipe up");
                        if (circleView.getWidth() < dpToPx(120)) {
                            break;
                        }
                    }

                    int x_diff_move = x_cord - x_init_cord;
                    int y_diff_move = y_cord - y_init_cord;
                    //Logger.d("Y axis movement:" + y_diff_move);

                    x_cord_Destination = x_init_margin + x_diff_move;
                    y_cord_Destination = y_init_margin + y_diff_move;

                    //calculate distance between center point and touch point = radius
                    //int radius = y_diff_move - cy;
                    //Logger.d("Radius is: " + radius);

                    circleZoomViewParams.y = y_cord_Destination;
                    windowManager.updateViewLayout(circleZoomView, circleZoomViewParams);
                    int radius = (defaultCircleDiameter / 2) + y_diff_move;
                    circlePullViewParams.y = y_cord_Destination - (radius * 2) - dpToPx(17);

                    resetBitmapColor();
                    privacyShadeCanvas.drawCircle(cx, cy, radius, transparentPaint);
                    bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                    privacyShadeView.setBackground(bitmapDrawable);
                    windowManager.updateViewLayout(circlePullView, circlePullViewParams);
                    circleViewParams.height = radius * 2;
                    circleViewParams.width = radius * 2;

                    windowManager.updateViewLayout(circleView, circleViewParams);
                    break;
                case MotionEvent.ACTION_UP:
                    defaultCircleDiameter = circleView.getWidth();
                    Bundle bundle = new Bundle();
                    bundle.putString("Circle_Zoom", "Circle zoomed to at time: " + getCurrentTime() + "\t" + getCurrentDate() + "\n Diameter: " + defaultCircleDiameter + "\n X: " + circleViewParams.x + "\n Y: " + circleViewParams.y);
                    mFireBaseAnalytics.logEvent("Circle_Motion_Events", bundle);
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    private ImageButton toggleCircleButton;
    private ShapeType shapeType;
    private BroadcastReceiver screenLockReceiver;
    private boolean isClosedBeforeLock = false;

    public int getTouchedViewId() {
        return touchedViewId;
    }

    public void setTouchedViewId(int touchedViewId) {
        this.touchedViewId = touchedViewId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        Bus bus = PrivacyScreenApplication.bus;
        bus.register(this);
        FirebaseApp.initializeApp(this);
        Logger.d("PrivacyShadeService.onCreate()");
        mFireBaseAnalytics = FirebaseAnalytics.getInstance(this);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        initializeThreads();
    }

    private void handleStart() {
        /*
        Initial configuration of everything
         */
        isFirstTimeOpen = preferences.getBoolean("isFirstTime", true);
        Logger.d("Is first time open? " + isFirstTimeOpen);
        shouldShowNotification = preferences.getBoolean("should_show_notifications", true);
        Logger.d("Should show notification first: " + shouldShowNotification);
        startTime = getCurrentTime();
        currentDate = getCurrentDate();
        Bundle bundle = new Bundle();
        bundle.putString("Start_time", "Overlay started at time: " + startTime + "on " + currentDate);
        mFireBaseAnalytics.logEvent("Lifecycle_Events", bundle);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        if (!preferences.contains("opacity")) {
            setDefaultOpacity(0.5f);
            preferences.edit().putFloat("opacity", 0.5f).apply();
        } else {
            setDefaultOpacity(preferences.getFloat(("opacity"), 0.5f));
        }

        createDefaultPrivacyShade();
        addPrivacyShade();

        String shapeType;
        if (!preferences.contains("shape")) {
            addTransparentRectangle();
        } else {
            shapeType = preferences.getString("shape", "rectangle");
            if (shapeType.equals("rectangle")) {
                addTransparentRectangle();
            } else {
                addTransparentCircle();
            }
        }
        addPrivacyShadeMenu();
        setUpScreenLockReceiver();
        /*if (isFirstTimeOpen) {
            //showFirstTimeWalkThrough();
        }*/

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            collectAppUsageData();
        }*/
    }

    private void createDefaultPrivacyShade() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        int topLeft, bottomRight, defaultRectangleTop;

        topLeft = (screenHeight / 2) - defaultRectangleHeight / 2;
        bottomRight = (screenHeight / 2) + defaultRectangleHeight / 2;

        defaultRectangleTop = 0;

        //noinspection SuspiciousNameCombination
        transparentRect = new Rect(defaultRectangleTop, topLeft, screenWidth, bottomRight);
        transparentPaint = new Paint();
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAntiAlias(true);

        backgroundType = preferences.getString("background_type", "color");

        if (backgroundType.equals("color")) {
            Logger.d("Background type is color");
            privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            privacyShadeBitmap.eraseColor((ContextCompat.getColor(getApplicationContext(), R.color.black)));
        } else {
            Logger.d("Background type is wallpaper/vinyl");
            Bitmap sourceBitmap = BitmapUtils.getBitmapFromFile();
            scaledBitmap = BitmapUtils.getScaledBitmap(sourceBitmap, screenWidth, screenHeight);
            privacyShadeBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, screenWidth, screenHeight);
        }

        //bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, matrix, null);
    }

    @SuppressLint("InflateParams")
    private void addPrivacyShade() {
        privacyShadeView = (RelativeLayout) inflater.inflate(R.layout.layout_privacy_screen, null);
        privacyShadeImageView = (ImageView) privacyShadeView.findViewById(R.id.privacyShade_imageView);
        szWindow.set(screenWidth, screenHeight);

        /*
        Underlying touches are supported by TYPE_SYSTEM_OVERLAY and not by TYPE_PHONE
		 */
        privacyShadeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        privacyShadeParams.gravity = Gravity.TOP | Gravity.START;
        privacyShadeParams.alpha = getDefaultOpacity();
        windowManager.addView(privacyShadeView, privacyShadeParams);
    }

    @SuppressLint("InflateParams")
    private void addTransparentRectangle() {
        setShapeType(ShapeType.RECTANGLE);
        preferences.edit().putString("shape", "rectangle").apply();
        /*
        Punch a transparent rectangle into the privacy shade
         */
        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
        privacyShadeImageView.setImageBitmap(privacyShadeBitmap);
        if (toggleCircleButton != null) {
            toggleCircleButton.setImageResource(R.drawable.ic_panorama_fish_eye_white_24dp);
        }

        int top, bottom;

        top = transparentRect.top;
        //left = transparentRect.left;
        bottom = transparentRect.bottom;
        //right = transparentRect.right;


        /*
        Add bottom line to the transparent rectangle
         */
        bottomLineView = (RelativeLayout) inflater.inflate(R.layout.layout_bottomline, null);
        bottomLineDragView = (ImageView) bottomLineView.findViewById(R.id.bottomLine_dragLineView);
        bottomLineZoomView = (ImageView) bottomLineView.findViewById(R.id.bottomLine_zoomLineView);
        dragTooltipHolder = (RelativeLayout) bottomLineView.findViewById(R.id.tooltip_drag_container);
        //bottomLineView.setPadding(0,30,0,30);
        WindowManager.LayoutParams bottomLineParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                defaultRectangleBorderWidth * 3,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        bottomLineParams.gravity = Gravity.TOP | Gravity.START;
        bottomLineParams.x = 0;
        //Logger.d("Status Bar height: " + getStatusBarHeight(getApplicationContext()));
        bottomLineParams.y = bottom;
        windowManager.addView(bottomLineView, bottomLineParams);
        bottomLineDragView.setOnTouchListener(bottomLineTouchListener);
        bottomLineZoomView.setOnTouchListener(bottomLineTouchListener);

        /*
        Add topline view over transparent rectangle
         */
        topLineView = (RelativeLayout) inflater.inflate(R.layout.layout_topline, null);
        topLineDragView = (ImageView) topLineView.findViewById(R.id.topLine_dragLineView);
        topLineZoomView = (ImageView) topLineView.findViewById(R.id.topLine_zoomLineView);
        WindowManager.LayoutParams topLineParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                defaultRectangleBorderWidth * 3,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        topLineParams.gravity = Gravity.TOP | Gravity.START;
        topLineParams.x = 0;
        topLineParams.y = top - 90;
        windowManager.addView(topLineView, topLineParams);
        topLineDragView.setOnTouchListener(topLineTouchListener);
        topLineZoomView.setOnTouchListener(topLineTouchListener);
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("deprecation")
    private void showFirstTimeWalkThrough() {

        Logger.d("Showing first time walkthrough");

        dragHelpRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_walkthrough, null);
        TextView dragHelpTextView = (TextView) dragHelpRelativeLayout.findViewById(R.id.textView_walkThrough);
        dragHelpTextView.setText(getResources().getString(R.string.walkthrough_drag_text));
        zoomHelpRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_walkthrough, null);
        TextView zoomHelpTextView = (TextView) zoomHelpRelativeLayout.findViewById(R.id.textView_walkThrough);
        zoomHelpTextView.setText(getResources().getString(R.string.walkthrough_zoom_text));
        menuHelpRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_walkthrough, null);
        TextView menuHelpTextView = (TextView) menuHelpRelativeLayout.findViewById(R.id.textView_walkThrough);
        menuHelpTextView.setText(getResources().getString(R.string.walkthrough_menu_text));

        WindowManager.LayoutParams dragHelperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        dragHelperParams.gravity = Gravity.TOP | Gravity.END;
        dragHelperParams.y = (int) bottomLineView.getY() + dpToPx(15);

        WindowManager.LayoutParams zoomHelperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        zoomHelperParams.gravity = Gravity.TOP | Gravity.START;
        zoomHelperParams.y = (int) topLineView.getY() - topLineView.getWidth() - dpToPx(15);

        WindowManager.LayoutParams menuHelperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        menuHelperParams.gravity = Gravity.TOP | Gravity.END;
        menuHelperParams.y = (int) menuParent.getY();

        windowManager.addView(dragHelpRelativeLayout, dragHelperParams);
        windowManager.addView(menuHelpRelativeLayout, menuHelperParams);
        windowManager.addView(zoomHelpRelativeLayout, zoomHelperParams);
    }

    @SuppressLint("InflateParams")
    private void addPrivacyShadeMenu() {

        menuParent = (RelativeLayout) inflater.inflate(R.layout.layout_menu_privacy_shade, null);
        menuView = (RelativeLayout) menuParent.findViewById(R.id.privacyShade_menu_holder);

        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.END;
        menuParams.y = getStatusBarHeight(getApplicationContext()) * 2;

        RippleView removeShadeButton = (RippleView) menuView.findViewById(R.id.hamburger_close_shade_rippleView);
        toggleShapeLinearLayout = (LinearLayout) menuView.findViewById(R.id.toggle_circle_parent);
        toggleCircleButton = (ImageButton) menuView.findViewById(R.id.toggle_circle_imageButton);
        RippleView toggleBrightnessSeekBarButton = (RippleView) menuView.findViewById(R.id.brightness_menu_rippleView);

        RippleView submitFeedbackButton = (RippleView) menuView.findViewById(R.id.rippleView_menu_feedback);
        RippleView settingsButton = (RippleView) menuView.findViewById(R.id.rippleView_menu_settings);
        RippleView customizeShadeButton = (RippleView) menuView.findViewById(R.id.customize_menu_rippleView);
        RippleView toggleShapeRippleView = (RippleView) menuView.findViewById(R.id.rippleView_menu_change_shape);
        final RippleView toggleMenuButton = (RippleView) menuParent.findViewById(R.id.rippleView_menu_hamburger);

        RippleView shareButton = (RippleView) menuView.findViewById(R.id.hamburger_share_rippleView);


        customizeShadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customize = new Intent(PrivacyShadeService.this, VinylActivity.class);
                customize.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(customize);
                stopSelf();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Share button clicked");
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);

                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Privacy Screen");
                    String sAux = "\nProtect your screen privacy in public places...\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.sand5.privacyscreen \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(i, "Choose an app"));
                } catch (Exception e) {
                    //e.toString();
                }
                hideHamburgerMenu();
                stopSelf();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("Settings button clicked");
                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Settings opened");
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);
                stopSelf();
                Intent i = new Intent(PrivacyShadeService.this, SettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                //hideHamburgerMenu();
            }
        });


        removeShadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Remove shade button clicked");
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);
                isClosedBeforeLock = true;
                stopSelf();
            }
        });

        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Submit Feedback button clicked");
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);
                hideHamburgerMenu();
                stopSelf();
                openFeedbackTab();
            }
        });

        toggleMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFirstTimeOpen) {
                    removeWalkThroughLayouts();
                }

                if (menuView.getVisibility() == View.VISIBLE) {
                    menuView.setVisibility(View.GONE);
                } else {
                    menuView.setVisibility(View.VISIBLE);
                }
            }
        });


        toggleBrightnessSeekBarButton.setOnClickListener(new VisibleToggleClickListener() {
            @Override
            protected void changeVisibility(boolean visible) {

            }
        });

        toggleBrightnessSeekBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = brightnessSeekBarHolderLayout.getVisibility() == View.VISIBLE;
                String filler;
                if (visible) {
                    brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    filler = "opened";
                } else {
                    filler = "closed";
                    brightnessSeekBarHolderLayout.setVisibility(View.VISIBLE);
                }

                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Brightness bar " + filler);
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);

                /*Transition fadeTransition = new Fade();
                fadeTransition.setDuration(400);
                fadeTransition.setInterpolator(new FastOutSlowInInterpolator());
                TransitionManager.beginDelayedTransition(brightnessSeekBarView, fadeTransition);
                brightnessSeekBarHolderLayout.setVisibility(visible ? View.VISIBLE : View.GONE);*/

                hideHamburgerMenu();
            }
        });

        toggleShapeRippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Menu_Events", "Shape Change button opened");
                mFireBaseAnalytics.logEvent("Menu_Events", bundle);
                switch (getShapeType()) {
                    case CIRCLE:
                        changeShape(ShapeType.RECTANGLE);
                        toggleCircleButton.setImageResource(R.drawable.ic_panorama_fish_eye_white_24dp);
                        break;
                    case RECTANGLE:
                        changeShape(ShapeType.CIRCLE);
                        toggleCircleButton.setImageResource(R.drawable.ic_crop_5_4_white_24dp);
                        break;
                }
                hideHamburgerMenu();
            }
        });

        /*setPrivacyShadeMenuColors(removeShadeButton);
        setPrivacyShadeMenuColors(shareButton);
        setPrivacyShadeMenuColors(toggleMenuButton);
        setPrivacyShadeMenuColors(toggleCircleButton);
        setPrivacyShadeMenuColors(submitFeedbackButton);
        setPrivacyShadeMenuColors(settingsButton);
        setPrivacyShadeMenuColors(toggleBrightnessSeekBarButton);*/

        windowManager.addView(menuParent, menuParams);
        menuView.post(new Runnable() {
            @Override
            public void run() {
                addOpacitySeekBar();
            }
        });
    }

    /*private void setPrivacyShadeMenuColors(ImageButton imageButton) {
        int colorString = preferences.getInt("menu_color", Color.WHITE);
        Logger.d("Color from preferences:" + colorString);

        Drawable exitDrawable = imageButton.getDrawable();
        ColorFilter filter = new LightingColorFilter(colorString, colorString);
        exitDrawable.setColorFilter(filter);
        imageButton.setImageDrawable(exitDrawable);
    }*/

    private void removeWalkThroughLayouts() {
        preferences.edit().putBoolean("isFirstTime", false).apply();

        if (menuHelpRelativeLayout != null) {
            windowManager.removeView(menuHelpRelativeLayout);
        }

        if (dragHelpRelativeLayout != null) {
            windowManager.removeView(dragHelpRelativeLayout);
        }

        if (zoomHelpRelativeLayout != null) {
            windowManager.removeView(zoomHelpRelativeLayout);
        }
    }

    @SuppressLint("InflateParams")
    private void addTransparentCircle() {
        setShapeType(ShapeType.CIRCLE);
        Bundle bundle = new Bundle();
        bundle.putString("Window_Events", "Selected Circle shape");

        mFireBaseAnalytics.logEvent("Window_Events", bundle);
        if (toggleCircleButton != null) {
            toggleCircleButton.setImageResource(R.drawable.ic_crop_5_4_white_24dp);
        }
        preferences.edit().putString("shape", "circle").apply();
        circleView = (RelativeLayout) inflater.inflate(R.layout.layout_circle, null);
        WindowManager.LayoutParams circleViewParams = new WindowManager.LayoutParams(
                defaultCircleDiameter,
                defaultCircleDiameter,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        circleViewParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        circleView.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Circle Parent Height:" + circleView.getHeight());
                Log.d(TAG, "Circle Parent Width:" + circleView.getWidth());
                Log.d(TAG, "Circle Parent Height DP:" + pxToDp(circleView.getHeight()));
                Log.d(TAG, "Circle Parent Width DP:" + pxToDp(circleView.getWidth()));

            }
        });
        circleImageView = (ImageView) circleView.findViewById(R.id.transparent_circle_imageView);
        windowManager.addView(circleView, circleViewParams);
        circleImageView.post(new Runnable() {
            @Override
            public void run() {
                /*
                Punch a transparent circle into the privacy shade
                */
                privacyShadeCanvas.drawCircle(screenWidth / 2, screenHeight / 2, circleImageView.getWidth() / 2 - dpToPx(5), transparentPaint);
                privacyShadeImageView.setImageBitmap(privacyShadeBitmap);
                addCirclePullBar();
                addCircleZoomBar();
            }
        });
    }

    @SuppressLint("InflateParams")
    private void addCirclePullBar() {
        int[] circleLocation = new int[2];
        circleView.getLocationOnScreen(circleLocation);
        int x = circleLocation[0];
        int y = circleLocation[1];

        circlePullView = (RelativeLayout) inflater.inflate(R.layout.layout_pull_circle, null);
        WindowManager.LayoutParams circlePullViewParams = new WindowManager.LayoutParams(
                dpToPx(40),
                dpToPx(20),
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        circlePullViewParams.gravity = Gravity.TOP | Gravity.START;

        circlePullViewParams.x = x + (defaultCircleDiameter / 2) - (dpToPx(40) / 2); //- (circlePullView.getWidth()*2));
        circlePullViewParams.y = y - dpToPx(20) + dpToPx(3);

        ImageView circlePullImageView = (ImageView) circlePullView.findViewById(R.id.circle_pull_imageView);
        circlePullImageView.setOnTouchListener(circleEyeTouchListener);
        windowManager.addView(circlePullView, circlePullViewParams);
    }

    @SuppressLint("InflateParams")
    private void addCircleZoomBar() {
        int[] circleLocation = new int[2];
        circleView.getLocationOnScreen(circleLocation);
        int x = circleLocation[0];
        int y = circleLocation[1];

        circleZoomView = (RelativeLayout) inflater.inflate(R.layout.layout_zoom_circle, null);
        WindowManager.LayoutParams circleZoomViewParams = new WindowManager.LayoutParams(
                dpToPx(40),
                dpToPx(20),
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        circleZoomViewParams.gravity = Gravity.TOP | Gravity.START;
        circleZoomViewParams.x = x + (defaultCircleDiameter / 2) - (dpToPx(40) / 2);
        circleZoomViewParams.y = y + defaultCircleDiameter - dpToPx(3);

        ImageView circleZoomImageView = (ImageView) circleZoomView.findViewById(R.id.circle_zoom_imageView);
        circleZoomImageView.setOnTouchListener(circleZoomTouchListener);
        windowManager.addView(circleZoomView, circleZoomViewParams);
    }

    @SuppressLint("InflateParams")
    private void addOpacitySeekBar() {
        brightnessSeekBarView = (RelativeLayout) inflater.inflate(R.layout.layout_brightness_seekbar, null);
        brightnessSeekBarHolderLayout = (LinearLayout) brightnessSeekBarView.findViewById(R.id.brightness_seekbar_holder);
        WindowManager.LayoutParams seekBarParams = new WindowManager.LayoutParams(
                screenWidth - dpToPx(56),
                dpToPx(56),
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        seekBarParams.gravity = Gravity.START;
        seekBarParams.y = (int) menuParent.getY();

        final SeekBar brightnessSeekBar = (SeekBar) brightnessSeekBarView.findViewById(R.id.brightness_seekbar);

        brightnessSeekBar.setMax(maximumDefaultBrightness);

        int progressBar = 100 - Math.round(getDefaultOpacity() * 100);

        Bundle bundle = new Bundle();
        bundle.putString("Opacity", "Opacity at " + getCurrentTime() + "\t" + getCurrentDate() + "is: " + progressBar);
        mFireBaseAnalytics.logEvent("Customization_Events", bundle);

        brightnessSeekBar.setProgress(progressBar);

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setDefaultOpacity((float) (100 - progress) / 100);
                float opacity = (float) (100 - progress) / 100;
                Logger.d("Opacity that is put in preferences: " + opacity);
                preferences.edit().putFloat("opacity", opacity).apply();
                privacyShadeParams.alpha = opacity;
                windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Bundle bundle = new Bundle();
                bundle.putString("Opacity", "Opacity changed at " + getCurrentTime() + "\t" + getCurrentDate() + "to: " + (100 - Math.round(getDefaultOpacity() * 100)));
                mFireBaseAnalytics.logEvent("Customization_Events", bundle);

                final Handler h = new Handler();
                Runnable r1 = new Runnable() {
                    @Override
                    public void run() {
                        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
                    }
                };
                h.postDelayed(r1, 1000);
            }
        });
        windowManager.addView(brightnessSeekBarView, seekBarParams);
    }

    private void initializeThreads() {

        handler = new Handler();
        bottomLineRunnable = new Runnable() {
            @Override
            public void run() {

                int[] windowLineLocations = getWindowLineLocations();
                /*int[] bottomLineLocation = new int[2];
                int[] topLineLocation = new int[2];
                bottomLineView.getLocationOnScreen(bottomLineLocation);
                int x = bottomLineLocation[0];
                Logger.d("Bottom Line Y: " + bottomLineY);
                topLineView.getLocationOnScreen(topLineLocation);*/
                int bottomLineY = windowLineLocations[3];
                int topLineY = windowLineLocations[1];

                resetBitmapColor();

                transparentRect.top = topLineY + 90;
                transparentRect.bottom = bottomLineY + 15;

                if (getTouchedViewId() == R.id.bottomLine_zoomLineView) {
                    defaultRectangleHeight = transparentRect.height();
                    Bundle bundle = new Bundle();
                    bundle.putString("Bottom_Line_Dragged_to", "Bottom Line Zoomed to:" + bottomLineY);
                    bundle.putString("New_Rectangle_height", "" + defaultRectangleHeight);
                    mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);
                }

                if (backgroundType != null) {
                    if (!backgroundType.equals("color")) {
                        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, matrix, null);
                        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
                        //bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                    } else {
                        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
                        //bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        privacyShadeImageView.setImageBitmap(privacyShadeBitmap);
                    }
                });
            }
        };

        topLineRunnable = new Runnable() {
            @Override
            public void run() {

                int[] windowLineLocations = getWindowLineLocations();
                /*int[] bottomLineLocation = new int[2];
                int[] topLineLocation = new int[2];
                bottomLineView.getLocationOnScreen(bottomLineLocation);
                int x = bottomLineLocation[0];
                Logger.d("Bottom Line Y: " + bottomLineY);
                topLineView.getLocationOnScreen(topLineLocation);*/
                int bottomLineY = windowLineLocations[3];
                int topLineY = windowLineLocations[1];

                resetBitmapColor();

                transparentRect.top = topLineY + 90;
                transparentRect.bottom = bottomLineY + 15;

                if (getTouchedViewId() == R.id.topLine_zoomLineView) {
                    defaultRectangleHeight = transparentRect.height();

                    Bundle bundle = new Bundle();
                    bundle.putString("Bottom_Line_Zoom", "Bottom Line Zoomed to:" + bottomLineY);
                    bundle.putString("New_Rectangle_Height", "New rectangle height: " + defaultRectangleHeight);
                    mFireBaseAnalytics.logEvent("Rectangle_Motion_Events", bundle);
                }
                if (backgroundType != null) {
                    if (!backgroundType.equals("color")) {
                        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, matrix, null);
                        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
                        bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                    } else {
                        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
                        bitmapDrawable = new BitmapDrawable(getResources(), privacyShadeBitmap);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        privacyShadeImageView.setImageBitmap(privacyShadeBitmap);
                    }
                });

            }
        };

        topLineThread = new Thread(topLineRunnable);
        bottomLineThread = new Thread(bottomLineRunnable);
    }

    private int[] getWindowLineLocations() {
        int[] bottomLineLocation = new int[2];
        int[] topLineLocation = new int[2];
        topLineView.getLocationOnScreen(topLineLocation);
        bottomLineView.getLocationOnScreen(bottomLineLocation);
        return ArrayUtils.combineIntArrays(topLineLocation, bottomLineLocation);
    }

    /*private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        //startActivityForResult(intent, REQUEST_INVITE);
    }*/

    @Subscribe
    public void hideScreen(OnScreenLockEvent onScreenLockEvent) {
        Logger.d("Screen Locked Otto");
        Bundle bundle = new Bundle();
        bundle.putString("Hide_Screen_Lock", "Hide screen: " + getCurrentTime());
        mFireBaseAnalytics.logEvent("Lifecycle_Events", bundle);
        privacyShadeView.setVisibility(View.GONE);
        menuParent.setVisibility(View.GONE);
        brightnessSeekBarHolderLayout.setVisibility(View.GONE);
        switch (getShapeType()) {
            case RECTANGLE:
                bottomLineView.setVisibility(View.GONE);
                topLineView.setVisibility(View.GONE);
                bottomLineZoomView.setVisibility(View.GONE);
                topLineZoomView.setVisibility(View.GONE);
                bottomLineDragView.setVisibility(View.GONE);
                topLineDragView.setVisibility(View.GONE);
                break;
            case CIRCLE:
                circleView.setVisibility(View.GONE);
                circlePullView.setVisibility(View.GONE);
                circleZoomView.setVisibility(View.GONE);
                break;
        }
    }

    @Subscribe
    public void showScreen(OnScreenUnLockEvent onScreenUnLockEvent) {
        Logger.d("Screen Unlocked Otto");
        Bundle bundle = new Bundle();
        bundle.putString("Show_Screen_Lock", "Show screen: " + getCurrentTime());
        mFireBaseAnalytics.logEvent("Lifecycle_Events", bundle);
        privacyShadeView.setVisibility(View.VISIBLE);
        menuParent.setVisibility(View.VISIBLE);
        switch (getShapeType()) {
            case RECTANGLE:
                bottomLineView.setVisibility(View.VISIBLE);
                topLineView.setVisibility(View.VISIBLE);
                bottomLineZoomView.setVisibility(View.VISIBLE);
                topLineZoomView.setVisibility(View.VISIBLE);
                bottomLineDragView.setVisibility(View.VISIBLE);
                topLineDragView.setVisibility(View.VISIBLE);
                break;
            case CIRCLE:
                circleView.setVisibility(View.VISIBLE);
                circlePullView.setVisibility(View.VISIBLE);
                circleZoomView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void hideHamburgerMenu() {
        if (menuView.getVisibility() == View.VISIBLE) {
            menuView.setVisibility(View.GONE);
        }
    }

    private void openFeedbackTab() {
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

    @SuppressWarnings("deprecation")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int w = windowManager.getDefaultDisplay().getWidth();
        int h = windowManager.getDefaultDisplay().getHeight();
        szWindow.set(w, h);

        WindowManager.LayoutParams privacyShadeViewLayoutParams = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "PrivacyShadeService.onConfigurationChanged -> landscape");

            if (privacyShadeViewLayoutParams.y + (privacyShadeView.getHeight() + getStatusBarHeight(getApplicationContext())) > szWindow.y) {
                privacyShadeViewLayoutParams.y = szWindow.y - (privacyShadeView.getHeight() + getStatusBarHeight(getApplicationContext()));
                windowManager.updateViewLayout(privacyShadeView, privacyShadeViewLayoutParams);

                switch (getShapeType()) {
                    case CIRCLE:
                        changeShape(ShapeType.CIRCLE);

                        break;
                    case RECTANGLE:
                        changeShape(ShapeType.RECTANGLE);
                        break;
                }
            }

            if (privacyShadeViewLayoutParams.x != 0 && privacyShadeViewLayoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "PrivacyShadeService.onConfigurationChanged -> portrait");

            if (privacyShadeViewLayoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

            switch (getShapeType()) {
                case CIRCLE:
                    changeShape(ShapeType.CIRCLE);
                    break;
                case RECTANGLE:
                    changeShape(ShapeType.RECTANGLE);
                    break;
            }

        }

    }

    private void changeShape(ShapeType shapeType) {

        if (privacyShadeView != null) {
            windowManager.removeView(privacyShadeView);
        }

        if (circleView != null) {
            windowManager.removeView(circleView);
            circleView = null;
        }

        if (circlePullView != null) {
            windowManager.removeView(circlePullView);
            circlePullView = null;
        }
        if (circleZoomView != null) {
            windowManager.removeView(circleZoomView);
            circleZoomView = null;
        }

        if (bottomLineView != null) {
            windowManager.removeView(bottomLineView);
            bottomLineView = null;
        }

        if (topLineView != null) {
            windowManager.removeView(topLineView);
            topLineView = null;
        }

        createDefaultPrivacyShade();
        addPrivacyShade();
        switch (shapeType) {
            case CIRCLE:
                addTransparentCircle();
                break;
            case RECTANGLE:
                addTransparentRectangle();
                break;
        }
    }

    private void resetBitmapColor() {
        if (backgroundType != null) {
            if (backgroundType.equals("color")) {
                privacyShadeBitmap.eraseColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            } else {
                privacyShadeBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, screenWidth, screenHeight);
            }
        }

    }

    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            moveToLeft(x_cord_now);
        } else {
            moveToRight(x_cord_now);
        }
    }

    private void moveToLeft(final int x_cord_now) {
        final int x = szWindow.x - x_cord_now;

        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = 0 - (int) (double) bounceValue(step, x);
                windowManager.updateViewLayout(privacyShadeView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;
                windowManager.updateViewLayout(privacyShadeView, mParams);
            }
        }.start();
    }

    private void moveToRight(final int x_cord_now) {
        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;
                mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - privacyShadeView.getWidth();
                windowManager.updateViewLayout(privacyShadeView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - privacyShadeView.getWidth();
                windowManager.updateViewLayout(privacyShadeView, mParams);
            }
        }.start();
    }

    private double bounceValue(long step, long scale) {
        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
    }

    private void setUpScreenLockReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_ANSWER);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        screenLockReceiver = new ScreenLockReceiver();
        registerReceiver(screenLockReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Logger.d("PrivacyShadeService.onStartCommand()");
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
                    startPrivacyScreenService();
                } else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
                    stopPrivacyScreenService();
                } else if (intent.getAction().equals(Constants.CALLRECEIVED_ACTION)) {
                    handleCallReceived();
                } else if (intent.getAction().equals(Constants.CALLENDED_ACTION)) {
                    handleCallEnded();
                }
            }
        }
        return START_NOT_STICKY;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void collectAppUsageData() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getTotalTimeInForeground() > 0) {
                Logger.d("Usage stats:" + usageStats.getPackageName());
            }
        }
    }*/

    private void handleCallReceived() {
        Bundle bundle = new Bundle();
        bundle.putString("Call_Events", "Call received at:" + getCurrentTime() + "on: " + getCurrentDate());
        mFireBaseAnalytics.logEvent("Call_Events", bundle);
        Logger.d("Call received in service");
        if (windowManager != null & privacyShadeParams != null) {
            privacyShadeParams.alpha = 0.1f;
            windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
        }
    }

    private void handleCallEnded() {
        Bundle bundle = new Bundle();
        bundle.putString("Call_Events", "Call ended at:" + getCurrentTime() + "on: " + getCurrentDate());
        mFireBaseAnalytics.logEvent("Call_Events", bundle);
        Logger.d("Call ended in service");
        if (windowManager != null & privacyShadeParams != null) {
            privacyShadeParams.alpha = getDefaultOpacity();
            windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
        }
    }

    private void startPrivacyScreenService() {
        handleStart();
        startForegroundService();
        if (!isRunning) {
            ScheduledExecutorService backgroundService = Executors.newSingleThreadScheduledExecutor();
            backgroundService.scheduleAtFixedRate(new TimerIncreasedRunnable(
                    this), 0, 1000, TimeUnit.MILLISECONDS);
            isRunning = true;
        }
    }

    private void stopPrivacyScreenService() {
        isRunning = false;
        stopSelf();
        ServiceBootstrap.stopAlwaysOnService(this);
    }

    // Run service in foreground so it is less likely to be killed by system
    private void startForegroundService() {
        boolean shouldShowStatusBarIcon = preferences.getBoolean("should_show_notification_icon", false);
        Intent stopServiceIntent = new Intent(this, PrivacyShadeService.class);
        int icon;

        stopServiceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification;
        if (shouldShowStatusBarIcon) {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.notification_joiner_shade_is_on))
                    .setContentText(getResources().getString(R.string.notification_turn_off_shade))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.ic_stat_security))
                    .setContentIntent(stopServicePendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.notification_joiner_shade_is_on))
                    .setContentText(getResources().getString(R.string.notification_turn_off_shade))
                    .setSmallIcon(android.R.color.transparent)
                    .setContentIntent(stopServicePendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build();
        }
        startForeground(9999, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("PrivacyService.onDestroy");

        if (privacyShadeView != null) {
            windowManager.removeView(privacyShadeView);
        }

        if (menuParent != null) {
            windowManager.removeView(menuParent);
        }

        if (circleView != null) {
            windowManager.removeView(circleView);
        }

        if (bottomLineView != null) {
            windowManager.removeView(bottomLineView);
        }

        if (topLineView != null) {
            windowManager.removeView(topLineView);
        }

        if (brightnessSeekBarView != null) {
            windowManager.removeView(brightnessSeekBarView);
        }

        if (circlePullView != null) {
            windowManager.removeView(circlePullView);
        }

        if (circleZoomView != null) {
            windowManager.removeView(circleZoomView);
        }

        if (isFirstTimeOpen) {
            if (menuHelpRelativeLayout != null) {
                windowManager.removeView(menuHelpRelativeLayout);
            }

            if (dragHelpRelativeLayout != null) {
                windowManager.removeView(dragHelpRelativeLayout);
            }

            if (zoomHelpRelativeLayout != null) {
                windowManager.removeView(zoomHelpRelativeLayout);
            }
        }

        try {
            endTime = getCurrentTime();
            Bundle bundle = new Bundle();
            bundle.putString("Stop_Time", endTime);
            bundle.putString("Total_Usage_time", getTimeDifference(startTime, endTime));
            mFireBaseAnalytics.logEvent("Lifecycle_Events", bundle);
        } catch (Exception exception) {
            FirebaseCrash.report(exception);
        }

        boolean shouldShowNotifications = preferences.getBoolean("should_show_notifications", true);
        if (shouldShowNotifications) {
            Logger.d("Should show notification true");
            startActiveNotificationService();
        } else {
            Logger.d("Should show notification false");
        }

        if (screenLockReceiver != null) {
            unregisterReceiver(screenLockReceiver);
        }

        /*int numberOfTimesOpened = preferences.getInt("launchCount",0);

        Logger.d("Number of times opened: " + numberOfTimesOpened);
        if(numberOfTimesOpened == 3){
            Intent i = new Intent(PrivacyShadeService.this, RatingsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }*/

        Process.killProcess(Process.myPid());
    }

    /*private void fadeOutRectangleWindow(){
        Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.animation_fade_out);
        bottomLineDragView.startAnimation(animationFadeOut);
        topLineDragView.startAnimation(animationFadeOut);

    }*/

    /*private void fadeInRectangleWindow(){
         Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.animation_fade_in);
        bottomLineDragView.startAnimation(animationFadeIn);
        topLineDragView.startAnimation(animationFadeIn);
    }*/

    private void startActiveNotificationService() {
        Logger.d("PrivacyShadeService.onStop, starting notification service");
        Intent startServiceIntent = new Intent(Constants.STARTFOREGROUND_ACTION);
        startServiceIntent.setClass(this, PersistentNotificationService.class);
        startServiceIntent.putExtra("isClosedBeforeLocking", isClosedBeforeLock);
        startServiceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(startServiceIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d("PrivacyShadeService.onBind()");
        return null;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public float getDefaultOpacity() {
        return defaultOpacity;
    }

    public void setDefaultOpacity(float defaultOpacity) {
        this.defaultOpacity = defaultOpacity;
    }

    private enum ShapeType {RECTANGLE, CIRCLE}

    private class TimerIncreasedRunnable implements Runnable {
        private SharedPreferences currentSharedPreferences;

        TimerIncreasedRunnable(Context context) {
            this.currentSharedPreferences = context.getSharedPreferences(
                    Constants.SHAREDPREF_APP_STRING, MODE_PRIVATE);
        }

        @Override
        public void run() {
            int timeCount = this.readTimeCount() + 1;
            this.writeTimeCount(timeCount);
            int currentEpochTimeInSeconds = (int) (System.currentTimeMillis() / 1000L);
            Log.v(TAG, "Count:" + timeCount + " at time:"
                    + currentEpochTimeInSeconds);
        }

        private int readTimeCount() {
            return this.currentSharedPreferences.getInt(
                    Constants.SHAREDPREF_RUNNINGTIMECOUNT_STRING, 0);
        }

        private void writeTimeCount(int timeCount) {
            this.currentSharedPreferences.edit().putInt(
                    Constants.SHAREDPREF_RUNNINGTIMECOUNT_STRING,
                    timeCount).apply();
        }
    }


}
