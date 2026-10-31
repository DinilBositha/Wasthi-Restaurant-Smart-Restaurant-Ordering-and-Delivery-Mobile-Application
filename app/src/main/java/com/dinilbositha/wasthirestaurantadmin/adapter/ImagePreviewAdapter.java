package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ItemImagePreviewBinding;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {

    public interface OnRemoveImageListener {
        void onRemove(int position, Object item);
    }

    private final List<Object> imageItems = new ArrayList<>();
    private OnRemoveImageListener removeListener;

    public ImagePreviewAdapter() {
    }

    public ImagePreviewAdapter(OnRemoveImageListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setOnRemoveImageListener(OnRemoveImageListener removeListener) {
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImagePreviewBinding binding = ItemImagePreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object item = imageItems.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item)
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo)
                .centerCrop()
                .into(holder.binding.imgPreview);

        holder.binding.btnRemoveImage.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                removeListener.onRemove(pos, imageItems.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public void updateData(List<?> items) {
        imageItems.clear();
        if (items != null) {
            imageItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ItemImagePreviewBinding binding;

        public ImageViewHolder(@NonNull ItemImagePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}