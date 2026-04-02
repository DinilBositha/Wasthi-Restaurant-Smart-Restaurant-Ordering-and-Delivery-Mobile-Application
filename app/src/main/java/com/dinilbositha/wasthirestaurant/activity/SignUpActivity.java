package com.dinilbositha.wasthirestaurant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurant.databinding.ActivitySignUpBinding;
import com.dinilbositha.wasthirestaurant.utils.ValidationUtils;
import com.dinilbositha.wasthirestaurant.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // user login wela innawanam direct MainActivity yanna
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupClicks();
        observeViewModel();
        setupPasswordWatcher();
    }

    private void setupClicks() {
        binding.signUpLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

        binding.signUpRegBtn.setOnClickListener(v -> registerUser());
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, this::setLoading);

        authViewModel.getRegisterSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.layoutSignUpPassword.setError(error);
                binding.signUpPassword.requestFocus();
            }
        });
    }

    private void setupPasswordWatcher() {
        binding.signUpPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                if (ValidationUtils.isValidatePassword(password)) {
                    binding.layoutSignUpPassword.setError(null);
                    binding.layoutSignUpPassword.setErrorEnabled(false);
                } else {
                    binding.layoutSignUpPassword.setErrorEnabled(true);
                    binding.layoutSignUpPassword.setError("Password must be at least 8 characters with Capital, Simple and Numbers");
                    binding.layoutSignUpPassword.setErrorIconDrawable(null);
                }
            }
        });
    }

    private void registerUser() {
        String name = binding.signUpName.getText().toString().trim();
        String email = binding.signUpEmail.getText().toString().trim();
        String password = binding.signUpPassword.getText().toString().trim();
        String retypePassword = binding.signUpReTypePassword.getText().toString().trim();

        clearErrors();

        if (name.isEmpty()) {
            binding.layoutSignUpName.setError("Please enter your name");
            binding.signUpName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            binding.layoutSignUpEmail.setError("Email is required");
            binding.signUpEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutSignUpEmail.setError("Enter valid Email Address");
            binding.signUpEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.layoutSignUpPassword.setError("Password is required");
            binding.signUpPassword.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidatePassword(password)) {
            binding.layoutSignUpPassword.setError("Password must be at least 8 characters with Capital, Simple and Numbers");
            binding.signUpPassword.requestFocus();
            return;
        }

        if (!retypePassword.equals(password)) {
            binding.layoutSignUpRTypePassword.setError("Password and retype password must be the same");
            binding.signUpReTypePassword.requestFocus();
            return;
        }

        authViewModel.registerUser(name, email, password);
    }

    private void setLoading(boolean isLoading) {
        binding.signUpRegBtn.setEnabled(!isLoading);
        binding.signUpRegBtn.setAlpha(isLoading ? 0.5f : 1f);
    }

    private void clearErrors() {
        binding.layoutSignUpName.setError(null);
        binding.layoutSignUpEmail.setError(null);
        binding.layoutSignUpPassword.setError(null);
        binding.layoutSignUpRTypePassword.setError(null);
    }
}