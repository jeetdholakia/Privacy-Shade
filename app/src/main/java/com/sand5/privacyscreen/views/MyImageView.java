package com.sand5.privacyscreen.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jeetdholakia on 5/3/17.
 */

public class MyImageView extends ImageView {

    private Paint mPaint;
    private Bitmap mBitmap;
    private Paint mTransparentPaint;
    private Rect mTransparentRectangle;

    public MyImageView(Context context) {
        this(context, null, 0);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //transparentRect = new Rect(defaultRectangleTop, topLeft, screenWidth, bottomRight);
    }
}
