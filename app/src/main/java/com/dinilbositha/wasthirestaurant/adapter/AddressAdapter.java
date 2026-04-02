package com.dinilbositha.wasthirestaurant.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurant.databinding.ItemAddressCardBinding;
import com.dinilbositha.wasthirestaurant.model.AddressModel;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressActionListener {
        void onDelete(AddressModel address);
    }

    private final List<AddressModel> addressList;
    private final OnAddressActionListener listener;

    public AddressAdapter(List<AddressModel> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressCardBinding binding = ItemAddressCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressModel address = addressList.get(position);

        holder.binding.txtAddressTitle.setText(
                address.getTitle() != null ? address.getTitle() : "Address"
        );

        holder.binding.txtFullName.setText(
                address.getFullName() != null ? address.getFullName() : ""
        );

        holder.binding.txtMobileNumber.setText(
                address.getMobileNumber() != null ? address.getMobileNumber() : ""
        );

        holder.binding.txtAddressLine.setText(address.getDisplayAddress());

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    public void removeItem(AddressModel address) {
        int position = addressList.indexOf(address);
        if (position != -1) {
            addressList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        ItemAddressCardBinding binding;

        public AddressViewHolder(@NonNull ItemAddressCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}