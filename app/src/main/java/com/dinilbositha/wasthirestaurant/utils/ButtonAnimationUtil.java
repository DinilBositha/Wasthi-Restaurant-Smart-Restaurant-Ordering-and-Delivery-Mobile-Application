package com.dinilbositha.wasthirestaurant.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.dinilbositha.wasthirestaurant.R;

public class ButtonAnimationUtil {

    public interface OnAnimationEnd {
        void onEnd();
    }

    public static void animateClick(View view, Context context, OnAnimationEnd onEnd) {
        Animation scaleDown = AnimationUtils.loadAnimation(context, R.anim.btn_scale_down);
        Animation scaleUp = AnimationUtils.loadAnimation(context, R.anim.btn_scale_up);

        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleUp);
                if (onEnd != null) {
                    onEnd.onEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.startAnimation(scaleDown);
    }

    public static void animateClick(View view, Context context) {
        animateClick(view, context, null);
    }
}