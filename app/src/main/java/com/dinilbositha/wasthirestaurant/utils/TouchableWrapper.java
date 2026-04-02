package com.dinilbositha.wasthirestaurant.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TouchableWrapper extends FrameLayout {

    private boolean touchEnabled = true;

    public TouchableWrapper(@NonNull Context context) {
        super(context);
    }

    public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // If touch is disabled, intercept and consume the event
        return !touchEnabled;
    }

    public void setTouchEnabled(boolean enabled) {
        this.touchEnabled = enabled;
    }
}