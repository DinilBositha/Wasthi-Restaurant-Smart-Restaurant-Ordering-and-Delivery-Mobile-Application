package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.databinding.FragmentProfileBinding;
import com.dinilbositha.wasthirestaurant.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }

        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        loadUserData();

        binding.btnEditProfile.setOnClickListener(v -> {
            // TODO: Open EditProfileFragment
        });
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            setGuestData();
            return;
        }

        String uid = firebaseUser.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        if (user != null) {
                            String name = TextUtils.isEmpty(user.getName()) ? "User" : user.getName();
                            String email = TextUtils.isEmpty(user.getEmail()) ? "Not available" : user.getEmail();
                            if (!TextUtils.isEmpty(user.getProfileUrl())) {
                                Glide.with(this)
                                        .load(user.getProfileUrl())
                                        .placeholder(R.drawable.profile)
                                        .error(R.drawable.profile)
                                        .into(binding.imgProfile);
                            }
                            binding.txtUserName.setText(name);
                            binding.txtUserEmail.setText(email);
                            binding.txtFullName.setText(name);
                            binding.txtEmail.setText(email);
                            binding.txtPhone.setText("Not available");
                        } else {
                            setFallbackFirebaseAuthData(firebaseUser);
                        }
                    } else {
                        setFallbackFirebaseAuthData(firebaseUser);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    setFallbackFirebaseAuthData(firebaseUser);
                });
    }
    private void setGuestData() {
        binding.txtUserName.setText("Guest User");
        binding.txtUserEmail.setText("Not signed in");
        binding.txtFullName.setText("Guest User");
        binding.txtEmail.setText("Not signed in");
        binding.txtPhone.setText("Not available");
    }

    private void setFallbackFirebaseAuthData(FirebaseUser user) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        String phone = user.getPhoneNumber();

        if (TextUtils.isEmpty(name)) {
            name = "User";
        }

        if (TextUtils.isEmpty(email)) {
            email = "Not available";
        }

        if (TextUtils.isEmpty(phone)) {
            phone = "Not available";
        }

        binding.txtUserName.setText(name);
        binding.txtUserEmail.setText(email);
        binding.txtFullName.setText(name);
        binding.txtEmail.setText(email);
        binding.txtPhone.setText(phone);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMainNavigationUi();
        }

        binding = null;
    }
}
