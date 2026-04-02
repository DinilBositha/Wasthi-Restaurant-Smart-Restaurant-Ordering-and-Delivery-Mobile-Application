package com.dinilbositha.wasthirestaurantadmin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurantadmin.model.Product;
import com.dinilbositha.wasthirestaurantadmin.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends ViewModel {

    private final ProductRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public ProductViewModel() {
        repository = new ProductRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadProducts() {
        repository.getAllProducts(loading, products, error);
    }

    public void addProduct(Product product) {
        repository.addProduct(product, message, error);
    }

    public void updateProduct(Product product) {
        repository.updateProduct(product, message, error);
    }

    public void updateProductStatus(String productId, boolean status) {
        repository.updateProductStatus(productId, status, message, error);
    }

    public void clearMessage() {
        message.setValue(null);
    }

    public void clearError() {
        error.setValue(null);
    }
}