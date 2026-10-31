package com.dinilbositha.wasthirestaurant.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.BottomSheetPasscodeBinding;
import com.dinilbositha.wasthirestaurant.utils.AppSecurityManager;
import com.dinilbositha.wasthirestaurant.utils.BiometricAuthHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PasscodeBottomSheetDialog extends BottomSheetDialogFragment {

    public static final int MODE_LOGIN = 1;
    public static final int MODE_SET_PASSCODE = 2;
    public static final int MODE_VERIFY_ONLY = 3;

    public interface PasscodeListener {
        void onPasscodeSuccess();
        void onPasscodeCancelled();
    }

    private BottomSheetPasscodeBinding binding;
    private int mode = MODE_LOGIN;
    private PasscodeListener listener;

    private final StringBuilder enteredPin = new StringBuilder();
    private String firstEnteredPin = null; // For set mode confirmation
    private AppSecurityManager securityManager;

    public static PasscodeBottomSheetDialog newInstance(int mode, PasscodeListener listener) {
        PasscodeBottomSheetDialog dialog = new PasscodeBottomSheetDialog();
        dialog.mode = mode;
        dialog.listener = listener;
        return dialog;
    }

    public void setPasscodeListener(PasscodeListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPasscodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        securityManager = AppSecurityManager.getInstance(requireContext());

        setupUiForMode();
        setupKeypad();

        // If biometric is enabled and in login/verify mode, show biometric button
        boolean canBio = BiometricAuthHelper.canAuthenticateWithBiometrics(requireContext()) && securityManager.isBiometricEnabled();
        binding.btnBiometricKey.setVisibility(canBio ? View.VISIBLE : View.INVISIBLE);

        binding.btnBiometricKey.setOnClickListener(v -> triggerBiometric());

        binding.btnUsePassword.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPasscodeCancelled();
            }
            dismiss();
        });
    }

    private void setupUiForMode() {
        switch (mode) {
            case MODE_SET_PASSCODE:
                binding.txtPasscodeTitle.setText("Set 4-Digit Passcode");
                binding.txtPasscodeSubtitle.setText("Create a 4-digit PIN for quick access");
                binding.btnBiometricKey.setVisibility(View.INVISIBLE);
                binding.btnUsePassword.setText("Cancel");
                break;
            case MODE_VERIFY_ONLY:
                binding.txtPasscodeTitle.setText("Verify Passcode");
                binding.txtPasscodeSubtitle.setText("Enter your existing 4-digit PIN");
                binding.btnUsePassword.setText("Cancel");
                break;
            case MODE_LOGIN:
            default:
                binding.txtPasscodeTitle.setText("Quick Login");
                binding.txtPasscodeSubtitle.setText("Enter your 4-digit PIN or use Fingerprint");
                binding.btnUsePassword.setText("Use Email & Password");
                break;
        }
    }

    private void setupKeypad() {
        View.OnClickListener numClickListener = v -> {
            if (enteredPin.length() < 4) {
                String digit = ((android.widget.TextView) v).getText().toString();
                enteredPin.append(digit);
                updateDots();

                if (enteredPin.length() == 4) {
                    handlePinComplete();
                }
            }
        };

        binding.key0.setOnClickListener(numClickListener);
        binding.key1.setOnClickListener(numClickListener);
        binding.key2.setOnClickListener(numClickListener);
        binding.key3.setOnClickListener(numClickListener);
        binding.key4.setOnClickListener(numClickListener);
        binding.key5.setOnClickListener(numClickListener);
        binding.key6.setOnClickListener(numClickListener);
        binding.key7.setOnClickListener(numClickListener);
        binding.key8.setOnClickListener(numClickListener);
        binding.key9.setOnClickListener(numClickListener);

        binding.btnDeleteKey.setOnClickListener(v -> {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updateDots();
                hideError();
            }
        });
    }

    private void updateDots() {
        ImageView[] dots = {binding.dot1, binding.dot2, binding.dot3, binding.dot4};
        for (int i = 0; i < 4; i++) {
            if (i < enteredPin.length()) {
                dots[i].setImageResource(R.drawable.pin_dot_filled);
            } else {
                dots[i].setImageResource(R.drawable.pin_dot_empty);
            }
        }
    }

    private void handlePinComplete() {
        String pin = enteredPin.toString();

        if (mode == MODE_SET_PASSCODE) {
            if (firstEnteredPin == null) {
                // First step complete -> prompt to confirm
                firstEnteredPin = pin;
                enteredPin.setLength(0);
                updateDots();
                binding.txtPasscodeTitle.setText("Confirm Passcode");
                binding.txtPasscodeSubtitle.setText("Re-enter your 4-digit PIN to confirm");
            } else {
                // Confirm step
                if (firstEnteredPin.equals(pin)) {
                    securityManager.setPasscode(pin);
                    Toast.makeText(requireContext(), "Passcode set successfully!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onPasscodeSuccess();
                    }
                    dismiss();
                } else {
                    showError("Passcodes do not match. Try again.");
                    firstEnteredPin = null;
                    enteredPin.setLength(0);
                    updateDots();
                    binding.txtPasscodeTitle.setText("Set 4-Digit Passcode");
                    binding.txtPasscodeSubtitle.setText("Create a 4-digit PIN for quick access");
                }
            }
        } else {
            // Login or Verify mode
            if (securityManager.verifyPasscode(pin)) {
                if (listener != null) {
                    listener.onPasscodeSuccess();
                }
                dismiss();
            } else {
                showError("Incorrect PIN. Please try again.");
                shakeDots();
                enteredPin.setLength(0);
                updateDots();
            }
        }
    }

    private void triggerBiometric() {
        if (!isAdded()) return;
        BiometricAuthHelper.showBiometricPrompt(
                requireActivity(),
                "Fingerprint Authentication",
                "Verify your identity to proceed",
                "Use PIN",
                new BiometricAuthHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        if (listener != null) {
                            listener.onPasscodeSuccess();
                        }
                        dismiss();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        showError("Fingerprint not recognized. Try again or enter PIN.");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, String errString) {
                        // User cancelled or pressed negative button
                    }

                    @Override
                    public void onUsePasscodeClicked() {
                        // Keep keypad open for PIN entry
                    }
                }
        );
    }

    private void showError(String msg) {
        binding.txtPinError.setText(msg);
        binding.txtPinError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.txtPinError.setVisibility(View.INVISIBLE);
    }

    private void shakeDots() {
        try {
            Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
            binding.layoutPinDots.startAnimation(shake);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
