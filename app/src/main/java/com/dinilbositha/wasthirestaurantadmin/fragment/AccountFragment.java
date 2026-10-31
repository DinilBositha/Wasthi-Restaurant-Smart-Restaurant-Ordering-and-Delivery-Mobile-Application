package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.activity.LoginActivity;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentAccountBinding;
import com.dinilbositha.wasthirestaurantadmin.utils.AppSecurityManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AppSecurityManager securityManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        securityManager = AppSecurityManager.getInstance(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadAdminProfile();
        setupBiometricToggle();
        setupLogout();

        binding.cardExportPdfReport.setOnClickListener(v -> {
            com.dinilbositha.wasthirestaurantadmin.dialog.ExportReportBottomSheetDialog
                    .newInstance()
                    .show(getParentFragmentManager(), "ExportReportDialog");
        });
    }

    private void loadAdminProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        if (currentUser.getEmail() != null) {
            binding.txtAdminEmail.setText(currentUser.getEmail());
        }

        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            binding.txtAdminName.setText(currentUser.getDisplayName());
        }

        // Fetch user document from Firestore
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || binding == null) return;
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String profileUrl = documentSnapshot.getString("profileUrl");

                        if (name != null && !name.isEmpty()) {
                            binding.txtAdminName.setText(name);
                        }
                        if (email != null && !email.isEmpty()) {
                            binding.txtAdminEmail.setText(email);
                        }
                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.profile)
                                    .into(binding.imgAdminAvatar);
                        }
                    }
                });
    }

    private void setupBiometricToggle() {
        boolean supported = securityManager.isBiometricSupported(requireContext());

        if (!supported) {
            binding.switchFingerprint.setEnabled(false);
            binding.switchFingerprint.setChecked(false);
            binding.txtFingerprintStatus.setText("Biometric sensor not supported on this device");
        } else {
            binding.switchFingerprint.setChecked(securityManager.isBiometricEnabled());
            binding.switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
                securityManager.setBiometricEnabled(isChecked);
                Toast.makeText(
                        requireContext(),
                        isChecked ? "Fingerprint quick login enabled" : "Fingerprint login disabled",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out from Wasthi Admin?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        firebaseAuth.signOut();
                        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
