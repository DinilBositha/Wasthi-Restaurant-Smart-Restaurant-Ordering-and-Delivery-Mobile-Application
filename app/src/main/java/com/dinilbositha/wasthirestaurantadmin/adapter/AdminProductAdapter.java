package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ItemProductAdminBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onEditClick(Product product);
        void onStatusClick(Product product);
    }

    private final List<Product> fullList = new ArrayList<>();
    private final List<Product> filteredList = new ArrayList<>();
    private final OnProductClickListener listener;

    public AdminProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductAdminBinding binding = ItemProductAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);

        holder.binding.txtProductTitle.setText(safe(product.getProductTitle()));
        holder.binding.txtCategoryName.setText("Category: " + safe(product.getCategoryName()));
        holder.binding.txtBasePrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getBasePrice()));

        if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getProductImage().get(0))
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.binding.imgProduct);
        } else {
            holder.binding.imgProduct.setImageResource(R.drawable.app_logo);
        }

        if (product.isStatus()) {
            holder.binding.btnProductStatus.setText("Active");
            holder.binding.btnProductStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            );
        } else {
            holder.binding.btnProductStatus.setText("Inactive");
            holder.binding.btnProductStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#F44336"))
            );
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(product);
        });

        holder.binding.btnProductStatus.setOnClickListener(v -> {
            if (listener != null) listener.onStatusClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateData(List<Product> newList) {
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
            String lower = query.toLowerCase(Locale.getDefault()).trim();

            for (Product product : fullList) {
                String title = safe(product.getProductTitle()).toLowerCase(Locale.getDefault());
                String category = safe(product.getCategoryName()).toLowerCase(Locale.getDefault());

                if (title.contains(lower) || category.contains(lower)) {
                    filteredList.add(product);
                }
            }
        }

        notifyDataSetChanged();
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductAdminBinding binding;

        public ProductViewHolder(@NonNull ItemProductAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}