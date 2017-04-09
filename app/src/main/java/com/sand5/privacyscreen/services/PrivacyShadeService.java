package com.sand5.privacyscreen.services;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
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
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.events.OnScreenLockEvent;
import com.sand5.privacyscreen.events.OnScreenUnLockEvent;
import com.sand5.privacyscreen.receivers.ScreenLockReceiver;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ServiceBootstrap;
import com.sand5.privacyscreen.utils.VisibleToggleClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sand5.privacyscreen.utils.DisplayUtils.dpToPx;
import static com.sand5.privacyscreen.utils.DisplayUtils.pxToDp;

public class PrivacyShadeService extends Service {

    // TODO: 4/2/17 Enable pinch to zoom (from feedback)
    // TODO: 4/7/17 Save preferences (coordinates when unlocked)
    // TODO: 4/7/17 Remove lag on dragging
    // TODO: 4/8/17 Rounded icons
    // TODO: 4/8/17 Add landscape mode
    // TODO: 4/8/17 Add Pulling indicators

    static final int REFRESH_RATE = 5;
    public static boolean isRunning = false;
    private final String TAG = "PrivacyShadeService";
    int circleViewMarginX;
    int circleViewMarginY;
    int circleViewDestinationX;
    int circleViewDestinationY;
    int counter = 0;
    int[] bottomLineLocation = new int[2];
    int[] topLineLocation = new int[2];
    int[] circleLocation = new int[2];
    int[] circlePullLocation = new int[2];
    int topLineX, topLineY, bottomLineX, bottomLineY, circleX, circleY, circlePullX, circlePullY;
    private int screenHeight;
    private int screenWidth;
    private WindowManager windowManager;
    private RelativeLayout privacyShadeView;
    private RelativeLayout brightnessSeekBarView;
    private LinearLayout menuView;
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
    private RelativeLayout topLineView;
    private int defaultRectangleHeight = dpToPx(100);
    private int defaultRectangleBorderWidth = 30;
    private int defaultShadeColor;
    private float defaultOpacity;
    private WindowManager.LayoutParams privacyShadeParams;
    private boolean isTouchingBottomLine = false;
    private LinearLayout seekbarHolderLayout;
    private int maximumDefaultBrightness = 80;
    private Matrix matrix = new Matrix();
    private boolean isTouchingTopLine = false;
    private BitmapDrawable bitmapDrawable;
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
                    counter += 1;
                    if ((REFRESH_RATE % counter) == 0) {
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

                        bottomLineView.post(new Runnable() {
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

                                resetBitmapColor();
                                privacyShadeCanvas.drawRect(0, topLineY + defaultRectangleBorderWidth / 2, screenWidth, bottomLineY + defaultRectangleBorderWidth / 2, transparentPaint);
                                bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
                                privacyShadeView.setBackground(bitmapDrawable);
                            }
                        });
                    }
                    counter = 0;
                    break;
                case MotionEvent.ACTION_UP:
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

                            resetBitmapColor();
                            privacyShadeCanvas.drawRect(0, topLineY + defaultRectangleBorderWidth / 2, screenWidth, bottomLineY + defaultRectangleBorderWidth / 2, transparentPaint);
                            bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
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
    private int defaultCircleRadius = dpToPx(150);
    private RelativeLayout circlePullView;
    private int numberOfFingers;
    private int x_cord_Destination;
    private int y_cord_Destination;
    View.OnTouchListener circleEyeTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            numberOfFingers = event.getPointerCount();
            WindowManager.LayoutParams circleViewParams = (WindowManager.LayoutParams) circleView.getLayoutParams();
            WindowManager.LayoutParams circlePullViewParams = (WindowManager.LayoutParams) circlePullView.getLayoutParams();

            int x_cord = (int) event.getRawX();
            int y_cord = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
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

                    windowManager.updateViewLayout(circlePullView, circlePullViewParams);
                    windowManager.updateViewLayout(circleView, circleViewParams);

                    circleView.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] circleLocation = new int[2];
                            circleView.getLocationOnScreen(circleLocation);
                            int x = circleLocation[0];
                            int y = circleLocation[1];

                            resetBitmapColor();
                            privacyShadeCanvas.drawCircle(x + (circleImageView.getWidth() / 2), y + (circleImageView.getWidth() / 2), circleImageView.getWidth() / 2 - dpToPx(5), transparentPaint);
                            bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
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
    private ImageButton toggleCircleButton;
    private ShapeType shapeType;
    private BroadcastReceiver screenLockReceiver;
    private boolean isResuming = false;
    private boolean isClosedBeforeLock = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        Bus bus = PrivacyScreenApplication.bus;
        bus.register(this);
        Logger.d("PrivacyShadeService.onCreate()");
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
    }

    private void saveCoordinates(ShapeType shapeType) {
        Logger.d("Screen is locking, saving coordinates!");
        switch (shapeType) {
            case RECTANGLE:
                topLineView.getLocationOnScreen(topLineLocation);
                topLineX = topLineLocation[0];
                topLineY = topLineLocation[1];
                bottomLineView.getLocationOnScreen(bottomLineLocation);
                bottomLineX = bottomLineLocation[0];
                bottomLineY = bottomLineLocation[1];
                preferences.edit().putInt("transparentRectangleTop", transparentRect.top).apply();
                preferences.edit().putInt("transparentRectangleBottom", transparentRect.bottom).apply();
                preferences.edit().putInt("topLineCoordinateX", topLineX).apply();
                preferences.edit().putInt("topLineCoordinateY", topLineY).apply();
                preferences.edit().putInt("bottomLineCoordinateX", bottomLineX).apply();
                preferences.edit().putInt("bottomLineCoordinateY", bottomLineY).apply();
                break;
            case CIRCLE:
                circleView.getLocationOnScreen(circleLocation);
                circlePullView.getLocationOnScreen(circlePullLocation);
                circleX = circleLocation[0];
                circleY = circleLocation[1];
                circlePullX = circlePullLocation[0];
                circlePullY = circlePullLocation[1];
                preferences.edit().putInt("circleX", circleX).apply();
                preferences.edit().putInt("circleY", circleY).apply();
                preferences.edit().putInt("circlePullX", circleX).apply();
                preferences.edit().putInt("circlePullY", circleY).apply();
                break;
        }
    }

    private void handleStart() {
        /*
        Initial configuration of everything
         */
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        defaultShadeColor = ContextCompat.getColor(getApplicationContext(), R.color.black);
        if (!preferences.contains("opacity")) {
            setDefaultOpacity(0.5f);
            preferences.edit().putFloat("opacity", 0.5f).apply();
        } else {
            setDefaultOpacity(preferences.getFloat(("opacity"), 0.5f));
        }

        createDefaultPrivacyShade();
        addPrivacyShade();
        addPrivacyShadeMenu();
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
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            collectAppUsageData();
        }*/
        setUpScreenLockReceiver();
    }

    private void createDefaultPrivacyShade() {

        int topLeft, bottomRight, defaultRectangleTop;

        if (isResuming) {
            topLeft = preferences.getInt("transparentRectangleTop", 330);
            bottomRight = preferences.getInt("transparentRectangleBottom", 630);
        } else {
            topLeft = (screenHeight / 2) - defaultRectangleHeight / 2;
            bottomRight = (screenHeight / 2) + defaultRectangleHeight / 2;
        }
        defaultRectangleTop = 0;
        transparentRect = new Rect(defaultRectangleTop, topLeft, screenWidth, bottomRight);
        transparentPaint = new Paint();
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAntiAlias(true);
        privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        privacyShadeBitmap.eraseColor(defaultShadeColor);
        bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, matrix, null);
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
        privacyShadeParams.alpha = getDefaultOpacity();
        windowManager.addView(privacyShadeView, privacyShadeParams);
    }

    private void addTransparentRectangle() {
        setShapeType(ShapeType.RECTANGLE);
        preferences.edit().putString("shape", "rectangle").apply();
        /*
        Punch a transparent rectangle into the privacy shade
         */
        privacyShadeCanvas.drawRect(transparentRect, transparentPaint);
        privacyShadeView.setBackground(bitmapDrawable);
        toggleCircleButton.setImageResource(R.drawable.ic_panorama_fish_eye_white_24dp);

        int top, left, bottom, right;

        top = transparentRect.top;
        left = transparentRect.left;
        bottom = transparentRect.bottom;
        right = transparentRect.right;

        /*Logger.d("Transparent Rectangle Top: " + top);
        Logger.d("Transparent Rectangle Left: " + left);
        Logger.d("Transparent Rectangle Bottom: " + bottom);
        Logger.d("Transparent Rectangle Right: " + right);*/

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
        //Logger.d("Status Bar height: " + getStatusBarHeight(getApplicationContext()));
        bottomLineParams.y = bottom - defaultRectangleBorderWidth / 2;
        windowManager.addView(bottomLineView, bottomLineParams);
        bottomLineView.setOnTouchListener(bottomLineTouchListener);

        /*bottomLineView.post(new Runnable() {
            @Override
            public void run() {
                int[] bottomLineCoordinates = new int[2];
                int bottomLineCoordinateX;
                int bottomLineCoordinateY;

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
        });*/

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
        windowManager.addView(topLineView, topLineParams);
        topLineView.setOnTouchListener(topLineTouchListener);

        /*topLineView.post(new Runnable() {
            @Override
            public void run() {
                //Logger.d("Line Padding top: " + topLineView.getY());
            }
        });*/
    }

    private void addPrivacyShadeMenu() {
        menuView = (LinearLayout) inflater.inflate(R.layout.layout_menu_privacy_shade, null);
        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.END;

        ImageButton removeShadeButton = (ImageButton) menuView.findViewById(R.id.button_close_privacy_screen);
        toggleCircleButton = (ImageButton) menuView.findViewById(R.id.toggle_circle_imageButton);
        ImageButton toggleRectangleButton = (ImageButton) menuView.findViewById(R.id.toggle_rectangle_imageButton);
        ImageButton toggleBrightnessSeekBarButton = (ImageButton) menuView.findViewById(R.id.toggle_brightness_imageButton);

        removeShadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedBeforeLock = true;
                stopSelf();
            }
        });


        toggleBrightnessSeekBarButton.setOnClickListener(new VisibleToggleClickListener() {
            @Override
            protected void changeVisibility(boolean visible) {
                Transition fadeTransition = new Fade();
                fadeTransition.setDuration(400);
                fadeTransition.setInterpolator(new FastOutSlowInInterpolator());
                //fadeTransition.setStartDelay(200);
                TransitionManager.beginDelayedTransition(brightnessSeekBarView, fadeTransition);
                seekbarHolderLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });

        toggleCircleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        toggleRectangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        setShapeType(ShapeType.CIRCLE);
        toggleCircleButton.setImageResource(R.drawable.ic_crop_5_4_white_24dp);
        preferences.edit().putString("shape", "circle").apply();
        circleView = (RelativeLayout) inflater.inflate(R.layout.layout_circle, null);
        WindowManager.LayoutParams circleViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
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
                /*Log.d(TAG, "Circle Height:" + circleImageView.getHeight());
                Log.d(TAG, "Circle Width:" + circleImageView.getWidth());
                Log.d(TAG, "Circle Height DP:" + pxToDp(circleImageView.getHeight()));
                Log.d(TAG, "Circle Width DP:" + pxToDp(circleImageView.getWidth()));*/
                /*
                Punch a transparent circle into the privacy shade
                */
                privacyShadeCanvas.drawCircle(screenWidth / 2, screenHeight / 2, circleImageView.getWidth() / 2 - dpToPx(5), transparentPaint);
                privacyShadeView.setBackground(bitmapDrawable);
                addCirclePullBar();
            }
        });
    }

    private void addCirclePullBar() {

        int[] circleLocation = new int[2];
        circleView.getLocationOnScreen(circleLocation);
        int x = circleLocation[0];
        int y = circleLocation[1];

        circlePullView = (RelativeLayout) inflater.inflate(R.layout.layout_pull_circle, null);
        WindowManager.LayoutParams circlePullViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        circlePullViewParams.gravity = Gravity.TOP | Gravity.START;
        circlePullViewParams.x = x - 72;
        circlePullViewParams.y = y - 72;

        /*circlePullView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Circle Parent Height:" + circlePullView.getHeight());
                Log.d(TAG, "Circle Parent Width:" + circlePullView.getWidth());
                Log.d(TAG, "Circle Parent Height DP:" + pxToDp(circlePullView.getHeight()));
                Log.d(TAG, "Circle Parent Width DP:" + pxToDp(circlePullView.getWidth()));
            }
        });*/


        ImageView circlePullImageView = (ImageView) circlePullView.findViewById(R.id.circle_pull_imageView);
        circlePullImageView.setOnTouchListener(circleEyeTouchListener);
        windowManager.addView(circlePullView, circlePullViewParams);
    }

    private void addOpacitySeekBar() {
        brightnessSeekBarView = (RelativeLayout) inflater.inflate(R.layout.layout_brightness_seekbar, null);
        seekbarHolderLayout = (LinearLayout) brightnessSeekBarView.findViewById(R.id.brightness_seekbar_holder);
        WindowManager.LayoutParams seekBarParams = new WindowManager.LayoutParams(
                screenWidth - dpToPx(56),
                dpToPx(56),
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        seekBarParams.gravity = Gravity.TOP | Gravity.START;
        seekBarParams.y = menuView.getBottom() - dpToPx(14);

        SeekBar brightnessSeekBar = (SeekBar) brightnessSeekBarView.findViewById(R.id.brightness_seekbar);
        brightnessSeekBar.setMax(maximumDefaultBrightness);

        Logger.d("Default opacity: " + getDefaultOpacity());
        int progressBar = 100 - Math.round(getDefaultOpacity() * 100);
        Logger.d("seekBar opacity: " + progressBar);

        brightnessSeekBar.setProgress(progressBar);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setDefaultOpacity((float) (100 - progress) / 100);
                float opacity = (float) (100 - progress) / 100;
                Logger.d("Opacity that is put in preferences: " + opacity);
                preferences.edit().putFloat("opacity", opacity).apply();
                //Logger.d("On Progress Changed: " + opacity);
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
    }

    @Subscribe
    public void hideScreen(OnScreenLockEvent onScreenLockEvent) {
        Logger.d("Screen Locked Otto");
        privacyShadeView.setVisibility(View.GONE);
        menuView.setVisibility(View.GONE);
        switch (getShapeType()) {
            case RECTANGLE:
                bottomLineView.setVisibility(View.GONE);
                topLineView.setVisibility(View.GONE);

                break;
            case CIRCLE:
                circleView.setVisibility(View.GONE);
                circlePullView.setVisibility(View.GONE);
                seekbarHolderLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Subscribe
    public void showScreen(OnScreenUnLockEvent onScreenUnLockEvent) {
        Logger.d("Screen Unlocked Otto");
        privacyShadeView.setVisibility(View.VISIBLE);
        menuView.setVisibility(View.VISIBLE);
        switch (getShapeType()) {
            case RECTANGLE:
                bottomLineView.setVisibility(View.VISIBLE);
                topLineView.setVisibility(View.VISIBLE);
                break;
            case CIRCLE:
                circleView.setVisibility(View.VISIBLE);
                circlePullView.setVisibility(View.VISIBLE);
                break;
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
        privacyShadeBitmap.eraseColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
    }

    private void setUpScreenLockReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_ANSWER);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        screenLockReceiver = new ScreenLockReceiver();
        registerReceiver(screenLockReceiver, filter);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("PrivacyShadeService.onStartCommand()");
        if (intent != null) {
            //Logger.d("Intent is not null");
            if (intent.getAction() != null) {
                //Logger.d("Intent action is not null");
                if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
                    isResuming = false;
                    handleStart();
                    startForegroundService();
                    if (!isRunning) {
                        ScheduledExecutorService backgroundService = Executors.newSingleThreadScheduledExecutor();
                        backgroundService.scheduleAtFixedRate(new TimerIncreasedRunnable(
                                this), 0, 1000, TimeUnit.MILLISECONDS);
                        isRunning = true;
                    }
                } else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
                    isRunning = false;
                    stopSelf();
                    ServiceBootstrap.stopAlwaysOnService(this);
                } else if (intent.getAction().equals(Constants.CALLRECEIVED_ACTION)) {
                    Logger.d("Call received in service");
                    privacyShadeParams.alpha = 0.2f;
                    windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
                } else if (intent.getAction().equals(Constants.CALLENDED_ACTION)) {
                    Logger.d("Call ended in service");
                    privacyShadeParams.alpha = getDefaultOpacity();
                    windowManager.updateViewLayout(privacyShadeView, privacyShadeParams);
                }
                /*else if (intent.getAction().equals(Constants.PAUSEFOREGROUND_ACTION)) {
                    Logger.d("Lockscreen activated, service shutting down");
                    isRunning = false;
                    saveCoordinates(getShapeType());
                    stopSelf();
                    ServiceBootstrap.stopAlwaysOnService(this);
                }else if (intent.getAction().equals(Constants.RESUMEFOREGROUND_ACTION)) {
                    Logger.d("Resuming from lockscreen");
                    isResuming = true;
                    handleStart();
                    startForegroundService();
                    if (!isRunning) {
                        ScheduledExecutorService backgroundService = Executors.newSingleThreadScheduledExecutor();
                        backgroundService.scheduleAtFixedRate(new TimerIncreasedRunnable(
                                this), 0, 1000, TimeUnit.MILLISECONDS);
                        isRunning = true;
                    }
                }*/
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

        if (circlePullView != null) {
            windowManager.removeView(circlePullView);
        }

        Logger.d("PrivacyShadeService.onStop, starting notification service");
        Intent startServiceIntent = new Intent(Constants.STARTFOREGROUND_ACTION);
        startServiceIntent.setClass(this, PersistentNotificationService.class);
        startServiceIntent.putExtra("isClosedBeforeLocking", isClosedBeforeLock);
        startServiceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(startServiceIntent);

        unregisterReceiver(screenLockReceiver);
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
