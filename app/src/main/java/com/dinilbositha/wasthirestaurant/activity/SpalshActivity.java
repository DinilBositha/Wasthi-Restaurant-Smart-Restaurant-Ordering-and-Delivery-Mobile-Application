package com.dinilbositha.wasthirestaurant.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;

public class SpalshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
           getWindow().setDecorFitsSystemWindows(false);
           WindowInsetsController controller = getWindow().getInsetsController();
           if(controller != null){
               controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
           }

       }else{
           getWindow().setFlags(
                   WindowManager.LayoutParams.FLAG_FULLSCREEN,
                   WindowManager.LayoutParams.FLAG_FULLSCREEN
           );
       }
        setContentView(R.layout.activity_spalsh);
        ImageView imageView = findViewById(R.id.spalshLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.app_logo)
                .override(300)
                .into(imageView);
        new Handler().postDelayed(this::proceedFromSplash, 2500);
    }

    private void proceedFromSplash() {
        android.content.SharedPreferences preferences = getSharedPreferences("wasthi_prefs", MODE_PRIVATE);
        boolean isIntroSeen = preferences.getBoolean("intro_seen", false);

        if (!isIntroSeen) {
            startActivity(new Intent(SpalshActivity.this, IntroActivity.class));
            finish();
            return;
        }

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        com.dinilbositha.wasthirestaurant.utils.AppSecurityManager securityManager =
                com.dinilbositha.wasthirestaurant.utils.AppSecurityManager.getInstance(this);

        if (user != null) {
            if (securityManager.isAppLockEnabled() && (securityManager.isBiometricEnabled() || securityManager.isPasscodeSet())) {
                authenticateAppLock();
            } else {
                startActivity(new Intent(SpalshActivity.this, MainActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(SpalshActivity.this, SignInActivity.class));
            finish();
        }
    }

    private void authenticateAppLock() {
        com.dinilbositha.wasthirestaurant.utils.AppSecurityManager securityManager =
                com.dinilbositha.wasthirestaurant.utils.AppSecurityManager.getInstance(this);

        if (securityManager.isBiometricEnabled() && com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper.canAuthenticateWithBiometrics(this)) {
            com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper.showBiometricPrompt(
                    this,
                    "Wasthi Restaurant Locked",
                    "Unlock using your fingerprint or PIN",
                    securityManager.isPasscodeSet() ? "Use PIN" : "Cancel",
                    new com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper.BiometricCallback() {
                        @Override
                        public void onAuthenticationSucceeded() {
                            startActivity(new Intent(SpalshActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, String errString) {
                            if (securityManager.isPasscodeSet()) {
                                showPasscodeDialog();
                            }
                        }

                        @Override
                        public void onUsePasscodeClicked() {
                            if (securityManager.isPasscodeSet()) {
                                showPasscodeDialog();
                            }
                        }
                    }
            );
        } else if (securityManager.isPasscodeSet()) {
            showPasscodeDialog();
        } else {
            startActivity(new Intent(SpalshActivity.this, MainActivity.class));
            finish();
        }
    }

    private void showPasscodeDialog() {
        com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog dialog =
                com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.newInstance(
                        com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.MODE_LOGIN,
                        new com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.PasscodeListener() {
                            @Override
                            public void onPasscodeSuccess() {
                                startActivity(new Intent(SpalshActivity.this, MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onPasscodeCancelled() {
                                finish();
                            }
                        }
                );
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "AppLockPasscodeDialog");
    }
}