package com.dinilbositha.wasthirestaurantadmin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurantadmin.model.Order;
import com.dinilbositha.wasthirestaurantadmin.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderViewModel extends ViewModel {

    private final OrderRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lastPage = new MutableLiveData<>(false);

    private final List<Order> currentOrders = new ArrayList<>();

    public OrderViewModel() {
        repository = new OrderRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLastPage() {
        return lastPage;
    }

    public void loadFirstPage() {
        repository.getOrders(true, loading, new MutableLiveData<List<Order>>() {
            @Override
            public void setValue(List<Order> value) {
                currentOrders.clear();
                if (value != null) {
                    currentOrders.addAll(value);
                }
                orders.setValue(new ArrayList<>(currentOrders));
                lastPage.setValue(repository.isLastPage());
            }
        }, error);
    }

    public void loadNextPage() {
        repository.getOrders(false, loading, new MutableLiveData<List<Order>>() {
            @Override
            public void setValue(List<Order> value) {
                if (value != null && !value.isEmpty()) {
                    currentOrders.addAll(value);
                }
                orders.setValue(new ArrayList<>(currentOrders));
                lastPage.setValue(repository.isLastPage());
            }
        }, error);
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        repository.updateOrderStatus(orderId, newStatus, message, error);
    }
}