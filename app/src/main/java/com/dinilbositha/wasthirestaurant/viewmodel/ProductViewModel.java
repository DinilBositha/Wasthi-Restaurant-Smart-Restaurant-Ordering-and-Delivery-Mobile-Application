package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.model.Product;
import com.dinilbositha.wasthirestaurant.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends ViewModel {

    private final ProductRepository repository = new ProductRepository();

    public LiveData<Product> getProductById(String productId) {
        return repository.getProductById(productId);
    }

    public LiveData<List<Product>> getRelatedProducts(String categoryId, String currentProductId) {
        return repository.getRelatedProducts(categoryId, currentProductId);
    }

    public LiveData<List<Product>> getMoreProducts(String currentProductId, String currentCategoryId) {
        return repository.getMoreProducts(currentProductId, currentCategoryId);
    }
}