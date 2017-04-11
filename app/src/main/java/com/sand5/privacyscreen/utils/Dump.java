package com.sand5.privacyscreen.utils;

public class Dump {

    /*
      Dumping grounds for code that might be useful in future
     */

    /*
    @Subscribe
    public void rectangleDimensionsChanged(DimensionChangeEvent event) {
        ImageView imageView = (ImageView) event.getView();
        Logger.d("New Rectangle Top:" + imageView.getTop());
        Logger.d("New Rectangle Bottom:" + imageView.getBottom());
        Logger.d("New Rectangle Left:" + imageView.getLeft());
        Logger.d("New Rectangle Right:" + imageView.getRight());
        Logger.d("New rectangle X and Y:" + event.getX() + "\t" + event.getY());
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) rectangleImageView.getLayoutParams();
        layoutParams.x = (int) event.getX();
        layoutParams.y = (int) event.getY();
        windowManager.updateViewLayout(rectangleView, layoutParams);
    }

    public static float makeDistance(float x1, float y1, float x2, float y2) {
        float delta1 = (x2 - x1) * (x2 - x1);
        float delta2 = (y2 - y1) * (y2 - y1);
        float distance = (float) Math.sqrt(delta1 + delta2);
        return distance;
    }


     private double bounceValue(long step, long scale) {
        double value = scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
        return value;
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

            if (layoutParams.y + (privacyShadeView.getHeight() + getStatusBarHeight(getApplicationContext())) > szWindow.y) {
                layoutParams.y = szWindow.y - (privacyShadeView.getHeight() + getStatusBarHeight(getApplicationContext()));
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

    //IMPORTANT TOUCH LISTENER CODE

    privacyShadeView.setOnTouchListener(new View.OnTouchListener() {
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
		});

		private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float newX;
        private float newY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            Logger.d("On Scale Begin");
            newX = detector.getFocusX();
            newY = detector.getFocusY();

            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Logger.d("On Scale");
            if (detector.getCurrentSpan() > detector.getPreviousSpan() + 1.5) {
                onZoom(detector.getCurrentSpan());
            } else if (detector.getPreviousSpan() > detector.getCurrentSpan() + 1.5) {
                onPinch(detector.getCurrentSpan());
                if (rectangleView.getScaleX() < 1) {
                    rectangleView.setScaleX(1);
                    rectangleView.setScaleY(1);
                    rectangleView.setTranslationX(1);
                    rectangleView.setTranslationY(1);
                }

            }

            return super.onScale(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            Logger.d("On Scale End");
        }

        private void onZoom(float span) {
            Logger.d("On Zoom");

            if (scaleGestureDetector.isInProgress()) {
                rectangleView.setScaleX(rectangleView.getScaleX() + 0.001f * getResources().getDisplayMetrics().densityDpi / getResources().getDisplayMetrics().density);
                rectangleView.setScaleY(rectangleView.getScaleY() + 0.001f * getResources().getDisplayMetrics().densityDpi / getResources().getDisplayMetrics().density);
                rectangleView.setTranslationX(rectangleView.getTranslationX() + (rectangleView.getPivotX() - newX) * (1 - rectangleView.getScaleX()));
                rectangleView.setTranslationY(rectangleView.getTranslationY() + (rectangleView.getPivotY() - newY) * (1 - rectangleView.getScaleY()));
                rectangleView.setPivotX(newX);
                rectangleView.setPivotY(newY);

            } else {
                rectangleView.setScaleX(rectangleView.getScaleX());
                rectangleView.setScaleY(rectangleView.getScaleY());
                rectangleView.setTranslationX(rectangleView.getTranslationX());
                rectangleView.setTranslationY(rectangleView.getTranslationY());
            }

        }

        private void onPinch(float span) {
            Logger.d("On Pinch");

            rectangleView.setScaleX(rectangleView.getScaleX() - 0.001f * getResources().getDisplayMetrics().densityDpi / getResources().getDisplayMetrics().density);
            rectangleView.setScaleY(rectangleView.getScaleY() - 0.001f * getResources().getDisplayMetrics().densityDpi / getResources().getDisplayMetrics().density);
            rectangleView.setTranslationX(rectangleView.getTranslationX() + (rectangleView.getPivotX() - newX) * (1 - rectangleView.getScaleX()));
            rectangleView.setTranslationY(rectangleView.getTranslationY() + (rectangleView.getPivotY() - newY) * (1 - rectangleView.getScaleY()));
            rectangleView.setPivotX(newX);
            rectangleView.setPivotY(newY);

        }
    }

    void saveCoordinates(ShapeType shapeType) {
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

    */

}
