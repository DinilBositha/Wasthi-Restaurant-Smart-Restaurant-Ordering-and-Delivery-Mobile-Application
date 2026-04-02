package com.dinilbositha.wasthirestaurantadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.dinilbositha.wasthirestaurantadmin.databinding.ActivityLoginBinding;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupClicks();
        observeViewModel();
    }

    private void setupClicks() {
        binding.loginBtn.setOnClickListener(v -> loginAdmin());


    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, this::setLoading);

        authViewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
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
        binding.loginBtn.setAlpha(isLoading ? 0.5f : 1f);
        binding.loginBtn.setText(isLoading ? "Logging in..." : "Login");
    }

    private void clearErrors() {
        binding.layoutSignInEmail.setError(null);
        binding.layoutSignInPassword.setError(null);
    }
}