package com.dinilbositha.wasthirestaurantadmin.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ActivityMainBinding;
import com.dinilbositha.wasthirestaurantadmin.fragment.CategoryFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.HomeFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.OrdersFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.QrAdminFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_orders) {
                loadFragment(new OrdersFragment());
                return true;
            } else if (itemId == R.id.nav_QR) {

                loadFragment(new QrAdminFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}