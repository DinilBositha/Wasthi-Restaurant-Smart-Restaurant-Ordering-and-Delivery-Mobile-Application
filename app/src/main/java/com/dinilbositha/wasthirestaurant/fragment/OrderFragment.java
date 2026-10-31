package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.adapter.OrderAdapter;
import com.dinilbositha.wasthirestaurant.databinding.FragmentOrderBinding;
import com.dinilbositha.wasthirestaurant.model.Order;
import com.dinilbositha.wasthirestaurant.viewmodel.OrderViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private final List<Order> orderList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        loadOrders();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orderList);
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerOrders.setAdapter(orderAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void loadOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || TextUtils.isEmpty(currentUser.getUid())) {
            showLoading(false);
            showEmptyState(true);
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        orderViewModel.getUserOrders(currentUser.getUid()).observe(getViewLifecycleOwner(), orders -> {
            showLoading(false);
            orderList.clear();

            if (orders != null && !orders.isEmpty()) {
                orderList.addAll(orders);
                orderAdapter.notifyDataSetChanged();

                binding.recyclerOrders.setVisibility(View.VISIBLE);
                binding.layoutEmptyOrders.setVisibility(View.GONE);
            } else {
                orderAdapter.notifyDataSetChanged();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.logoOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        binding.layoutEmptyOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMainNavigationUi();
        }

        binding = null;
    }
}