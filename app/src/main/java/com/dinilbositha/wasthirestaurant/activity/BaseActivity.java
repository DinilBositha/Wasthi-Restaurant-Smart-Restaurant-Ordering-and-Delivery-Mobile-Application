package com.dinilbositha.wasthirestaurant.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.utils.NetworkUtil;

public abstract class BaseActivity extends AppCompatActivity {

    private View noInternetView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupNoInternetLayout() {
        ViewGroup rootView = findViewById(android.R.id.content);

        noInternetView = LayoutInflater.from(this)
                .inflate(R.layout.network_status_view, rootView, false);

        rootView.addView(noInternetView);

        Button retryButton = noInternetView.findViewById(R.id.btnRetryConnection);
        retryButton.setOnClickListener(v -> {
            if (NetworkUtil.isNetworkAvailable(this)) {
                hideNoInternetLayout();
                onConnectionRestored();
            } else {
                showNoInternetLayout();
            }
        });

        if (!NetworkUtil.isNetworkAvailable(this)) {
            showNoInternetLayout();
        }
    }

    public void showNoInternetLayout() {
        if (noInternetView != null) {
            noInternetView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideNoInternetLayout() {
        if (noInternetView != null) {
            noInternetView.setVisibility(View.GONE);
        }
    }

    protected boolean hasInternet() {
        return NetworkUtil.isNetworkAvailable(this);
    }

    protected void onConnectionRestored() {
        // override if needed
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasInternet()) {
            showNoInternetLayout();
        } else {
            hideNoInternetLayout();
        }
    }
}