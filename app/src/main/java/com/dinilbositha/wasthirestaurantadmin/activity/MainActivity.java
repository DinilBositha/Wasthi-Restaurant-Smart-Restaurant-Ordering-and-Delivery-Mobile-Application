package com.dinilbositha.wasthirestaurantadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ActivityMainBinding;
import com.dinilbositha.wasthirestaurantadmin.fragment.AccountFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.HomeFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.OrdersFragment;
import com.dinilbositha.wasthirestaurantadmin.fragment.QrAdminFragment;
import com.dinilbositha.wasthirestaurantadmin.utils.AppSecurityManager;
import com.dinilbositha.wasthirestaurantadmin.utils.BiometricAuthHelper;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private AppSecurityManager securityManager;
    private static boolean isAuthenticatedThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        securityManager = AppSecurityManager.getInstance(this);

        // Check if Admin is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            isAuthenticatedThisSession = false;
            redirectToLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        setupBottomNavigation();
        setupLockOverlayClicks();
        checkAndPromptBiometricLock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseAuth.getCurrentUser() == null) {
            isAuthenticatedThisSession = false;
            redirectToLogin();
            return;
        }

        if (!isAuthenticatedThisSession && isBiometricLockRequired()) {
            showLockOverlay();
        }
    }

    private boolean isBiometricLockRequired() {
        return securityManager.isBiometricSupported(this)
                && securityManager.isBiometricEnrolled(this)
                && securityManager.isBiometricEnabled();
    }

    private void checkAndPromptBiometricLock() {
        if (!isAuthenticatedThisSession && isBiometricLockRequired()) {
            showLockOverlay();
            binding.getRoot().post(this::promptFingerprintUnlock);
        } else {
            unlockDashboard();
        }
    }

    private void showLockOverlay() {
        binding.layoutLockOverlay.setVisibility(View.VISIBLE);
        binding.layoutMainContent.setVisibility(View.GONE);
    }

    private void unlockDashboard() {
        isAuthenticatedThisSession = true;
        binding.layoutLockOverlay.setVisibility(View.GONE);
        binding.layoutMainContent.setVisibility(View.VISIBLE);
    }

    private void promptFingerprintUnlock() {
        BiometricAuthHelper.showBiometricPrompt(
                this,
                "Admin Authentication",
                "Scan fingerprint to unlock Wasthi Admin",
                "Logout",
                new BiometricAuthHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        unlockDashboard();
                        Toast.makeText(MainActivity.this, "Unlocked successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(MainActivity.this, "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, String errString) {
                        // Keep lock overlay visible
                    }

                    @Override
                    public void onUsePasscodeClicked() {
                        // Negative button was "Logout"
                        firebaseAuth.signOut();
                        isAuthenticatedThisSession = false;
                        redirectToLogin();
                    }
                }
        );
    }

    private void setupLockOverlayClicks() {
        binding.btnTouchFingerprint.setOnClickListener(v -> promptFingerprintUnlock());

        binding.btnLogoutFromLock.setOnClickListener(v -> {
            firebaseAuth.signOut();
            isAuthenticatedThisSession = false;
            redirectToLogin();
        });
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
            } else if (itemId == R.id.nav_account) {
                loadFragment(new AccountFragment());
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

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}