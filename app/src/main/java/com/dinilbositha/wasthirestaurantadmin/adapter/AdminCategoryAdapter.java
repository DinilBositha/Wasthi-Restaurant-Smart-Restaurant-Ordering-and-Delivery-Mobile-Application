package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ItemCategoryAdminBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Category;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryStatusClickListener {
        void onStatusClick(Category category);
    }

    private final List<Category> categoryList = new ArrayList<>();
    private final OnCategoryStatusClickListener listener;

    public AdminCategoryAdapter(OnCategoryStatusClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryAdminBinding binding = ItemCategoryAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.binding.txtCategoryName.setText(category.getCategoryName());

        if (category.getImgUrl() != null && !category.getImgUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImgUrl())
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.binding.imgCategory);
        } else {
            holder.binding.imgCategory.setImageResource(R.drawable.app_logo);
        }

        // STATUS UI UPDATE
        if (category.isStatus()) {
            holder.binding.btnCategoryStatus.setText("Active");
            holder.binding.btnCategoryStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2E7D32"))
            );
            holder.binding.btnCategoryStatus.setTextColor(Color.WHITE);
        } else {
            holder.binding.btnCategoryStatus.setText("Inactive");
            holder.binding.btnCategoryStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#C62828"))
            );
            holder.binding.btnCategoryStatus.setTextColor(Color.WHITE);
        }

        holder.binding.btnCategoryStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateData(List<Category> list) {
        categoryList.clear();
        if (list != null) {
            categoryList.addAll(list);
        }
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryAdminBinding binding;

        public CategoryViewHolder(@NonNull ItemCategoryAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}