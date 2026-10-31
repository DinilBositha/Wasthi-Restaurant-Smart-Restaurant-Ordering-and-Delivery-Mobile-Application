package com.dinilbositha.wasthirestaurant.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurant.databinding.ItemSelectAddressBinding;
import com.dinilbositha.wasthirestaurant.model.AddressModel;

import java.util.List;

public class AddressSelectAdapter extends RecyclerView.Adapter<AddressSelectAdapter.AddressViewHolder> {

    public interface OnAddressClickListener {
        void onAddressClicked(AddressModel addressModel);
    }

    private final List<AddressModel> addressList;
    private final OnAddressClickListener listener;
    private String selectedAddressId;

    public AddressSelectAdapter(List<AddressModel> addressList,
                                String selectedAddressId,
                                OnAddressClickListener listener) {
        this.addressList = addressList;
        this.selectedAddressId = selectedAddressId;
        this.listener = listener;
    }

    public void setSelectedAddressId(String selectedAddressId) {
        this.selectedAddressId = selectedAddressId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectAddressBinding binding = ItemSelectAddressBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressModel model = addressList.get(position);

        holder.binding.txtAddressTitle.setText(model.getTitle());
        holder.binding.txtAddressName.setText(model.getFullName());
        holder.binding.txtAddressPhone.setText(model.getMobileNumber());
        holder.binding.txtAddressLine.setText(new StringBuilder().append(model.getAddressLine1()).append("").append(model.getAddressLine2()).toString());

        boolean isSelected = model.getId() != null && model.getId().equals(selectedAddressId);
        holder.binding.radioAddress.setChecked(isSelected);

        holder.binding.getRoot().setOnClickListener(v -> listener.onAddressClicked(model));
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        ItemSelectAddressBinding binding;

        public AddressViewHolder(ItemSelectAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}