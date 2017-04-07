package com.sand5.privacyscreen.events;

import android.view.View;

/**
 * Created by jeetdholakia on 4/3/17.
 */

public class DimensionChangeEvent {

    private float x;
    private float y;
    private View view;

    public DimensionChangeEvent(View view, float x, float y) {
        this.x = x;
        this.y = y;
        this.view = view;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
