package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ItemVariantPreviewBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VariantPreviewAdapter extends RecyclerView.Adapter<VariantPreviewAdapter.VariantViewHolder> {

    public interface OnVariantStatusChangeListener {
        void onStatusChanged(int position, Product.ProductVariant variant, boolean isActive);
    }

    public interface OnVariantDeleteListener {
        void onDeleteVariant(int position, Product.ProductVariant variant);
    }

    public interface OnVariantEditListener {
        void onEditVariant(int position, Product.ProductVariant variant);
    }

    private final List<Product.ProductVariant> variantList = new ArrayList<>();
    private OnVariantStatusChangeListener statusChangeListener;
    private OnVariantDeleteListener deleteListener;
    private OnVariantEditListener editListener;

    public void setOnVariantStatusChangeListener(OnVariantStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    public void setOnVariantDeleteListener(OnVariantDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnVariantEditListener(OnVariantEditListener listener) {
        this.editListener = listener;
    }

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
                        "Type: %s | Price: LKR %,.2f | Stock: %d",
                        variant.getType(),
                        variant.getProductPrice(),
                        variant.getStockCount())
        );

        // Status Badge & Switch
        boolean isActive = variant.isVariantStatus();
        updateStatusBadge(holder.binding, isActive);

        // Remove listener before setting check state to prevent recursion
        holder.binding.switchVariantItemStatus.setOnCheckedChangeListener(null);
        holder.binding.switchVariantItemStatus.setChecked(isActive);

        holder.binding.switchVariantItemStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            variant.setVariantStatus(isChecked);
            updateStatusBadge(holder.binding, isChecked);
            if (statusChangeListener != null) {
                statusChangeListener.onStatusChanged(holder.getAdapterPosition(), variant, isChecked);
            }
        });

        holder.binding.btnDeleteVariant.setOnClickListener(v -> {
            if (deleteListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < variantList.size()) {
                    deleteListener.onDeleteVariant(pos, variantList.get(pos));
                }
            }
        });

        holder.binding.containerVariantDetails.setOnClickListener(v -> {
            if (editListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < variantList.size()) {
                    editListener.onEditVariant(pos, variantList.get(pos));
                }
            }
        });
    }

    private void updateStatusBadge(ItemVariantPreviewBinding binding, boolean isActive) {
        if (isActive) {
            binding.txtVariantStatusBadge.setText("ACTIVE");
            binding.txtVariantStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            binding.txtVariantStatusBadge.setBackgroundResource(R.drawable.bg_variant_active);
        } else {
            binding.txtVariantStatusBadge.setText("DEACTIVE");
            binding.txtVariantStatusBadge.setTextColor(Color.parseColor("#C62828"));
            binding.txtVariantStatusBadge.setBackgroundResource(R.drawable.bg_variant_inactive);
        }
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