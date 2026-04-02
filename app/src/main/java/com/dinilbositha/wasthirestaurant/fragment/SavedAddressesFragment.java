package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.adapter.AddressAdapter;
import com.dinilbositha.wasthirestaurant.databinding.FragmentSavedAddressesBinding;
import com.dinilbositha.wasthirestaurant.model.AddressModel;
import com.dinilbositha.wasthirestaurant.viewmodel.AddressViewModel;

import java.util.ArrayList;
import java.util.List;

public class SavedAddressesFragment extends Fragment {

    private FragmentSavedAddressesBinding binding;
    private AddressViewModel addressViewModel;
    private AddressAdapter addressAdapter;
    private final List<AddressModel> addressList = new ArrayList<>();

    public SavedAddressesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSavedAddressesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        setupRecyclerView();
        setupClicks();
        observeData();
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter(addressList, address -> {
            showDeleteConfirmDialog(address);
        });

        binding.recyclerAddresses.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerAddresses.setAdapter(addressAdapter);
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        binding.btnAddAddress.setOnClickListener(v -> openAddAddressScreen());

        binding.btnAddNewAddress.setOnClickListener(v -> openAddAddressScreen());
    }

    private void openAddAddressScreen() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragemenet_container, new AddAddressFragment())
                .addToBackStack(null)
                .commit();
    }

    private void observeData() {
        binding.progressLoading.setVisibility(View.VISIBLE);

        addressViewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            binding.progressLoading.setVisibility(View.GONE);

            addressList.clear();
            if (addresses != null) {
                addressList.addAll(addresses);
            }
            addressAdapter.notifyDataSetChanged();

            boolean isEmpty = addressList.isEmpty();
            binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.btnAddNewAddress.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        addressViewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Address deleted", Toast.LENGTH_SHORT).show();
                addressViewModel.clearDeleteSuccess();
            }
        });

        addressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        addressViewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Address saved successfully", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        });
        addressViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressLoading.setVisibility(
                    Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE
            );
        });
    }

    private void showDeleteConfirmDialog(AddressModel address) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    addressAdapter.removeItem(address);
                    addressViewModel.deleteAddress(address.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        addressViewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            addressList.clear();
            if (addresses != null) {
                addressList.addAll(addresses);
            }
            addressAdapter.notifyDataSetChanged();

            boolean isEmpty = addressList.isEmpty();
            binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }
}