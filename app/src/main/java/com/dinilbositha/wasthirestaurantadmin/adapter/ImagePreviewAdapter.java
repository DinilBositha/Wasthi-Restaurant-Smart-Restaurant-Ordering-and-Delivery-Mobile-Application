package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.net.Uri;
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

    private final List<Uri> imageUris = new ArrayList<>();

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
        Glide.with(holder.itemView.getContext())
                .load(imageUris.get(position))
                .placeholder(R.drawable.app_logo)
                .into(holder.binding.imgPreview);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void updateData(List<Uri> uris) {
        imageUris.clear();
        if (uris != null) {
            imageUris.addAll(uris);
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