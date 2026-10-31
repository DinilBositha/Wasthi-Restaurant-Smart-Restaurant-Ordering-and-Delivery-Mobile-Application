package com.dinilbositha.wasthirestaurantadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurantadmin.databinding.ActivityLoginBinding;
import com.dinilbositha.wasthirestaurantadmin.utils.AppSecurityManager;
import com.dinilbositha.wasthirestaurantadmin.utils.BiometricAuthHelper;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private AppSecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, navigate straight to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        securityManager = AppSecurityManager.getInstance(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupBiometricUi();
        setupClicks();
        observeViewModel();

        // Auto prompt fingerprint if enabled and credentials saved
        if (securityManager.isQuickLoginAvailable(this)) {
            binding.getRoot().post(this::promptFingerprintLogin);
        }
    }

    private void setupBiometricUi() {
        if (!securityManager.isBiometricSupported(this)) {
            binding.layoutBiometricSection.setVisibility(View.GONE);
        } else {
            binding.layoutBiometricSection.setVisibility(View.VISIBLE);
        }
    }

    private void setupClicks() {
        binding.loginBtn.setOnClickListener(v -> loginAdmin());
        binding.btnBiometricLogin.setOnClickListener(v -> promptFingerprintLogin());
    }

    private void promptFingerprintLogin() {
        if (!securityManager.isBiometricSupported(this)) {
            Toast.makeText(this, "Biometric authentication is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!securityManager.isBiometricEnrolled(this)) {
            Toast.makeText(this, "No fingerprints enrolled. Please set up a fingerprint in Android Settings", Toast.LENGTH_LONG).show();
            return;
        }

        if (!securityManager.hasSavedCredentials()) {
            Toast.makeText(this, "Please log in with Email and Password once to enable Fingerprint login", Toast.LENGTH_LONG).show();
            return;
        }

        BiometricAuthHelper.showBiometricPrompt(
                this,
                "Admin Quick Login",
                "Scan your fingerprint to access the Admin Panel",
                "Use Password",
                new BiometricAuthHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        String email = securityManager.getSavedEmail();
                        String password = securityManager.getSavedPassword();

                        if (email != null && password != null) {
                            binding.signInEmail.setText(email);
                            binding.signInPassword.setText(password);
                            authViewModel.loginAdmin(email, password);
                        } else {
                            Toast.makeText(LoginActivity.this, "Saved credentials missing. Please login manually.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(LoginActivity.this, "Fingerprint not recognized. Try again.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, String errString) {
                        // User cancelled or error occurred
                    }

                    @Override
                    public void onUsePasscodeClicked() {
                        binding.signInPassword.requestFocus();
                    }
                }
        );
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, this::setLoading);

        authViewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                String email = binding.signInEmail.getText() != null
                        ? binding.signInEmail.getText().toString().trim()
                        : "";
                String password = binding.signInPassword.getText() != null
                        ? binding.signInPassword.getText().toString().trim()
                        : "";

                if (!email.isEmpty() && !password.isEmpty()) {
                    securityManager.saveUserCredentials(email, password);
                    securityManager.setBiometricEnabled(true);
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.layoutSignInPassword.setError(error);
                binding.signInPassword.requestFocus();
            }
        });
    }

    private void loginAdmin() {
        String email = binding.signInEmail.getText() != null
                ? binding.signInEmail.getText().toString().trim()
                : "";

        String password = binding.signInPassword.getText() != null
                ? binding.signInPassword.getText().toString().trim()
                : "";

        clearErrors();

        if (email.isEmpty()) {
            binding.layoutSignInEmail.setError("Email is required");
            binding.signInEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutSignInEmail.setError("Enter valid email address");
            binding.signInEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.layoutSignInPassword.setError("Password is required");
            binding.signInPassword.requestFocus();
            return;
        }

        authViewModel.loginAdmin(email, password);
    }

    private void setLoading(boolean isLoading) {
        binding.loginBtn.setEnabled(!isLoading);
        binding.btnBiometricLogin.setEnabled(!isLoading);
        binding.loginBtn.setAlpha(isLoading ? 0.5f : 1f);
        binding.loginBtn.setText(isLoading ? "Logging in..." : "Login");
    }

    private void clearErrors() {
        binding.layoutSignInEmail.setError(null);
        binding.layoutSignInPassword.setError(null);
    }
}