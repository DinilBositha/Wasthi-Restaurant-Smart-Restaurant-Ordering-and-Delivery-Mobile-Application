package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.app.AlertDialog;
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

import com.dinilbositha.wasthirestaurantadmin.adapter.AdminOrderAdapter;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentOrdersBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Order;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.OrderViewModel;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderViewModel orderViewModel;
    private AdminOrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();

    private final String[] orderStatuses = {
            "Pending",
            "Preparing",
            "Ready",
            "Delivered",
            "Cancelled"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        setupRecyclerView();
        setupClicks();
        observeViewModel();
        binding.btnBack.setOnClickListener(v-> requireActivity().getSupportFragmentManager().popBackStack());

        orderViewModel.loadFirstPage();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this::showStatusDialog);
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerOrders.setAdapter(adapter);
    }

    private void setupClicks() {
        binding.btnLoadMore.setOnClickListener(v -> orderViewModel.loadNextPage());
    }

    private void showStatusDialog(Order order) {
        if (order == null || getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Change Order Status")
                .setItems(orderStatuses, (dialog, which) -> {
                    String selectedStatus = orderStatuses[which];

                    if (order.getOrderId() != null && !order.getOrderId().trim().isEmpty()) {
                        orderViewModel.updateOrderStatus(order.getOrderId(), selectedStatus);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        orderViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        orderViewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();

            if (orders != null && !orders.isEmpty()) {
                orderList.addAll(orders);
                binding.recyclerOrders.setVisibility(View.VISIBLE);
                binding.txtEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerOrders.setVisibility(View.GONE);
                binding.txtEmpty.setVisibility(View.VISIBLE);
            }

            adapter.updateData(orderList);
        });

        orderViewModel.getLastPage().observe(getViewLifecycleOwner(), isLastPage -> {
            if (Boolean.TRUE.equals(isLastPage)) {
                binding.btnLoadMore.setVisibility(View.GONE);
            } else {
                binding.btnLoadMore.setVisibility(
                        orderList.isEmpty() ? View.GONE : View.VISIBLE
                );
            }
        });

        orderViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (!TextUtils.isEmpty(error)) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        orderViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                orderViewModel.loadFirstPage();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}