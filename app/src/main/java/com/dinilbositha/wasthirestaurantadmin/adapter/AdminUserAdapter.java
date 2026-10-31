package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurantadmin.databinding.ItemUserAdminBinding;
import com.dinilbositha.wasthirestaurantadmin.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserStatusClickListener {
        void onUserStatusClick(User user);
    }

    private final List<User> fullList = new ArrayList<>();
    private final List<User> filteredList = new ArrayList<>();
    private final OnUserStatusClickListener listener;

    public AdminUserAdapter(OnUserStatusClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserAdminBinding binding = ItemUserAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredList.get(position);

        holder.binding.txtUserName.setText(safe(user.getName()));
        holder.binding.txtUserEmail.setText(safe(user.getEmail()));
        holder.binding.txtUserRole.setText("Role: " + safe(user.getRole()));
        holder.binding.txtUserInitial.setText(getInitial(user.getName()));

        if (user.isActive()) {
            holder.binding.btnUserStatus.setText("Active");
            holder.binding.btnUserStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            );
        } else {
            holder.binding.btnUserStatus.setText("Inactive");
            holder.binding.btnUserStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#F44336"))
            );
        }

        holder.binding.btnUserStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserStatusClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateData(List<User> newList) {
        fullList.clear();
        filteredList.clear();

        if (newList != null) {
            fullList.addAll(newList);
            filteredList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();

            for (User user : fullList) {
                String name = safe(user.getName()).toLowerCase(Locale.getDefault());
                String email = safe(user.getEmail()).toLowerCase(Locale.getDefault());

                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredList.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        return name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserAdminBinding binding;

        public UserViewHolder(@NonNull ItemUserAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}