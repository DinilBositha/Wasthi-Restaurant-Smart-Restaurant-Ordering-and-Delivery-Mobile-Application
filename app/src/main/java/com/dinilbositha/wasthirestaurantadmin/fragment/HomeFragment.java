package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentHomeBinding;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // temporary sample values
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        observeDashboardStats();

        binding.cardManage.setOnClickListener(v -> {
            loadFragment(new CategoryFragment());
        });

        binding.cardManageProducts.setOnClickListener(v -> {
            loadFragment(new ProductsFragment());
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            // navigate to UsersFragment
            loadFragment(new UsersFragment());
        });
    }
    private void observeDashboardStats() {
        homeViewModel.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.totalOrdersCount.setText(String.valueOf(stats.getTotalOrders()));
                binding.pendingOrdersCount.setText(String.valueOf(stats.getPendingOrders()));
                binding.productsCount.setText(String.valueOf(stats.getProductsCount()));
                binding.usersCount.setText(String.valueOf(stats.getUsersCount()));
            }
        });
    }
    private void loadFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}