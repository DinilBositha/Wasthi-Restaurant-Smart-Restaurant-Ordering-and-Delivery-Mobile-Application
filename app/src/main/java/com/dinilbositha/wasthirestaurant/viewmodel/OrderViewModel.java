package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.model.Order;
import com.dinilbositha.wasthirestaurant.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends ViewModel {

    private final OrderRepository repository = new OrderRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>("");

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getSuccess() {
        return success;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void saveOrder(Order order) {
        loading.setValue(true);
        success.setValue(false);
        error.setValue("");

        repository.saveOrder(order, new OrderRepository.OnOrderSaveListener() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                success.setValue(true);
            }

            @Override
            public void onFailure(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public LiveData<List<Order>> getUserOrders(String userId) {
        return repository.getUserOrders(userId);
    }
}