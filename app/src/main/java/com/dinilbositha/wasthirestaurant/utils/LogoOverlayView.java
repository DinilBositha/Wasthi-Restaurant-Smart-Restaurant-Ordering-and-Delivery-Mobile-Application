package com.dinilbositha.wasthirestaurant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.LayoutLogoOverlayBinding;
import com.dinilbositha.wasthirestaurant.utils.Resource;

public class LogoOverlayView extends FrameLayout {

    private LayoutLogoOverlayBinding binding;
    private boolean isAnimating = false;

    public LogoOverlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LogoOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LogoOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = LayoutLogoOverlayBinding.inflate(LayoutInflater.from(context), this, true);
        setVisibility(View.GONE);
    }

    public <T> void bindResource(Resource<T> resource) {
        if (resource == null) return;

        if (resource.isLoading()) {
            showLogo();
        } else {
            hideLogo(null);
        }
    }

    public <T> void bindResource(Resource<T> resource, @Nullable Runnable onFinished) {
        if (resource == null) return;

        if (resource.isLoading()) {
            showLogo();
        } else {
            hideLogo(onFinished);
        }
    }

    public void showLogo() {
        if (isAnimating || getVisibility() == View.VISIBLE) return;

        isAnimating = true;
        setAlpha(1f);
        setVisibility(View.VISIBLE);
        bringToFront();

        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        binding.imgLogo.startAnimation(fadeIn);
    }

    public void hideLogo(@Nullable Runnable onFinished) {
        if (getVisibility() == View.GONE) {
            if (onFinished != null) onFinished.run();
            return;
        }

        isAnimating = true;

        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.imgLogo.clearAnimation();
                setVisibility(View.GONE);
                isAnimating = false;

                if (onFinished != null) {
                    onFinished.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        binding.imgLogo.startAnimation(fadeOut);
    }
}