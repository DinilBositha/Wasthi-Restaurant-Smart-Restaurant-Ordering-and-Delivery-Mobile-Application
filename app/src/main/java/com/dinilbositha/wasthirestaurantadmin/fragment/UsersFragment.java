package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurantadmin.adapter.AdminUserAdapter;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentUsersBinding;
import com.dinilbositha.wasthirestaurantadmin.model.User;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private static final String TAG = "USER_DEBUG";

    private FragmentUsersBinding binding;
    private UserViewModel userViewModel;
    private AdminUserAdapter adapter;
    private final List<User> userList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupClicks();
        observeViewModel();

        Log.d(TAG, "UsersFragment loaded - calling loadUsers()");
        userViewModel.loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this::showStatusChangeDialog);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void showStatusChangeDialog(User user) {
        if (user == null || getContext() == null) return;

        boolean newStatus = !user.isActive();
        String actionText = newStatus ? "activate" : "deactivate";

        new AlertDialog.Builder(requireContext())
                .setTitle("Change User Status")
                .setMessage("Are you sure you want to " + actionText + " " + safe(user.getName()) + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (user.getUid() != null && !user.getUid().trim().isEmpty()) {
                        userViewModel.updateUserStatus(user.getUid(), newStatus);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        userViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        userViewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (binding == null) return;

            Log.d(TAG, "Observed users: " + (users != null ? users.size() : 0));

            userList.clear();
            if (users != null) {
                userList.addAll(users);
            }

            adapter.updateData(userList);
            updateEmptyState();
        });

        userViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(error)) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                userViewModel.loadUsers();
            }
        });
    }

    private void updateEmptyState() {
        if (binding == null || adapter == null) return;

        if (adapter.getItemCount() == 0) {
            binding.recyclerUsers.setVisibility(View.GONE);
            binding.txtEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerUsers.setVisibility(View.VISIBLE);
            binding.txtEmpty.setVisibility(View.GONE);
        }
    }

    private String safe(String value) {
        return value == null ? "this user" : value;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}