package com.dinilbositha.wasthirestaurant.utils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricAuthHelper {

    public interface BiometricCallback {
        void onAuthenticationSucceeded();
        void onAuthenticationFailed();
        void onAuthenticationError(int errorCode, String errString);
        void onUsePasscodeClicked();
    }

    public static boolean canAuthenticateWithBiometrics(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG;
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static boolean canAuthenticateWithDeviceCredentials(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void showBiometricPrompt(
            @NonNull FragmentActivity activity,
            @NonNull String title,
            @NonNull String subtitle,
            @NonNull String negativeButtonText,
            @NonNull BiometricCallback callback
    ) {
        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    callback.onUsePasscodeClicked();
                } else {
                    callback.onAuthenticationError(errorCode, errString.toString());
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSucceeded();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle);

        if (negativeButtonText != null && !negativeButtonText.isEmpty()) {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText);
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        } else {
            // Allow device PIN / Pattern / Password if supported
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptInfoBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                );
            } else {
                promptInfoBuilder.setNegativeButtonText("Use Passcode");
            }
        }

        biometricPrompt.authenticate(promptInfoBuilder.build());
    }
}
