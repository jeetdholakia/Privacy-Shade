package com.sand5.privacyscreen.services;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.os.IBinder;
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

import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sand5.privacyscreen.utils.DisplayUtils.dpToPx;
import static com.sand5.privacyscreen.utils.DisplayUtils.getScreenCenterCoordinates;
import static com.sand5.privacyscreen.utils.DisplayUtils.getStatusBarHeight;
import static com.sand5.privacyscreen.utils.DisplayUtils.pxToDp;

public class PrivacyShadeService extends Service {

    // TODO: 4/2/17 Enable pinch to zoom

    public static boolean isRunning = false;
    private final String TAG = "PrivacyShadeService";
    int[] bottomLineCoordinates = new int[2];
    int bottomLineCoordinateX;
    int bottomLineCoordinateY;
    private int screenHeight;
    private int screenWidth;
    private WindowManager windowManager;
    private RelativeLayout privacyShadeView;
    private RelativeLayout brightnessSeekBarView;
    private LinearLayout menuView;
    private ImageButton removeShadeButton;
    private ImageButton toggleCircleButton;
    private ImageButton toggleRectangleButton;
    private ImageButton toggleOpacityButton;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private LayoutInflater inflater;
    private DisplayMetrics displayMetrics;
    private RelativeLayout circleView;
    private ImageView circleImageView;
    private Canvas privacyShadeCanvas;
    private Bitmap privacyShadeBitmap;
    private Paint transparentPaint;
    private SeekBar brightnessSeekBar;
    private SharedPreferences preferences;
    private Rect transparentRect;
    private RelativeLayout bottomLineView;
    private RelativeLayout topLineView;
    private int defaultRectangleHeight = 300;
    private int defaultRectangleTop = 0;
    private int defaultRectangleLeft;
    private int defaultRectangleBottom = 0;
    private int defaultRectangleRight;
    private int defaultRectangleBorderWidth = 30;
    private int defaultShadeColor;
    private int defaultOpacity = 50;
    private WindowManager.LayoutParams privacyShadeParams;
    private boolean isTouchingBottomLine = false;
    View.OnTouchListener bottomLineTouchListener = new View.OnTouchListener() {

        int numberOfFingers;
        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouchingBottomLine = true;
            numberOfFingers = event.getPointerCount();
            WindowManager.LayoutParams bottomLineLayoutParams = (WindowManager.LayoutParams) bottomLineView.getLayoutParams();
            WindowManager.LayoutParams topLineLayoutParams = (WindowManager.LayoutParams) topLineView.getLayoutParams();
            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;
                    x_init_margin = bottomLineLayoutParams.x;
                    y_init_margin = bottomLineLayoutParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    int x_diff_move = x_cord - x_init_cord;
                    int y_diff_move = y_cord - y_init_cord;

                    x_cord_Destination = x_init_margin + x_diff_move;
                    y_cord_Destination = y_init_margin + y_diff_move;

                    bottomLineLayoutParams.x = x_cord_Destination;
                    bottomLineLayoutParams.y = y_cord_Destination;

                    topLineLayoutParams.x = x_cord_Destination;
                    topLineLayoutParams.y = y_cord_Destination - defaultRectangleHeight;

                    windowManager.updateViewLayout(bottomLineView, bottomLineLayoutParams);
                    windowManager.updateViewLayout(topLineView, topLineLayoutParams);

                    topLineView.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] bottomLineLocation = new int[2];
                            int[] topLineLocation = new int[2];
                            bottomLineView.getLocationOnScreen(bottomLineLocation);
                            int x = bottomLineLocation[0];
                            int bottomLineY = bottomLineLocation[1];
                            Logger.d("Bottom Line Y: " + bottomLineY);
                            topLineView.getLocationOnScreen(topLineLocation);
                            int topLineY = topLineLocation[1];
                            Logger.d("Top Line Y: " + topLineY);
                            privacyShadeCanvas = null;
                            privacyShadeBitmap = null;
                            transparentPaint = null;
                            privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                            privacyShadeBitmap.eraseColor(getResources().getColor(R.color.black));
                            privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                            privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
                            transparentPaint = new Paint();
                            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                            transparentPaint.setColor(Color.TRANSPARENT);
                            transparentPaint.setAntiAlias(true);
                            privacyShadeCanvas.drawRect(0, topLineY + defaultRectangleBorderWidth / 2, 1080, bottomLineY + defaultRectangleBorderWidth / 2, transparentPaint);
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
                            privacyShadeView.setBackground(bitmapDrawable);
                        }
                    });
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    private boolean isTouchingTopLine = false;
    View.OnTouchListener topLineTouchListener = new View.OnTouchListener() {

        int numberOfFingers;
        int x_cord_Destination, y_cord_Destination;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isTouchingTopLine = true;
            numberOfFingers = event.getPointerCount();
            WindowManager.LayoutParams bottomLineLayoutParams = (WindowManager.LayoutParams) bottomLineView.getLayoutParams();
            WindowManager.LayoutParams topLineLayoutParams = (WindowManager.LayoutParams) topLineView.getLayoutParams();
            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    x_init_cord = x_cord;
                    y_init_cord = y_cord;
                    x_init_margin = topLineLayoutParams.x;
                    y_init_margin = topLineLayoutParams.y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    int x_diff_move = x_cord - x_init_cord;
                    int y_diff_move = y_cord - y_init_cord;

                    x_cord_Destination = x_init_margin + x_diff_move;
                    y_cord_Destination = y_init_margin + y_diff_move;

                    topLineLayoutParams.x = x_cord_Destination;
                    topLineLayoutParams.y = y_cord_Destination;

                    bottomLineLayoutParams.x = x_cord_Destination;
                    bottomLineLayoutParams.y = y_cord_Destination + defaultRectangleHeight;

                    windowManager.updateViewLayout(topLineView, topLineLayoutParams);
                    windowManager.updateViewLayout(bottomLineView, bottomLineLayoutParams);

                    topLineView.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] bottomLineLocation = new int[2];
                            int[] topLineLocation = new int[2];
                            topLineView.getLocationOnScreen(topLineLocation);
                            int x = topLineLocation[0];
                            int topLineY = topLineLocation[1];
                            Logger.d("Bottom Line Y: " + topLineY);
                            bottomLineView.getLocationOnScreen(bottomLineLocation);
                            int bottomLineY = bottomLineLocation[1];
                            Logger.d("Top Line Y: " + topLineY);
                            privacyShadeCanvas = null;
                            privacyShadeBitmap = null;
                            transparentPaint = null;
                            privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                            privacyShadeBitmap.eraseColor(getResources().getColor(R.color.black));
                            privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                            privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
                            transparentPaint = new Paint();
                            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                            transparentPaint.setColor(Color.TRANSPARENT);
                            transparentPaint.setAntiAlias(true);
                            privacyShadeCanvas.drawRect(0, topLineY + defaultRectangleBorderWidth / 2, 1080, bottomLineY + defaultRectangleBorderWidth / 2, transparentPaint);
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
                            privacyShadeView.setBackground(bitmapDrawable);
                        }
                    });
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Logger.d("PrivacyShadeService.onCreate()");
    }

    private void handleStart() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        defaultShadeColor = getResources().getColor(R.color.black);
        ArrayList<Float> coordinateList = getScreenCenterCoordinates(windowManager);
        int x = Math.round(coordinateList.get(0));
        int y = Math.round(coordinateList.get(1));
        int topLeft = (screenHeight / 2) - defaultRectangleHeight / 2;
        int bottomRight = (screenHeight / 2) + defaultRectangleHeight / 2;
        Logger.d("Top left: " + topLeft);
        Logger.d("Bottom Right: " + bottomRight);
        transparentRect = new Rect(defaultRectangleTop, topLeft, screenWidth, bottomRight);
        //addTransparentCircle();
        addPrivacyShade();
        addTransparentRectangle();
        addPrivacyShadeMenu();
        //addOpacitySeekBar();
    }

    private void addTransparentRectangle() {
        /*
        Punch a transparent rectangle into the privacy shade
         */
        privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        privacyShadeBitmap.eraseColor(defaultShadeColor);
        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
        transparentPaint = new Paint();
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAntiAlias(true);
        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);

        int top = transparentRect.top;
        int left = transparentRect.left;
        int bottom = transparentRect.bottom;
        int right = transparentRect.right;
        Logger.d("Transparent Rectangle Top: " + top);
        Logger.d("Transparent Rectangle Left: " + left);
        Logger.d("Transparent Rectangle Bottom: " + bottom);
        Logger.d("Transparent Rectangle Right: " + right);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
        privacyShadeView.setBackground(bitmapDrawable);

        /*
        Add bottom line to the transparent rectangle
         */
        bottomLineView = (RelativeLayout) inflater.inflate(R.layout.layout_line, null);
        //bottomLineView.setPadding(0,30,0,30);
        WindowManager.LayoutParams bottomLineParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                defaultRectangleBorderWidth * 3,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        bottomLineParams.gravity = Gravity.TOP | Gravity.START;
        bottomLineParams.x = 0;
        Logger.d("Status Bar height: " + getStatusBarHeight(getApplicationContext()));
        bottomLineParams.y = bottom - defaultRectangleBorderWidth / 2;
        windowManager.addView(bottomLineView, bottomLineParams);

        bottomLineView.post(new Runnable() {
            @Override
            public void run() {
                bottomLineView.getLocationOnScreen(bottomLineCoordinates);
                bottomLineCoordinateX = bottomLineCoordinates[0];
                bottomLineCoordinateY = bottomLineCoordinates[1];
                Logger.d("Line View Height: " + bottomLineView.getHeight());
                Logger.d("Line View Width: " + bottomLineView.getWidth());
                Logger.d("Line View X: " + bottomLineView.getX());
                Logger.d("Line View Y: " + bottomLineView.getY());
                Logger.d("Line View Screen X: " + bottomLineCoordinateX);
                Logger.d("Line View Screen Y: " + bottomLineCoordinateY);

            }
        });

        bottomLineView.setOnTouchListener(bottomLineTouchListener);

        /*
        Add topline view over transparent rectangle
         */
        topLineView = (RelativeLayout) inflater.inflate(R.layout.layout_line, null);
        WindowManager.LayoutParams topLineParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                defaultRectangleBorderWidth * 3,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        topLineParams.gravity = Gravity.TOP | Gravity.START;
        topLineParams.x = 0;
        topLineParams.y = top - defaultRectangleBorderWidth / 2;
        topLineView.setLayoutParams(topLineParams);
        topLineView.post(new Runnable() {
            @Override
            public void run() {
                //Logger.d("Line Padding top: " + topLineView.getY());
            }
        });

        windowManager.addView(topLineView, topLineParams);
        topLineView.setOnTouchListener(topLineTouchListener);
    }

    private void addPrivacyShade() {
        privacyShadeView = (RelativeLayout) inflater.inflate(R.layout.layout_privacy_screen, null);

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
        privacyShadeParams.x = 0;
        privacyShadeParams.y = 0;
        privacyShadeParams.alpha = defaultOpacity;
        windowManager.addView(privacyShadeView, privacyShadeParams);
    }

    private void addPrivacyShadeMenu() {
        menuView = (LinearLayout) inflater.inflate(R.layout.layout_menu_privacy_shade, null);
        //menuView.setPadding(0,0,32,0);
        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.END;

        removeShadeButton = (ImageButton) menuView.findViewById(R.id.button_close_privacy_screen);
        toggleCircleButton = (ImageButton) menuView.findViewById(R.id.toggle_circle_imageButton);
        toggleRectangleButton = (ImageButton) menuView.findViewById(R.id.toggle_rectangle_imageButton);
        toggleOpacityButton = (ImageButton) menuView.findViewById(R.id.toggle_brightness_imageButton);


        /*removeShadeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        removeShadeButton.requestFocusFromTouch();
                        Log.d("Touch","got");
                        return true;
                    case MotionEvent.ACTION_UP:
                        removeShadeButton.requestFocusFromTouch();
                        Log.d("Touch","got");
                        return true;
                    default:
                        return true;
                }
            }
        });*/

        removeShadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        toggleOpacityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (brightnessSeekBarView.getVisibility() == View.VISIBLE) {
                    brightnessSeekBarView.setVisibility(View.GONE);
                } else {
                    brightnessSeekBarView.setVisibility(View.VISIBLE);
                }
            }
        });

        windowManager.addView(menuView, menuParams);
        menuView.post(new Runnable() {
            @Override
            public void run() {
                addOpacitySeekBar();
            }
        });
    }

    private void addTransparentCircle() {
        circleView = (RelativeLayout) inflater.inflate(R.layout.layout_circle, null);
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

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
        windowManager.addView(circleView, paramRemove);
        circleImageView.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Circle Height:" + circleImageView.getHeight());
                Log.d(TAG, "Circle Width:" + circleImageView.getWidth());
                Log.d(TAG, "Circle Height DP:" + pxToDp(circleImageView.getHeight()));
                Log.d(TAG, "Circle Width DP:" + pxToDp(circleImageView.getWidth()));
            }
        });
    }

    private void addOpacitySeekBar() {
        brightnessSeekBarView = (RelativeLayout) inflater.inflate(R.layout.layout_brightness_seekbar, null);
        //brightnessSeekBarView.setPadding(72,0,72,0);
        WindowManager.LayoutParams seekBarParams = new WindowManager.LayoutParams(
                screenWidth - dpToPx(56),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        seekBarParams.gravity = Gravity.TOP | Gravity.START;
        seekBarParams.y = menuView.getBottom() - 72 - 32;

        brightnessSeekBar = (SeekBar) brightnessSeekBarView.findViewById(R.id.brightness_seekbar);
        brightnessSeekBar.setMax(100);
        brightnessSeekBar.setProgress(defaultOpacity);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float opacity = (float) (100 - progress) / 100;
                Logger.d("On Progress Changed: " + opacity);
                privacyShadeParams.alpha = opacity;
                windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        windowManager.addView(brightnessSeekBarView, seekBarParams);
        brightnessSeekBarView.setVisibility(View.GONE);
    }

    private void showOpacitySeekBar() {

    }

    private void hideOpacitySeekBar() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("PrivacyShadeService.onStartCommand()");
        if (intent != null) {
            Logger.d("Intent is not null");
            if (intent.getAction() != null) {
                Logger.d("Intent action is not null");
                if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
                    handleStart();
                    if (!isRunning) {
                        ScheduledExecutorService backgroundService = Executors.newSingleThreadScheduledExecutor();
                        backgroundService.scheduleAtFixedRate(new TimerIncreasedRunnable(
                                this), 0, 1000, TimeUnit.MILLISECONDS);
                        isRunning = true;
                        }
                    startForegroundService();
                } else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
                    //Stop the service
                    isRunning = false;
                    stopSelf();
                    ServiceBootstrap.stopAlwaysOnService(this);
                }
                }
            }
        return START_STICKY;
    }

    // Run service in foreground so it is less likely to be killed by system
    private void startForegroundService() {

        Intent stopServiceIntent = new Intent(this, PrivacyShadeService.class);
        stopServiceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.notification_joiner_shade_is_on))
                .setContentText(getResources().getString(R.string.notification_turn_off_shade))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.color.transparent)
                .setContentIntent(stopServicePendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
        startForeground(9999, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (privacyShadeView != null) {
            windowManager.removeView(privacyShadeView);
        }

        if (menuView != null) {
            windowManager.removeView(menuView);
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

        Logger.d("PrivacyShadeService.onStop, starting notification service");
        Intent startServiceIntent = new Intent(Constants.STARTFOREGROUND_ACTION);
        startServiceIntent.setClass(this, PersistentNotificationService.class);
        startServiceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(startServiceIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d("PrivacyShadeService.onBind()");
        return null;
    }

    public class TimerIncreasedRunnable implements Runnable {
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
