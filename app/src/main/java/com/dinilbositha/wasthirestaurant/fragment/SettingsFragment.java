package com.dinilbositha.wasthirestaurant.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.SignInActivity;
import com.dinilbositha.wasthirestaurant.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUserInfo();
        setupMenuTitlesAndIcons();
        setupClicks();
        updateLoginLogoutUi();
    }

    private boolean isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            binding.txtUserName.setText("Guest User");
            binding.txtUserEmail.setText("Tap to sign in");
            return;
        }

        String name = user.getDisplayName();
        String email = user.getEmail();

        if (TextUtils.isEmpty(name)) {
            name = "User";
        }

        if (TextUtils.isEmpty(email)) {
            email = "No email available";
        }

        binding.txtUserName.setText(name);
        binding.txtUserEmail.setText(email);
    }

    private void updateLoginLogoutUi() {
        TextView logoutText = binding.layoutLogout.findViewById(R.id.txtLogout);

        if (isUserLoggedIn()) {
            if (logoutText != null) {
                logoutText.setText("Logout");
            }
        } else {
            if (logoutText != null) {
                logoutText.setText("Login");
            }
        }
    }

    private void setupMenuTitlesAndIcons() {
        setMenuItem(binding.getRoot().findViewById(R.id.itemProfile), "Profile", R.drawable.profile);
        setMenuItem(binding.getRoot().findViewById(R.id.itemOrders), "My Orders", R.drawable.cart);
        setMenuItem(binding.getRoot().findViewById(R.id.itemAddresses), "Saved Addresses", R.drawable.location);
        setMenuItem(binding.getRoot().findViewById(R.id.itemSettings), "Settings", R.drawable.setting);
        setMenuItem(binding.getRoot().findViewById(R.id.itemHelp), "Help Center", R.drawable.help);
        setMenuItem(binding.getRoot().findViewById(R.id.itemAbout), "About Us", R.drawable.info);
    }

    private void setMenuItem(View itemView, String title, int iconRes) {
        if (itemView == null) return;

        TextView txtTitle = itemView.findViewById(R.id.menuTitle);
        ImageView imgIcon = itemView.findViewById(R.id.menuIconImg);

        if (txtTitle != null) {
            txtTitle.setText(title);
        }

        if (imgIcon != null) {
            imgIcon.setImageResource(iconRes);
        }
    }

    private void setupClicks() {
        binding.layoutProfileCard.setOnClickListener(v -> {
            if (requireLogin()) return;
            openFragment(new ProfileFragment());
        });

        binding.getRoot().findViewById(R.id.itemProfile)
                .setOnClickListener(v -> {
                    if (requireLogin()) return;
                    openFragment(new ProfileFragment());
                });

        binding.getRoot().findViewById(R.id.itemOrders)
                .setOnClickListener(v -> {
                    if (requireLogin()) return;
                    openFragment(new OrderFragment());
                });



        binding.getRoot().findViewById(R.id.itemAddresses)
                .setOnClickListener(v -> {
                    if (requireLogin()) return;
                    openFragment(new SavedAddressesFragment());
                });

        binding.getRoot().findViewById(R.id.itemSettings)
                .setOnClickListener(v -> {
                    Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
                });

        binding.getRoot().findViewById(R.id.itemHelp)
                .setOnClickListener(v -> {
                    openFragment(new HelpCenterFragment());
                });

        binding.getRoot().findViewById(R.id.itemAbout)
                .setOnClickListener(v -> {
                    openFragment(new AboutUsFragment());
                });

        binding.layoutLogout.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(requireContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(requireContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean requireLogin() {
        if (!isUserLoggedIn()) {
            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), SignInActivity.class));
            return true;
        }
        return false;
    }

    private void openFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragemenet_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserInfo();
        updateLoginLogoutUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}