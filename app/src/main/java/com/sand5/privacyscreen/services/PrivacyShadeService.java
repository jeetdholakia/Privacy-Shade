package com.sand5.privacyscreen.services;


import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.utils.MyDialog;
import com.sand5.privacyscreen.utils.Utils;

public class PrivacyShadeService extends Service {
    private final String TAG = "PrivacyShadeService";
    int screenHeight, screenWidth;
    Handler myHandler = new Handler();
    private WindowManager windowManager;
    private RelativeLayout privacyShadeView, seekBarView;
    private LinearLayout menuView;
    private LinearLayout txtView, txt_linearlayout;
    Runnable myRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            }
        }
    };
    private ImageButton removeShadeButton;
    private ImageButton toggleCircleButton;
    private ImageButton toggleRectangleButton;
    private ImageButton toggleOpacityButton;
    private TextView txt1;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private boolean isLeft = true;
    private String sMsg = "";
    private Button stopPrivacyScreen;
    private LayoutInflater inflater;
    private DisplayMetrics displayMetrics;
    private RelativeLayout circleView, rectangleView;
    private ImageView circleImageView, rectangleImageView;
    private Canvas privacyShadeCanvas;
    private Bitmap privacyShadeBitmap;
    private Paint transparentPaint;
    private SeekBar opacitySeekBar;
    private int[] rectangleLocationCoordinates = new int[2];
    private int rectangleX;
    private int rectangleY;
    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix = new Matrix();

    public static float pxToDp(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().getDisplayMetrics());
    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d(Utils.LogTag, "ChatHeadService.onCreate()");

    }

    private void handleStart() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        //addTransparencyCircle();
        addTransparencyRectangle();
        addPrivacyShadeMenu();

    }

    private void addPrivacyShade() {
        Log.d(TAG, "Add Privacy Shade");
        privacyShadeView = (RelativeLayout) inflater.inflate(R.layout.layout_privacy_screen, null);
        privacyShadeView.setBackgroundColor(Color.BLACK);
        privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        privacyShadeBitmap.eraseColor(getResources().getColor(R.color.black90));
        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
        transparentPaint = new Paint();
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAntiAlias(true);
        //privacyShadeCanvas.drar(x + 150, y + 150, circleImageView.getWidth()/2, transparentPaint);
        Log.d(TAG, "Privacy shade rectangle dimensions:" + "\n" + "Rectangle X: " + rectangleX + "\n" + "Rectangle Y: " + rectangleY);
        Log.d(TAG, "Privacy shade rectangle dimensions:" + "\n" + "Rectangle Height: " + rectangleView.getHeight() + "\n" + "Rectangle Width: " + rectangleView.getWidth());
        privacyShadeCanvas.drawRect(0, rectangleY, 1080, rectangleY + rectangleView.getHeight(), transparentPaint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
        privacyShadeView.setBackground(bitmapDrawable);
        szWindow.set(screenWidth, screenHeight);

		/*
        Underlying touches are supported by TYPE_SYSTEM_OVERLAY and not by TYPE_PHONE
		 */
        WindowManager.LayoutParams privacyShadeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        privacyShadeParams.gravity = Gravity.TOP | Gravity.START;
        privacyShadeParams.x = 0;
        privacyShadeParams.y = 0;
        windowManager.addView(privacyShadeView, privacyShadeParams);


        /*privacyShadeView.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
			boolean isLongclick = false, inBounded = false;
			int remove_img_width = 0, remove_img_height = 0;

			Handler handler_longClick = new Handler();
			Runnable runnable_longClick = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Log.d(Utils.LogTag, "Into runnable_longClick");

					isLongclick = true;
					menuView.setVisibility(View.VISIBLE);
					chathead_longclick();
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();

				int x_cord = (int) event.getRawX();
				int y_cord = (int) event.getRawY();
				int x_cord_Destination, y_cord_Destination;

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						time_start = System.currentTimeMillis();
						handler_longClick.postDelayed(runnable_longClick, 600);

						remove_img_width = removeImg.getLayoutParams().screenWidth;
						remove_img_height = removeImg.getLayoutParams().screenHeight;

						x_init_cord = x_cord;
						y_init_cord = y_cord;

						x_init_margin = layoutParams.x;
						y_init_margin = layoutParams.y;

						if(txtView != null){
							txtView.setVisibility(View.GONE);
							myHandler.removeCallbacks(myRunnable);
						}
						break;
					case MotionEvent.ACTION_MOVE:
						int x_diff_move = x_cord - x_init_cord;
						int y_diff_move = y_cord - y_init_cord;

						x_cord_Destination = x_init_margin + x_diff_move;
						y_cord_Destination = y_init_margin + y_diff_move;

						if(isLongclick){
							int x_bound_left = szWindow.x / 2 - (int)(remove_img_width * 1.5);
							int x_bound_right = szWindow.x / 2 +  (int)(remove_img_width * 1.5);
							int y_bound_top = szWindow.y - (int)(remove_img_height * 1.5);

							if((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top){
								inBounded = true;

								int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
								int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() ));

								if(removeImg.getLayoutParams().screenHeight == remove_img_height){
									removeImg.getLayoutParams().screenHeight = (int) (remove_img_height * 1.5);
									removeImg.getLayoutParams().screenWidth = (int) (remove_img_width * 1.5);

									WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) menuView.getLayoutParams();
									param_remove.x = x_cord_remove;
									param_remove.y = y_cord_remove;

									windowManager.updateViewLayout(menuView, param_remove);
								}

								layoutParams.x = x_cord_remove + (Math.abs(menuView.getWidth() - privacyShadeView.getWidth())) / 2;
								layoutParams.y = y_cord_remove + (Math.abs(menuView.getHeight() - privacyShadeView.getHeight())) / 2 ;

								windowManager.updateViewLayout(privacyShadeView, layoutParams);
								break;
							}else{
								inBounded = false;
								removeImg.getLayoutParams().screenHeight = remove_img_height;
								removeImg.getLayoutParams().screenWidth = remove_img_width;

								WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) menuView.getLayoutParams();
								int x_cord_remove = (szWindow.x - menuView.getWidth()) / 2;
								int y_cord_remove = szWindow.y - (menuView.getHeight() + getStatusBarHeight() );

								param_remove.x = x_cord_remove;
								param_remove.y = y_cord_remove;

								windowManager.updateViewLayout(menuView, param_remove);
							}

						}


						layoutParams.x = x_cord_Destination;
						layoutParams.y = y_cord_Destination;

						windowManager.updateViewLayout(privacyShadeView, layoutParams);
						break;
					case MotionEvent.ACTION_UP:
						isLongclick = false;
						menuView.setVisibility(View.GONE);
						removeImg.getLayoutParams().screenHeight = remove_img_height;
						removeImg.getLayoutParams().screenWidth = remove_img_width;
						handler_longClick.removeCallbacks(runnable_longClick);

						if(inBounded){
							if(MyDialog.active){
								MyDialog.myDialog.finish();
							}

							stopService(new Intent(ChatHeadService.this, ChatHeadService.class));
							inBounded = false;
							break;
						}


						int x_diff = x_cord - x_init_cord;
						int y_diff = y_cord - y_init_cord;

						if(Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5){
							time_end = System.currentTimeMillis();
							if((time_end - time_start) < 300){
								chathead_click();
							}
						}

						y_cord_Destination = y_init_margin + y_diff;

						int BarHeight =  getStatusBarHeight();
						if (y_cord_Destination < 0) {
							y_cord_Destination = 0;
						} else if (y_cord_Destination + (privacyShadeView.getHeight() + BarHeight) > szWindow.y) {
							y_cord_Destination = szWindow.y - (privacyShadeView.getHeight() + BarHeight );
						}
						layoutParams.y = y_cord_Destination;

						inBounded = false;
						resetPosition(x_cord);

						break;
					default:
						Log.d(Utils.LogTag, "privacyShadeView.setOnTouchListener  -> event.getAction() : default");
						break;
				}
				return true;
			}
		});*/
    }

    private void addOpacitySeekBar() {
        seekBarView = (RelativeLayout) inflater.inflate(R.layout.layout_brightness_seekbar, null);
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        opacitySeekBar = (SeekBar) seekBarView.findViewById(R.id.brightness_seekbar);
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        windowManager.addView(seekBarView, paramRemove);
        seekBarView.setVisibility(View.GONE);
    }

    private void showOpacitySeekBar() {

    }

    private void hideOpacitySeekBar() {

    }

    private void addPrivacyShadeMenu() {
        menuView = (LinearLayout) inflater.inflate(R.layout.layout_remove_privacy_shade, null);
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.TOP | Gravity.END;

        removeShadeButton = (ImageButton) menuView.findViewById(R.id.button_close_privacy_screen);
        toggleCircleButton = (ImageButton) menuView.findViewById(R.id.toggle_circle_imageButton);
        toggleRectangleButton = (ImageButton) menuView.findViewById(R.id.toggle_rectangle_imageButton);
        toggleOpacityButton = (ImageButton) menuView.findViewById(R.id.toggle_brightness_imageButton);

        removeShadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        windowManager.addView(menuView, paramRemove);
    }

    private void addTransparencyRectangle() {
        rectangleView = (RelativeLayout) inflater.inflate(R.layout.layout_rectangle, null);
        WindowManager.LayoutParams paramRectangle = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        paramRectangle.gravity = Gravity.START | Gravity.CENTER_VERTICAL;


        rectangleImageView = (ImageView) rectangleView.findViewById(R.id.transparent_rectangle_imageView);
        windowManager.addView(rectangleView, paramRectangle);

        rectangleView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Calculating dimensions of rectangle");
                rectangleView.getLocationOnScreen(rectangleLocationCoordinates);
                rectangleX = rectangleLocationCoordinates[0];
                rectangleY = rectangleLocationCoordinates[1];
                Log.d(TAG, "Default Location on screen X: " + rectangleX + "\t Y: " + rectangleY);
                Log.d(TAG, "Rectangle left: " + rectangleView.getLeft());
                Log.d(TAG, "Rectangle right: " + rectangleView.getRight());
                Log.d(TAG, "Rectangle top:" + rectangleView.getTop());
                Log.d(TAG, "Rectangle bottom: " + rectangleView.getBottom());
                Log.d(TAG, "Rectangle  getX(): " + rectangleView.getX());
                Log.d(TAG, "Rectangle  getY(): " + rectangleView.getY());
                Log.d(TAG, "Rectangle Parent Height:" + rectangleView.getHeight());
                Log.d(TAG, "Rectangle Parent Width:" + rectangleView.getWidth());

                addPrivacyShade();
            }
        });

        rectangleView.setOnTouchListener(new View.OnTouchListener() {
            int numberOfFingers;
            long time_start = 0, time_end = 0;
            boolean isLongclick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(Utils.LogTag, "Into runnable_longClick");

                    isLongclick = true;
                    menuView.setVisibility(View.VISIBLE);
                    //chathead_longclick();
                }
            };
            int x_cord_Destination, y_cord_Destination;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                numberOfFingers = event.getPointerCount();
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) rectangleView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();
                        //handler_longClick.postDelayed(runnable_longClick, 600);

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        /*if(isLongclick){
                            int x_bound_left = szWindow.x / 2 - (int)(remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 +  (int)(remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int)(remove_img_height * 1.5);

                            if((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top){
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() ));


                                layoutParams.x = x_cord_remove + (Math.abs(menuView.getWidth() - circleView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(menuView.getHeight() - circleView.getHeight())) / 2 ;

                                windowManager.updateViewLayout(circleView, layoutParams);
                                break;
                            }else{
                                inBounded = false;

                                WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) menuView.getLayoutParams();
                                int x_cord_remove = (szWindow.x - menuView.getWidth()) / 2;
                                int y_cord_remove = szWindow.y - (menuView.getHeight() + getStatusBarHeight() );

                                param_remove.x = x_cord_remove;
                                param_remove.y = y_cord_remove;

                                windowManager.updateViewLayout(menuView, param_remove);
                            }

                        }*/


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(rectangleView, layoutParams);

                        rectangleView.post(new Runnable() {
                            @Override
                            public void run() {
                                int[] location = new int[2];
                                Log.d(TAG, "On Rectangle Moved X: " + rectangleImageView.getX());
                                Log.d(TAG, "On Rectangle Moved Y: " + rectangleImageView.getY());
                                rectangleView.getLocationOnScreen(location);
                                int x = location[0];
                                int y = location[1];
                                Log.d(TAG, "Location on screen X: " + x + "\t Y: " + y);
                                privacyShadeView.setBackgroundColor(Color.BLACK);
                                privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                                privacyShadeBitmap.eraseColor(getResources().getColor(R.color.black90));
                                privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                                privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
                                transparentPaint = new Paint();
                                transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                                transparentPaint.setColor(Color.TRANSPARENT);
                                transparentPaint.setAntiAlias(true);
                                //privacyShadeCanvas.drar(x + 150, y + 150, circleImageView.getWidth()/2, transparentPaint);
                                privacyShadeCanvas.drawRect(0, y, 1080, y + rectangleView.getHeight(), transparentPaint);
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
                                privacyShadeView.setBackground(bitmapDrawable);
                            }
                        });

                        break;
                    case MotionEvent.ACTION_UP:

                        // handler_longClick.removeCallbacks(runnable_longClick);

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();
                            if ((time_end - time_start) < 300) {
                                // chathead_click();
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (rectangleView.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (rectangleView.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;

                        inBounded = false;
                        resetPosition(x_cord);

                        break;
                    default:
                        Log.d(Utils.LogTag, "privacyShadeView.setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }
        });
    }

    private void addTransparencyCircle() {
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


        circleView.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
            boolean isLongclick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(Utils.LogTag, "Into runnable_longClick");

                    isLongclick = true;
                    menuView.setVisibility(View.VISIBLE);
                    //chathead_longclick();
                }
            };
            int x_cord_Destination, y_cord_Destination;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) circleView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();
                        //handler_longClick.postDelayed(runnable_longClick, 600);

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        /*if(isLongclick){
                            int x_bound_left = szWindow.x / 2 - (int)(remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 +  (int)(remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int)(remove_img_height * 1.5);

                            if((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top){
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() ));


                                layoutParams.x = x_cord_remove + (Math.abs(menuView.getWidth() - circleView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(menuView.getHeight() - circleView.getHeight())) / 2 ;

                                windowManager.updateViewLayout(circleView, layoutParams);
                                break;
                            }else{
                                inBounded = false;

                                WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) menuView.getLayoutParams();
                                int x_cord_remove = (szWindow.x - menuView.getWidth()) / 2;
                                int y_cord_remove = szWindow.y - (menuView.getHeight() + getStatusBarHeight() );

                                param_remove.x = x_cord_remove;
                                param_remove.y = y_cord_remove;

                                windowManager.updateViewLayout(menuView, param_remove);
                            }

                        }*/


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;


                        windowManager.updateViewLayout(circleView, layoutParams);
                        int[] location = new int[2];
                        Log.d(TAG, "On Circle Moved X: " + circleImageView.getX());
                        Log.d(TAG, "On Circle Moved Y: " + circleImageView.getY());
                        circleView.getLocationOnScreen(location);
                        int x = location[0];
                        int y = location[1];
                        Log.d(TAG, "Location on screen X: " + x + "\t Y: " + y);
                        privacyShadeView.setBackgroundColor(Color.BLACK);
                        privacyShadeBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                        privacyShadeBitmap.eraseColor(getResources().getColor(R.color.black90));
                        privacyShadeCanvas = new Canvas(privacyShadeBitmap);
                        privacyShadeCanvas.drawBitmap(privacyShadeBitmap, new Matrix(), null);
                        transparentPaint = new Paint();
                        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                        transparentPaint.setColor(Color.TRANSPARENT);
                        transparentPaint.setAntiAlias(true);
                        privacyShadeCanvas.drawCircle(x + 150, y + 150, circleImageView.getWidth() / 2, transparentPaint);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(privacyShadeBitmap);
                        privacyShadeView.setBackground(bitmapDrawable);
                        break;
                    case MotionEvent.ACTION_UP:

                        // handler_longClick.removeCallbacks(runnable_longClick);

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();
                            if ((time_end - time_start) < 300) {
                                // chathead_click();
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (circleView.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (circleView.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;

                        inBounded = false;
                        resetPosition(x_cord);

                        break;
                    default:
                        Log.d(Utils.LogTag, "privacyShadeView.setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }
        });
    }

    private void drawCanvasCircle(Canvas canvas, Bitmap bitmap, int x, int y, int radius) {


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            int w = windowManager.getDefaultDisplay().getWidth();
            int h = windowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(Utils.LogTag, "ChatHeadService.onConfigurationChanged -> landscape");

            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            }

            if (layoutParams.y + (privacyShadeView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (privacyShadeView.getHeight() + getStatusBarHeight());
                windowManager.updateViewLayout(privacyShadeView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(Utils.LogTag, "ChatHeadService.onConfigurationChanged -> portrait");

            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            }

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);

        } else {
            isLeft = false;
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
        double value = scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
        return value;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    private void chathead_click() {
        if (MyDialog.active) {
            MyDialog.myDialog.finish();
        } else {
            Intent it = new Intent(this, MyDialog.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(it);
        }

    }

    private void chathead_longclick() {
        Log.d(Utils.LogTag, "Into ChatHeadService.chathead_longclick() ");

        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) menuView.getLayoutParams();
        int x_cord_remove = (szWindow.x - menuView.getWidth()) / 2;
        int y_cord_remove = szWindow.y - (menuView.getHeight() + getStatusBarHeight());

        param_remove.x = x_cord_remove;
        param_remove.y = y_cord_remove;

        windowManager.updateViewLayout(menuView, param_remove);
    }

    private void showMsg(String sMsg) {
        if (txtView != null && privacyShadeView != null) {
            Log.d(Utils.LogTag, "ChatHeadService.showMsg -> sMsg=" + sMsg);
            txt1.setText(sMsg);
            myHandler.removeCallbacks(myRunnable);

            WindowManager.LayoutParams param_chathead = (WindowManager.LayoutParams) privacyShadeView.getLayoutParams();
            WindowManager.LayoutParams param_txt = (WindowManager.LayoutParams) txtView.getLayoutParams();

            txt_linearlayout.getLayoutParams().height = privacyShadeView.getHeight();
            txt_linearlayout.getLayoutParams().width = szWindow.x / 2;

			/*if(isLeft){
				param_txt.x = param_chathead.x + chatheadImg.getWidth();
				param_txt.y = param_chathead.y;

				txt_linearlayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			}else{
				param_txt.x = param_chathead.x - szWindow.x / 2;
				param_txt.y = param_chathead.y;

				txt_linearlayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			}*/

            txtView.setVisibility(View.VISIBLE);
            windowManager.updateViewLayout(txtView, param_txt);

            myHandler.postDelayed(myRunnable, 4000);

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d(Utils.LogTag, "ChatHeadService.onStartCommand() -> startId=" + startId);

        if (intent != null) {
            Bundle bd = intent.getExtras();

            if (bd != null)
                sMsg = bd.getString(Utils.EXTRA_MSG);

            if (sMsg != null && sMsg.length() > 0) {
                if (startId == Service.START_STICKY) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            showMsg(sMsg);
                        }
                    }, 300);

                } else {
                    showMsg(sMsg);
                }

            }

        }

        if (startId == Service.START_STICKY) {
            handleStart();
            return super.onStartCommand(intent, flags, startId);
        } else {
            return Service.START_NOT_STICKY;
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (privacyShadeView != null) {
            windowManager.removeView(privacyShadeView);
        }

		/*if(txtView != null){
			windowManager.menuView(txtView);
		}*/

        if (menuView != null) {
            windowManager.removeView(menuView);
        }

        if (circleView != null) {
            windowManager.removeView(circleView);
        }

        if (rectangleView != null) {
            windowManager.removeView(rectangleView);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(Utils.LogTag, "ChatHeadService.onBind()");
        return null;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);
            rectangleImageView.setImageMatrix(matrix);
            return true;
        }
    }


}
