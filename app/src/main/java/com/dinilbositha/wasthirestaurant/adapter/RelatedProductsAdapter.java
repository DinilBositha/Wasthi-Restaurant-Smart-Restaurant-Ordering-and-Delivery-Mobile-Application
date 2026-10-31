package com.dinilbositha.wasthirestaurant.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ItemRelatedProductBinding;
import com.dinilbositha.wasthirestaurant.model.Product;

import java.util.List;

public class RelatedProductsAdapter extends RecyclerView.Adapter<RelatedProductsAdapter.RelatedViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> productList;
    private final OnProductClickListener listener;

    public RelatedProductsAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRelatedProductBinding binding = ItemRelatedProductBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RelatedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.binding.txtProductName.setText(product.getProductTitle());
        holder.binding.txtProductPrice.setText(String.format("LKR %.2f", product.getBasePrice()));
        holder.binding.txtProductCategory.setText(
                product.getCategoryName() != null ? product.getCategoryName() : ""
        );

        String imageUrl = (product.getProductImage() != null && !product.getProductImage().isEmpty())
                ? product.getProductImage().get(0) : null;

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.error_food)
                    .centerCrop()
                    .into(holder.binding.imgProduct);
        } else {
            holder.binding.imgProduct.setImageResource(R.drawable.cart);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    static class RelatedViewHolder extends RecyclerView.ViewHolder {
        ItemRelatedProductBinding binding;

        public RelatedViewHolder(ItemRelatedProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
