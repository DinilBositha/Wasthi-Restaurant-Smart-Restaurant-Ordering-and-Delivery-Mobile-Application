package com.dinilbositha.wasthirestaurant.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.BottomSheetSecuritySettingsBinding;
import com.dinilbositha.wasthirestaurant.utils.AppSecurityManager;
import com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SecuritySettingsBottomSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetSecuritySettingsBinding binding;
    private AppSecurityManager securityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetSecuritySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = AppSecurityManager.getInstance(requireContext());

        updateUiState();
        setupListeners();
    }

    private void updateUiState() {
        binding.switchBiometric.setChecked(securityManager.isBiometricEnabled());
        binding.switchAppLock.setChecked(securityManager.isAppLockEnabled());

        if (securityManager.isPasscodeSet()) {
            binding.txtPasscodeStatus.setText("Passcode is configured");
            binding.btnSetupPasscode.setText("Change");
        } else {
            binding.txtPasscodeStatus.setText("Not configured");
            binding.btnSetupPasscode.setText("Set PIN");
        }
    }

    private void setupListeners() {
        // Biometric Switch
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!BiometricAuthHelper.canAuthenticateWithBiometrics(requireContext())) {
                    Toast.makeText(requireContext(), "Fingerprint hardware not available or no fingerprints enrolled in device settings", Toast.LENGTH_LONG).show();
                    binding.switchBiometric.setChecked(false);
                    return;
                }
                securityManager.setBiometricEnabled(true);
                Toast.makeText(requireContext(), "Fingerprint Login enabled", Toast.LENGTH_SHORT).show();
            } else {
                securityManager.setBiometricEnabled(false);
                Toast.makeText(requireContext(), "Fingerprint Login disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup / Change Passcode
        binding.btnSetupPasscode.setOnClickListener(v -> {
            PasscodeBottomSheetDialog passcodeDialog = PasscodeBottomSheetDialog.newInstance(
                    PasscodeBottomSheetDialog.MODE_SET_PASSCODE,
                    new PasscodeBottomSheetDialog.PasscodeListener() {
                        @Override
                        public void onPasscodeSuccess() {
                            updateUiState();
                        }

                        @Override
                        public void onPasscodeCancelled() {
                        }
                    }
            );
            passcodeDialog.show(getParentFragmentManager(), "SetPasscodeDialog");
        });

        // App Launch Lock Switch
        binding.switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !securityManager.isPasscodeSet() && !securityManager.isBiometricEnabled()) {
                Toast.makeText(requireContext(), "Please set up a Passcode or enable Fingerprint first", Toast.LENGTH_SHORT).show();
                binding.switchAppLock.setChecked(false);
                return;
            }
            securityManager.setAppLockEnabled(isChecked);
        });

        binding.btnCloseSecurity.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
