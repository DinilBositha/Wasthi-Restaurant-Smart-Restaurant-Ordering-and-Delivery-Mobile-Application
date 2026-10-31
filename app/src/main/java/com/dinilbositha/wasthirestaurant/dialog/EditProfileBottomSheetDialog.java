package com.dinilbositha.wasthirestaurant.dialog;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.BottomSheetEditProfileBinding;
import com.dinilbositha.wasthirestaurant.utils.CloudinaryHelper;
import com.dinilbositha.wasthirestaurant.utils.ValidationUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfileBottomSheetDialog extends BottomSheetDialogFragment {

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newName, String newPhone, String newProfileUrl);
    }

    private BottomSheetEditProfileBinding binding;
    private OnProfileUpdatedListener listener;

    private String currentName = "";
    private String currentPhone = "";
    private String currentProfileUrl = "";
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<String> imagePickerLauncher;

    public static EditProfileBottomSheetDialog newInstance(
            String currentName,
            String currentPhone,
            String currentProfileUrl,
            OnProfileUpdatedListener listener
    ) {
        EditProfileBottomSheetDialog dialog = new EditProfileBottomSheetDialog();
        dialog.currentName = currentName != null ? currentName : "";
        dialog.currentPhone = currentPhone != null ? currentPhone : "";
        dialog.currentProfileUrl = currentProfileUrl != null ? currentProfileUrl : "";
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        if (binding != null) {
                            binding.imgEditProfile.setPadding(0, 0, 0, 0);
                            Glide.with(this)
                                    .load(uri)
                                    .circleCrop()
                                    .into(binding.imgEditProfile);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.editFullName.setText(currentName);
        if (!"Not available".equalsIgnoreCase(currentPhone)) {
            binding.editPhone.setText(currentPhone);
        }

        if (!TextUtils.isEmpty(currentProfileUrl)) {
            binding.imgEditProfile.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(currentProfileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(binding.imgEditProfile);
        }

        binding.imgEditProfile.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        binding.btnCancelEdit.setOnClickListener(v -> dismiss());
    }

    private void saveProfileChanges() {
        String newName = binding.editFullName.getText() != null ? binding.editFullName.getText().toString().trim() : "";
        String newPhone = binding.editPhone.getText() != null ? binding.editPhone.getText().toString().trim() : "";

        binding.layoutEditName.setError(null);
        binding.layoutEditPhone.setError(null);

        if (TextUtils.isEmpty(newName)) {
            binding.layoutEditName.setError("Full name is required");
            binding.editFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPhone)) {
            binding.layoutEditPhone.setError("Mobile number is required");
            binding.editPhone.requestFocus();
            return;
        }

        if (!Boolean.TRUE.equals(ValidationUtils.isValidateMobile(newPhone))) {
            binding.layoutEditPhone.setError("Please enter a valid mobile number (e.g. 07XXXXXXXX)");
            binding.editPhone.requestFocus();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        setLoading(true);

        if (selectedImageUri != null) {
            // First upload image to Cloudinary
            CloudinaryHelper.uploadImage(requireContext(), selectedImageUri, "profile_images", new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    if (!isAdded()) return;
                    updateFirestoreProfile(firebaseUser, newName, newPhone, imageUrl);
                }

                @Override
                public void onError(String errorMessage) {
                    if (!isAdded()) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(long bytes, long totalBytes) {
                }
            });
        } else {
            // Update without changing image
            updateFirestoreProfile(firebaseUser, newName, newPhone, currentProfileUrl);
        }
    }

    private void updateFirestoreProfile(FirebaseUser user, String newName, String newPhone, String imageUrl) {
        String uid = user.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);
        if (!TextUtils.isEmpty(imageUrl)) {
            updates.put("profileUrl", imageUrl);
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;

                    // Update Firebase Auth Display Name & Photo
                    UserProfileChangeRequest.Builder reqBuilder = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName);
                    if (!TextUtils.isEmpty(imageUrl)) {
                        reqBuilder.setPhotoUri(Uri.parse(imageUrl));
                    }
                    user.updateProfile(reqBuilder.build());

                    setLoading(false);
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onProfileUpdated(newName, newPhone, imageUrl);
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        binding.btnSaveProfile.setEnabled(!isLoading);
        binding.btnCancelEdit.setEnabled(!isLoading);
        binding.btnSaveProfile.setText(isLoading ? "Saving Changes..." : "Save Changes");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
