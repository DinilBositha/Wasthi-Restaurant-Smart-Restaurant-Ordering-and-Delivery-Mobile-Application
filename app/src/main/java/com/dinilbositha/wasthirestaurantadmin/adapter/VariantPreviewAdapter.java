package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurantadmin.databinding.ItemVariantPreviewBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VariantPreviewAdapter extends RecyclerView.Adapter<VariantPreviewAdapter.VariantViewHolder> {

    private final List<Product.ProductVariant> variantList = new ArrayList<>();

    @NonNull
    @Override
    public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVariantPreviewBinding binding = ItemVariantPreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VariantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
        Product.ProductVariant variant = variantList.get(position);

        holder.binding.txtVariantName.setText(variant.getVariantName());
        holder.binding.txtVariantMeta.setText(
                String.format(Locale.getDefault(),
                        "Type: %s | Price: %.2f | Stock: %d",
                        variant.getType(),
                        variant.getProductPrice(),
                        variant.getStockCount())
        );
    }

    @Override
    public int getItemCount() {
        return variantList.size();
    }

    public void updateData(List<Product.ProductVariant> list) {
        variantList.clear();
        if (list != null) {
            variantList.addAll(list);
        }
        notifyDataSetChanged();
    }

    static class VariantViewHolder extends RecyclerView.ViewHolder {
        ItemVariantPreviewBinding binding;

        public VariantViewHolder(@NonNull ItemVariantPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}