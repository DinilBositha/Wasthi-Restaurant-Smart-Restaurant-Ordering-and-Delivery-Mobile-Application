package com.dinilbositha.wasthirestaurant.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ActivitySignInBinding;
import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.utils.AppSecurityManager;
import com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper;
import com.dinilbositha.wasthirestaurant.utils.NotificationHelper;
import com.dinilbositha.wasthirestaurant.viewmodel.AuthViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.CartViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private AppSecurityManager securityManager;

    private static final String CHANNEL_ID = "login_channel_id";
    private static final String CHANNEL_NAME = "Login Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for login success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        securityManager = AppSecurityManager.getInstance(this);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        cartViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(CartViewModel.class);

        createNotificationChannel();
        setupClicks();
        setupQuickLogin();
        observeViewModel();
        setupClickResetPassword();
        observeResetPassword();
    }

    private void setupQuickLogin() {
        updateQuickLoginVisibility();

        binding.btnQuickLogin.setOnClickListener(v -> startQuickAuth());

        // Pre-fill email if saved
        if (securityManager.hasSavedCredentials() && binding.signInEmail.getText().toString().isEmpty()) {
            binding.signInEmail.setText(securityManager.getSavedEmail());
        }
    }

    private void updateQuickLoginVisibility() {
        boolean hasSaved = securityManager.hasSavedCredentials();
        boolean hasBio = securityManager.isBiometricEnabled() || BiometricAuthHelper.canAuthenticateWithBiometrics(this);
        boolean hasPin = securityManager.isPasscodeSet();

        if (hasSaved && (hasBio || hasPin)) {
            binding.layoutQuickLogin.setVisibility(android.view.View.VISIBLE);
            if (securityManager.isBiometricEnabled()) {
                binding.btnQuickLogin.setIconResource(R.drawable.ic_fingerprint);
                binding.btnQuickLogin.setText("Unlock with Fingerprint");
            } else if (securityManager.isPasscodeSet()) {
                binding.btnQuickLogin.setIconResource(R.drawable.ic_passcode);
                binding.btnQuickLogin.setText("Unlock with 4-Digit PIN");
            } else {
                binding.btnQuickLogin.setIconResource(R.drawable.ic_fingerprint);
                binding.btnQuickLogin.setText("Quick Login with Biometrics");
            }
        } else {
            binding.layoutQuickLogin.setVisibility(android.view.View.GONE);
        }
    }

    private void startQuickAuth() {
        if (!securityManager.hasSavedCredentials()) {
            android.widget.Toast.makeText(this, "Please sign in with Email & Password first to enable Quick Login", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        if (BiometricAuthHelper.canAuthenticateWithBiometrics(this)) {
            BiometricAuthHelper.showBiometricPrompt(
                    this,
                    "Wasthi Restaurant Quick Login",
                    "Scan your fingerprint or use passcode to sign in",
                    securityManager.isPasscodeSet() ? "Use PIN" : "Cancel",
                    new BiometricAuthHelper.BiometricCallback() {
                        @Override
                        public void onAuthenticationSucceeded() {
                            performAutoLogin();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            android.widget.Toast.makeText(SignInActivity.this, "Fingerprint not recognized", android.widget.Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, String errString) {
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
            android.widget.Toast.makeText(this, "Please configure Biometric or Passcode in Settings", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showPasscodeDialog() {
        com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog dialog =
                com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.newInstance(
                        com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.MODE_LOGIN,
                        new com.dinilbositha.wasthirestaurant.dialog.PasscodeBottomSheetDialog.PasscodeListener() {
                            @Override
                            public void onPasscodeSuccess() {
                                performAutoLogin();
                            }

                            @Override
                            public void onPasscodeCancelled() {
                            }
                        }
                );
        dialog.show(getSupportFragmentManager(), "PasscodeDialog");
    }

    private void performAutoLogin() {
        String email = securityManager.getSavedEmail();
        String password = securityManager.getSavedPassword();

        if (email != null && password != null) {
            binding.signInEmail.setText(email);
            binding.signInPassword.setText(password);
            authViewModel.loginUser(email, password);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQuickLoginVisibility();
    }

    private void setupClicks() {
        binding.signInSignUpBtn.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });

        binding.loginBtn.setOnClickListener(v -> loginUser());
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, this::setLoading);

        authViewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {

                List<CartItem> cartItems = cartViewModel.getCartItems().getValue();

                if (cartItems != null && !cartItems.isEmpty()) {
                    cartViewModel.syncLocalCartToFirestore();
                } else {
                    cartViewModel.loadCartFromFirestore();
                }

                String email = binding.signInEmail.getText().toString().trim();
                String password = binding.signInPassword.getText().toString().trim();
                if (!email.isEmpty() && !password.isEmpty()) {
                    securityManager.saveUserCredentials(email, password);
                    if (BiometricAuthHelper.canAuthenticateWithBiometrics(this) && !securityManager.isBiometricEnabled()) {
                        securityManager.setBiometricEnabled(true);
                    }
                }

                saveFcmToken();
                showLoginNotification();
                authViewModel.clearLoginSuccess();

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.layoutSignInPassword.setError(error);
                binding.signInPassword.requestFocus();
                authViewModel.clearErrorMessage();
            }
        });
    }

    private void loginUser() {
        String email = binding.signInEmail.getText().toString().trim();
        String password = binding.signInPassword.getText().toString().trim();

        clearErrors();
        authViewModel.clearErrorMessage();
        authViewModel.clearLoginSuccess();

        if (email.isEmpty()) {
            binding.layoutSignInEmail.setError("Email address is required");
            binding.signInEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutSignInEmail.setError("Email address is not valid");
            binding.signInEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.layoutSignInPassword.setError("Password is required");
            binding.signInPassword.requestFocus();
            return;
        }

        authViewModel.loginUser(email, password);
    }

    private void setLoading(boolean isLoading) {
        binding.loginBtn.setEnabled(!isLoading);
        binding.loginBtn.setAlpha(isLoading ? 0.5f : 1f);
    }

    private void clearErrors() {
        binding.layoutSignInEmail.setError(null);
        binding.layoutSignInPassword.setError(null);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void saveFcmToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("fcmToken", token);
                        }
                    });
        }
    }

    private void setupClickResetPassword() {
        binding.ForgotPasswordBtn.setOnClickListener(v -> {
            String email = binding.signInEmail.getText().toString().trim();

            binding.layoutSignInEmail.setError(null);

            if (email.isEmpty()) {
                binding.layoutSignInEmail.setError("Email address is required");
                binding.signInEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.layoutSignInEmail.setError("Email address is not valid");
                binding.signInEmail.requestFocus();
            } else {
                authViewModel.resetPassword(email);
            }
        });
    }

    private void observeResetPassword() {
        authViewModel.getResetPasswordSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {

                Intent intent = new Intent(this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                NotificationHelper.showNotification(
                        this,
                        "reset_password_channel",
                        "Reset Password Notifications",
                        "Notifications for password reset",
                        2001,
                        "Password Reset",
                        "If an account exists for this email, a password reset link has been sent.",
                        intent
                );

                authViewModel.clearResetPasswordSuccess();
            }
        });
    }

    private void showLoginNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Login Successful")
                .setContentText("Welcome back to Wasthi Restaurant!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
    }
}